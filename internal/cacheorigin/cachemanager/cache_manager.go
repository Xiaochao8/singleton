/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package cachemanager

import (
	"fmt"
	"sync"

	"github.com/pkg/errors"

	"github.com/vmware/singleton/internal/cache"
	"github.com/vmware/singleton/internal/cacheorigin"
	localbundle2 "github.com/vmware/singleton/internal/cacheorigin/localbundle"
	server2 "github.com/vmware/singleton/internal/cacheorigin/server"
	"github.com/vmware/singleton/internal/common"
	"github.com/vmware/singleton/internal/msgorigin"
	"github.com/vmware/singleton/internal/msgorigin/localbundle"
	"github.com/vmware/singleton/internal/msgorigin/server"
)

type (
	CacheService struct {
		cacheorigin.CacheOrigin
		UpdateStatusMap sync.Map
	}
)

// !+CacheService

func NewCacheService(originList msgorigin.MessageOriginList) *CacheService {
	oList := cacheorigin.CacheOriginList{}
	for _, msgOrigin := range originList {
		switch msgOrigin.(type) {
		case *server.ServerDAO:
			oList = append(oList, &server2.ServerCache{ServerDAO: msgOrigin.(*server.ServerDAO)})
			server2.InitCacheInfoMap()
		case *localbundle.BundleDAO:
			oList = append(oList, &localbundle2.BundleCache{BundleDAO: msgOrigin.(*localbundle.BundleDAO)})
		}
	}

	return &CacheService{UpdateStatusMap: sync.Map{}, CacheOrigin: oList}
}

func (s *CacheService) Get(item *common.DataItem) (err error) {
	data, ok := cache.CacheInst.Get(item.ID)
	if ok {
		item.Data = data
		s.refreshCache(item) // Will refresh in a seperate thread. Need a new item avoid wrong data modification.
		return nil
	}

	err = s.PopulateCache(item)
	if err == nil {
		item.Data, ok = cache.CacheInst.Get(item.ID)
		if !ok {
			return errors.New(fmt.Sprintf("Fail to get: %+v", item.ID))
		}
	}
	return err
}

func (s *CacheService) PopulateCache(item *common.DataItem) (err error) {
	if status, locked := s.lockItem(item); locked {
		err = s.fetch(item, status)
	} else {
		<-status
	}

	return err
}

func (s *CacheService) refreshCache(item *common.DataItem) {
	if s.IsExpired(item) {
		if status, locked := s.lockItem(item); locked {
			go s.fetch(&common.DataItem{ID: item.ID}, status)
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

func (s *CacheService) fetch(item *common.DataItem, status chan struct{}) (err error) {
	defer s.unlockItem(item, status)

	if _, cached := cache.CacheInst.Get(item.ID); cached && !s.IsExpired(item) {
		return nil
	}

	common.Log.Info(fmt.Sprintf("Start fetching ID: %+v", item.ID))

	return s.CacheOrigin.Get(item)
}

// !-CacheService
