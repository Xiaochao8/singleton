/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vipclient.i18n.base.instances.Formatting;
import com.vmware.vipclient.i18n.base.instances.Message;
import com.vmware.vipclient.i18n.base.instances.TranslationMessage;

/**
 * provide a factory to create all kind of I18n instances
 */
public class I18nFactory {

    Logger                          logger      = LoggerFactory.getLogger(I18nFactory.class);


    // define global instance of I18nFactory
    private static I18nFactory      factory     = null;

    // store Message instance
    private Map<String, Message>    messages    = new HashMap<>();

    // store Formatting instance
    private Map<String, Formatting> formattings = new HashMap<>();

    /**
     * create I18nFactory
     *
     */
    private I18nFactory() {

    }

    /**
     * get an instance of I18nFactory
     *
     * @return
     */
    public static synchronized I18nFactory getInstance() {
        if (factory == null) {
            factory = new I18nFactory();
        }
        return factory;
    }

    /**
     * get an instance of com.vmware.vipclient.i18n.base.instances.Message
     *
     * @param c
     * @return
     */
    @SuppressWarnings("rawtypes")
    public Message getMessageInstance(final Class c, final VIPCfg cfg) {
        Message i = null;
        if (c == null)
            return i;

        String key;
        if (null == cfg) {
            key = c.getCanonicalName();
        } else {
            key = cfg.getProductName();
        }
        if (this.messages.containsKey(key))
            return this.messages.get(key);
        else {
            try {
                Object o = c.newInstance();
                if (o instanceof Message) {
                    i = (Message) o;
                    this.messages.put(key, i);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                this.logger.error("", e);
            }
        }

        if (i instanceof TranslationMessage) {
            ((TranslationMessage) i).setCfg(cfg);
        }
        return i;
    }

    public Message getMessageInstance(final Class c) {
        return this.getMessageInstance(c, null);
    }

    /**
     * get a instance of com.vmware.vipclient.i18n.base.instances.Formatting
     *
     * @param c
     * @return
     */
    @SuppressWarnings("rawtypes")
    public Formatting getFormattingInstance(final Class c) {
        Formatting i = null;
        if (c == null) {
            this.logger.error("the parameter class is null!");
            return i;
        } else if (VIPCfg.getInstance().getI18nScope() == null) {
            this.logger.error("i18nScope is null!");
            return i;
        } else {
            String key = c.getCanonicalName();
            if (this.formattings.containsKey(key))
                return this.formattings.get(key);
            else {
                try {
                    Object o = c.newInstance();
                    if (o instanceof Formatting) {
                        i = (Formatting) o;
                        this.formattings.put(key, i);
                    }
                } catch (InstantiationException | IllegalAccessException e) {
                    this.logger.error("", e);
                }
            }
            return i;
        }
    }

    public Map<String, Message> getMessages() {
        return this.messages;
    }

    public void setMessages(final Map<String, Message> messages) {
        this.messages = messages;
    }

    public Map<String, Formatting> getFormattings() {
        return this.formattings;
    }

    public void setFormattings(final Map<String, Formatting> formattings) {
        this.formattings = formattings;
    }

}
