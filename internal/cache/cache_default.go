/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package cache

import (
	"sync"
)

type defaultCache struct {
	m *sync.Map
}

func NewCache() Cache {
	return &defaultCache{new(sync.Map)}
}
func (c *defaultCache) Get(key interface{}) (value interface{}, found bool) {
	return c.m.Load(key)
}
func (c *defaultCache) Set(key interface{}, value interface{}) {
	c.m.Store(key, value)
}
