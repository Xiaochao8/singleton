/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.concurrent.ConcurrentHashMap;


public class LocaleData extends ConcurrentHashMap<String, ComponentData> {

    String localeName;

    /**
     *
     */
    public LocaleData(final String localeName) {
        this.localeName = localeName;
    }
}
