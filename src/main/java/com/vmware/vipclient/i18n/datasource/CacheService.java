/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.base.cache.Cache;
import com.vmware.vipclient.i18n.base.cache.TranslationCacheManager;
import com.vmware.vipclient.i18n.messages.dto.MessagesDTO;
import com.vmware.vipclient.i18n.util.ConstantsKeys;
import com.vmware.vipclient.i18n.util.LocaleUtility;

public class CacheService {
    private final MessagesDTO dto;

    public CacheService(final MessagesDTO dto) {
        this.dto = dto;
    }

    public Map<String, String> getCacheOfComponent() {
        String cacheKey = this.dto.getCompositStrAsCacheKey();
        Locale matchedLocale = LocaleUtility.pickupLocaleFromList(
                this.getSupportedLocalesFromCache(),
                this.getLocaleByCachedKey(cacheKey));
        cacheKey = cacheKey.substring(0,
                cacheKey.indexOf(ConstantsKeys.UNDERLINE_POUND) + 2)
                + matchedLocale.toLanguageTag();
        VIPCfg.getInstance().getCacheManager();
        Cache c = TranslationCacheManager.getCache(VIPCfg.CACHE_L3);
        if (c == null)
            return null;
        else
            return c.get(cacheKey);
    }

    public void addCacheOfComponent(final Map<String, String> dataMap) {
        String cacheKey = this.dto.getCompositStrAsCacheKey();
        VIPCfg.getInstance().getCacheManager();
        Cache c = TranslationCacheManager.getCache(VIPCfg.CACHE_L3);
        if (c != null) {
            c.put(cacheKey, dataMap);
        }
    }

    public void updateCacheOfComponent(final Map<String, String> dataMap) {
        String cacheKey = this.dto.getCompositStrAsCacheKey();
        VIPCfg.getInstance().getCacheManager();
        Cache c = TranslationCacheManager.getCache(VIPCfg.CACHE_L3);
        if (c != null) {
            Map<String, String> oldmap = c.get(cacheKey);
            if (oldmap == null) {
                c.put(cacheKey, dataMap);
            } else {
                oldmap.putAll(dataMap);
                c.put(cacheKey, oldmap);
            }
        }
    }

    public boolean isContainComponent() {
        boolean f = false;
        String cacheKey = this.dto.getCompositStrAsCacheKey();
        VIPCfg.getInstance().getCacheManager();
        Cache c = TranslationCacheManager.getCache(VIPCfg.CACHE_L3);
        if (c != null) {
            f = c.keySet().contains(cacheKey);
        }
        return f;
    }

    public boolean isContainStatus() {
        boolean f = false;
        String cacheKey = this.dto.getTransStatusAsCacheKey();
        VIPCfg.getInstance().getCacheManager();
        Cache c = TranslationCacheManager.getCache(VIPCfg.CACHE_L3);
        if (c != null) {
            f = c.keySet().contains(cacheKey);
        }
        return f;
    }

    public Map<String, String> getCacheOfStatus() {
        String cacheKey = this.dto.getTransStatusAsCacheKey();
        VIPCfg.getInstance().getCacheManager();
        Cache c = TranslationCacheManager.getCache(VIPCfg.CACHE_L3);
        if (c == null)
            return null;
        else
            return c.get(cacheKey);
    }

    public void addCacheOfStatus(final Map<String, String> dataMap) {
        String cacheKey = this.dto.getTransStatusAsCacheKey();
        VIPCfg.getInstance().getCacheManager();
        Cache c = TranslationCacheManager.getCache(VIPCfg.CACHE_L3);
        if (c != null) {
            c.put(cacheKey, dataMap);
        }
    }

    public List<Locale> getSupportedLocalesFromCache() {
        List<Locale> locales = new ArrayList<>();
        VIPCfg.getInstance().getCacheManager();
        Cache c = TranslationCacheManager.getCache(VIPCfg.CACHE_L3);
        if (c == null)
            return locales;
        Set<String> keySet = c.keySet();
        Object[] keys = keySet.toArray();
        Map<String, Object> tempMap = new HashMap<>();
        for (Object key : keys) {
            String ckey = (String) key;
            String locale = ckey.substring(
                    ckey.indexOf(ConstantsKeys.UNDERLINE_POUND) + 2,
                    ckey.length());
            if (!tempMap.containsKey(locale)) {
                locales.add(Locale.forLanguageTag(locale.replace("_", "-")));
                tempMap.put(locale, locale);
            }
        }
        return locales;
    }

    private Locale getLocaleByCachedKey(final String key) {
        String locale = key.substring(
                key.indexOf(ConstantsKeys.UNDERLINE_POUND) + 2, key.length());
        return Locale.forLanguageTag(locale.replace("_", "-"));
    }
}
