/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.base.cache.MessageCache;
import com.vmware.vipclient.i18n.base.cache.TranslationCacheManager;

public class DataSourceManager {
    private static final Logger      logger = LoggerFactory.getLogger(DataSourceManager.class);


    private static HashMap<String, DataSourceManager> dataManagers     = new HashMap<>();

    private final VIPCfg                              cfg;

    private final CacheDataSource                     cacheDataSource;
    private final BundleDataSource                    bundleDataSource;
    private final ServerDataSource                    serverDataSource;
    private final List<AbstractDataSource> dataSources      = new ArrayList<>();


    /**
     *
     */
    private DataSourceManager(final VIPCfg cfg) {
        this.cfg = cfg;
        this.cacheDataSource = CacheDataSource.getCacheDataSource(this.cfg);
        this.bundleDataSource = BundleDataSource.getBundleDataSource(this.cfg);
        this.serverDataSource = ServerDataSource.getServerDataSource(this.cfg);
        this.dataSources.add(this.cacheDataSource);
        this.dataSources.add(this.bundleDataSource);
        this.dataSources.add(this.serverDataSource);
    }

    public static synchronized DataSourceManager getDataManager(final VIPCfg cfg) {
        DataSourceManager inst = dataManagers.get(cfg.getProductName());
        if (inst == null) {
            inst = new DataSourceManager(cfg);

            createTranslationCache();

            inst.initCache();

            DataSynchronizer.startSynchronizer(cfg);
        }
        return inst;
    }

    public ProductData getProductTranslation() {

        ProductData data = this.doGetProductTranslation(this.dataSources.listIterator());

        return data;
    }

    private ProductData doGetProductTranslation(final ListIterator<AbstractDataSource> iter) {
        ProductData data = null;
        if (iter.hasNext()) {
            AbstractDataSource source = iter.next();

            if (source.status == Status.READY)
                return source.getProductTranslation();

            data = this.doGetProductTranslation(iter);
            source.refreshData(data);
        }

        return data;
    }

    public List<String> getComponentList(final String product, final String version) {
        // TODO Auto-generated method stub
        return null;
    }


    public List<String> getLocaleList(final String product, final String version) {
        // TODO Auto-generated method stub
        return null;
    }


    public Map<String, String> getComponentTranslation(final String locale,
            final String component) {
        ListIterator<AbstractDataSource> iter = this.dataSources.listIterator();
        ComponentData data = this.doGetComponentTranslation(iter, locale, component);
        return data;

    }

    private ComponentData doGetComponentTranslation(final ListIterator<AbstractDataSource> iter,  final String locale, final String component) {
        ComponentData data = null;
        if(iter.hasNext()) {
            AbstractDataSource source = iter.next();

            data = source.getComponentTranslation(locale, component);
            if (null == data) {
                data = this.doGetComponentTranslation(iter,  locale,                        component);
                // The data must be not null if no exception throws
                LocaleData lData = new LocaleData(locale);
                lData.put(component, data);
                ProductData pData = new ProductData(this.cfg.getProductName(), this.cfg.getVersion());
                pData.put(locale, lData);
                source.refreshData(pData);
            }
        }
        return data;
    }


    public Map<String, Object> getComponentsTranslation(final List<String> locale, final List<String> component) {
        return null;
    }


    public String getStringTranslation(final String locale, final String component, final String key) {
        // TODO Auto-generated method stub
        return null;
    }


    private static synchronized void createTranslationCache() {
        if (TranslationCacheManager.getCache(VIPCfg.CACHE_L3) == null) {
            TranslationCacheManager.createTranslationCacheManager().registerCache(VIPCfg.CACHE_L3, MessageCache.class);
            DataSourceManager.logger.info("Translation Cache created.");
        }
    }

    public void initCache() {
        if (!this.cfg.isEnableCache() || !this.cfg.isInitializeCache())
            return;

        logger.info("Start initializing cache for pruduct {}.", this.cfg.getProductName());
        this.getProductTranslation();

    }
}
