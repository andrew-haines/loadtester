package com.ahaines.loadtest.testrungraph;

import com.ahaines.loadtest.request.RequestInfo;
import com.ahaines.loadtest.request.StandardRequestTypes;
import com.ahaines.loadtest.response.ResponseInfo;

public class TestGraphWalker {

	private TestNode currentNode;
	
	public TestGraphWalker(TestNode startingNode){
		this.currentNode = startingNode;
	}
	
	public RequestInfo getRequestFromResponse(ResponseInfo response){
		RequestInfo requestInfo = null;
		if (response.getRequestType() != StandardRequestTypes.INITIAL_REQUEST){
			TestNode nextNode = currentNode.getNextNode(response);
			currentNode = nextNode;
		}
		
		if (currentNode != null){
			requestInfo = currentNode.getRequest();
		}
		
		return requestInfo;
	}
}
