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
import com.ahaines.loadtest.testrungraph.nodes.SellItemAuditChangeNode.RequestTypes;
import com.playfish.marlin.protostuff.transport.TransportAuditChangeType;
import com.playfish.marlin.protostuff.transport.TransportMoveBlockFromInventoryToTowerAuditChange;

public class MoveBlockFromInventoryToTowerAuditChangeNode <PREV_NODE extends CheckoutAwareNode & SessionAwareNode & AuditChangeAwareNode & PurchasedBlockInstanceIdAwareNode> extends SingleTransitionTestNode<PREV_NODE> implements SessionAwareNode, CheckoutAwareNode, AuditChangeAwareNode, PurchasedBlockInstanceIdAwareNode{

	public static enum RequestTypes implements RequestType {
		MOVE_BLOCK_TO_TOWER;
	}
	
	private final ProtostuffWrapperUtil utils;
	
	public MoveBlockFromInventoryToTowerAuditChangeNode(BackLookTestNode<? extends TestNode> nextNode, ProtostuffWrapperUtil utils) {
		super(nextNode);
		this.utils = utils;
	}
	
	public MoveBlockFromInventoryToTowerAuditChangeNode(ProtostuffWrapperUtil utils) {
		this(null, utils);
	}
	
	@Override
	public RequestInfo getRequest() {
		int checkoutVersion = getPreviousNode().getCheckoutVersion();
		String sessionKey = getPreviousNode().getSessionKey();
		String manifestVersion = getPreviousNode().getManifestVersion();
		
		TransportMoveBlockFromInventoryToTowerAuditChange sellItem = new TransportMoveBlockFromInventoryToTowerAuditChange();
		sellItem.setInstanceId(getPreviousNode().getPurchsedBlockInstanceId());
		sellItem.setXPosition(0);
		sellItem.setYPosition(0);
		
		Map<TransportAuditChangeType, TransportMoveBlockFromInventoryToTowerAuditChange> changes = new HashMap<TransportAuditChangeType, TransportMoveBlockFromInventoryToTowerAuditChange>();
		changes.put(TransportAuditChangeType.MOVE_BLOCK_FROM_INVENTORY_TO_TOWER, sellItem);
		return utils.addToAuditBatchAndWrap(RequestTypes.MOVE_BLOCK_TO_TOWER, checkoutVersion, getPreviousNode().getSaveVersion().get(), sessionKey, manifestVersion, changes);

	}

	@Override
	public AtomicInteger getSaveVersion() {
		return getPreviousNode().getSaveVersion();
	}

	@Override
	public int getCheckoutVersion() {
		return getPreviousNode().getCheckoutVersion();
	}

	@Override
	public AtomicLong getNextSequenceId() {
		return getPreviousNode().getNextSequenceId();
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
	public void responseCallback(ResponseInfo response) {
		getPreviousNode().getSaveVersion().incrementAndGet();
	}

	@Override
	public long getPurchsedBlockInstanceId() {
		return getPreviousNode().getPurchsedBlockInstanceId();
	}
}
