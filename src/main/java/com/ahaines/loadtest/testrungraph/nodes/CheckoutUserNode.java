package com.ahaines.loadtest.testrungraph.nodes;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.ahaines.loadtest.generators.ProtostuffWrapperUtil;
import com.ahaines.loadtest.request.RequestInfo;
import com.ahaines.loadtest.request.RequestType;
import com.ahaines.loadtest.response.ResponseInfo;
import com.ahaines.loadtest.testrungraph.BackLookTestNode;
import com.ahaines.loadtest.testrungraph.SingleTransitionTestNode;
import com.ahaines.loadtest.testrungraph.TestNode;
import com.dyuproject.protostuff.Message;
import com.playfish.marlin.protostuff.transport.CheckoutUserResponse;

public class CheckoutUserNode<PREV_NODE extends SessionAwareNode> extends SingleTransitionTestNode<PREV_NODE> implements SessionAwareNode, CheckoutAwareNode, AuditChangeAwareNode{

	private static final String CHECKOUT_USER_REQUEST_PATH = "api/game/user/checkout";
	
	public static enum RequestTypes implements RequestType{
		CHECKOUT_USER;
	}
	
	private final ProtostuffWrapperUtil protoUtils;
	
	private CheckoutUserResponse response;
	private final AtomicInteger saveVersion;
	
	public CheckoutUserNode(BackLookTestNode<? extends TestNode> nextNode, ProtostuffWrapperUtil utils) {
		super(nextNode);
		this.protoUtils = utils;
		saveVersion = new AtomicInteger(1);
	}
	
	public CheckoutUserNode(ProtostuffWrapperUtil util){
		this(null, util);
	}

	@Override
	public RequestInfo getRequest() {
		// set save version to 0
		
		String sessionKey = getPreviousNode().getSessionKey();
		String manifestValue = getPreviousNode().getManifestVersion();
		byte[] payload = protoUtils.<Message>wrapRequestAndExternalize(null, sessionKey, manifestValue);
		return new RequestInfo(RequestTypes.CHECKOUT_USER, CHECKOUT_USER_REQUEST_PATH, payload, "POST", protoUtils.getMimeType());
	}

	@Override
	public void responseCallback(ResponseInfo response) {
		this.response = protoUtils.unWrapAndInternalize(response.getResponseContent(), new CheckoutUserResponse());
		saveVersion.set(this.response.getTransportUser().getLastSaveVersion()+1);
	}
	
	public CheckoutUserResponse getCheckoutUserResponse(){
		return response;
	}

	@Override
	public String getSessionKey() {
		return getPreviousNode().getSessionKey();
	}

	@Override
	public String getManifestVersion() {
		return getPreviousNode().getManifestVersion();
	}

	@Override
	public int getCheckoutVersion() {
		return response.getTransportUser().getCheckoutVersion();
	}

	@Override
	public AtomicLong getNextSequenceId() {
		return new AtomicLong(response.getTransportUser().getNextUniqueInstanceId());
	}

	@Override
	public AtomicInteger getSaveVersion() {
		return saveVersion;
	}

}
