/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vip.i18n;

import java.util.Date;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.vmware.vipclient.i18n.I18nFactory;
import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.base.cache.FormattingCache;
import com.vmware.vipclient.i18n.base.instances.DateFormatting;
import com.vmware.vipclient.i18n.exceptions.VIPClientInitException;

public class DateFormatUtilTest extends BaseTestClass {
    DateFormatting dateFormatI18n;

    @Before
    public void init() {
        VIPCfg gc = VIPCfg.getInstance();
        try {
            gc.initialize("vipconfig");
        } catch (VIPClientInitException e) {
            this.logger.error("", e);
        }
        gc.initializeVIPService();
        gc.createFormattingCache(FormattingCache.class);
        I18nFactory i18n = I18nFactory.getInstance();
        this.dateFormatI18n = (DateFormatting) i18n.getFormattingInstance(DateFormatting.class);
    }

    @Test
    public void testFormatDate() {
        final long timestamp = 1511156364801l;

        final String fullDateForZh = "2017年11月20日星期一";
        final String longDateForZh = "2017年11月20日";
        final String mediumDateForZh = "2017年11月20日";
        final String shortDateForZh = "2017/11/20";
        final String fullTimeForZh = "GMT+08:00 下午1:39:24";
        final String longTimeForZh = "GMT+8 下午1:39:24";
        final String mediumTimeForZh = "下午1:39:24";
        final String shortTimeForZh = "下午1:39";
        final String fullForZh = "2017年11月20日星期一 GMT+08:00 下午1:39:24";
        final String longForZh = "2017年11月20日 GMT+8 下午1:39:24";
        final String mediumForZh = "2017年11月20日 下午1:39:24";
        final String shortForZh = "2017/11/20 下午1:39";

        final String fullDateForFr = "lundi 20 novembre 2017";
        final String longDateForFr = "20 novembre 2017";
        final String mediumDateForFr = "20 nov. 2017";
        final String shortDateForFr = "20/11/2017";
        final String fullTimeForFr = "13:39:24 GMT+08:00";
        final String longTimeForFr = "13:39:24 GMT+8";
        final String mediumTimeForFr = "13:39:24";
        final String shortTimeForFr = "13:39";
        final String fullForFr = "lundi 20 novembre 2017 à 13:39:24 GMT+08:00";
        final String longForFr = "20 novembre 2017 à 13:39:24 GMT+8";
        final String mediumForFr = "20 nov. 2017 à 13:39:24";
        final String shortForFr = "20/11/2017 13:39";

        final String timeZone = "GMT+8";
        Date date = new Date(timestamp);

        final Locale zhLocale = new Locale("zh", "CN");
        Assert.assertEquals(fullDateForZh, this.dateFormatI18n.formatDate(date,
                "fullDate", timeZone, zhLocale));
        Assert.assertEquals(longDateForZh, this.dateFormatI18n.formatDate(date,
                "longDate", timeZone, zhLocale));
        Assert.assertEquals(mediumDateForZh, this.dateFormatI18n.formatDate(
                date, "mediumDate", timeZone, zhLocale));
        Assert.assertEquals(shortDateForZh, this.dateFormatI18n.formatDate(date,
                "shortDate", timeZone, zhLocale));
        Assert.assertEquals(fullTimeForZh, this.dateFormatI18n.formatDate(date,
                "fullTime", timeZone, zhLocale));
        Assert.assertEquals(longTimeForZh, this.dateFormatI18n.formatDate(date,
                "longTime", timeZone, zhLocale));
        Assert.assertEquals(mediumTimeForZh, this.dateFormatI18n.formatDate(
                date, "mediumTime", timeZone, zhLocale));
        Assert.assertEquals(shortTimeForZh, this.dateFormatI18n.formatDate(date,
                "shortTime", timeZone, zhLocale));
        Assert.assertEquals(fullForZh, this.dateFormatI18n.formatDate(date,
                "full", timeZone, zhLocale));
        Assert.assertEquals(longForZh, this.dateFormatI18n.formatDate(date,
                "long", timeZone, zhLocale));
        Assert.assertEquals(mediumForZh, this.dateFormatI18n.formatDate(date,
                "medium", timeZone, zhLocale));
        Assert.assertEquals(shortForZh, this.dateFormatI18n.formatDate(date,
                "short", timeZone, zhLocale));

        final Locale frLocale = new Locale("fr", "");
        Assert.assertEquals(fullDateForFr, this.dateFormatI18n.formatDate(date,
                "fullDate", timeZone, frLocale));
        Assert.assertEquals(longDateForFr, this.dateFormatI18n.formatDate(date,
                "longDate", timeZone, frLocale));
        Assert.assertEquals(mediumDateForFr, this.dateFormatI18n.formatDate(
                date, "mediumDate", timeZone, frLocale));
        Assert.assertEquals(shortDateForFr, this.dateFormatI18n.formatDate(date,
                "shortDate", timeZone, frLocale));
        Assert.assertEquals(fullTimeForFr, this.dateFormatI18n.formatDate(date,
                "fullTime", timeZone, frLocale));
        Assert.assertEquals(longTimeForFr, this.dateFormatI18n.formatDate(date,
                "longTime", timeZone, frLocale));
        Assert.assertEquals(mediumTimeForFr, this.dateFormatI18n.formatDate(
                date, "mediumTime", timeZone, frLocale));
        Assert.assertEquals(shortTimeForFr, this.dateFormatI18n.formatDate(date,
                "shortTime", timeZone, frLocale));
        Assert.assertEquals(fullForFr, this.dateFormatI18n.formatDate(date,
                "full", timeZone, frLocale));
        Assert.assertEquals(longForFr, this.dateFormatI18n.formatDate(date,
                "long", timeZone, frLocale));
        Assert.assertEquals(mediumForFr, this.dateFormatI18n.formatDate(date,
                "medium", timeZone, frLocale));
        Assert.assertEquals(shortForFr, this.dateFormatI18n.formatDate(date,
                "short", timeZone, frLocale));
    }

