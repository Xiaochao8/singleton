/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

type (
	cacheOrigin interface {
		messageOrigin
		IsExpired(item *dataItem) bool
	}

	cacheOriginList []cacheOrigin
)