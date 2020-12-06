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

type (
	cacheService struct {
		origins         cacheOriginList
		updateStatusMap sync.Map
	}
	serverCache struct {
		*serverDAO
	}
	bundleCache struct {
		*bundleDAO
	}
)

//!+cacheService

func newCacheService(originList messageOriginList) *cacheService {
	cs := cacheService{updateStatusMap: sync.Map{}}

	for _, msgOrigin := range originList {
		switch msgOrigin.(type) {
		case *serverDAO:
			cs.origins = append(cs.origins, &serverCache{msgOrigin.(*serverDAO)})
		case *bundleDAO:
			cs.origins = append(cs.origins, &bundleCache{msgOrigin.(*bundleDAO)})
		}
	}

	return &cs
}
func (s *cacheService) Get(item *dataItem) (err error) {
	data, ok := cache.Get(item.id)
	if ok {
		item.data = data
		s.refreshCache(item) // Will refresh in a seperate thread. Need a new item avoid wrong data modification.
		return nil
	}

	err = s.populateCache(item)
	if err == nil {
		item.data, ok = cache.Get(item.id)
		if !ok {
			return errors.New(fmt.Sprintf("Fail to get: %+v", item.id))
		}
	}
	return err
}

func (s *cacheService) IsExpired(item *dataItem) (expired bool) {
	for _, o := range s.origins {
		return o.IsExpired(item)
	}

	return
}

func (s *cacheService) populateCache(item *dataItem) (err error) {
	if status, locked := s.lockItem(item); locked {
		err = s.doRefresh(item, false, status)
	} else {
		<-status
	}

	return err
}

func (s *cacheService) refreshCache(item *dataItem) {
	if s.IsExpired(item) {
		status, locked := s.lockItem(item)
		if locked {
			go s.doRefresh(&dataItem{id: item.id}, true, status)
		}
	}
}

func (s *cacheService) lockItem(item *dataItem) (chan struct{}, bool) {
	actual, loaded := s.updateStatusMap.LoadOrStore(item.id, make(chan struct{}))
	status := actual.(chan struct{})
	return status, !loaded
}

func (s *cacheService) unlockItem(item *dataItem, status chan struct{}) {
	close(status)
	s.updateStatusMap.Delete(item.id)
}

func (s *cacheService) doRefresh(item *dataItem, existInCache bool, status chan struct{}) (err error) {
	defer s.unlockItem(item, status)

	logger.Info(fmt.Sprintf("Start fetching ID: %+v", item.id))

	for _, dao := range s.origins {
		if existInCache && !dao.IsExpired(item) {
			return nil
		}

		err = dao.Get(item)
		if err != nil {
			logger.Error(fmt.Sprintf(originQueryFailure, dao, err.Error()))
			if e, ok := err.(stackTracer); ok {
				logger.Error(fmt.Sprintf("%+v", e.StackTrace()))
			}
		} else {
			return nil
		}
	}

	return err
}

//!-cacheService

//!+serverCache

func (*serverCache) IsExpired(item *dataItem) bool {
	info := getCacheInfo(item)
	return info.isExpired()
}
func (c *serverCache) Get(item *dataItem) (err error) {
	info := getCacheInfo(item)
	item.attrs = info
	err = c.serverDAO.Get(item)
	if c.isSuccess(err) {
		headers, ok := item.attrs.(http.Header)
		if ok {
			c.updateCacheInfo(headers, info)
		}
		if err == nil { // http code 200
			cache.Set(item.id, item.data)
			info.setETag(headers.Get(httpHeaderETag))
		}
		setCacheInfo(item, info) // Save a new object to the info map

		return nil
	}

	return err
}

var cacheControlRE = regexp.MustCompile(`(?i)\bmax-age\b\s*=\s*\b(\d+)\b`)

func (*serverCache) updateCacheInfo(headers http.Header, info *itemCacheInfo) {
	info.setTime(time.Now().Unix())

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

	logger.Warn("Wrong cache control: " + cc)
	info.setAge(cacheDefaultExpires)
}

func (c *serverCache) isSuccess(err error) bool {
	if err != nil {
		myErr, ok := err.(*serverError)
		if !ok || myErr.code != http.StatusNotModified {
			return false
		}
	}

	return true
}

//!-serverCache

//!+bundleCache

func (*bundleCache) IsExpired(item *dataItem) bool {
	return false
}
func (c *bundleCache) Get(item *dataItem) (err error) {
	err = c.bundleDAO.Get(item)
	if err == nil {
		cache.Set(item.id, item.data)
		return nil
	}
	return err
}

//!-bundleCache
