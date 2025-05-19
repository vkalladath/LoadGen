package com.sedai.apps.loadgen;

import java.time.Instant;
import java.util.List;

public class LoadConfig {
	//used for caching
	protected Instant lastUpdatedTime;

	private int configReloadIntervalMinutes = 5;
	
	private int responseSize = 1024; // bytes
	private List<BackendConfig> backendConfigs;

	public int getResponseSize() {
		return responseSize;
	}

	public void setResponseSize(int responseSize) {
		this.responseSize = responseSize;
	}

	public List<BackendConfig> getBackendConfigs() {
		return backendConfigs;
	}

	public void setBackendConfigs(List<BackendConfig> backendConfigs) {
		this.backendConfigs = backendConfigs;
	}

	public int getConfigReloadIntervalMinutes() {
		return configReloadIntervalMinutes;
	}

	public void setConfigReloadIntervalMinutes(int configReloadIntervalMinutes) {
		this.configReloadIntervalMinutes = configReloadIntervalMinutes;
	}

}
