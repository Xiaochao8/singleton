/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"fmt"

	"github.com/pkg/errors"
)

//!+ transInst
type transInst struct {
	msgOrigin messageOrigin
}

func (t *transInst) GetStringMessage(name, version, locale, component, key string, args ...string) (string, error) {
	if name == "" || version == "" || locale == "" || component == "" || key == "" {
		return key, errors.New(wrongPara)
	}
	bundleData, err := t.GetComponentMessages(name, version, locale, component)
	if err != nil {
		return "", err
	}

	if msg, ok := bundleData.Get(key); ok {
		return msg, nil
	} else {
		return "", fmt.Errorf("Fail to get message for locale: %s, component: %s, key: %s", locale, component, key)
	}
}

func (t *transInst) GetLocaleList(name, version string) (data []string, err error) {
	if name == "" || version == "" {
		return nil, errors.New(wrongPara)
	}

	item := &dataItem{dataItemID{itemLocales, name, version, "", ""}, nil, nil}
	err = t.msgOrigin.Get(item)
	data, _ = item.data.([]string)
	return
}

func (t *transInst) GetComponentList(name, version string) (data []string, err error) {
	if name == "" || version == "" {
		return nil, errors.New(wrongPara)
	}

	item := &dataItem{dataItemID{itemComponents, name, version, "", ""}, nil, nil}
	err = t.msgOrigin.Get(item)
	data, _ = item.data.([]string)
	return
}

func (t *transInst) GetComponentMessages(name, version, locale, component string) (data ComponentMsgs, err error) {
	if name == "" || version == "" || locale == "" || component == "" {
		return nil, errors.New(wrongPara)
	}

	item := &dataItem{dataItemID{itemComponent, name, version, locale, component}, nil, nil}
	err = t.msgOrigin.Get(item)
	data, _ = item.data.(ComponentMsgs)
	return
}

//!- transInst

// // localeTrans translation of a locale
// type localeTrans struct {
// 	Translation
// 	locale string
// }

// // GetStringMessage Get a message with optional arguments
// func (t *localeTrans) GetStringMessage(name, version, locale, component, key string, args ...string) (string, error) {
// 	if locale == t.locale {
// 		return "", fmt.Errorf("locale '%s' is already default locale", locale)
// 	}
// 	return t.Translation.GetStringMessage(name, version, t.locale, component, key)
// }

// // GetComponentMessages Get component messages
// func (t *localeTrans) GetComponentMessages(name, version, locale, component string) (ComponentMsgs, error) {
// 	if locale == t.locale {
// 		return nil, fmt.Errorf("locale '%s' is already default locale", locale)
// 	}
// 	return t.Translation.GetComponentMessages(name, version, t.locale, component)
// }

// type sourceTrans struct {
// 	transInst
// }

// // GetStringMessage Get a message with optional arguments
// func (t *sourceTrans) GetStringMessage(name, version, locale, component, key string, args ...string) (string, error) {
// 	newLocale := "source"
// 	return t.transInst.GetStringMessage(name, version, newLocale, component, key)
// }

// // GetComponentMessages Get component messages
// func (t *sourceTrans) GetComponentMessages(name, version, locale, component string) (data ComponentMsgs, err error) {
// 	newLocale := "source"
// 	return t.transInst.GetComponentMessages(name, version, newLocale, component)
// }
