/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.exceptions.VIPJavaClientException;
import com.vmware.vipclient.i18n.util.FileUtil;
import com.vmware.vipclient.i18n.util.StringUtil;

public class BundleDataSource extends AbstractDataSource {
    private static final Logger     logger = LoggerFactory.getLogger(BundleDataSource.class);

    private static HashMap<String, BundleDataSource> bundleDataSources = new HashMap<>();
    private final VIPCfg                             cfg;

    private BundleDataSource(final VIPCfg cfg) {
        this.cfg = cfg;
    }

    public static synchronized BundleDataSource getBundleDataSource(final VIPCfg cfg) {
        BundleDataSource inst = bundleDataSources.get(cfg.getProductName());
        if (inst == null) {
            inst = new BundleDataSource(cfg);
            String bundleFolder = cfg.getBundleFolder();
            if (!StringUtil.isEmpty(bundleFolder)) {
                inst.status = Status.READY;
            }
        }
        return inst;
    }


    @Override
    public Type getSourceType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Status getSourceStatus() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized ProductData getProductTranslation() {
        if (this.status != Status.READY)
            return null;

        Path productRoot = Paths.get(this.cfg.getBundleFolder());
        if (!productRoot.toFile().exists())
            throw new VIPJavaClientException("bundle folder doesn't exist!");

        ProductData pData = new ProductData(this.cfg.getProductName(), this.cfg.getVersion());
        try {
            Files.walkFileTree(productRoot, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                    if (!FileUtil.getFileExtension(file.toFile()).equalsIgnoreCase(".properties"))
                        return FileVisitResult.CONTINUE;

                    try {
                        Properties props = FileUtil.readPropertiesFile(file.toFile().getAbsolutePath());
                        Map<String, String> messages = FileUtil.convertPropertiesToMap(props);
                        String locale = FileUtil.getLocale(file.toFile());
                        String component = file.getParent().getFileName().toString();

                        LocaleData localeData = pData.get(locale);
                        if (null == localeData) {
                            localeData = new LocaleData(locale);
                            pData.put(locale, localeData);
                        }
                        ComponentData componentData = new ComponentData(component);
                        componentData.setData(messages);
                        localeData.put(locale, componentData);

                    } catch (IOException e) {
                        logger.error("", e);
                    }

                    return FileVisitResult.CONTINUE;
                }
            });
        } catch (IOException e) {
            logger.error("", e);
        }

        return pData;
    }

    synchronized void setProductTranslation(final ProductData pData) {
        if (this.status == Status.NA)
            return;

        if (StringUtil.isEmpty(this.cfg.getBundleFolder()))
            return;

        Path productRoot = Paths.get(this.cfg.getBundleFolder());
        try {
            Files.delete(productRoot);

            for (Entry<String, LocaleData> entry : pData.entrySet()) {
                String locale = entry.getKey();
                LocaleData lData = entry.getValue();

                for (Entry<String, ComponentData> compEntry : lData.entrySet()) {
                    String compName = compEntry.getKey();
                    ComponentData compData = compEntry.getValue();

                    Path filepath = Paths.get(productRoot.toString(), compName, "messages_" + locale + ".properties");
                    FileUtil.savePropertiesFile(filepath.toString(),
                            FileUtil.convertMapToProperties(compData.getData()));

                }
            }
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    @Override
    public Set<String> getComponentList() {
        if (this.status != Status.READY)
            return null;

        Set<String> lSet = new HashSet<>();
        File productRoot = new File(this.cfg.getBundleFolder());
        lSet.addAll(Arrays.asList(productRoot.list((dir, name) -> new File(dir, name).isDirectory())));
        return lSet;
    }

    @Override
    public Set<String> getLocaleList() {
        if (this.status != Status.READY)
            return null;

        return this.getProductTranslation().getLocales();
    }

    @Override
    public ComponentData getComponentTranslation(final String locale, final String component) {
        if (this.status != Status.READY)
            return null;

        return this.getProductTranslation().get(locale).get(component);
    }

    @Override
    public Map<String, Object> getComponentsTranslation(final List<String> locale, final List<String> component) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public synchronized void refreshData(final ProductData pData) {
        if (this.status == Status.NA)
            return;

        if (StringUtil.isEmpty(this.cfg.getBundleFolder()))
            return;

        Path productRoot = Paths.get(this.cfg.getBundleFolder(), this.cfg.getProductName(), this.cfg.getVersion());

        for (Entry<String, LocaleData> entry : pData.entrySet()) {
            String locale = entry.getKey();
            LocaleData lData = entry.getValue();

            for (Entry<String, ComponentData> compEntry : lData.entrySet()) {
                String compName = compEntry.getKey();
                ComponentData compData = compEntry.getValue();

                Path filepath = Paths.get(productRoot.toString(), compName, "messages_" + locale + ".properties");
                try {
                    FileUtil.savePropertiesFile(filepath.toString(),
                            FileUtil.convertMapToProperties(compData.getData()));
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

}
