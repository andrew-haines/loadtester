package com.ahaines.loadtest.testrungraph;

import java.util.HashMap;
import java.util.Map;

import com.ahaines.loadtest.generators.PerUserPayloadGenerator;
import com.ahaines.loadtest.request.RequestInfo;
import com.ahaines.loadtest.request.TestRun;
import com.ahaines.loadtest.response.ResponseInfo;

public class PayloadGraphAdapter extends PerUserPayloadGenerator{

	private final TestGraphWalkerBuilder graphWalkerBuilder;
	
	public PayloadGraphAdapter (TestGraphWalkerBuilder graphWalkerBuilder){
		this.graphWalkerBuilder = graphWalkerBuilder;
	}
	@Override
	protected TestRun getTestRun(final Long userId) {
		
		final TestGraphWalker graphWalker = graphWalkerBuilder.buildForUser(userId);
		
		return new TestRun(){

			private final Map<ResponseInfo, RequestInfo> requestCache = new HashMap<ResponseInfo, RequestInfo>();
			
			@Override
			public boolean hasRequest(ResponseInfo response) {
				RequestInfo requestInfo = graphWalker.getRequestFromResponse(response);
				if (requestInfo != null){
					requestCache.put(response, requestInfo);
					return true;
				}
				return false;
			}

			@Override
			public RequestInfo getRequest(ResponseInfo response) {
				return requestCache.get(response);
			}
			
		};
	}

}
