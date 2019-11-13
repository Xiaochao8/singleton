/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ResourceBundle;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import com.vmware.vipclient.i18n.base.DataSourceEnum;
import com.vmware.vipclient.i18n.base.Task;
import com.vmware.vipclient.i18n.base.VIPService;
import com.vmware.vipclient.i18n.base.cache.Cache;
import com.vmware.vipclient.i18n.base.cache.CacheMode;
import com.vmware.vipclient.i18n.base.cache.TranslationCacheManager;
import com.vmware.vipclient.i18n.exceptions.VIPClientInitException;
import com.vmware.vipclient.i18n.exceptions.VIPJavaClientException;
import com.vmware.vipclient.i18n.messages.api.opt.local.LocalSourceOpt;
import com.vmware.vipclient.i18n.messages.dto.MessagesDTO;
import com.vmware.vipclient.i18n.messages.service.ProductService;

/**
 * a class uses to define the global environment setting for I18nFactory
 */
public class VIPCfg {

    Logger                                 logger        = LoggerFactory.getLogger(VIPCfg.class);

    // define global instance
    private static VIPCfg                  gcInstance;
    private static Map<String, VIPCfg>     moduleCfgs    = new HashMap<>();
    private VIPService                     vipService;
    private TranslationCacheManager        translationCacheManager;

    // data origin
    private DataSourceEnum                 messageOrigin = DataSourceEnum.VIP;

    // cache mode
    private CacheMode                      cacheMode     = CacheMode.MEMORY;

    private String                         cachePath;

    // define the global parameters
    private boolean                        pseudo;
    private boolean                        collectSource;
    private boolean                        cleanCache;
    private long                           cacheExpiredTime;
    private boolean                        machineTranslation;
    private boolean                        initializeCache;
    private int                            interalCleanCache;
    private String                         productName;
    private String                         version;
    private String                         vipServer;

    private ArrayList<Map<String, Object>> components;
    private String                         i18nScope     = "numbers,dates,currencies,plurals,measurements";

    // define key for cache management
    public static final String             CACHE_L3      = "CACHE_L3";
    public static final String             CACHE_L2      = "CACHE_L2";

    public boolean isSubInstance() {
        return isSubInstance;
    }

    public void setSubInstance(final boolean subInstance) {
        isSubInstance = subInstance;
    }

    private boolean isSubInstance = false;

    private VIPCfg() {

    }

    /**
     * create a default instance of VIPCfg
     *
     * @return
     */
    public static synchronized VIPCfg getInstance() {
        if (gcInstance == null) {
            gcInstance = new VIPCfg();
        }
        return gcInstance;
    }

    /**
     * create a default instance of VIPCfg
     *
     * @return
     */
    public static synchronized VIPCfg getSubInstance(final String productName) {
        if (!VIPCfg.moduleCfgs.containsKey(productName)) {
            final VIPCfg cfg = new VIPCfg();
            cfg.isSubInstance = true;
            VIPCfg.moduleCfgs.put(productName, cfg);
        }

        if (VIPCfg.moduleCfgs.containsKey(productName) && VIPCfg.moduleCfgs.get(productName) != null)
            return VIPCfg.moduleCfgs.get(productName);
        else
            return gcInstance;
    }

    /**
     * initialize the instance by parameter
     *
     * @param vipServer
     * @param productName
     * @param version
     */
    public void initialize(final String vipServer, final String productName, final String version) {
        this.productName = productName;
        this.version = version;
        this.vipServer = vipServer;
    }

