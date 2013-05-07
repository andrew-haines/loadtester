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
import com.playfish.marlin.protostuff.transport.TransportBuyBlockAuditChange;

public class BuyItemAuditChangeNode<PREV_NODE extends CheckoutAwareNode & SessionAwareNode & AuditChangeAwareNode> extends SingleTransitionTestNode<PREV_NODE> implements SessionAwareNode, CheckoutAwareNode, AuditChangeAwareNode, PurchasedBlockInstanceIdAwareNode{

	public static enum RequestTypes implements RequestType {
		BUY_BLOCK
	}
	private final ProtostuffWrapperUtil utils;
	private long purchasedBlockId;
	
	public BuyItemAuditChangeNode(BackLookTestNode<? extends TestNode> nextNode, ProtostuffWrapperUtil utils) {
		super(nextNode);
		this.utils = utils;
	}
	
	public BuyItemAuditChangeNode(ProtostuffWrapperUtil utils){
		this(null, utils);
	}

	@Override
	public RequestInfo getRequest() {
		
		purchasedBlockId = getPreviousNode().getNextSequenceId().get();
		int checkoutVersion = getPreviousNode().getCheckoutVersion();
		String sessionKey = getPreviousNode().getSessionKey();
		String manifestVersion = getPreviousNode().getManifestVersion();
		
		TransportBuyBlockAuditChange buyItem = new TransportBuyBlockAuditChange();
		buyItem.setBlockDescriptionId(1);//filter coffee machine
		buyItem.setBlockInstanceId(purchasedBlockId);
		
		Map<TransportAuditChangeType, TransportBuyBlockAuditChange> changes = new HashMap<TransportAuditChangeType, TransportBuyBlockAuditChange>();
		changes.put(TransportAuditChangeType.BUY_BLOCK, buyItem);
		return utils.addToAuditBatchAndWrap(RequestTypes.BUY_BLOCK, checkoutVersion, getPreviousNode().getSaveVersion().get(), sessionKey, manifestVersion, changes);
		
	}

	@Override
	public void responseCallback(ResponseInfo response) {
		getPreviousNode().getNextSequenceId().incrementAndGet();
		getPreviousNode().getSaveVersion().incrementAndGet();
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

	@Override
	public long getPurchsedBlockInstanceId() {
		return purchasedBlockId;
	}

}
