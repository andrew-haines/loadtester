package com.ahaines.loadtest.testrungraph.nodes;

import java.util.concurrent.atomic.AtomicLong;

import com.ahaines.loadtest.testrungraph.TestNode;

public interface CheckoutAwareNode extends TestNode {

	public int getCheckoutVersion();
	
	public AtomicLong getNextSequenceId();
}
