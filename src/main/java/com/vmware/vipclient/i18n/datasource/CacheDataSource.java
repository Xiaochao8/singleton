/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.vmware.vipclient.i18n.VIPCfg;

class CacheDataSource extends AbstractDataSource {

    private static HashMap<String, CacheDataSource> cacheDataSources = new HashMap<>();
    private final VIPCfg cfg;

    private ProductData                             productData;

    private CacheDataSource(final VIPCfg cfg) {
        this.cfg = cfg;
    }

    public static synchronized CacheDataSource getCacheDataSource(final VIPCfg cfg) {
        CacheDataSource inst = cacheDataSources.get(cfg.getProductName());
        if (inst == null) {
            inst = new CacheDataSource(cfg);
            if (cfg.isEnableCache()) {
                inst.status = Status.Uninitialized;
            }
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
    public synchronized ProductData getProductTranslation() {
        if (this.status != Status.READY)
            return null;

        return this.productData;
    }

    synchronized void setProductTranslation(final ProductData data) {
        this.productData = data;

        this.status = Status.READY;
    }

    @Override
    public synchronized Set<String> getComponentList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized Set<String> getLocaleList() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ComponentData getComponentTranslation(final String locale,
            final String component) {
        if (this.status != Status.READY)
            return null;

        return this.getProductTranslation().get(locale).get(component);

    }

    @Override
    public Map<String, Object> getComponentsTranslation(final List<String> locale, final List<String> component) {
        // TODO Auto-generated method stub
        return null;
    }

    public String getStringTranslation(final String locale, final String component, final String key) {
        if (this.status != Status.READY)
            return null;

        return this.getComponentTranslation(locale, component).getData().get(key);
    }


    @Override
    public synchronized void refreshData(final ProductData data) {
        for(Entry<String, LocaleData> entry : data.entrySet()) {
            String locale = entry.getKey();
            LocaleData lData = entry.getValue();

            for(Entry<String, ComponentData> compEntry: lData.entrySet()) {
                String compName = compEntry.getKey();
                ComponentData compData = compEntry.getValue();

                LocaleData existingLocaleData = this.productData.get(locale);
                if (null == existingLocaleData) {
                    existingLocaleData = new LocaleData(locale);
                    this.productData.put(locale, existingLocaleData);
                }
                existingLocaleData.put(compName, compData);
            }
        }

        this.status = Status.READY;
    }
}
