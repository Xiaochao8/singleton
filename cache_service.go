/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"fmt"
	"net/http"
	"regexp"
	"strconv"
	"sync"
	"time"

	"github.com/pkg/errors"
)

var cache Cache

const (
	idle uint32 = iota
	updating
)

// !+cacheService
type cacheService struct {
	origins         messageOriginList
	updateStatusMap sync.Map
}

func newCacheService(originList messageOriginList) *cacheService {
	cs := cacheService{updateStatusMap: sync.Map{}, origins: originList}
	return &cs
}
func (s *cacheService) Get(item *dataItem) (err error) {
	data, ok := cache.Get(item.id)
	if ok {
		item.data = data
		if s.IsExpired(item) {
			go s.refresh(&dataItem{id: item.id}, true)
		}

		return nil
	}

	err = s.refresh(item, false)
	if err == nil {
		item.data, ok = cache.Get(item.id)
		if !ok {
			return errors.New(fmt.Sprintf("Fail to get: %+v", item.id))
		}
	}
	return err
}

func (s *cacheService) IsExpired(item *dataItem) bool {
	var expired bool
	for _, o := range s.origins {
		return o.IsExpired(item)
	}

	return expired
}

func (s *cacheService) refresh(item *dataItem, exist bool) error {
	var err error
	actual, loaded := s.updateStatusMap.LoadOrStore(item.id, make(chan struct{}))
	status := actual.(chan struct{})
	if !loaded { // This is to make sure that there is only one thread to contact message source(service or storage)
		defer func() {
			defer close(status)
			s.updateStatusMap.Delete(item.id)
		}()

		logger.Info(fmt.Sprintf("Start fetching ID: %+v", item.id))

		info := getCacheInfo(item)
		startTime := time.Now().Unix()
		for _, dao := range s.origins {
			if exist && !dao.IsExpired(item) {
				return nil
			}

			switch dao.(type) {
			case *serverDAO:
				item.attrs = info
				err = dao.Get(item)
				if isSuccess(err) {
					info.setTime(startTime)
					headers, ok := item.attrs.(http.Header)
					if ok {
						updateCacheControl(headers, info)
					}
					if err == nil { // http code 200
						cache.Set(item.id, item.data)
						info.setETag(headers.Get(httpHeaderETag))
					}

					return nil
				}
			case *bundleDAO:
				err = dao.Get(item)
				if err == nil {
					info.setTime(startTime)
					cache.Set(item.id, item.data)
					return nil
				}
			}

			if err != nil {
				logger.Error(fmt.Sprintf(originQueryFailure, dao, err.Error()))
				if e, ok := err.(stackTracer); ok {
					logger.Error(fmt.Sprintf("%+v", e.StackTrace()))
				}
			}
		}

		return err
	} else if !exist {
		<-status
	}

	return nil
}

// !-cacheService

var cacheControlRE = regexp.MustCompile(`(?i)\bmax-age\b\s*=\s*\b(\d+)\b`)

func updateCacheControl(headers http.Header, info *itemCacheInfo) {
	if len(headers) == 0 || info == nil {
		return
	}

	cc := headers.Get(httpHeaderCacheControl)
	results := cacheControlRE.FindStringSubmatch(cc)
	if len(results) == 2 {
		age, parseErr := strconv.ParseInt(results[1], 10, 64)
		if parseErr == nil {
			info.setAge(age)
			return
		}
	}

	info.setAge(cacheDefaultExpires)
	logger.Warn("Wrong cache control: " + cc)
}

func isSuccess(err error) bool {
	if err != nil {
		myErr, ok := err.(*serverError)
		if !ok || myErr.code != http.StatusNotModified {
			return false
		}
	}

	return true
}