    /**
     * initialize the instance by a properties file
     *
     * @param cfg
     */
    public void initialize(final String cfg) throws VIPClientInitException {
        final ResourceBundle prop = ResourceBundle.getBundle(cfg);
        if (prop == null)
            throw new VIPClientInitException("Can't not initialize VIPCfg, resource bundle is null.");

        if (prop.containsKey("productName")) {
            productName = prop.getString("productName");
        }
        if (isSubInstance() && !VIPCfg.moduleCfgs.containsKey(productName))
            throw new VIPClientInitException(
                    "Can't not initialize sub VIPCfg instance, the product name is not defined in config file.");
        if (prop.containsKey("version")) {
            version = prop.getString("version");
        }
        if (prop.containsKey("vipServer")) {
            vipServer = prop.getString("vipServer");
        }
        if (prop.containsKey("pseudo")) {
            pseudo = Boolean.parseBoolean(prop.getString("pseudo"));
        }
        if (prop.containsKey("collectSource")) {
            collectSource = Boolean.parseBoolean(prop.getString("collectSource"));
        }
        if (prop.containsKey("initializeCache")) {
            initializeCache = Boolean.parseBoolean(prop.getString("initializeCache"));
        }
        if (prop.containsKey("cleanCache")) {
            cleanCache = Boolean.parseBoolean(prop.getString("cleanCache"));
        }
        if (prop.containsKey("machineTranslation")) {
            machineTranslation = Boolean.parseBoolean(prop.getString("machineTranslation"));
        }
        if (prop.containsKey("i18nScope")) {
            i18nScope = prop.getString("i18nScope");
        }
        if (prop.containsKey("cacheExpiredTime")) {
            cacheExpiredTime = Long.parseLong(prop.getString("cacheExpiredTime"));
        }
    }

    /**
     * initialize the instance by a configuration file
     *
     * @param cfg
     */
    @SuppressWarnings("serial")
    private void initializeWithYamlFile(final String cfg) throws IOException {
        final InputStream stream = ClassLoader.getSystemResourceAsStream(cfg);
        final LinkedHashMap<String, Object> data = new Yaml().loadAs(stream, new LinkedHashMap<String, Object>() {}.getClass());

        for (final Entry<String, Object> entry : data.entrySet()) {
            try {
                this.getClass().getDeclaredField(entry.getKey()).set(this, entry.getValue());
            } catch (final IllegalArgumentException e) {
                throw new VIPJavaClientException(
                        String.format("Invalid value '%s' for setting '%s'!", entry.getValue(), entry.getKey()), e);
            } catch (final NoSuchFieldException e) {
                throw new VIPJavaClientException("Invalid setting item: " + entry.getKey());
            } catch (SecurityException | IllegalAccessException e) {
                throw new VIPJavaClientException("Unknow errorr");
            }
        }

        // Load source bundles
        LocalSourceOpt.loadResources(components);
    }

    /**
     * initialize VIPService instances to provide HTTP requester
     */
    public void initializeVIPService() {
        vipService = VIPService.getVIPServiceInstance();
        try {
            vipService.initializeVIPService(productName, version,
                    vipServer);
        } catch (final MalformedURLException e) {
            logger.error("'vipServer' in configuration isn't a valid URL!");
        }
    }

    /**
     * set cache from out-process
     *
     * @param c
     */
    public void setTranslationCache(final Cache c) {
        translationCacheManager = TranslationCacheManager
                .createTranslationCacheManager();
        if (translationCacheManager != null) {
            translationCacheManager.registerCache(VIPCfg.CACHE_L3, c);
            logger.info("Translation Cache created.");
        }
        if (isInitializeCache()) {
            logger.info("Initializing Cache.");
            initializeMessageCache();
        }
        if (isCleanCache()) {
            logger.info("startTaskOfCacheClean.");
            Task.startTaskOfCacheClean(VIPCfg.getInstance(), interalCleanCache);
        }
        final Cache createdCache = TranslationCacheManager
                .getCache(VIPCfg.CACHE_L3);
        if (createdCache != null && getCacheExpiredTime() > 0) {
            c.setExpiredTime(getCacheExpiredTime());
        }
    }

