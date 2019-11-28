/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmware.vipclient.i18n.VIPCfg;

public class ServerDataSource extends AbstractDataSource {

    private static HashMap<String, ServerDataSource> serverDataSources = new HashMap<>();
    private final VIPCfg                             cfg;

    private ServerDataSource(final VIPCfg cfg) {
        this.cfg = cfg;
    }

    public static synchronized ServerDataSource getServerDataSource(final VIPCfg cfg) {
        ServerDataSource inst = serverDataSources.get(cfg.getProductName());
        if (inst == null) {
            inst = new ServerDataSource(cfg);
        }
        return inst;
    }

    @Override
    public Type getSourceType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Status getSourceStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProductData getProductTranslation(final String product, final String version) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getComponentList(final String product, final String version) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getLocaleList(final String product, final String version) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProductData getComponentTranslation(final String product, final String version, final String locale,
            final String component) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Map<String, Object> getComponentsTranslation(final String product, final String version,
            final List<String> locale,
            final List<String> component) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getStringTranslation(final String product, final String version, final String locale,
            final String component,
            final String key) {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public void refreshData(final ProductData data) {
        // Server doesn't need to refresh
    }



}
