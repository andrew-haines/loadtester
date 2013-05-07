package com.ahaines.loadtest.testrungraph;

import com.ahaines.loadtest.response.ResponseInfo;
import com.ahaines.loadtest.response.StandardResponseTypes;

public abstract class SingleTransitionTestNode<PREV extends TestNode> implements BackLookTestNode<PREV>{
	
	private BackLookTestNode<TestNode> nextNode;
	private PREV prevNode;
	
	protected SingleTransitionTestNode(BackLookTestNode<? extends TestNode> nextNode){
		setNextNode(nextNode);
	}
	
	protected SingleTransitionTestNode(){
		this(null);
	}

	@Override
	public BackLookTestNode<?> getNextNode(ResponseInfo response) {
		if (response.getResponseType() == StandardResponseTypes.RESPONSE_OK){
			responseCallback(response);
			if (nextNode != null){
				nextNode.notifyCurrentNode(this);
			}
			return nextNode;
		} else {
			return this; // retry this node if it failed
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void setNextNode(BackLookTestNode<? extends TestNode> nextNode){
		this.nextNode = (BackLookTestNode<TestNode>)nextNode;;
	}
	
	public abstract void responseCallback(ResponseInfo response);

	@Override
	public void notifyCurrentNode(PREV prevNode) {
		this.prevNode = prevNode;
	}
	
	public PREV getPreviousNode(){
		return prevNode;
	}
}
