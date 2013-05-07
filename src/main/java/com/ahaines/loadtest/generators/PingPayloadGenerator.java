package com.ahaines.loadtest.generators;

import com.playfish.server.datatype.DatatypeOutput;

public class PingPayloadGenerator extends RpcPayloadGenerator{

	public PingPayloadGenerator(String method, String path, String mimeType) {
		super(method, path, mimeType);
	}

	@Override
	protected void processPayload(DatatypeOutput output) {
		// dont actually need to do anything for ping...
	}

	@Override
	int getRpcHandlerCode() {
		return 0xFE;
	}

}
