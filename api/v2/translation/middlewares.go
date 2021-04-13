/*
 * Copyright 2021 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package translation

import (
	"net/http"

	"sgtnserver/api"
	"sgtnserver/internal/sgtnerror"
	"sgtnserver/modules/translation/translationservice"

	"github.com/gin-gonic/gin"
	"go.uber.org/zap"
)

// HandleVersionFallback ...
func HandleVersionFallback(c *gin.Context) {
	if c.Request.Method != http.MethodGet {
		return
	}

	var ok bool
	name := c.Param(api.ProductNameAPIKey)
	if name == "" {
		if name, ok = c.GetQuery(api.ProductNameAPIKey); !ok {
			return
		}
	}
	version := c.Param(api.VersionAPIKey)
	if version == "" {
		if version, ok = c.GetQuery(api.VersionAPIKey); !ok {
			return
		}
	}

	pickedVersion := translationservice.PickupVersion(name, version)
	c.Set(api.SgtnVersionKey, pickedVersion)
	if pickedVersion != version {
		c.Set(api.VerFallbackKey, true)
		api.GetLogger(c).Warn("Version fallback occurs", zap.String("from", version), zap.String("to", pickedVersion))
	}
}

func HandleAllowList(c *gin.Context) {
	if c.IsAborted() || c.Request.Method != http.MethodGet {
		return
	}

	productName := c.Param(api.ProductNameAPIKey)
	if productName == "" {
		var ok bool
		if productName, ok = c.GetQuery(api.ProductNameAPIKey); !ok {
			return
		}
	}

	if !translationservice.IsProductExist(productName) {
		api.AbortWithError(c, sgtnerror.StatusBadRequest.WithUserMessage("Product '%s' doesn't exist", productName))
	}
}