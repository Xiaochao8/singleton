/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class VIPCfgFactory {
	private static VIPCfgFactory inst = createInstance();
	private static final String MAIN_PRODUCT_NAME = "mainProductName";

	private final VIPCfg mainCfg = new VIPCfg();
	private final Map<String, VIPCfg> configs = new ConcurrentHashMap<>();

	private static VIPCfgFactory createInstance() {
		VIPCfgFactory newInst = new VIPCfgFactory();
		newInst.mainCfg.setProductName(MAIN_PRODUCT_NAME);
		newInst.configs.put(newInst.mainCfg.getProductName(), newInst.mainCfg);
		return newInst;
	}

	public static VIPCfg getMainCfg() {
		return inst.mainCfg;
	}

	public static VIPCfg getCfg(String productName) {
		synchronized (inst.configs) {
			VIPCfg cfg = inst.configs.get(productName);
			if (cfg != null) {
				return cfg;
			}

			VIPCfg newCfg = new VIPCfg();
			newCfg.setProductName(productName);
			inst.configs.put(productName, newCfg);
			return newCfg;
		}
	}

	public static void changeProductName(VIPCfg cfg, String oldName) {
		if (cfg.getProductName().equals(oldName)) {
			return;
		}

		synchronized (inst.configs) {
			inst.configs.remove(oldName);
			inst.configs.put(cfg.getProductName(), cfg);
		}
	}
}
