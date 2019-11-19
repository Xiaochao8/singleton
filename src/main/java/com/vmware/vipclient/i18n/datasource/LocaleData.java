/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.HashMap;


public class LocaleData extends HashMap<String, ComponentData> {

    String                     localeName;

    /**
     *
     */
    public LocaleData(final String localeName) {
        this.localeName = localeName;
    }
}
