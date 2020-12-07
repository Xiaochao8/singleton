/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package server

import (
	"net"
	"net/http"
	"net/url"
	"path"
	"strings"
	"sync/atomic"
	"time"

	json "github.com/json-iterator/go"
	"github.com/pkg/errors"

	"github.com/vmware/singleton/internal/cache"
	"github.com/vmware/singleton/internal/common"
	"github.com/vmware/singleton/internal/msgorigin/localbundle"
)

const ServerRetryInterval = 2 // second
const (
	ServerNormal uint32 = iota
	ServerTimeout
)

func NewServer(serverURL string) (*ServerDAO, error) {
	svrURL, err := url.Parse(serverURL)
	if err != nil {
		return nil, err
	}

	s := &ServerDAO{SvrURL: svrURL, headers: atomic.Value{}}
	s.headers.Store(make(map[string]string, 0))

	return s, nil
}

//!+serverDAO

type ServerDAO struct {
	SvrURL          *url.URL
	Status          uint32
	LastErrorMoment int64
	headers         atomic.Value
}

func (s *ServerDAO) Get(item *common.DataItem) (err error) {
	var data interface{}
	info := item.Attrs.(*cache.ItemCacheInfo)

	switch item.ID.IType {
	case common.ItemComponent:
		data = new(queryProduct)
	case common.ItemLocales:
		data = new(queryLocales)
	case common.ItemComponents:
		data = new(queryComponents)
	default:
		return errors.Errorf(common.InvalidItemType, item.ID.IType)
	}

	urlToQuery := s.prepareURL(item)

	headers := s.getHTTPHeaders()
	headers[common.HttpHeaderIfNoneMatch] = info.GetETag()
	resp, err := s.sendRequest(urlToQuery, headers, data)
	if resp != nil {
		item.Attrs = resp.Header
	}
	if err != nil {
		return err
	}

	switch item.ID.IType {
	case common.ItemComponent:
		pData := data.(*queryProduct)
		if len(pData.Bundles) != 1 || pData.Bundles[0].Messages == nil {
			return errors.New(common.WrongServerData)
		}
		item.Data = &common.DefaultComponentMsgs{Messages: pData.Bundles[0].Messages}
	case common.ItemLocales:
		localesData := data.(*queryLocales)
		if localesData.Locales == nil {
			return errors.New(common.WrongServerData)
		}
		item.Data = localesData.Locales
	case common.ItemComponents:
		componentsData := data.(*queryComponents)
		if componentsData.Components == nil {
			return errors.New(common.WrongServerData)
		}
		item.Data = componentsData.Components
	default:
		return errors.Errorf(common.InvalidItemType, item.ID.IType)
	}

	return nil
}

func (s *ServerDAO) prepareURL(item *common.DataItem) *url.URL {
	urlToQuery := *s.SvrURL
	var myURL string

	id := item.ID
	name, version, locale, component := id.Name, id.Version, id.Locale, id.Component

	switch item.ID.IType {
	case common.ItemComponent:
		myURL = ProductTranslationGetConst
		addURLParams(&urlToQuery, map[string]string{LocalesConst: locale, ComponentsConst: component})
	case common.ItemLocales:
		myURL = ProductLocaleListGetConst
	case common.ItemComponents:
		myURL = ProductComponentListGetConst
	}

	myURL = strings.Replace(myURL, "{"+ProductNameConst+"}", name, 1)
	myURL = strings.Replace(myURL, "{"+VersionConst+"}", version, 1)

	urlToQuery.Path = path.Join(urlToQuery.Path, myURL)

	return &urlToQuery
}

