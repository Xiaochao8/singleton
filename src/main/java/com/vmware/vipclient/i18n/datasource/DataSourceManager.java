/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.base.cache.MessageCache;
import com.vmware.vipclient.i18n.base.cache.TranslationCacheManager;
import com.vmware.vipclient.i18n.exceptions.VIPJavaClientException;

public class DataSourceManager {
    private static final Logger      logger = LoggerFactory.getLogger(DataSourceManager.class);


    static HashMap<String, DataSourceManager> dataManagers = new HashMap<>();

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
        // ProductData data = this.doGetProductTranslation(this.dataSources.listIterator());

        // try {
        return this.cacheDataSource.getProductTranslation();
        // } finally {
        // if (this.cacheDataSource.isExpired()) {
        // this.initCache();
        // }
        // }
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

    public Set<String> getComponents() {
        return this.cacheDataSource.getComponents();
    }


    public Set<String> getLocales() {
        return this.cacheDataSource.getLocales();
    }


    public Map<String, String> getComponentTranslation(final String locale, final String component) {
        // if (!(this.isValidLocale(locale) && this.isValidComponent(component)))
        // return null;

        // ListIterator<AbstractDataSource> iter = this.dataSources.listIterator();
        // ComponentData data = this.doGetComponentTranslation(iter, locale, component);
        // return data;

        return this.cacheDataSource.getComponentTranslation(locale, component);
    }

    private boolean isValidLocale(final String locale) {
        return this.getLocales().contains(locale);
    }

    private boolean isValidComponent(final String component) {
        return this.getComponents().contains(component);
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
        return this.cacheDataSource.getStringTranslation(locale, component, key);
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

        logger.info("Initializing cache for pruduct {}.", this.cfg.getProductName());

        ProductData pData = null;
        try {
            pData = this.serverDataSource.getProductTranslation();
        } catch (Exception e) {
            logger.error("", e);
        }

        try {
            if (null == pData || pData.isEmpty()) {
                pData = this.bundleDataSource.getProductTranslation();
            }
            else {
                this.bundleDataSource.setProductTranslation(pData);
            }
        } catch (Exception e) {
            logger.error("", e);
        }

        if (null == pData || pData.isEmpty())
            throw new VIPJavaClientException("Failed to get any data!");
        else {
            this.cacheDataSource.setProductTranslation(pData);
        }
    }

    void syncCache() {
        try {
            this.initCache();
        }catch(Exception e) {
            logger.error("", e);
        }
    }
}
