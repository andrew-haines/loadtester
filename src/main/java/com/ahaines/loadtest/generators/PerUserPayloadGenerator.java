package com.ahaines.loadtest.generators;

import java.util.HashMap;
import java.util.Map;

import com.ahaines.loadtest.request.TestRun;

public abstract class PerUserPayloadGenerator implements PayloadGenerator{
	
	private Iterable<Long> users;

	@Override
	public Map<Long, TestRun> getPayload() throws PayloadGenerationException {
		Map<Long, TestRun> runners = new HashMap<Long, TestRun>();
		
		for (Long userId: users){
			runners.put(userId, getTestRun(userId));
		}
		
		return runners;
	}

	protected abstract TestRun getTestRun(Long userId);

	@Override
	public void initGenerator(Iterable<Long> users) {
		this.users = users;
	}

}
