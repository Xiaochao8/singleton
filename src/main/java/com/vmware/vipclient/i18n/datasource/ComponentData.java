/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.HashMap;
import java.util.Map;


public class ComponentData extends HashMap<String, String> {

    String componentName;

    Map<String, String> messages;

    /**
     *
     */
    public ComponentData(final String componentName) {
        this.componentName = componentName;
    }

    public void setData(final Map<String, String> data) {
        this.messages = data;
    }

    public Map<String, String> getData() {
        return this.messages;
    }
}
