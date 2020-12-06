/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package cacheorigin

import (
	"net/http"
	"regexp"
	"strconv"
	"sync"
	"time"

	"github.com/vmware/singleton/cache"
	"github.com/vmware/singleton/common"
	"github.com/vmware/singleton/msgorigin/server"
)

//!+ServerCache

type ServerCache struct {
	*server.ServerDAO
}

func (*ServerCache) IsExpired(item *common.DataItem) bool {
	info := GetCacheInfo(item)
	return info.IsExpired()
}
func (c *ServerCache) Get(item *common.DataItem) (err error) {
	info := GetCacheInfo(item)
	item.Attrs = info
	err = c.ServerDAO.Get(item)
	if c.isSuccess(err) {
		headers, ok := item.Attrs.(http.Header)
		if ok {
			c.updateCacheInfo(headers, info)
		}
		if err == nil { // http code 200
			CacheInst.Set(item.ID, item.Data)
			info.SetETag(headers.Get(common.HTTPHeaderETag))
		}
		SetCacheInfo(item, info) // Save a new object to the info map

		return nil
	}

	return err
}

var cacheControlRE = regexp.MustCompile(`(?i)\bmax-age\b\s*=\s*\b(\d+)\b`)

func (*ServerCache) updateCacheInfo(headers http.Header, info *cache.ItemCacheInfo) {
	info.SetTime(time.Now().Unix())

	if len(headers) == 0 || info == nil {
		return
	}

	cc := headers.Get(common.HTTPHeaderCacheControl)
	results := cacheControlRE.FindStringSubmatch(cc)
	if len(results) == 2 {
		age, parseErr := strconv.ParseInt(results[1], 10, 64)
		if parseErr == nil {
			info.SetAge(age)
			return
		}
	}

	common.Log.Warn("Wrong cache control: " + cc)
	info.SetAge(common.CacheDefaultExpires)
}

func (c *ServerCache) isSuccess(err error) bool {
	if err != nil {
		myErr, ok := err.(*common.ServerError)
		if !ok || myErr.Code != http.StatusNotModified {
			return false
		}
	}

	return true
}

//!-ServerCache

var CacheInfoMap *sync.Map

func InitCacheInfoMap() {
	CacheInfoMap = new(sync.Map)
}

func GetCacheInfo(item *common.DataItem) *cache.ItemCacheInfo {
	data, _ := CacheInfoMap.LoadOrStore(item.ID, *cache.NewItemCacheInfo())
	info := data.(cache.ItemCacheInfo)
	return &info // Return the pointer of a new object instead of existing object in map
}

func SetCacheInfo(item *common.DataItem, info *cache.ItemCacheInfo) {
	CacheInfoMap.Store(item.ID, *info) // Save an object instead of a pointer
}
