/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"bytes"
	"testing"

	"github.com/rs/zerolog"
	"github.com/stretchr/testify/assert"

	"github.com/vmware/singleton/internal/common"
)

func TestLogger(t *testing.T) {

	saved := common.Log
	defer func() { common.Log = saved }()

	buf := new(bytes.Buffer)
	common.Log = &common.DefaultLogger{zerolog.New(buf).With().Timestamp().Logger()}

	msg := "Test Warn Level"
	common.Log.Warn(msg)

	assert.Contains(t, buf.String(), msg)
}
