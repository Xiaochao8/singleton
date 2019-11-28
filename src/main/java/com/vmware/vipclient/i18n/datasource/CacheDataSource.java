/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.messages.dto.MessagesDTO;
import com.vmware.vipclient.i18n.messages.service.ProductService;

class CacheDataSource extends AbstractDataSource {

    private static HashMap<String, CacheDataSource> cacheDataSources = new HashMap<>();
    private final VIPCfg cfg;

    private CacheDataSource(final VIPCfg cfg) {
        this.cfg = cfg;
    }

    public static synchronized CacheDataSource getCacheDataSource(final VIPCfg cfg) {
        CacheDataSource inst = cacheDataSources.get(cfg.getProductName());
        if (inst == null) {
            inst = new CacheDataSource(cfg);
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
        // TODO Auto-generated method stub

    }

    public void addProduct(final VIPCfg cfg) {
        final MessagesDTO dto = new MessagesDTO();
        dto.setProductID(cfg.getProductName());
        dto.setVersion(cfg.getVersion());
        new ProductService(dto).getAllComponentTranslation();
    }

}
