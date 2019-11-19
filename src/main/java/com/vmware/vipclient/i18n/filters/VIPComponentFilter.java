/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n.filters;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vipclient.i18n.I18nFactory;
import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.base.cache.MessageCache;
import com.vmware.vipclient.i18n.base.instances.TranslationMessage;
import com.vmware.vipclient.i18n.exceptions.VIPClientInitException;
import com.vmware.vipclient.i18n.util.LocaleUtility;

/**
 * This class is specified as a filter in web.xml
 *
 */
public class VIPComponentFilter implements Filter {
    private final Logger logger = LoggerFactory.getLogger(VIPComponentFilter.class);

    @Override
    public void doFilter(final ServletRequest request, final ServletResponse response,
            final FilterChain chain) throws IOException, ServletException {
        String locale = this.getParamFromQuery(request, "locale");
        String component = this.getParamFromURI(request, "component");
        Map<String, String> ctmap;
        String messages = "{}";
        if (!LocaleUtility.isDefaultLocale(locale) && this.translation != null) {
            ctmap = this.translation.getStrings(LocaleUtility.fmtToMappedLocale(locale),
                    component);
            if (ctmap != null) {
                messages = JSONObject.toJSONString(ctmap);
            }
        }
        OutputStream os = response.getOutputStream();
        response.setContentType("text/javascript;charset=UTF-8");
        os.write(("var translation = {" + "\"messages\" : " + messages + ", "
                + "\"productName\" : \"" + VIPCfg.getInstance().getProductName()
                + "\", " + "\"version\" : \"" + VIPCfg.getInstance().getVersion()
                + "\", " + "\"vipServer\" : \""
                + VIPCfg.getInstance().getVipServer() + "\", " + "\"pseudo\" : \""
                + VIPCfg.getInstance().isPseudo() + "\", "
                + "\"collectSource\" : \"" + VIPCfg.getInstance().isCollectSource() + "\"};")
                .getBytes("UTF-8"));
    }

    private String getParamFromURI(final ServletRequest request, final String paramName) {
        HttpServletRequest res = (HttpServletRequest) request;
        String path = res.getRequestURI();
        String localepath = path
                .substring(path.indexOf(paramName) + paramName.length() + 1,
                        path.length());
        return localepath.substring(0,
                localepath.indexOf('/') >= 0 ? localepath.indexOf('/')
                        : localepath.length());
    }

    private String getParamFromQuery(final ServletRequest request, final String paramName) {
        HttpServletRequest res = (HttpServletRequest) request;
        String queryStr = res.getQueryString();
        String localepath = queryStr.substring(queryStr.indexOf(paramName)
                + paramName.length() + 1, queryStr.length());
        return localepath.substring(0,
                localepath.indexOf('/') >= 0 ? localepath.indexOf('/')
                        : localepath.length());
    }

    private String getSourceFromBody(final ServletRequest request) {
        BufferedReader br;
        String line;
        StringBuilder source = new StringBuilder("");
        try {
            br = request.getReader();
            while ((line = br.readLine()) != null) {
                source.append(line);
            }
        } catch (IOException e) {
            this.logger.error("", e);
        }
        return source.toString();
    }

    @Override
    public void destroy() {
        // Do Nothing
    }

    private TranslationMessage translation;
    private final VIPCfg             gc = VIPCfg.getInstance();

    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        if (this.gc.getVipService() == null) {
            try {
                this.gc.initialize("vipconfig");
            } catch (VIPClientInitException e) {
                this.logger.error("", e);
            }
            this.gc.initializeVIPService();
        }
        this.gc.createTranslationCache(MessageCache.class);
        I18nFactory i18n = I18nFactory.getInstance();
        this.translation = (TranslationMessage) i18n.getMessageInstance(TranslationMessage.class);
    }
}
