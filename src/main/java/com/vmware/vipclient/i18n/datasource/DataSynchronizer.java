/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.util.StringUtil;

class DataSynchronizer {
    private static final Logger logger = LoggerFactory.getLogger(DataSynchronizer.class);

    public static void startSynchronizer(final VIPCfg cfg) {
        if (StringUtil.isEmpty(cfg.getVipServer()) || !(cfg.isCleanCache() && cfg.getCacheExpiredTime() > 0))
            return;

        logger.info("Start sache synchronizer.");

        Timer timer = new Timer(true);
        TimerTask task = new TimerTask() {
            @Override
            public void run() {
                DataSourceManager dmgr = DataSourceManager.getDataManager(cfg);
                dmgr.syncCache();
            }
        };
        timer.scheduleAtFixedRate(task, cfg.getCacheExpiredTime(), cfg.getCacheExpiredTime());
    }
}
