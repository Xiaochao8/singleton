/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"testing"

	"github.com/stretchr/testify/assert"

	"github.com/vmware/singleton/cache"
	"github.com/vmware/singleton/cache/cacheorigin"
	"github.com/vmware/singleton/common"
)

func TestCacheExpireWhenNeverExpire(t *testing.T) {

	newCfg := testCfg
	newCfg.ServerURL = ""
	resetInst(&newCfg)

	locale, component := "fr", "sunglow"
	item := &common.DataItem{common.DataItemID{common.ItemComponent, name, version, locale, component}, nil, nil}
	info := cacheorigin.GetCacheInfo(item)

	GetTranslation().GetComponentMessages(name, version, locale, component)

	// value is initial value(cacheDefaultExpires) because only local bundles are available. No chance to change this.
	assert.Equal(t, int64(common.CacheDefaultExpires), info.GetAge())

	// Rename dir to make sure getting from cache
	// bundleDir := GetTranslation().(*defaultTrans).ds.bundle.root
	// tempDir := bundleDir + "temp"
	// os.Rename(bundleDir, tempDir)
	// defer os.Rename(tempDir, bundleDir)

	// Run again to get from cache
	msgs, err := GetTranslation().GetComponentMessages(name, version, locale, component)
	assert.Nil(t, err)
	assert.Equal(t, 4, msgs.(*common.DefaultComponentMsgs).Size())
}

func TestRegisterCache(t *testing.T) {
	if cacheorigin.CacheInst == nil {
		resetInst(&testCfg)
	}

	oldCache := cacheorigin.CacheInst
	newCache := cache.NewCache()
	RegisterCache(newCache)
	//Check cache doesn't change because cache is already initialized.
	assert.Equal(t, oldCache, cacheorigin.CacheInst)

	cacheorigin.CacheInst = nil
	RegisterCache(newCache)
	//Check cache is changed because cache is nil before registration.
	assert.Equal(t, newCache, cacheorigin.CacheInst)
}
