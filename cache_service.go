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
		s.refresh(&dataItem{id: item.id}, true) // Will refresh in a seperate thread. Need a new item avoid wrong data modification.
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

func (s *cacheService) refresh(item *dataItem, existInCache bool) (err error) {
	actual, loaded := s.updateStatusMap.LoadOrStore(item.id, make(chan struct{}))

	if existInCache {
		if !loaded && s.IsExpired(item) {
			go s.doRefresh(&dataItem{id: item.id}, true)
		}
	} else {
		status := actual.(chan struct{})
		if !loaded {
			defer close(status)
			err = s.doRefresh(item, false)
		} else {
			<-status
		}
	}
	return err
}

func (s *cacheService) doRefresh(item *dataItem, existInCache bool) (err error) {
	defer s.updateStatusMap.Delete(item.id)

	logger.Info(fmt.Sprintf("Start fetching ID: %+v", item.id))

	info := getCacheInfo(item)
	for _, dao := range s.origins {
		if existInCache && !dao.IsExpired(item) {
			return nil
		}

		switch dao.(type) {
		case *serverDAO:
			item.attrs = info
			err = dao.Get(item)
			if isSuccess(err) {
				headers, ok := item.attrs.(http.Header)
				if ok {
					updateCacheInfo(headers, info)
				}
				if err == nil { // http code 200
					cache.Set(item.id, item.data)
					info.setETag(headers.Get(httpHeaderETag))
				}
				setCacheInfo(item, info) // Save a new object to the info map

				return nil
			}
		case *bundleDAO:
			err = dao.Get(item)
			if err == nil {
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
}

// !-cacheService

var cacheControlRE = regexp.MustCompile(`(?i)\bmax-age\b\s*=\s*\b(\d+)\b`)

func updateCacheInfo(headers http.Header, info *itemCacheInfo) {
	if len(headers) == 0 || info == nil {
		return
	}

	info.setTime(time.Now().Unix())

	cc := headers.Get(httpHeaderCacheControl)
	results := cacheControlRE.FindStringSubmatch(cc)
	if len(results) == 2 {
		age, parseErr := strconv.ParseInt(results[1], 10, 64)
		if parseErr == nil {
			info.setAge(age)
			return
		}
	}

	logger.Warn("Wrong cache control: " + cc)
	info.setAge(cacheDefaultExpires)
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
