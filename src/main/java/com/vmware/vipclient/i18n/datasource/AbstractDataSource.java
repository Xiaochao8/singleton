/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import com.vmware.vipclient.i18n.VIPCfg;

abstract class AbstractDataSource implements DataSource {
    Status status = Status.NA;
    Type   type;

    protected Status getSourceStatus() {
        return this.status;
    }

    protected abstract Type getSourceType();

    protected abstract void refreshData(ProductData data);


    protected VIPCfg getConfig(final String product) {
        VIPCfg cfg = VIPCfg.getSubInstance(product);
        if (!cfg.getProductName().equals(product))
            return null;

        return cfg;
    }
}
