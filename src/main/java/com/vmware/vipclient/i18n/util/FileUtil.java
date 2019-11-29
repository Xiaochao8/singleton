/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
    static Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static Properties readPropertiesFile(final String file) throws IOException {
        final Properties props = new Properties();
        try (InputStream is = ClassLoader.getSystemResourceAsStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));) {
            props.load(reader);
        }
        return props;
    }

    public static void savePropertiesFile(final String filepath, final Properties props) throws IOException {
        try (OutputStream output = new FileOutputStream(filepath)) {
            props.store(output, null);
        }
    }

    public static Map<String, String> convertPropertiesToMap(final Properties props) {
        Map<String, String> map = new HashMap<>();
        for (Entry<Object, Object> entry : props.entrySet()) {
            map.put(entry.getKey().toString(), entry.getValue().toString());
        }

        return map;
    }

    public static Properties convertMapToProperties(final Map<String, String> map) {
        Properties props = new Properties();
        props.putAll(map);
        return props;
    }

    public static JSONObject readJSONFile(final String file) throws ParseException, IOException {
        try (InputStream is = ClassLoader.getSystemResourceAsStream(file);
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));) {
            return (JSONObject) new JSONParser().parse(reader);
        }
    }

    public static JSONObject readJarJsonFile(final String jarPath, final String filePath) {
        JSONObject jsonObj = null;
        URL url = null;
        String path;
        if (jarPath.startsWith("file:")
                && jarPath.lastIndexOf(".jar!") > 0) {
            path = "jar:" + jarPath + filePath;
        } else {
            path = "jar:file:" + jarPath + "!/" + filePath;
        }

        try {
            url = new URL(path);

            try (InputStream fis = url.openStream();
                    Reader reader = new InputStreamReader(fis, "UTF-8");) {

                final Object o = new JSONParser().parse(reader);
                if (o != null) {
                    jsonObj = (JSONObject) o;
                }
            } catch (final Exception e) {
                logger.error("", e);
            }
        } catch (final MalformedURLException e1) {
            logger.error("", e1);
        }

        return jsonObj;
    }

    public static JSONObject readLocalJsonFile(final String filePath) {
        final String basePath = FileUtil.class.getClassLoader()
                .getResource("").getFile();
        JSONObject jsonObj = null;
        final File file = new File(basePath + filePath);
        if (file.exists()) {
            try (InputStream fis = new FileInputStream(file);
                    Reader reader = new InputStreamReader(fis, "UTF-8");) {
                final Object o = new JSONParser().parse(reader);
                if (o != null) {
                    jsonObj = (JSONObject) o;
                }
            } catch (final Exception e) {
                logger.error("", e);
            }
        }

        return jsonObj;
    }

    public static String getFileExtension(final File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf('.');
        if (lastIndexOf == -1)
            return ""; // empty extension
        return name.substring(lastIndexOf);
    }

    public static String getFileBasename(final File file) {
        return file.getName().replaceFirst("[.][^.]+$", "");
    }

    public static String getLocale(final File file) {
        String baseName = getFileBasename(file);
        return baseName.substring(baseName.lastIndexOf('_') + 1);
    }

    public static void moveFile(final String from, final String to) throws IOException, InterruptedException {
        File fromFile = new File(from);
        int i =0;
        while (!fromFile.renameTo(new File(to)) && i < 5) {
            i++;
            TimeUnit.MILLISECONDS.sleep(100);
        }

        if (i >= 5)
            throw new IOException("Move failed");
    }
}
