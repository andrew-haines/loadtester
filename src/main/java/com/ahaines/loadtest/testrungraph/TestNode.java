package com.ahaines.loadtest.testrungraph;

import com.ahaines.loadtest.request.RequestInfo;
import com.ahaines.loadtest.response.ResponseInfo;

public interface TestNode {

	public TestNode getNextNode(ResponseInfo response);

	public RequestInfo getRequest();
}