    /**
     * create translation cache
     *
     * @param cacheClass
     * @return
     */
    public synchronized Cache createTranslationCache(final Class<?> cacheClass) {
        translationCacheManager = TranslationCacheManager
                .createTranslationCacheManager();
        if (translationCacheManager != null) {
            if (TranslationCacheManager.getCache(VIPCfg.CACHE_L3) == null) {
                translationCacheManager.registerCache(VIPCfg.CACHE_L3,
                        cacheClass);
                logger.info("Translation Cache created.");
                if (isInitializeCache()) {
                    logger.info("InitializeCache.");
                    initializeMessageCache();
                }
                if (isCleanCache()) {
                    logger.info("startTaskOfCacheClean.");
                    Task.startTaskOfCacheClean(VIPCfg.getInstance(), interalCleanCache);
                }
                final Cache c = TranslationCacheManager.getCache(VIPCfg.CACHE_L3);
                if (c != null && getCacheExpiredTime() > 0) {
                    c.setExpiredTime(getCacheExpiredTime());
                }
            }

            return TranslationCacheManager.getCache(VIPCfg.CACHE_L3);
        } else
            return null;

    }

    /**
     * create cache for formatting data
     *
     * @param cacheClass
     */
    public Cache createFormattingCache(final Class<?> cacheClass) {
        translationCacheManager = TranslationCacheManager
                .createTranslationCacheManager();
        if (translationCacheManager != null) {
            translationCacheManager.registerCache(VIPCfg.CACHE_L2,
                    cacheClass);
            logger.info("Formatting cache created.");
        }
        if (isCleanCache()) {
            logger.error("clean cache.");
            Task.startTaskOfCacheClean(VIPCfg.getInstance(), interalCleanCache);
        }
        return TranslationCacheManager.getCache(VIPCfg.CACHE_L2);
    }

    /**
     * load all translation to cache by product
     */
    public void initializeMessageCache() {
        final MessagesDTO dto = new MessagesDTO();
        dto.setProductID(getProductName());
        dto.setVersion(getVersion());
        new ProductService(dto).getAllComponentTranslation();
        if (translationCacheManager != null) {
            logger.info("Translation data is loaded to cache, size is " + translationCacheManager.size() + ".");
        }
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(final String productName) {
        this.productName = productName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getVipServer() {
        return vipServer;
    }

    public void setVipServer(final String vipServer) {
        this.vipServer = vipServer;
    }

    public boolean isPseudo() {
        return pseudo;
    }

    public void setPseudo(final boolean pseudo) {
        this.pseudo = pseudo;
    }

    public boolean isCollectSource() {
        return collectSource;
    }

    public void setCollectSource(final boolean collectSource) {
        this.collectSource = collectSource;
    }

    public boolean isCleanCache() {
        return cleanCache;
    }

    public void setCleanCache(final boolean cleanCache) {
        this.cleanCache = cleanCache;
    }

    public VIPService getVipService() {
        return vipService;
    }

    public TranslationCacheManager getCacheManager() {
        return translationCacheManager;
    }

    public int getInteralCleanCache() {
        return interalCleanCache;
    }

    public void setInteralCleanCache(final int interalCleanCache) {
        this.interalCleanCache = interalCleanCache;
    }

    public String getI18nScope() {
        return i18nScope;
    }

    public void setI18nScope(final String i18nScope) {
        this.i18nScope = i18nScope;
    }

    public boolean isMachineTranslation() {
        return machineTranslation;
    }

    public void setMachineTranslation(final boolean machineTranslation) {
        this.machineTranslation = machineTranslation;
    }

    public DataSourceEnum getMessageOrigin() {
        return messageOrigin;
    }

    public void setMessageOrigin(final DataSourceEnum messageOrigin) {
        this.messageOrigin = messageOrigin;
    }

    public boolean isInitializeCache() {
        return initializeCache;
    }

    public void setInitializeCache(final boolean initializeCache) {
        this.initializeCache = initializeCache;
    }

    public long getCacheExpiredTime() {
        return cacheExpiredTime;
    }

    public void setCacheExpiredTime(final long cacheExpiredTime) {
        this.cacheExpiredTime = cacheExpiredTime;
    }

    public CacheMode getCacheMode() {
        return cacheMode;
    }

    public void setCacheMode(final CacheMode cacheMode) {
        this.cacheMode = cacheMode;
    }

    public String getCachePath() {
        return cachePath;
    }

    public void setCachePath(final String cachePath) {
        this.cachePath = cachePath;
    }
}
