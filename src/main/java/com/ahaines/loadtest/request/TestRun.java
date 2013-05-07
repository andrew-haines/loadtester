package com.ahaines.loadtest.request;

import com.ahaines.loadtest.response.ResponseInfo;

public interface TestRun {
	
	boolean hasRequest(ResponseInfo response);

	RequestInfo getRequest(ResponseInfo response);
}
