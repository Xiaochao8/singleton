/*
 * Copyright 2019-2020 VMware, Inc.
 * SPDX-License-Identifier: EPL-2.0
 */
package com.vmware.vipclient.i18n;

import java.util.HashMap;
import java.util.Map;

public class VIPCfgFactory {
	private static final VIPCfg mainCfg = new VIPCfg();
	private static final Map<String, VIPCfg> configs = new HashMap<>();

	private VIPCfgFactory() {
	}

	public static VIPCfg getMainCfg() {
		return mainCfg;
	}

	public static VIPCfg getCfg(String productName) {
		synchronized (configs) {
			VIPCfg cfg = configs.get(productName);
			if (cfg != null) {
				return cfg;
			}

			VIPCfg newCfg = new VIPCfg(productName);
			configs.put(productName, newCfg);
			return newCfg;
		}
	}

	@Deprecated 
	public static void changeProductName(VIPCfg cfg, String oldName) {
		if (cfg.getProductName().equals(oldName)) {
			return;
		}

		synchronized (configs) {
			configs.remove(oldName);
			configs.put(cfg.getProductName(), cfg);
		}
	}
}
