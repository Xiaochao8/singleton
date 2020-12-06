/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package localbundle

import (
	"io/ioutil"
	"path/filepath"
	"strings"

	json "github.com/json-iterator/go"
	"github.com/pkg/errors"

	"github.com/vmware/singleton/common"
)

const (
	bundlePrefix = "messages_"
	bundleSuffix = ".json"
)

type bundleFile struct {
	Component string            `json:"component"`
	Messages  map[string]string `json:"messages"`
	Locale    string            `json:"locale"`
}

//!+bundleDAO

type BundleDAO struct {
	Root string
}

func (d *BundleDAO) Get(item *common.DataItem) (err error) {
	id := item.ID
	switch id.IType {
	case common.ItemComponent:
		item.Data, err = d.GetComponentMessages(id.Name, id.Version, id.Locale, id.Component)
	case common.ItemLocales:
		item.Data, err = d.GetLocaleList(id.Name, id.Version)
	case common.ItemComponents:
		item.Data, err = d.GetComponentList(id.Name, id.Version)
	default:
		err = errors.Errorf(common.InvalidItemType, item.ID.IType)
	}

	return
}

func (d *BundleDAO) GetComponentList(name, version string) ([]string, error) {
	fis, err := ioutil.ReadDir(filepath.Join(d.Root, name, version))
	if err != nil {
		return nil, err
	}

	comps := make([]string, len(fis))
	for i, fi := range fis {
		if fi.IsDir() {
			comps[i] = fi.Name()
		}
	}

	return comps, nil
}
func (d *BundleDAO) GetLocaleList(name, version string) ([]string, error) {
	comps, err := d.GetComponentList(name, version)
	if err != nil {
		return nil, err
	}

	locales := map[string]struct{}{}
	for _, component := range comps {
		fPath := filepath.Join(d.Root, name, version, component)
		fis, err := ioutil.ReadDir(fPath)
		if err != nil {
			return nil, err
		}

		for _, fi := range fis {
			if !fi.IsDir() {
				locales[strings.ToLower(fi.Name())] = struct{}{}
			}
		}
	}

	lSlice := make([]string, 0, len(locales))
	for k := range locales {
		lSlice = append(lSlice, strings.TrimSuffix(strings.TrimPrefix(k, bundlePrefix), bundleSuffix))
	}

	return lSlice, nil
}

func (d *BundleDAO) GetComponentMessages(name, version, locale, component string) (common.ComponentMsgs, error) {
	compDirPath := filepath.Join(d.Root, name, version, component)
	files, err := ioutil.ReadDir(compDirPath)
	if err != nil {
		return nil, err
	}

	filename := bundlePrefix + locale + bundleSuffix
	for _, f := range files {
		if !f.IsDir() && strings.ToLower(filename) == strings.ToLower(f.Name()) {
			filename = f.Name()
			break
		}
	}

	contents, err := ioutil.ReadFile(filepath.Join(compDirPath, filename))
	if err != nil {
		return nil, err
	}

	b := new(bundleFile)
	err = json.Unmarshal(contents, b)
	if err != nil {
		return nil, err
	}
	if len(b.Messages) == 0 {
		return nil, errors.New("Wrong data from local bundle file")
	}

	return &common.DefaultComponentMsgs{b.Messages}, nil
}

//!-bundleDAO
