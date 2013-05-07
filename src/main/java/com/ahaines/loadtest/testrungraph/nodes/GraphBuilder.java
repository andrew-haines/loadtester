package com.ahaines.loadtest.testrungraph.nodes;

import java.util.Deque;
import java.util.LinkedList;

import com.ahaines.loadtest.testrungraph.SingleTransitionTestNode;
import com.ahaines.loadtest.testrungraph.TestNode;

public class GraphBuilder {

	private final Deque<SingleTransitionTestNode<?>> nodes = new LinkedList<SingleTransitionTestNode<?>>();
	
	public GraphBuilder addNode(SingleTransitionTestNode<?> node){
		if (!nodes.isEmpty()){
			nodes.peekLast().setNextNode(node);
		}
		nodes.add(node);
		return this;
	}
	
	public GraphBuilder repeat(){
		nodes.peekLast().setNextNode(nodes.peekFirst());
		return this;
	}
	
	public TestNode build(){
		return nodes.peekFirst();
	}
}
