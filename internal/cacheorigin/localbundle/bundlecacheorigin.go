/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package localbundle

import (
    "github.com/vmware/singleton/internal/cache"
    "github.com/vmware/singleton/internal/common"
    "github.com/vmware/singleton/internal/msgorigin/localbundle"
)

//!+BundleCache

type BundleCache struct {
    *localbundle.BundleDAO
}

func (*BundleCache) IsExpired(*common.DataItem) bool {
    return false
}

func (c *BundleCache) Get(item *common.DataItem) (err error) {
    err = c.BundleDAO.Get(item)
    if err == nil {
        cache.CacheInst.Set(item.ID, item.Data)
        return nil
    }
    return err
}

//!-BundleCache