func (s *ServerDAO) sendRequest(u *url.URL, header map[string]string, data interface{}) (*http.Response, error) {
	if atomic.LoadUint32(&s.Status) == ServerTimeout {
		if time.Now().Unix()-atomic.LoadInt64(&s.LastErrorMoment) < ServerRetryInterval {
			return nil, errors.New("Server times out")
		}
		atomic.StoreUint32(&s.Status, ServerNormal)
	}

	resp, err := GetDataFromServer(u, header, data)
	if err != nil {
		rootErr := errors.Cause(err)
		switch oe := rootErr.(type) {
		case net.Error:
			if oe.Timeout() {
				atomic.StoreUint32(&s.Status, ServerTimeout)
				atomic.StoreInt64(&s.LastErrorMoment, time.Now().Unix())
			}
		}

		return resp, err
	}

	return resp, nil
}

func (s *ServerDAO) SetHTTPHeaders(h map[string]string) {
	newHeaders := make(map[string]string, len(h))
	for k, v := range h {
		newHeaders[k] = v
	}

	s.headers.Store(newHeaders)
}

func (s *ServerDAO) getHTTPHeaders() (newHeaders map[string]string) {
	originalHeaders := s.headers.Load().(map[string]string)
	newHeaders = make(map[string]string, len(originalHeaders))
	for k, v := range originalHeaders {
		newHeaders[k] = v
	}

	return
}

//!-serverDAO

//!+common functions

func addURLParams(u *url.URL, args map[string]string) {
	values := u.Query()
	for k, v := range args {
		values.Add(k, v)
	}
	u.RawQuery = values.Encode()
}

var GetDataFromServer = func(u *url.URL, header map[string]string, data interface{}) (*http.Response, error) {
	type respResult struct {
		Code       int    `json:"code"`
		Message    string `json:"message"`
		ServerTime string `json:"serverTime"`
	}
	bodyObj := &struct {
		Result    respResult `json:"response"`
		Signature string     `json:"signature"`
		Data      json.Any   `json:"data"`
	}{}

	var bodyBytes []byte
	resp, err := localbundle.HTTPGet(u.String(), header, &bodyBytes)
	if err != nil {
		return resp, err
	}

	// Log.Debug(fmt.Sprintf("resp is: %#v", resp))

	if !isHTTPSuccess(resp.StatusCode) {
		return resp, &common.ServerError{resp.StatusCode, bodyObj.Result.Code, resp.Status, bodyObj.Result.Message}
	}

	err = json.Unmarshal(bodyBytes, bodyObj)
	if err != nil {
		return resp, errors.WithStack(err)
	}

	if !isBusinessSuccess(bodyObj.Result.Code) {
		return resp, &common.ServerError{resp.StatusCode, bodyObj.Result.Code, resp.Status, bodyObj.Result.Message}
	}

	// newData := bodyObj.Data.MustBeValid()
	// bodyObj.Data.ToVal(newData)

	bodyObj.Data.ToVal(data)

	//common.Log.Debug(fmt.Sprintf("decoded data is: %#v", data))
	//common.Log.Debug(fmt.Sprintf("HTTP headers are: %+v", resp.Header))

	return resp, nil
}

func isHTTPSuccess(code int) bool {
	return code >= 200 && code < 300
}

func isBusinessSuccess(code int) bool {
	return (code >= 200 && code < 300) || (code >= 600 && code < 700)
}

//!-common functions

//!+REST API Response structures

type (
	queryProduct struct {
		Name       string   `json:"productName"`
		Version    string   `json:"version"`
		Locales    []string `json:"locales"`
		Components []string `json:"components"`
		Bundles    []struct {
			Component string            `json:"component"`
			Messages  map[string]string `json:"messages"`
			Locale    string            `json:"locale"`
		} `json:"bundles"`
		URL    string `json:"url"`
		Status string `json:"status"`
		ID     int    `json:"id"`
	}

	queryComponents struct {
		Components []string `json:"components"`
		Version    string   `json:"version"`
		Name       string   `json:"productName"`
	}
	queryLocales struct {
		Locales []string `json:"locales"`
		Version string   `json:"version"`
		Name    string   `json:"productName"`
	}
)

//!-REST API Response structures
