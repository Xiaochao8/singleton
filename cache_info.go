/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"sync"
	"time"
)

var cacheInfoMap *sync.Map

func initCacheInfoMap() {
	cacheInfoMap = new(sync.Map)
}

func getCacheInfo(item *dataItem) *itemCacheInfo {
	data, _ := cacheInfoMap.LoadOrStore(item.id, itemCacheInfo{0, cacheDefaultExpires, ""})
	info := data.(itemCacheInfo)
	return &info // Return the pointer of a new object instead of existing object in map
}

func setCacheInfo(item *dataItem, info *itemCacheInfo) {
	cacheInfoMap.Store(item.id, *info) // Save an object instead of a pointer
}

//!+itemCacheInfo
type itemCacheInfo struct {
	lastUpdate int64
	age        int64
	eTag       string
}

func (i *itemCacheInfo) setTime(t int64) {
	i.lastUpdate = t
}

func (i *itemCacheInfo) setAge(d int64) {
	i.age = d
}

func (i *itemCacheInfo) isExpired() bool {
	return time.Now().Unix()-i.lastUpdate >= i.age
}

func (i *itemCacheInfo) setETag(t string) {
	i.eTag = t
}
func (i *itemCacheInfo) getETag() string {
	return i.eTag
}

//!-itemCacheInfo
