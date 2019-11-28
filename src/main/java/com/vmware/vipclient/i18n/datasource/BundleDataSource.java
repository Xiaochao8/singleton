/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vipclient.i18n.VIPCfg;
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
    public ProductData getProductTranslation(final String product, final String version) {
        if (StringUtil.isEmpty(this.cfg.getBundleFolder()))
            return null;

        Path productRoot = Paths.get(this.cfg.getBundleFolder(), product, version);
        if (!productRoot.toFile().exists())
            return null;

        ProductData pData = new ProductData(product, version);
        try {
            Files.walkFileTree(productRoot, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                    if (!FileUtil.getFileExtension(file.toFile()).equalsIgnoreCase(".properties"))
                        return FileVisitResult.CONTINUE;

                    try {
                        Properties messages = FileUtil.readPropertiesFile(file.toFile().getAbsolutePath());
                        String locale = FileUtil.getFileBasename(file.toFile());
                        String component = FileUtil.getFileExtension(file.toFile());

                        LocaleData localeData = pData.get(locale);
                        if(null == localeData ) {
                            localeData  = new LocaleData(locale);
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

    @Override
    public List<String> getComponentList(final String product, final String version) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<String> getLocaleList(final String product, final String version) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProductData getComponentTranslation(final String product, final String version, final String locale,
            final String component) {
        if (this.status != Status.READY)
            return null;

        ProductData pData = new ProductData(product, version);
        return pData;

    }

    @Override
    public Map<String, Object> getComponentsTranslation(final String product, final String version,
            final List<String> locale,
            final List<String> component) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getStringTranslation(final String product, final String version, final String locale,
            final String component,
            final String key) {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public void refreshData(final ProductData pData) {
        if (this.status == Status.NA)
            return;

        String product = pData.productName;
        String version = pData.versionName;


        if (StringUtil.isEmpty(this.cfg.getBundleFolder()))
            return;

        Path productRoot = Paths.get(this.cfg.getBundleFolder(), product, version);

        try {
            Files.walkFileTree(productRoot, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs) {
                    if (!FileUtil.getFileExtension(file.toFile()).equalsIgnoreCase(".properties"))
                        return FileVisitResult.CONTINUE;

                    try {
                        Properties messages = FileUtil.readPropertiesFile(file.toFile().getAbsolutePath());
                        String component = file.getParent().getFileName().toString();
                        String locale = FileUtil.getLocale(file.toFile());

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
    }

}
