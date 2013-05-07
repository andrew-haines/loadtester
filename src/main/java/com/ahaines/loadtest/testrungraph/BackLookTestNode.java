package com.ahaines.loadtest.testrungraph;

public interface BackLookTestNode<PREV_NODE extends TestNode> extends TestNode{

	public void notifyCurrentNode(PREV_NODE prevNode);
}
