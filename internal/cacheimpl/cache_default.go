/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package cacheimpl

import (
	"sync"

	"github.com/vmware/singleton/cache"
)

var CacheInst cache.Cache

type DefaultCache struct {
	m sync.Map
}

func NewCache() *DefaultCache {
	return &DefaultCache{sync.Map{}}
}
func (c *DefaultCache) Get(key interface{}) (value interface{}, found bool) {
	return c.m.Load(key)
}
func (c *DefaultCache) Set(key interface{}, value interface{}) {
	c.m.Store(key, value)
}

