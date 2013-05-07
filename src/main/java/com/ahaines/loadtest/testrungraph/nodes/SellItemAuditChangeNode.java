package com.ahaines.loadtest.testrungraph.nodes;

import java.util.HashMap;
import java.util.Map;

import com.ahaines.loadtest.generators.ProtostuffWrapperUtil;
import com.ahaines.loadtest.request.RequestInfo;
import com.ahaines.loadtest.request.RequestType;
import com.ahaines.loadtest.response.ResponseInfo;
import com.ahaines.loadtest.testrungraph.BackLookTestNode;
import com.ahaines.loadtest.testrungraph.SingleTransitionTestNode;
import com.ahaines.loadtest.testrungraph.TestNode;
import com.playfish.marlin.protostuff.transport.TransportAuditChangeType;
import com.playfish.marlin.protostuff.transport.TransportSellBlockAuditChange;

public class SellItemAuditChangeNode <PREV_NODE extends CheckoutAwareNode & SessionAwareNode & AuditChangeAwareNode> extends SingleTransitionTestNode<PREV_NODE>{

	public static enum RequestTypes implements RequestType {
		SELL_BLOCK;
	}
	private final ProtostuffWrapperUtil utils;
	
	public SellItemAuditChangeNode(BackLookTestNode<? extends TestNode> nextNode, ProtostuffWrapperUtil utils) {
		super(nextNode);
		this.utils = utils;
	}
	
	public SellItemAuditChangeNode(ProtostuffWrapperUtil utils){
		this(null, utils);
	}
	
	@Override
	public RequestInfo getRequest() {
		int checkoutVersion = getPreviousNode().getCheckoutVersion();
		String sessionKey = getPreviousNode().getSessionKey();
		String manifestVersion = getPreviousNode().getManifestVersion();
		
		TransportSellBlockAuditChange sellItem = new TransportSellBlockAuditChange();
		sellItem.setUiSourceId(1);
		sellItem.setUiReferenceId(2);
		sellItem.setBlockInstanceId(getPreviousNode().getNextSequenceId().get());
		
		Map<TransportAuditChangeType, TransportSellBlockAuditChange> changes = new HashMap<TransportAuditChangeType, TransportSellBlockAuditChange>();
		changes.put(TransportAuditChangeType.SELL_BLOCK, sellItem);
		return utils.addToAuditBatchAndWrap(RequestTypes.SELL_BLOCK, checkoutVersion, getPreviousNode().getSaveVersion().get(), sessionKey, manifestVersion, changes);

	}
	
	@Override
	public void responseCallback(ResponseInfo response) {
		getPreviousNode().getSaveVersion().incrementAndGet();
	}
}
