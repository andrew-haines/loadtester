package com.ahaines.loadtest.response;

import com.ahaines.loadtest.request.RequestType;

public class ResponseInfo {

	private final ResponseType responseType;
	private final RequestType requestType;
	private final int responseRunNum;
	private final byte[] responseContent;
	
	public ResponseInfo(RequestType requestType, ResponseType responseType, byte[] responseContent, int responseRunNum){
		this.responseType = responseType;
		this.responseContent = responseContent;
		this.requestType = requestType;
		this.responseRunNum = responseRunNum;
	}
	
	public ResponseType getResponseType() {
		return responseType;
	}

	public byte[] getResponseContent() {
		return responseContent;
	}
	
	public RequestType getRequestType() {
		return requestType;
	}

	public int getResponseRunNum() {
		return responseRunNum;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((requestType == null) ? 0 : requestType.hashCode());
		result = prime * result + responseRunNum;
		result = prime * result
				+ ((responseType == null) ? 0 : responseType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ResponseInfo other = (ResponseInfo) obj;
		if (requestType == null) {
			if (other.requestType != null)
				return false;
		} else if (!requestType.equals(other.requestType))
			return false;
		if (responseRunNum != other.responseRunNum)
			return false;
		if (responseType == null) {
			if (other.responseType != null)
				return false;
		} else if (!responseType.equals(other.responseType))
			return false;
		return true;
	}

	public static class StandardResponseInfo extends ResponseInfo{

		public StandardResponseInfo(RequestType requestType, ResponseType responseType, int responseRunNum) {
			super(requestType, responseType, new byte[]{}, responseRunNum);
		}
	}

	public static interface ResponseType {
		
	}
}
