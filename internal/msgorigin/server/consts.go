/*
 * Copyright 2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */

package server

// api url
const (
    apiRoot        = "/i18n/api/v2/translation"
    aipProductRoot = apiRoot + "/products/{" + ProductNameConst + "}/versions/{" + VersionConst + "}"

    // product-based
    ProductTranslationGetConst   = aipProductRoot
    ProductLocaleListGetConst    = aipProductRoot + "/localelist"
    ProductComponentListGetConst = aipProductRoot + "/componentlist"
)

// api param name
const (
    ProductNameConst = "productname"
    VersionConst     = "version"
    ComponentsConst  = "components"
    LocalesConst     = "locales"
)
