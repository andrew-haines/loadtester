package com.ahaines.loadtest.response;

import com.ahaines.loadtest.response.ResponseInfo.ResponseType;

public enum StandardResponseTypes implements ResponseType{
	
	NO_PREVIOUS_RESPONSE,
	RESPONSE_OK,
	RESPONSE_TIMED_OUT,
	RESPONSE_IN_ERROR, 
	UNKNOWN_ERROR;
}
