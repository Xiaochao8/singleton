/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package server

import (
	"time"

	"github.com/vmware/singleton/internal/common"
)

//!+itemCacheInfo

type ItemCacheInfo struct {
	lastUpdate int64
	age        int64
	eTag       string
}

func NewItemCacheInfo() *ItemCacheInfo {
	return &ItemCacheInfo{0, common.CacheDefaultExpires, ""}
}

func (i *ItemCacheInfo) SetTime(t int64) {
	i.lastUpdate = t
}

func (i *ItemCacheInfo) SetAge(d int64) {
	i.age = d
}

func (i *ItemCacheInfo) IsExpired() bool {
	return time.Now().Unix()-i.lastUpdate >= i.age
}

func (i *ItemCacheInfo) SetETag(t string) {
	i.eTag = t
}
func (i *ItemCacheInfo) GetETag() string {
	return i.eTag
}
func (i *ItemCacheInfo) GetAge() int64 {
	return i.age
}

//!-itemCacheInfo
