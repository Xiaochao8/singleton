/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"testing"

	"github.com/stretchr/testify/assert"

	"github.com/vmware/singleton/cache/cacheorigin"
	"github.com/vmware/singleton/common"
	"github.com/vmware/singleton/translation"
)

func TestGetInst(t *testing.T) {

	resetInst(&testCfg)
	assert.Equal(t, testCfg.LocalBundles, inst.bundle.Root)
	// TODO: Test bundle

	if len(testCfg.ServerURL) != 0 {
		assert.NotNil(t, inst.server)
	}

	// Verify translation manager
	assert.NotNil(t, inst.trans)

	s := inst.trans.(*translation.TransMgr).Translation.(*translation.TransInst).MsgOrigin
	assert.NotNil(t, s)
	assert.NotNil(t, cacheorigin.CacheInst)
	assert.NotNil(t, cacheorigin.CacheInfoMap)
}

func TestCheckConfig(t *testing.T) {

	newCfg := testCfg
	newCfg.ServerURL, newCfg.LocalBundles = "", ""

	errString := common.OriginNotProvided
	err := checkConfig(&newCfg)
	assert.Equal(t, errString, err.Error())

	newCfg2 := testCfg
	newCfg2.DefaultLocale = ""
	errString2 := common.DefaultLocaleNotProvided
	err2 := checkConfig(&newCfg2)
	assert.Equal(t, errString2, err2.Error())

	assert.PanicsWithError(t, common.OriginNotProvided, func() { Initialize(&newCfg) })
}

func TestGetTranslation(t *testing.T) {
	inst = nil

	assert.PanicsWithError(t, common.Uninitialized, func() { GetTranslation() })
}

func TestSetHttpHeaders(t *testing.T) {
	inst = nil

	err := SetHTTPHeaders(nil)
	assert.Error(t, err)
}
