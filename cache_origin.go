/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import "fmt"

type (
	cacheOrigin interface {
		messageOrigin
		IsExpired(item *dataItem) bool
	}

	cacheOriginList []cacheOrigin
)

func (ol cacheOriginList) Get(item *dataItem) (err error) {
	_, cached := cache.Get(item.id)

	for _, dao := range ol {
		if cached && !dao.IsExpired(item) {
			return nil
		}

		err = dao.Get(item)
		if err != nil {
			logger.Error(fmt.Sprintf(originQueryFailure, dao, err.Error()))
			if e, ok := err.(stackTracer); ok {
				logger.Error(fmt.Sprintf("%+v", e.StackTrace()))
			}
		} else {
			return nil
		}
	}

	return
}

func (ol cacheOriginList) IsExpired(item *dataItem) bool {
	for _, o := range ol {
		return o.IsExpired(item)
	}

	return false
}
