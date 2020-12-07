/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package sgtn

import (
	"net/http"
	"strings"
	"sync"
	"time"

	"github.com/pkg/errors"

	"github.com/vmware/singleton/internal/cache"
	"github.com/vmware/singleton/internal/cache/cacheorigin"
	"github.com/vmware/singleton/internal/common"
	"github.com/vmware/singleton/internal/msgorigin"
	"github.com/vmware/singleton/internal/msgorigin/localbundle"
	server2 "github.com/vmware/singleton/internal/msgorigin/server"
	"github.com/vmware/singleton/internal/translation"
)

var (
	inst *instance
)

// instance Singleton instance
type instance struct {
	cfg            Config
	trans          Translation
	server         *server2.ServerDAO
	bundle         *localbundle.BundleDAO
	initializeOnce sync.Once
}

func init() {
	SetLogger(common.NewLogger())
	localbundle.HTTPClient = &http.Client{Timeout: time.Second * common.ServerTimeout}
}

// Initialize initialize the client
func Initialize(cfg *Config) {
	if err := checkConfig(cfg); err != nil {
		panic(err)
	}

	inst = &instance{}
	inst.cfg = *cfg
	inst.initializeOnce.Do(inst.doInitialize)
}

func (i *instance) doInitialize() {
	common.Log.Info("Initializing Singleton client")

	var originList msgorigin.MessageOriginList
	if len(i.cfg.ServerURL) != 0 {
		server, err := server2.NewServer(i.cfg.ServerURL)
		if err != nil {
			panic(err)
		}
		i.server = server
		originList = append(originList, server)
	}
	if strings.TrimSpace(i.cfg.LocalBundles) != "" {
		i.bundle = &localbundle.BundleDAO{i.cfg.LocalBundles}
		originList = append(originList, i.bundle)
	}
	CacheService := cacheorigin.NewCacheService(originList)

	transImpl := translation.TransInst{CacheService}
	var fallbackChains []string
	fallbackChains = append(fallbackChains, i.cfg.DefaultLocale)
	i.trans = translation.NewTransMgr(&transImpl, fallbackChains)

	cacheorigin.InitCacheInfoMap()
	if cacheorigin.CacheInst == nil {
		RegisterCache(cache.NewCache())
	}
}

func checkConfig(cfg *Config) error {
	switch {
	case cfg.LocalBundles == "" && cfg.ServerURL == "":
		return errors.New(common.OriginNotProvided)
	case cfg.DefaultLocale == "":
		return errors.New(common.DefaultLocaleNotProvided)
	default:
		return nil
	}
}

// GetTranslation Get translation instance
func GetTranslation() Translation {
	if inst == nil {
		panic(errors.New(common.Uninitialized))
	}

	return inst.trans
}

// SetHTTPHeaders Set customized HTTP headers
func SetHTTPHeaders(h map[string]string) error {
	if inst == nil {
		return errors.New(common.Uninitialized)
	}

	server := inst.server
	if server != nil {
		server.SetHTTPHeaders(h)
	}

	return nil
}

// RegisterCache Register cache implementation. There is a default implementation
func RegisterCache(c cache.Cache) {
	if cacheorigin.CacheInst != nil {
		return
	}

	cacheorigin.CacheInst = c
}

// SetLogger Set a global logger. There is a default console logger
func SetLogger(l common.Logger) {
	common.Log = l
}
