/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.List;
import java.util.Map;

import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.messages.dto.MessagesDTO;
import com.vmware.vipclient.i18n.messages.service.ProductService;

public class CacheDataSource extends AbstractDataSource {
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

    @Override
    public void addProduct(final VIPCfg cfg) {
        final MessagesDTO dto = new MessagesDTO();
        dto.setProductID(cfg.getProductName());
        dto.setVersion(cfg.getVersion());
        new ProductService(dto).getAllComponentTranslation();
    }

    private static CacheDataSource inst;

    /**
     * @return
     */
    public static synchronized CacheDataSource instance() {
        if (inst == null) {
            inst = new CacheDataSource();
        }
        return inst;
    }


}
