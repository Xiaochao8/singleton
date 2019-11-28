/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.HashMap;


public class ProductData extends HashMap<String, LocaleData> {
    String productName;
    String versionName;


    /**
     *
     */
    ProductData(final String productName, final String versionName) {
        this.productName = productName;
        this.versionName = versionName;
    }

}
