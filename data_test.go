/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"gopkg.in/h2non/gock.v1"

	"github.com/vmware/singleton/internal/cache"
	"github.com/vmware/singleton/internal/cacheorigin/cachemanager"
	"github.com/vmware/singleton/internal/cacheorigin/server"
	"github.com/vmware/singleton/internal/common"
	"github.com/vmware/singleton/internal/translation"
)

// Test cache control
func TestCC(t *testing.T) {

	var tests = []struct {
		desc      string
		mocks     []string
		locale    string
		component string
		etag      string
		maxage    int64
		msgLen    int
	}{
		{"Test Save CC", []string{"componentMessages-zh-Hans-sunglow"}, "zh-Hans", "sunglow", "1234567890", 1221965, 7},
		{"Test Send CC", []string{"componentMessages-zh-Hans-sunglow-sendCC"}, "zh-Hans", "sunglow", "0987654321", 2334, 7},
		{"Test Receive 304", []string{"componentMessages-zh-Hans-HTTP304"}, "zh-Hans", "sunglow", "0987654321", 3445, 7},
	}

	defer gock.Off()

	newCfg := testCfg
	newCfg.LocalBundles = ""
	resetInst(&newCfg)
	trans := GetTranslation()
	for _, testData := range tests {
		for _, m := range testData.mocks {
			EnableMockData(m)
		}

		item := &common.DataItem{ID: common.DataItemID{IType: common.ItemComponent, Name: name, Version: version, Locale: testData.locale, Component: testData.component}}

		err := trans.(*translation.TransMgr).TransInst.MsgOrigin.(*cachemanager.CacheService).PopulateCache(item)
		if err != nil {
			t.Errorf("%s failed: %v", testData.desc, err)
			continue
		}

		info := server.GetCacheInfo(item)
		item.Data, _ = cache.CacheInst.Get(item.ID)
		messages := item.Data.(common.ComponentMsgs)

		assert.NotNil(t, info)
		assert.Equal(t, testData.etag, info.GetETag())
		assert.Equal(t, testData.maxage, info.GetAge())
		assert.Equal(t, testData.msgLen, messages.(*common.DefaultComponentMsgs).Size())

		assert.True(t, gock.IsDone())

		expireCache(item)
	}
}

func TestFallbackToLocalBundles(t *testing.T) {
	resetInst(&testCfg)

	locale, component := "fr", "sunglow"
	item := &common.DataItem{ID: common.DataItemID{IType: common.ItemComponent, Name: name, Version: version, Locale: locale, Component: component}}
	info := server.GetCacheInfo(item)

	msgs, err := GetTranslation().GetComponentMessages(name, version, locale, component)
	assert.Nil(t, err)
	assert.Equal(t, 4, msgs.(*common.DefaultComponentMsgs).Size())
	assert.Equal(t, int64(common.CacheDefaultExpires), info.GetAge()) // Set max age to cacheDefaultExpires when server is unavailable temporarily.
}
