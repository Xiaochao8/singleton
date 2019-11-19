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
import com.vmware.vipclient.i18n.datasource.DataSourceManager;
import com.vmware.vipclient.i18n.exceptions.VIPClientInitException;
import com.vmware.vipclient.i18n.exceptions.VIPJavaClientException;
import com.vmware.vipclient.i18n.messages.api.opt.local.LocalSourceOpt;
import com.vmware.vipclient.i18n.util.StringUtil;

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

    private String                         bundleFolder;
    private String                         i18nScope     = "numbers,dates,currencies,plurals,measurements";

    // define key for cache management
    public static final String             CACHE_L3      = "CACHE_L3";
    public static final String             CACHE_L2      = "CACHE_L2";

    public boolean isSubInstance() {
        return this.isSubInstance;
    }

    public void setSubInstance(final boolean subInstance) {
        this.isSubInstance = subInstance;
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
     * initialize the instance by a configuration file
     *
     * @param cfg
     */
    public void initialize(final String cfg) throws VIPClientInitException {
        if (StringUtil.isEmpty(cfg))
            throw new VIPClientInitException("Can't not initialize VIPCfg, configuration file is empty.");

        if (cfg.endsWith(".yaml")) {
            try {
                this.initializeWithYamlFile(cfg);
            } catch (IOException e) {
                VIPClientInitException e1 = new VIPClientInitException(
                        String.format("Failed to initialize VIPCfg with: %s. Error message: %s", cfg, e.getMessage()));
                e1.setStackTrace(e.getStackTrace());
                throw e1;
            }
        } else {
            this.initializeWithPropFile(cfg);
        }

    }

    /**
     * initialize the instance by a properties file
     *
     * @param cfg
     */
    private void initializeWithPropFile(final String cfg) throws VIPClientInitException {
        final ResourceBundle prop = ResourceBundle.getBundle(cfg);
        if (prop == null)
            throw new VIPClientInitException("Can't not initialize VIPCfg, resource bundle is null.");

        if (prop.containsKey("productName")) {
            this.productName = prop.getString("productName");
        }
        if (this.isSubInstance() && !VIPCfg.moduleCfgs.containsKey(this.productName))
            throw new VIPClientInitException(
                    "Can't not initialize sub VIPCfg instance, the product name is not defined in config file.");
        if (prop.containsKey("version")) {
            this.version = prop.getString("version");
        }
        if (prop.containsKey("vipServer")) {
            this.vipServer = prop.getString("vipServer");
        }
        if (prop.containsKey("pseudo")) {
            this.pseudo = Boolean.parseBoolean(prop.getString("pseudo"));
        }
        if (prop.containsKey("collectSource")) {
            this.collectSource = Boolean.parseBoolean(prop.getString("collectSource"));
        }
        if (prop.containsKey("initializeCache")) {
            this.initializeCache = Boolean.parseBoolean(prop.getString("initializeCache"));
        }
        if (prop.containsKey("cleanCache")) {
            this.cleanCache = Boolean.parseBoolean(prop.getString("cleanCache"));
        }
        if (prop.containsKey("machineTranslation")) {
            this.machineTranslation = Boolean.parseBoolean(prop.getString("machineTranslation"));
        }
        if (prop.containsKey("i18nScope")) {
            this.i18nScope = prop.getString("i18nScope");
        }
        if (prop.containsKey("cacheExpiredTime")) {
            this.cacheExpiredTime = Long.parseLong(prop.getString("cacheExpiredTime"));
        }
    }

    /**
     * initialize the instance by a yaml configuration file
     *
     * @param cfg
     * @throws IOException, VIPClientInitException
     */
    @SuppressWarnings("serial")
    private void initializeWithYamlFile(final String cfg) throws IOException, VIPClientInitException {
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

        if (this.isSubInstance() && !VIPCfg.moduleCfgs.containsKey(this.productName))
            throw new VIPClientInitException(
                    "Can't not initialize sub VIPCfg instance, the product name is not defined in config file.");

        // Load source bundles
        LocalSourceOpt.loadResources(this.components);
    }

    /**
     * initialize VIPService instances to provide HTTP requester
     */
    public void initializeVIPService() {
        this.vipService = VIPService.getVIPServiceInstance();
        try {
            this.vipService.initializeVIPService(this.productName, this.version,
                    this.vipServer);
        } catch (final MalformedURLException e) {
            this.logger.error("'vipServer' in configuration isn't a valid URL!");
        }
    }

    /**
     * set cache from out-process
     *
     * @param c
     */
    public void setTranslationCache(final Cache c) {
        this.translationCacheManager = TranslationCacheManager
                .createTranslationCacheManager();
        if (this.translationCacheManager != null) {
            this.translationCacheManager.registerCache(VIPCfg.CACHE_L3, c);
            this.logger.info("Translation Cache created.");
        }
        if (this.isInitializeCache()) {
            this.logger.info("Initializing Cache.");
            this.initializeMessageCache();
        }
        if (this.isCleanCache()) {
            this.logger.info("startTaskOfCacheClean.");
            Task.startTaskOfCacheClean(VIPCfg.getInstance(), this.interalCleanCache);
        }
        final Cache createdCache = TranslationCacheManager
                .getCache(VIPCfg.CACHE_L3);
        if (createdCache != null && this.getCacheExpiredTime() > 0) {
            c.setExpiredTime(this.getCacheExpiredTime());
        }
    }

    /**
     * create translation cache
     *
     * @param cacheClass
     * @return
     */
    public synchronized Cache createTranslationCache(final Class<?> cacheClass) {
        DataSourceManager.instance().addProduct(this);

        this.translationCacheManager = TranslationCacheManager.createTranslationCacheManager();
        return TranslationCacheManager.getCache(VIPCfg.CACHE_L3);
    }

    /**
     * create cache for formatting data
     *
     * @param cacheClass
     */
    public Cache createFormattingCache(final Class<?> cacheClass) {
        this.translationCacheManager = TranslationCacheManager
                .createTranslationCacheManager();
        if (this.translationCacheManager != null) {
            this.translationCacheManager.registerCache(VIPCfg.CACHE_L2,
                    cacheClass);
            this.logger.info("Formatting cache created.");
        }
        if (this.isCleanCache()) {
            this.logger.error("clean cache.");
            Task.startTaskOfCacheClean(VIPCfg.getInstance(), this.interalCleanCache);
        }
        return TranslationCacheManager.getCache(VIPCfg.CACHE_L2);
    }

    /**
     * load all translation to cache by product
     */
    public void initializeMessageCache() {
        DataSourceManager.instance().initCache(this);
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductName(final String productName) {
        this.productName = productName;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(final String version) {
        this.version = version;
    }

    public String getVipServer() {
        return this.vipServer;
    }

    public void setVipServer(final String vipServer) {
        this.vipServer = vipServer;
    }

    public boolean isPseudo() {
        return this.pseudo;
    }

    public void setPseudo(final boolean pseudo) {
        this.pseudo = pseudo;
    }

    public boolean isCollectSource() {
        return this.collectSource;
    }

    public void setCollectSource(final boolean collectSource) {
        this.collectSource = collectSource;
    }

    public boolean isCleanCache() {
        return this.cleanCache;
    }

    public void setCleanCache(final boolean cleanCache) {
        this.cleanCache = cleanCache;
    }

    public VIPService getVipService() {
        return this.vipService;
    }

    public TranslationCacheManager getCacheManager() {
        return this.translationCacheManager;
    }

    public int getInteralCleanCache() {
        return this.interalCleanCache;
    }

    public void setInteralCleanCache(final int interalCleanCache) {
        this.interalCleanCache = interalCleanCache;
    }

    public String getI18nScope() {
        return this.i18nScope;
    }

    public void setI18nScope(final String i18nScope) {
        this.i18nScope = i18nScope;
    }

    public boolean isMachineTranslation() {
        return this.machineTranslation;
    }

    public void setMachineTranslation(final boolean machineTranslation) {
        this.machineTranslation = machineTranslation;
    }

    public DataSourceEnum getMessageOrigin() {
        return this.messageOrigin;
    }

    public void setMessageOrigin(final DataSourceEnum messageOrigin) {
        this.messageOrigin = messageOrigin;
    }

    public boolean isInitializeCache() {
        return this.initializeCache;
    }

    public void setInitializeCache(final boolean initializeCache) {
        this.initializeCache = initializeCache;
    }

    public long getCacheExpiredTime() {
        return this.cacheExpiredTime;
    }

    public void setCacheExpiredTime(final long cacheExpiredTime) {
        this.cacheExpiredTime = cacheExpiredTime;
    }

    public CacheMode getCacheMode() {
        return this.cacheMode;
    }

    public void setCacheMode(final CacheMode cacheMode) {
        this.cacheMode = cacheMode;
    }

    public String getCachePath() {
        return this.cachePath;
    }

    public void setCachePath(final String cachePath) {
        this.cachePath = cachePath;
    }

    public String getBundleFolder() {
        return this.bundleFolder;
    }

    public void setBundleFolder(final String bundleFolder) {
        this.bundleFolder = bundleFolder;
    }

}
