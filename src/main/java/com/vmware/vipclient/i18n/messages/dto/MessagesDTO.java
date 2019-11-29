/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n.messages.dto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.util.ConstantsKeys;
import com.vmware.vipclient.i18n.util.LocaleUtility;

/**
 * DTO objects for cache data encapsulation
 *
 */
public class MessagesDTO extends BaseDTO {
    Logger         logger = LoggerFactory.getLogger(MessagesDTO.class);

    private String comment;
    private String source;
    private String key;

    private String component;
    private String locale;

    public MessagesDTO() {
        super.setProductID(VIPCfg.getInstance().getProductName());
        super.setVersion(VIPCfg.getInstance().getVersion());
    }

    public MessagesDTO(final MessagesDTO another) {
        super.setProductID(another.getProductID());
        super.setVersion(another.getVersion());
        this.comment = another.comment;
        this.source = another.source;
        this.key = another.key;
        this.component = another.component;
        this.locale = another.locale;
    }

    /**
     * assembly the key of cache by productID, version, component and locale.
     *
     * @return The key of cache.
     */
    public String getCompositStrAsCacheKey() {
        StringBuilder key = new StringBuilder(super.getProductID());
        key.append(ConstantsKeys.UNDERLINE);
        key.append(super.getVersion());
        key.append(ConstantsKeys.UNDERLINE);
        key.append(this.component == null ? ConstantsKeys.DEFAULT_COMPONENT
                : this.component);
        key.append(ConstantsKeys.UNDERLINE);
        key.append(VIPCfg.getInstance().isPseudo());
        key.append(ConstantsKeys.UNDERLINE_POUND);
        key.append(this.locale == null ? ConstantsKeys.EN
                : LocaleUtility
                .fmtToMappedLocale(this.locale).toLanguageTag());
        return key.toString();
    }

    public String encryption(final String plainText) {
        String re_md5 = new String();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(plainText.getBytes());
            byte b[] = md.digest();
            int i;

            StringBuffer buf = new StringBuffer("");
            for (byte element : b) {
                i = element;
                if (i < 0) {
                    i += 256;
                }
                if (i < 16) {
                    buf.append("0");
                }
                buf.append(Integer.toHexString(i));
            }
            re_md5 = buf.toString();

        } catch (NoSuchAlgorithmException e) {
            this.logger.error("", e);
        }
        return re_md5;
    }

    public String getTransStatusAsCacheKey() {
        StringBuilder key = new StringBuilder(super.getProductID());
        key.append(ConstantsKeys.UNDERLINE);
        key.append(super.getVersion());
        key.append(ConstantsKeys.UNDERLINE);
        key.append(this.component == null ? ConstantsKeys.DEFAULT_COMPONENT
                : this.component);
        key.append(ConstantsKeys.UNDERLINE);
        key.append(ConstantsKeys.TRANSLATION_STATUS);
        key.append(ConstantsKeys.UNDERLINE);
        key.append(this.locale == null ? ConstantsKeys.EN
                : LocaleUtility
                .fmtToMappedLocale(this.locale).toLanguageTag());
        return key.toString();
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(final String comment) {
        this.comment = comment;
    }

    public String getSource() {
        return this.source;
    }

    public void setSource(final String source) {
        this.source = source;
    }

    public String getKey() {
        return this.key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public String getComponent() {
        return this.component;
    }

    public void setComponent(final String component) {
        this.component = component;
    }

    public String getLocale() {
        return this.locale;
    }

    public void setLocale(final String locale) {
        this.locale = LocaleUtility.normalizeToLanguageTag(locale);
    }

}
