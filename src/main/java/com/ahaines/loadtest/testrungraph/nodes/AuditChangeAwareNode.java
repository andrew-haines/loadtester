package com.ahaines.loadtest.testrungraph.nodes;

import java.util.concurrent.atomic.AtomicInteger;

import com.ahaines.loadtest.testrungraph.TestNode;

public interface AuditChangeAwareNode extends TestNode{

	AtomicInteger getSaveVersion();
}
