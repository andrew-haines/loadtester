package com.ahaines.loadtest.generators;

import java.util.HashMap;
import java.util.Map;

import com.ahaines.loadtest.request.RequestInfo;
import com.ahaines.loadtest.request.RequestType;
import com.ahaines.loadtest.request.TestRun;
import com.ahaines.loadtest.response.ResponseInfo;

public class StaticPayLoadGenerator implements PayloadGenerator {
	
	private enum RequestTypes implements RequestType{
		CONTENT_REQUEST;
	}
	
	private final String path;
	private final String method;
	private final String contentType;
	private Iterable<Long> users;
	
	public StaticPayLoadGenerator(String path, String method, String contentType){
		this.path = path;
		this.method = method;
		this.contentType = contentType;
	}

	public Map<Long, TestRun> getPayload() throws PayloadGenerationException {
		Map<Long, TestRun> map = new HashMap<Long, TestRun>();
		
		for (Long fbUser: users){
			map.put(fbUser, new TestRun(){

				public boolean hasRequest(ResponseInfo response) {
					return true;
				}

				public RequestInfo getRequest(ResponseInfo response) {
					return new RequestInfo(RequestTypes.CONTENT_REQUEST, path, new byte[]{}, method, contentType);
				}
				
			});
		}
		
		return map;
	}

	public void initGenerator(Iterable<Long> userIds) {
		this.users = userIds;
	}

}
