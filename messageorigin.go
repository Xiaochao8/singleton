/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

type messageOrigin interface {
	Get(item *dataItem) error
}

type messageOriginList []messageOrigin

func (ds messageOriginList) Get(item *dataItem) error {
	var err error
	for _, o := range ds {
		if err = o.Get(item); isFetchSuccess(err) {
			return err
		}
	}

	return err
}
