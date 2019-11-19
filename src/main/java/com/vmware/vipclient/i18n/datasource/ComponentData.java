/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.HashMap;
import java.util.Map;


public class ComponentData extends HashMap<String, String> {

    String componentName;

    Map<Object, Object> messages;

    /**
     *
     */
    public ComponentData(final String componentName) {
        this.componentName = componentName;
    }

    public void setData(final Map<Object, Object> data) {
        this.messages = data;
    }

    public Map<Object, Object> getData() {
        return this.messages;
    }
}
