/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"net"
	"net/http"
	"net/url"
	"testing"
	"time"

	"github.com/pkg/errors"
	"github.com/stretchr/testify/assert"
	"gopkg.in/h2non/gock.v1"

	server2 "github.com/vmware/singleton/internal/cacheorigin/server"
	"github.com/vmware/singleton/internal/common"
	"github.com/vmware/singleton/internal/msgorigin/server"
)

func TestGetLocaleCompAbnormal(t *testing.T) {

	saved := server.GetDataFromServer
	defer func() { server.GetDataFromServer = saved }()

	errMsg := "TestGetLocaleCompAbnormal"
	server.GetDataFromServer = func(u *url.URL, header map[string]string, data interface{}) (*http.Response, error) {
		return nil, errors.New(errMsg)
	}

	newCfg := testCfg
	newCfg.LocalBundles = ""
	resetInst(&newCfg)

	trans := GetTranslation()

	components, errcomp := trans.GetComponentList(name, version)
	assert.Nil(t, components)
	assert.Contains(t, errcomp.Error(), errMsg)

	components, errcomp = trans.GetComponentList(name, version)
	assert.Nil(t, components)
	assert.Contains(t, errcomp.Error(), errMsg)

	locales, errlocale := trans.GetLocaleList(name, version)
	assert.Nil(t, locales)
	assert.Contains(t, errlocale.Error(), errMsg)

	locales, errlocale = trans.GetLocaleList(name, version)
	assert.Nil(t, locales)
	assert.Contains(t, errlocale.Error(), errMsg)
}

func TestTimeout(t *testing.T) {

	oldClient := server.HTTPClient
	defer func() {
		gock.Off()
		server.HTTPClient = oldClient
	}()

	newTimeout := time.Microsecond * 10
	transport := http.Transport{
		Dial: func(network, addr string) (net.Conn, error) {
			return net.DialTimeout(network, addr, newTimeout)
		},
	}
	server.HTTPClient = &http.Client{Transport: &transport}

	mockReq := EnableMockDataWithTimes("componentMessages-fr-sunglow", 1)
	mockReq.Mock.Response().Delay(time.Microsecond * 11)

	locale, component := "fr", "sunglow"
	item := &common.DataItem{ID: common.DataItemID{IType: common.ItemComponent, Name: name, Version: version, Locale: locale, Component: component}}
	item.Attrs = server2.GetCacheInfo(item)

	resetInst(&testCfg)
	sgtnServer := inst.server

	// Get first time to set server stats as timeout
	err := sgtnServer.Get(item)
	_, ok := errors.Cause(err).(net.Error)
	assert.True(t, true, ok)
	assert.Equal(t, server.ServerTimeout, sgtnServer.Status)

	assert.True(t, gock.IsPending())

	// Get second time to get an error "Server times out" immediately
	err = sgtnServer.Get(item)
	assert.Equal(t, "Server times out", err.Error())
}

// Test return to normal status after serverRetryInterval and querying successfully
func TestTimeout2(t *testing.T) {

	defer gock.Off()

	EnableMockDataWithTimes("componentMessages-fr-sunglow", 1)

	locale, component := "fr", "sunglow"
	item := &common.DataItem{ID: common.DataItemID{IType: common.ItemComponent, Name: name, Version: version, Locale: locale, Component: component}}
	item.Attrs = server2.GetCacheInfo(item)

	resetInst(&testCfg)
	sgtnServer := inst.server

	sgtnServer.Status = server.ServerTimeout
	sgtnServer.LastErrorMoment = time.Now().Unix() - server.ServerRetryInterval - 1
	err := sgtnServer.Get(item)
	assert.Nil(t, err)
	assert.Equal(t, server.ServerNormal, sgtnServer.Status)
}

func TestVersionFallback(t *testing.T) {
	defer gock.Off()

	EnableMockDataWithTimes("componentMessages-versionfallback", 1)

	newCfg := testCfg
	newCfg.LocalBundles = ""
	resetInst(&newCfg)

	messages, err := inst.trans.GetComponentMessages(name, "1.0.1", "en", "sunglow")
	assert.Nil(t, err)
	assert.NotNil(t, messages)
	assert.Equal(t, 7, messages.(*common.DefaultComponentMsgs).Size())

	assert.True(t, gock.IsDone())
}
