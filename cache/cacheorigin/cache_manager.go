/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package cacheorigin

import (
	"fmt"
	"sync"

	"github.com/pkg/errors"

	"github.com/vmware/singleton/cache"
	"github.com/vmware/singleton/common"
	"github.com/vmware/singleton/msgorigin"
	"github.com/vmware/singleton/msgorigin/localbundle"
	"github.com/vmware/singleton/msgorigin/server"
)

var CacheInst cache.Cache

type (
	CacheService struct {
		CacheOrigin
		UpdateStatusMap sync.Map
	}
)

//!+CacheService

func NewCacheService(originList msgorigin.MessageOriginList) *CacheService {
	oList := CacheOriginList{}
	for _, msgOrigin := range originList {
		switch msgOrigin.(type) {
		case *server.ServerDAO:
			oList = append(oList, &ServerCache{msgOrigin.(*server.ServerDAO)})
		case *localbundle.BundleDAO:
			oList = append(oList, &BundleCache{msgOrigin.(*localbundle.BundleDAO)})
		}
	}

	return &CacheService{UpdateStatusMap: sync.Map{}, CacheOrigin: oList}
}

func (s *CacheService) Get(item *common.DataItem) (err error) {
	data, ok := CacheInst.Get(item.ID)
	if ok {
		item.Data = data
		s.refreshCache(item) // Will refresh in a seperate thread. Need a new item avoid wrong data modification.
		return nil
	}

	err = s.PopulateCache(item)
	if err == nil {
		item.Data, ok = CacheInst.Get(item.ID)
		if !ok {
			return errors.New(fmt.Sprintf("Fail to get: %+v", item.ID))
		}
	}
	return err
}

func (s *CacheService) PopulateCache(item *common.DataItem) (err error) {
	if status, locked := s.lockItem(item); locked {
		err = s.fetch(item, false, status)
	} else {
		<-status
	}

	return err
}

func (s *CacheService) refreshCache(item *common.DataItem) {
	if s.IsExpired(item) {
		status, locked := s.lockItem(item)
		if locked {
			go s.fetch(&common.DataItem{ID: item.ID}, true, status)
		}
	}
}

func (s *CacheService) lockItem(item *common.DataItem) (chan struct{}, bool) {
	actual, loaded := s.UpdateStatusMap.LoadOrStore(item.ID, make(chan struct{}))
	status := actual.(chan struct{})
	return status, !loaded
}

func (s *CacheService) unlockItem(item *common.DataItem, status chan struct{}) {
	close(status)
	s.UpdateStatusMap.Delete(item.ID)
}

func (s *CacheService) fetch(item *common.DataItem, existInCache bool, status chan struct{}) (err error) {
	defer s.unlockItem(item, status)

	common.Log.Info(fmt.Sprintf("Start fetching ID: %+v", item.ID))

	return s.CacheOrigin.Get(item)
}

//!-CacheService


