package com.ahaines.loadtest.generators;

import com.ahaines.loadtest.request.RequestType;
import com.ahaines.loadtest.response.ResponseInfo;
import com.dyuproject.protostuff.Message;

public class CheckOutPayloadGenerator extends ProtoWrappedRequestGenerator{
	
	private static final String CHECKOUT_USER_REQUEST_PATH = "game/user/checkout";
	
	public static enum RequestTypes implements RequestType{
		CHECKOUT_USER;
	}

	@Override
	protected ProtoStuffRequest getProtostuffRequest(ResponseInfo responseInfo, Long userId) {
		return new ProtoStuffRequest(null, CHECKOUT_USER_REQUEST_PATH, "POST", RequestTypes.CHECKOUT_USER);
	}

}
