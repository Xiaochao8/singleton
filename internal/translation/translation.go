/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package translation

import (
	"fmt"
	"strings"

	"github.com/pkg/errors"

	"github.com/vmware/singleton/internal/common"
	"github.com/vmware/singleton/internal/msgorigin"
)

//!+TransInst

type TransInst struct {
	MsgOrigin msgorigin.MessageOrigin
}

func (t *TransInst) GetStringMessage(name, version, locale, component, key string, args ...string) (string, error) {
	if name == "" || version == "" || locale == "" || component == "" || key == "" {
		return key, errors.New(common.WrongPara)
	}
	bundleData, err := t.GetComponentMessages(name, version, locale, component)
	if err != nil {
		return "", err
	}

	if msg, ok := bundleData.Get(key); ok {
		for i, arg := range args {
			placeholder := fmt.Sprintf("{%d}", i)
			msg = strings.Replace(msg, placeholder, arg, 1)
		}
		return msg, nil
	} else {
		return "", fmt.Errorf("fail to get message for locale: %s, component: %s, key: %s", locale, component, key)
	}
}

func (t *TransInst) GetLocaleList(name, version string) (data []string, err error) {
	if name == "" || version == "" {
		return nil, errors.New(common.WrongPara)
	}

	item := &common.DataItem{common.DataItemID{common.ItemLocales, name, version, "", ""}, nil, nil}
	err = t.MsgOrigin.Get(item)
	if nil != item.Data {
		data, _ = item.Data.([]string)
	}
	return
}

func (t *TransInst) GetComponentList(name, version string) (data []string, err error) {
	if name == "" || version == "" {
		return nil, errors.New(common.WrongPara)
	}

	item := &common.DataItem{common.DataItemID{common.ItemComponents, name, version, "", ""}, nil, nil}
	err = t.MsgOrigin.Get(item)
	if nil != item.Data {
		data, _ = item.Data.([]string)
	}
	return
}

func (t *TransInst) GetComponentMessages(name, version, locale, component string) (data common.ComponentMsgs, err error) {
	if name == "" || version == "" || locale == "" || component == "" {
		return nil, errors.New(common.WrongPara)
	}

	item := &common.DataItem{common.DataItemID{common.ItemComponent, name, version, locale, component}, nil, nil}
	err = t.MsgOrigin.Get(item)
	if nil != item.Data {
		data, _ = item.Data.(common.ComponentMsgs)
	}
	return
}

//!-TransInst
