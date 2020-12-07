/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"io"
	"net/http"
	"testing"

	"github.com/pkg/errors"
	"github.com/stretchr/testify/assert"

	"github.com/vmware/singleton/internal/msgorigin/localbundle"
)

func TestNewRequest(t *testing.T) {

	saved := localbundle.NewHTTPRequest
	defer func() { localbundle.NewHTTPRequest = saved }()

	errMsg := "TestNewRequest"
	localbundle.NewHTTPRequest = func(method, url string, body io.Reader) (*http.Request, error) {
		return nil, errors.New(errMsg)
	}

	urlToGet := "any url"
	_, err := localbundle.HTTPGet(urlToGet, map[string]string{}, nil)
	assert.NotNil(t, err)
	assert.Contains(t, err.Error(), errMsg)
}
