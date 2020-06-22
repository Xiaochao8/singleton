/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"fmt"
	"time"

	"github.com/pkg/errors"
)

//!+dataService

type cacheService struct {
	daos []messageOrigin
}

func newCacheService() *cacheService {
	return &cacheService{}
}
func (ds *cacheService) Get(item *dataItem) (err error) {
	data, ok := cache.Get(item.id)
	info := getCacheInfo(item)
	item.attrs = info
	if ok {
		item.data = data
		if info.isExpired() {
			go ds.fetch(item, false)
		}

		return nil
	}

	return ds.fetch(item, true)
}

func (ds *cacheService) fetch(item *dataItem, wait bool) error {
	var err error
	info := item.attrs.(*itemCacheInfo)

	if info.setUpdating() {
		defer info.setUpdated()
		logger.Debug(fmt.Sprintf("Start fetching ID: %+v", item.id))
		for _, o := range ds.daos {
			if err = o.Get(item); isFetchSucess(err) {
				if err == nil {
					cache.Set(item.id, item.data)
				} else {
					item.data, _ = cache.Get(item.id)
				}

				info.setTime(time.Now().Unix())
				return nil
			}

		}
		return err

	} else if wait {
		info.waitUpdate()
		var ok bool
		item.data, ok = cache.Get(item.id)
		if !ok {
			return errors.New(fmt.Sprintf("Fail to fetch ID: %+v", item.id))
		}
	}

	return nil
}

//!-dataService
