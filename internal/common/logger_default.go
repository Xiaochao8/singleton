/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package common

import (
	"os"

	"github.com/rs/zerolog"
)

type DefaultLogger struct {
	Logger zerolog.Logger
}

var Log Logger

func NewLogger() Logger {
	l := new(DefaultLogger)

	zerolog.TimeFieldFormat = zerolog.TimeFormatUnix
	zerolog.SetGlobalLevel(zerolog.InfoLevel)

	l.Logger = zerolog.New(os.Stderr).With().Timestamp().Logger()

	return l
}

func (l *DefaultLogger) Debug(message string) {
	l.Logger.Debug().Msg(message)
}

func (l *DefaultLogger) Info(message string) {
	l.Logger.Info().Msg(message)
}
func (l *DefaultLogger) Warn(message string) {
	l.Logger.Warn().Msg(message)
}
func (l *DefaultLogger) Error(message string) {
	l.Logger.Error().Msg(message)
}


