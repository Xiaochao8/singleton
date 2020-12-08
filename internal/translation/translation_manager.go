/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package translation

import (
	"fmt"

	"github.com/vmware/singleton/internal/common"
)

type TransMgr struct {
	*TransInst
	fallbackChain []string
}

func NewTransMgr(t *TransInst, fblocales []string) *TransMgr {
	return &TransMgr{TransInst: t, fallbackChain: fblocales}
}

// GetStringMessage Get a message with optional arguments
func (t *TransMgr) GetStringMessage(name, version, locale, component, key string, args ...string) (string, error) {
	message, err := t.TransInst.GetStringMessage(name, version, locale, component, key, args...)
	if err == nil {
		return message, nil
	}

	i := common.IndexIgnoreCase(t.fallbackChain, locale)
	for m := i + 1; m < len(t.fallbackChain); m++ {
		common.Log.Warn(fmt.Sprintf("fall back to locale '%s'", t.fallbackChain[m]))
		message, err = t.TransInst.GetStringMessage(name, version, t.fallbackChain[m], component, key, args...)
		if err == nil {
			return message, nil
		}
	}

	return key, err
}
