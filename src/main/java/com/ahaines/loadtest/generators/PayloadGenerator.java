package com.ahaines.loadtest.generators;

import java.util.Map;

import com.ahaines.loadtest.request.TestRun;

public interface PayloadGenerator {
	
	public Map<Long, TestRun> getPayload() throws PayloadGenerationException;

	void initGenerator(Iterable<Long> users);

}
