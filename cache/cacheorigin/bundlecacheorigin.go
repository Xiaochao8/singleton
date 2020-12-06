/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package cacheorigin

import (
    "github.com/vmware/singleton/common"
    "github.com/vmware/singleton/msgorigin/localbundle"
)

//!+BundleCache

type BundleCache struct {
    *localbundle.BundleDAO
}

func (*BundleCache) IsExpired(item *common.DataItem) bool {
    return false
}

func (c *BundleCache) Get(item *common.DataItem) (err error) {
    err = c.BundleDAO.Get(item)
    if err == nil {
        CacheInst.Set(item.ID, item.Data)
        return nil
    }
    return err
}

//!-BundleCache

