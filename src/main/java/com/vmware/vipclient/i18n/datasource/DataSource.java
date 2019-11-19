/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.List;
import java.util.Map;

public interface DataSource {

    ProductData getProductTranslation(String product, String version);

    List<String> getComponentList(String product, String version);

    List<String> getLocaleList(String product, String version);

    ProductData getComponentTranslation(String product, String version, String locale, String component);

    Map<String, Object> getComponentsTranslation(String product, String version, List<String> locale,
            List<String> component);

    String getStringTranslation(String product, String version, String locale, String component, String key);
}

