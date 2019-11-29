/*
 * Copyright 2019 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n.messages.service;

import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;

import com.vmware.vipclient.i18n.VIPCfg;
import com.vmware.vipclient.i18n.base.DataSourceEnum;
import com.vmware.vipclient.i18n.messages.api.opt.server.ProductBasedOpt;
import com.vmware.vipclient.i18n.messages.dto.BaseDTO;
import com.vmware.vipclient.i18n.messages.dto.MessagesDTO;
import com.vmware.vipclient.i18n.util.LocaleUtility;

public class ProductService {
    private MessagesDTO dto = null;

    public ProductService(final MessagesDTO dto) {
        this.dto = dto;
    }

    // get supported components defined in vip service
    public JSONArray getComponentsFromRemoteVIP() {
        BaseDTO baseDTO = new BaseDTO();
        baseDTO.setProductID(this.dto.getProductID());
        baseDTO.setVersion(this.dto.getVersion());
        ProductBasedOpt dao = new ProductBasedOpt(baseDTO);
        return dao.getComponentsFromRemoteVIP();
    }

    // get supported locales defined in vip service
    public JSONArray getSupportedLocalesFromRemoteVIP() {
        BaseDTO baseDTO = new BaseDTO();
        baseDTO.setProductID(this.dto.getProductID());
        baseDTO.setVersion(this.dto.getVersion());
        ProductBasedOpt dao = new ProductBasedOpt(baseDTO);
        return dao.getSupportedLocalesFromRemoteVIP();
    }

    public Map<MessagesDTO, Map<String, String>> getAllComponentTranslation() {
        Map<MessagesDTO, Map<String, String>> list = new HashMap<>();
        Object[] locales = {};
        Object[] components = {};
        if (VIPCfg.getInstance().getMessageOrigin() == DataSourceEnum.VIP) {
            locales = this.getSupportedLocalesFromRemoteVIP().toArray();
            components = this.getComponentsFromRemoteVIP()
                    .toArray();
        }
        for (Object locale : locales) {
            for (Object component : components) {
                MessagesDTO tempdto = new MessagesDTO(this.dto);
                tempdto.setComponent(((String) component).trim());
                tempdto.setLocale(LocaleUtility.fmtToMappedLocale((String) locale).toString().trim());
                Map<String, String> retMap = new ComponentService(tempdto).getComponentTranslation();
                if (retMap != null) {
                    list.put(tempdto, retMap);
                }
            }
        }

        return list;
    }
}
