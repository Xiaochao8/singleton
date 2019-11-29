/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class ProductData extends ConcurrentHashMap<String, LocaleData> {
    String productName;
    String versionName;


    // Set<String> components;
    //
    // Set<String> locales;

    /**
     *
     */
    ProductData(final String productName, final String versionName) {
        this.productName = productName;
        this.versionName = versionName;
    }

    public Set<String> getLocales() {
        // if (null != this.locales && !this.locales.isEmpty())
        // return this.locales;

        return this.keySet();
    }

    public Set<String> getComponents() {
        // if (null != this.components && !this.components.isEmpty())
        // return this.components;

        Set<String> compSet = new HashSet<>();
        for(Entry<String, LocaleData> lEntry : this.entrySet()) {
            compSet.addAll(lEntry.getValue().keySet());
        }
        return compSet;
    }
}
