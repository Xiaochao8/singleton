/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

 package sgtn

type (
	cacheOrigin interface {
		messageOrigin
		IsExpired(item *dataItem) bool
	}

	cacheOriginList []cacheOrigin
)