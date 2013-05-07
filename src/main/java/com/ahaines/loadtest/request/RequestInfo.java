package com.ahaines.loadtest.request;

public class RequestInfo {

	private final String path;
	private final byte[] payload;
	private final String method;
	private final String contentType;
	private final RequestType requestType;
	
	public RequestInfo(RequestType requestType, String path, byte[] payload, String method, String contentType){
		this.path = path;
		this.payload = payload;
		this.method = method;
		this.contentType = contentType;
		this.requestType = requestType;
	}

	public String getPath() {
		return path;
	}

	public byte[] getPayload() {
		return payload;
	}

	public String getMethod() {
		return method;
	}

	public String getMimeType() {
		return contentType;
	}

	public RequestType getRequestType() {
		return requestType;
	}
}
