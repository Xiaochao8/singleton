/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n.fmt.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspTagException;
import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.BodyTagSupport;
import javax.servlet.jsp.tagext.Tag;

import org.apache.taglibs.standard.tag.common.core.Util;

import com.vmware.vipclient.i18n.I18nFactory;
import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.base.cache.MessageCache;
import com.vmware.vipclient.i18n.base.instances.TranslationMessage;
import com.vmware.vipclient.i18n.exceptions.VIPClientInitException;
import com.vmware.vipclient.i18n.util.LocaleUtility;

public class MessageSupport extends BodyTagSupport {

    public static final Locale defaultLocale = new Locale("en", "US");
    private PageContext        pageContext;
    protected String           keyAttrValue;
    protected boolean          keySpecified;
    private String             var;
    private int                scope;
    private final List               params;
    private final String             component     = "JSP", bundle = "webui";
    private TranslationMessage translation;

    public MessageSupport() {
        this.params = new ArrayList<>();
        this.init();
    }

    private void init() {
        this.var = null;
        this.scope = 1;
        this.keyAttrValue = null;
        this.keySpecified = false;
        VIPCfg gc = VIPCfg.getInstance();
        try {
            gc.initialize("vipconfig");
        } catch (VIPClientInitException e) {

        }
        gc.initializeVIPService();
        gc.createTranslationCache(MessageCache.class);
        I18nFactory i18n = I18nFactory.getInstance();
        this.translation = (TranslationMessage) i18n.getMessageInstance(TranslationMessage.class);
    }

    @Override
    public int doStartTag() throws JspException {
        this.params.clear();
        return 2;
    }

    @Override
    public int doEndTag() throws JspException {
        String key = null;
        if (this.keySpecified) {
            key = this.keyAttrValue;
        } else if ((this.bodyContent != null)
                && (this.bodyContent.getString() != null)) {
            key = this.bodyContent.getString().trim();
        }
        if ((key == null) || (key.equals(""))) {
            try {
                this.pageContext.getOut().print("Key is null");
            } catch (IOException ioe) {
                throw new JspTagException(ioe.toString(), ioe);
            }
            return 6;
        }
        Locale locale = LocaleUtility.getLocale();
        Object[] args = this.params.isEmpty() ? null : this.params.toArray();
        String message = this.translation == null ? ""
                : this.translation.getString2(this.component, this.bundle, locale, key, "TranslationCache", args);
        if (this.var != null) {
            this.pageContext.setAttribute(this.var, message, this.scope);
        } else {
            try {
                this.pageContext.getOut().write(message);
            } catch (IOException ioe) {
                throw new JspTagException(ioe.toString(), ioe);
            }
        }
        return 0;
    }

    @Override
    public Tag getParent() {
        return null;
    }

    @Override
    public void release() {
        this.init();
    }

    @Override
    public void setPageContext(final PageContext arg0) {
        this.pageContext = arg0;
    }

    @Override
    public void setParent(final Tag arg0) {
    }

    public String getVar() {
        return this.var;
    }

    public void setVar(final String var) {
        this.var = var;
    }

    public int getScope() {
        return this.scope;
    }

    public void setScope(final String scope) {
        this.scope = Util.getScope(scope);
    }

    public void addParam(final Object arg) {
        this.params.add(arg);
    }
}
