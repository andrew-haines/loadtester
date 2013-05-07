package com.ahaines.loadtest.testrungraph.nodes;

import com.ahaines.loadtest.testrungraph.TestNode;

public interface SessionAwareNode extends TestNode{

	public String getSessionKey();
	public String getManifestVersion();
	
}
