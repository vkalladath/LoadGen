package com.sedai.apps.loadgen;

public class BackendConfig {

	private String url;
	private int requestSize = 100; // in bytes
	private DataTransform transform = DataTransform.SHA1_HASH;
	private int dataTransformIterations = 1;
	private int minLatencyMillis = 0;
	private int maxLatencyMillis = 0;
	private int responseSize = 1024; // in bytes

	public int getResponseSize() {
		return responseSize;
	}

	public void setResponseSize(int responseSize) {
		this.responseSize = responseSize;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getRequestSize() {
		return requestSize;
	}

	public void setRequestSize(int requestSize) {
		this.requestSize = requestSize;
	}

	public DataTransform getTransform() {
		return transform;
	}

	public void setTransform(DataTransform transform) {
		this.transform = transform;
	}

	public int getDataTransformIterations() {
		return dataTransformIterations;
	}

	public void setDataTransformIterations(int dataTransformIterations) {
		this.dataTransformIterations = dataTransformIterations;
	}

	public int getMinLatencyMillis() {
		return minLatencyMillis;
	}

	public void setMinLatencyMillis(int minLatencyMillis) {
		this.minLatencyMillis = minLatencyMillis;
	}

	public int getMaxLatencyMillis() {
		return maxLatencyMillis;
	}

	public void setMaxLatencyMillis(int maxLatencyMillis) {
		this.maxLatencyMillis = maxLatencyMillis;
	}

}
