/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package cacheorigin

import (
	"fmt"

	"github.com/pkg/errors"

	"github.com/vmware/singleton/common"
	"github.com/vmware/singleton/msgorigin"
)

type (
	CacheOrigin interface {
		msgorigin.MessageOrigin
		IsExpired(item *common.DataItem) bool
	}

	CacheOriginList []CacheOrigin
)

func (ol CacheOriginList) Get(item *common.DataItem) (err error) {
	_, cached := CacheInst.Get(item.ID)

	for _, dao := range ol {
		if cached && !dao.IsExpired(item) {
			return nil
		}

		err = dao.Get(item)
		if err != nil {
			common.Log.Error(fmt.Sprintf(common.OriginQueryFailure, dao, err.Error()))
			if e, ok := err.(stackTracer); ok {
				common.Log.Error(fmt.Sprintf("%+v", e.StackTrace()))
			}
		} else {
			return nil
		}
	}

	return
}

func (ol CacheOriginList) IsExpired(item *common.DataItem) bool {
	for _, o := range ol {
		return o.IsExpired(item)
	}

	return false
}

type stackTracer interface {
	StackTrace() errors.StackTrace
}