/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package msgorigin

import (
    "github.com/vmware/singleton/common"
)

type (
    MessageOrigin interface {
        Get(item *common.DataItem) error
    }

    MessageOriginList []MessageOrigin
)
