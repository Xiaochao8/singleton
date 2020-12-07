/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"testing"

	"github.com/stretchr/testify/assert"
	"gopkg.in/h2non/gock.v1"

	"github.com/vmware/singleton/internal/cache/cacheorigin"
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

		item := &common.DataItem{common.DataItemID{common.ItemComponent, name, version, testData.locale, testData.component}, nil, nil}

		err := trans.(*translation.TransMgr).TransInst.MsgOrigin.(*cacheorigin.CacheService).PopulateCache(item)
		if err != nil {
			t.Errorf("%s failed: %v", testData.desc, err)
			continue
		}

		info := cacheorigin.GetCacheInfo(item)
		item.Data, _ = cacheorigin.CacheInst.Get(item.ID)
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
	item := &common.DataItem{common.DataItemID{common.ItemComponent, name, version, locale, component}, nil, nil}
	info := cacheorigin.GetCacheInfo(item)

	msgs, err := GetTranslation().GetComponentMessages(name, version, locale, component)
	assert.Nil(t, err)
	assert.Equal(t, 4, msgs.(*common.DefaultComponentMsgs).Size())
	assert.Equal(t, int64(common.CacheDefaultExpires), info.GetAge()) // Set max age to cacheDefaultExpires when server is unavailable temporarily.
}
