/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package common

type (
	DefaultComponentMsgs struct {
		Messages map[string]string
	}
)

func (d *DefaultComponentMsgs) Get(key string) (value string, found bool) {
	value, found = d.Messages[key]
	return
}

func (d *DefaultComponentMsgs) Size() int {
	return len(d.Messages)
}
