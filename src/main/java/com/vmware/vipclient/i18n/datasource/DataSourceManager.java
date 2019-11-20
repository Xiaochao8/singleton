/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.ArrayList;
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

    private final CacheDataSource    cacheDataSource  = CacheDataSource.instance();
    private final BundleDataSource   bundleDataSource = BundleDataSource.instance();
    private final ServerDataSource   serverDataSource = ServerDataSource.instance();
    private final List<AbstractDataSource> dataSources      = new ArrayList<>();

    private static DataSourceManager inst;

    /**
     *
     */
    private DataSourceManager() {
        this.dataSources.add(this.cacheDataSource);
        this.dataSources.add(this.bundleDataSource);
        this.dataSources.add(this.serverDataSource);
    }

    public static synchronized DataSourceManager instance() {
        if (null == inst) {
            inst = new DataSourceManager();
        }
        return inst;
    }

    public ProductData getProductTranslation(final String product, final String version) {

        ProductData data = this.doGetProductTranslation(this.dataSources.listIterator(), product, version);

        return data;
    }

    private ProductData doGetProductTranslation(final ListIterator<AbstractDataSource> iter, final String product,
            final String version) {
        ProductData data = null;
        if (iter.hasNext()) {
            AbstractDataSource source = iter.next();

            if (source.status == Status.READY) {
                data = source.getProductTranslation(product, version);
            }
            if (null != data && data.size() > 0) {
            } else {
                data = this.doGetProductTranslation(iter, product, version);
                if (source.status != Status.NA) {
                    source.refreshData(data);
                }
            }
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


    public Map<String, String> getComponentTranslation(final String product, final String version, final String locale,
            final String component) {
        ListIterator<AbstractDataSource> iter = this.dataSources.listIterator();
        ProductData data = this.doGetComponentTranslation(iter, product, version, locale, component);
        return data.get(locale).get(component);

    }

    private ProductData doGetComponentTranslation(final ListIterator<AbstractDataSource> iter, final String product,
            final String version, final String locale, final String component) {
        ProductData data = null;
        if(iter.hasNext()) {
            AbstractDataSource source = iter.next();

            if (source.status == Status.READY) {
                data = source.getComponentTranslation(product, version, locale, component);
            }
            if (null != data && data.size() > 0) {
            }
            else {
                data = this.doGetComponentTranslation(iter, product, version, locale, component);
                if (source.status != Status.NA) {
                    source.refreshData(data);
                }
            }
        }
        return data;
    }


    public Map<String, Object> getComponentsTranslation(final String product, final String version,
            final List<String> locale,
            final List<String> component) {
        return null;
    }


    public String getStringTranslation(final String product, final String version, final String locale,
            final String component, final String key) {
        // TODO Auto-generated method stub
        return null;
    }


    public void addProduct(final VIPCfg cfg) {
        if (TranslationCacheManager.getCache(VIPCfg.CACHE_L3) == null) {
            TranslationCacheManager.createTranslationCacheManager().registerCache(VIPCfg.CACHE_L3, MessageCache.class);
            DataSourceManager.logger.info("Translation Cache created.");
        }

        this.initCache(cfg);

    }

    public void initCache(final VIPCfg cfg) {
        if (cfg.isInitializeCache() && this.cacheDataSource.getSourceStatus() == Status.READY) {
            logger.info("Initialize Cache.");

            this.getProductTranslation(cfg.getProductName(), cfg.getVersion());
        }

        DataSynchronizer.startSynchronizer(cfg);

        // final Cache c = TranslationCacheManager.getCache(VIPCfg.CACHE_L3);
        // if (c != null && cfg.getCacheExpiredTime() > 0) {
        // c.setExpiredTime(cfg.getCacheExpiredTime());
        // }
    }


}
