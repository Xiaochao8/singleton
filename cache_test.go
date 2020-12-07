/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"testing"

	"github.com/stretchr/testify/assert"

	"github.com/vmware/singleton/internal/cacheimpl"
	"github.com/vmware/singleton/internal/cachemanager/server"
	"github.com/vmware/singleton/internal/common"
)

func TestCacheExpireWhenNeverExpire(t *testing.T) {

	newCfg := testCfg
	newCfg.ServerURL = ""
	resetInst(&newCfg)

	locale, component := "fr", "sunglow"
	item := &common.DataItem{common.DataItemID{common.ItemComponent, name, version, locale, component}, nil, nil}
	info := server.GetCacheInfo(item)

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
	if cacheimpl.CacheInst == nil {
		resetInst(&testCfg)
	}

	oldCache := cacheimpl.CacheInst
	newCache := cacheimpl.NewCache()
	RegisterCache(newCache)
	//Check cache doesn't change because cache is already initialized.
	assert.Equal(t, oldCache, cacheimpl.CacheInst)

	cacheimpl.CacheInst = nil
	RegisterCache(newCache)
	//Check cache is changed because cache is nil before registration.
	assert.Equal(t, newCache, cacheimpl.CacheInst)
}
