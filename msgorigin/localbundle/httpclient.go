/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package localbundle

import (
	"io/ioutil"
	"net/http"

	"github.com/pkg/errors"

	"github.com/vmware/singleton/common"
)

const (
	ServerTimeout = 10
)

var (
	HTTPClient     *http.Client
	NewHTTPRequest = http.NewRequest
)

func HTTPGet(urlToGet string, header map[string]string, body *[]byte) (*http.Response, error) {
	common.Log.Info("URL to get is: " + urlToGet)

	req, err := NewHTTPRequest(http.MethodGet, urlToGet, nil)
	if err != nil {
		return nil, errors.WithStack(err)
	}
	req.Close = true
	for k, v := range header {
		req.Header.Add(k, v)
	}
	resp, err := HTTPClient.Do(req)
	if err != nil {
		return nil, errors.WithStack(err)
	}
	defer resp.Body.Close()

	b, err := ioutil.ReadAll(resp.Body)
	if err != nil {
		return resp, errors.WithStack(err)
	}
	*body = b

	return resp, err
}