    @Test
    public void testRegionFormatDate() {
        final long timestamp = 1511156364801l;

        final String fullDateForZh = "2017年11月20日星期一";
        final String longDateForZh = "2017年11月20日";
        final String mediumDateForZh = "2017年11月20日";
        final String shortDateForZh = "2017/11/20";
        final String fullTimeForZh = "GMT+08:00 下午1:39:24";
        final String longTimeForZh = "GMT+8 下午1:39:24";
        final String mediumTimeForZh = "下午1:39:24";
        final String shortTimeForZh = "下午1:39";
        final String fullForZh = "2017年11月20日星期一 GMT+08:00 下午1:39:24";
        final String longForZh = "2017年11月20日 GMT+8 下午1:39:24";
        final String mediumForZh = "2017年11月20日 下午1:39:24";
        final String shortForZh = "2017/11/20 下午1:39";

        final String fullDateForFr = "lundi 20 novembre 2017";
        final String longDateForFr = "20 novembre 2017";
        final String mediumDateForFr = "20 nov. 2017";
        final String shortDateForFr = "20/11/2017";
        final String fullTimeForFr = "13:39:24 GMT+08:00";
        final String longTimeForFr = "13:39:24 GMT+8";
        final String mediumTimeForFr = "13:39:24";
        final String shortTimeForFr = "13:39";
        final String fullForFr = "lundi 20 novembre 2017 à 13:39:24 GMT+08:00";
        final String longForFr = "20 novembre 2017 à 13:39:24 GMT+8";
        final String mediumForFr = "20 nov. 2017 à 13:39:24";
        final String shortForFr = "20/11/2017 13:39";

        final String timeZone = "GMT+8";
        Date date = new Date(timestamp);
        String language = "zh-Hans";
        String region = "CN";

        Assert.assertEquals(fullDateForZh, this.dateFormatI18n.formatDate(date,
                "fullDate", timeZone, language, region));
        Assert.assertEquals(longDateForZh, this.dateFormatI18n.formatDate(date,
                "longDate", timeZone, language, region));
        Assert.assertEquals(mediumDateForZh, this.dateFormatI18n.formatDate(
                date, "mediumDate", timeZone, language, region));
        Assert.assertEquals(shortDateForZh, this.dateFormatI18n.formatDate(date,
                "shortDate", timeZone, language, region));
        Assert.assertEquals(fullTimeForZh, this.dateFormatI18n.formatDate(date,
                "fullTime", timeZone, language, region));
        Assert.assertEquals(longTimeForZh, this.dateFormatI18n.formatDate(date,
                "longTime", timeZone, language, region));
        Assert.assertEquals(mediumTimeForZh, this.dateFormatI18n.formatDate(
                date, "mediumTime", timeZone, language, region));
        Assert.assertEquals(shortTimeForZh, this.dateFormatI18n.formatDate(date,
                "shortTime", timeZone, language, region));
        Assert.assertEquals(fullForZh, this.dateFormatI18n.formatDate(date,
                "full", timeZone, language, region));
        Assert.assertEquals(longForZh, this.dateFormatI18n.formatDate(date,
                "long", timeZone, language, region));
        Assert.assertEquals(mediumForZh, this.dateFormatI18n.formatDate(date,
                "medium", timeZone, language, region));
        Assert.assertEquals(shortForZh, this.dateFormatI18n.formatDate(date,
                "short", timeZone, language, region));

        String frlanguage = "fr";
        String frregion = "FR";
        // final Locale frLocale = new Locale("fr", "");
        Assert.assertEquals(fullDateForFr, this.dateFormatI18n.formatDate(date,
                "fullDate", timeZone, frlanguage, frregion));
        Assert.assertEquals(longDateForFr, this.dateFormatI18n.formatDate(date,
                "longDate", timeZone, frlanguage, frregion));
        Assert.assertEquals(mediumDateForFr, this.dateFormatI18n.formatDate(
                date, "mediumDate", timeZone, frlanguage, frregion));
        Assert.assertEquals(shortDateForFr, this.dateFormatI18n.formatDate(date,
                "shortDate", timeZone, frlanguage, frregion));
        Assert.assertEquals(fullTimeForFr, this.dateFormatI18n.formatDate(date,
                "fullTime", timeZone, frlanguage, frregion));
        Assert.assertEquals(longTimeForFr, this.dateFormatI18n.formatDate(date,
                "longTime", timeZone, frlanguage, frregion));
        Assert.assertEquals(mediumTimeForFr, this.dateFormatI18n.formatDate(
                date, "mediumTime", timeZone, frlanguage, frregion));
        Assert.assertEquals(shortTimeForFr, this.dateFormatI18n.formatDate(date,
                "shortTime", timeZone, frlanguage, frregion));
        Assert.assertEquals(fullForFr, this.dateFormatI18n.formatDate(date,
                "full", timeZone, frlanguage, frregion));
        Assert.assertEquals(longForFr, this.dateFormatI18n.formatDate(date,
                "long", timeZone, frlanguage, frregion));
        Assert.assertEquals(mediumForFr, this.dateFormatI18n.formatDate(date,
                "medium", timeZone, frlanguage, frregion));
        Assert.assertEquals(shortForFr, this.dateFormatI18n.formatDate(date,
                "short", timeZone, frlanguage, frregion));
    }

}
