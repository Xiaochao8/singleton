/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"fmt"
	"strings"
)

type transMgr struct {
	Translation
	fallbackChain []string
}

func newTransMgr(t Translation, fblocales []string) *transMgr {
	mgr := transMgr{Translation: t}
	mgr.fallbackChain = fblocales

	return &mgr
}

// GetStringMessage Get a message with optional arguments
func (t *transMgr) GetStringMessage(name, version, locale, component, key string, args ...string) (string, error) {
	message, err := t.Translation.GetStringMessage(name, version, locale, component, key)
	if err == nil {
		return message, nil
	}

	i := contains(t.fallbackChain, locale)
	// newLocale := locale
	for m := i + 1; m < len(t.fallbackChain); m++ {
		logger.Warn(fmt.Sprintf("fall back to locale '%s'", t.fallbackChain[m]))
		message, err = t.Translation.GetStringMessage(name, version, t.fallbackChain[m], component, key)
		if err == nil {
			// newLocale = fbLocle.defaultLocale
			break
		}
	}
	if err != nil {
		return "", err
	}

	for i, arg := range args {
		placeholder := fmt.Sprintf("{%d}", i)
		message = strings.Replace(message, placeholder, arg, 1)
	}

	return message, nil
}
