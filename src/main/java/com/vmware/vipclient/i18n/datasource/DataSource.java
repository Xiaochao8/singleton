/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DataSource {

    Set<String> getComponents();

    Set<String> getLocales();

    ProductData getProductTranslation();

    ComponentData getComponentTranslation(String locale, String component);

    Map<String, Object> getComponentsTranslation(List<String> locale,
            List<String> component);

}
