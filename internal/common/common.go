/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package common

import (
	"fmt"
	"strings"
)

// ComponentMsgs The interface of a component's messages
type ComponentMsgs interface {
	// Get Get a message by key
	Get(key string) (value string, found bool)
}

// Logger The logger interface
type Logger interface {
	Debug(message string)
	Info(message string)
	Warn(message string)
	Error(message string)
}

type DataItemID struct {
	IType                            itemType
	Name, Version, Locale, Component string
}

//!+error definition

type ServerError struct {
	Code         int
	BusinessCode int
	Msg          string
	BusinessMsg  string
}

func (e *ServerError) Error() string {
	return fmt.Sprintf("Error from server is HTTP code: %d, message: %s, business code: %d, message: %s",
		e.Code, e.Msg, e.BusinessCode, e.BusinessMsg)
}

//!-error definition

//!+dataItem

type itemType int8

const (
	ItemComponent itemType = iota
	ItemLocales
	ItemComponents
)

type DataItem struct {
	ID    DataItemID
	Data  interface{}
	Attrs interface{}
}

//!-dataItem


func IndexIgnoreCase(slices []string, item string) int {
	for i, s := range slices {
		if strings.EqualFold(s, item) {
			return i
		}
	}

	return -1
}
