package com.ahaines.loadtest.generators;

import com.playfish.marlin.servlet.TestUserUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.playfish.marlin.servlet.TestUserUtil.BootstrapSessionDetails;
import com.playfish.platform.rpc.session.RpcSessionCreationException;

public abstract class NewSessionPayloadGenerator implements PayloadGenerator{
	
	private final Map<Long, SessionDetails> sessionKeys;
	private final NewSessionGenerator generator;
	
	public NewSessionPayloadGenerator(NewSessionGenerator generator){
		sessionKeys = new HashMap<Long, SessionDetails>();
		this.generator = generator;
	}
	
	public NewSessionPayloadGenerator(){
		this(new BootstrapSessionGenerator());
	}

	@Override
	public void initGenerator(Iterable<Long> users) {
		
		
		for (Long userId: users){
			try {
				sessionKeys.put(userId, generator.getSessionDetails(userId));
			} catch (IOException e) {
				throw new RuntimeException("Unable to get session details for user: "+userId, e);
			} catch (RpcSessionCreationException e) {
				throw new RuntimeException("Unable to get session details for user: "+userId, e);
			}
		}
	}
	
	protected Iterable<Long> getAllUsers(){
		return sessionKeys.keySet();
	}

	protected String getSessionKey(Long uid){
		return sessionKeys.get(uid).getSessionKey();
	}
	
	protected String getManifestVersion(Long uid){
		return sessionKeys.get(uid).getManifestVersion();
	}
	
	protected static interface NewSessionGenerator {
		SessionDetails getSessionDetails(long userId) throws IOException, RpcSessionCreationException;
	}
	
	private static class BootstrapSessionGenerator implements NewSessionGenerator{

		@Override
		public SessionDetails getSessionDetails(long userId) throws IOException{
			final BootstrapSessionDetails details = TestUserUtil.getSessionDetails(userId);
			return new SessionDetails(){

				@Override
				public String getSessionKey() {
					return details.getSessionKey();
				}

				@Override
				public String getManifestVersion() {
					return details.getManifestVersion();
				}
			};
		}	
	}
	
	protected static interface SessionDetails{
		String getSessionKey();
		String getManifestVersion();
	}
}
