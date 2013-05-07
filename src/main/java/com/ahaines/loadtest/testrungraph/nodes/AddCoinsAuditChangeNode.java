package com.ahaines.loadtest.testrungraph.nodes;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import com.ahaines.loadtest.generators.ProtostuffWrapperUtil;
import com.ahaines.loadtest.request.RequestInfo;
import com.ahaines.loadtest.request.RequestType;
import com.ahaines.loadtest.response.ResponseInfo;
import com.ahaines.loadtest.testrungraph.BackLookTestNode;
import com.ahaines.loadtest.testrungraph.SingleTransitionTestNode;
import com.ahaines.loadtest.testrungraph.TestNode;
import com.playfish.marlin.protostuff.transport.TransportAuditChangeType;
import com.playfish.marlin.protostuff.transport.TransportTestAddCoinsAuditChange;

public class AddCoinsAuditChangeNode<PREV_NODE extends CheckoutAwareNode & SessionAwareNode & AuditChangeAwareNode> extends SingleTransitionTestNode<PREV_NODE> implements SessionAwareNode, CheckoutAwareNode, AuditChangeAwareNode{

	public static enum RequestTypes implements RequestType{
		ADD_COINS
	}
	private final ProtostuffWrapperUtil util;
	
	public AddCoinsAuditChangeNode(BackLookTestNode<? extends TestNode> nextNode, ProtostuffWrapperUtil util) {
		super(nextNode);
		this.util = util;
	}
	
	public AddCoinsAuditChangeNode(ProtostuffWrapperUtil util){
		this(null, util);
	}

	@Override
	public RequestInfo getRequest() {
		
		TransportTestAddCoinsAuditChange addCoins = new TransportTestAddCoinsAuditChange();
		addCoins.setCoinAmount(150);
		
		Map<TransportAuditChangeType, TransportTestAddCoinsAuditChange> changes = new HashMap<TransportAuditChangeType, TransportTestAddCoinsAuditChange>();
		changes.put(TransportAuditChangeType.TEST_ADD_COINS, addCoins);
		
		return util.addToAuditBatchAndWrap(RequestTypes.ADD_COINS, getCheckoutVersion(), getSaveVersion().get(), getSessionKey(), getManifestVersion(), changes);
	}

	@Override
	public void responseCallback(ResponseInfo response) {
		getSaveVersion().incrementAndGet();
	}

	@Override
	public int getCheckoutVersion() {
		return getPreviousNode().getCheckoutVersion();
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
	public AtomicInteger getSaveVersion() {
		return getPreviousNode().getSaveVersion();
	}

	@Override
	public AtomicLong getNextSequenceId() {
		return getPreviousNode().getNextSequenceId();
	}

}
