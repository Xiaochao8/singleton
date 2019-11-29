/**
 *
 */
package com.vmware.vipclient.i18n.datasource;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.json.simple.JSONObject;

import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.messages.api.opt.local.LocalMessagesOpt;
import com.vmware.vipclient.i18n.messages.api.opt.server.ComponentBasedOpt;
import com.vmware.vipclient.i18n.messages.api.opt.server.ProductBasedOpt;
import com.vmware.vipclient.i18n.messages.dto.BaseDTO;
import com.vmware.vipclient.i18n.messages.dto.MessagesDTO;
import com.vmware.vipclient.i18n.util.StringUtil;

public class ServerDataSource extends AbstractDataSource {

    private static HashMap<String, ServerDataSource> serverDataSources = new HashMap<>();
    private final VIPCfg                             cfg;

    private ServerDataSource(final VIPCfg cfg) {
        this.cfg = cfg;
    }

    public static synchronized ServerDataSource getServerDataSource(final VIPCfg cfg) {
        ServerDataSource inst = serverDataSources.get(cfg.getProductName());
        if (inst == null) {
            inst = new ServerDataSource(cfg);
            if (!StringUtil.isEmpty(cfg.getVipServer())) {
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
    public ProductData getProductTranslation() {
        if (this.status != Status.READY)
            return null;

        ProductData pData = new ProductData(this.cfg.getProductName(), this.cfg.getVersion());

        for (String locale : this.getLocaleList()) {
            for (String component : this.getComponentList()) {
                MessagesDTO dto = this.createDto();
                dto.setLocale(locale);
                dto.setComponent(component);
                Map<MessagesDTO, Map<String, String>> result = new LocalMessagesOpt(dto).getComponentMessages();

                for (Entry<MessagesDTO, Map<String, String>> entry : result.entrySet()) {
                    ComponentData cData = new ComponentData(component);
                    cData.setData(entry.getValue());
                    LocaleData lData = pData.computeIfAbsent(locale, LocaleData::new);
                    lData.put(component, cData);
                }
            }
        }

        return pData;
    }

    @Override
    public Set<String> getComponentList() {
        if (this.status != Status.READY)
            return null;

        Set<String> comps = new HashSet<>();
        BaseDTO dto = this.createDto();
        comps.addAll(new ProductBasedOpt(dto).getComponentsFromRemoteVIP());
        return comps;
    }

    @Override
    public Set<String> getLocaleList() {
        if (this.status != Status.READY)
            return null;

        Set<String> locales = new HashSet<>();
        BaseDTO dto = this.createDto();
        locales.addAll(new ProductBasedOpt(dto).getSupportedLocalesFromRemoteVIP());
        return locales;

    }

    @Override
    public ComponentData getComponentTranslation(final String locale, final String component) {
        if (this.status != Status.READY)
            return null;

        MessagesDTO dto = this.createDto();
        dto.setLocale(locale);
        dto.setComponent(component);
        JSONObject messages = new ComponentBasedOpt(dto).getComponentMessages();
        ComponentData cData = new ComponentData(component);
        cData.setData(messages);
        return cData;
    }

    @Override
    public Map<String, Object> getComponentsTranslation(final List<String> locale, final List<String> component) {
        if (this.status != Status.READY)
            return null;

        return null;
    }



    @Override
    public void refreshData(final ProductData data) {
        // Server doesn't need to refresh
    }


    private MessagesDTO createDto() {
        MessagesDTO dto = new MessagesDTO();
        dto.setProductID(this.cfg.getProductName());
        dto.setVersion(this.cfg.getVersion());
        return dto;
    }

}
