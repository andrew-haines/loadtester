package com.ahaines.loadtest.generators;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.ahaines.loadtest.request.TestRun;
import com.playfish.platform.facebook.FacebookUserAccessToken;
import com.playfish.platform.network.facebook.FacebookUid;
import com.playfish.platform.network.playfish.PlayfishUid;
import com.playfish.platform.rpc.session.RpcSession;
import com.playfish.platform.rpc.session.RpcSessionCreationException;
import com.playfish.platform.rpc.session.RpcSessionFactory;
import com.playfish.platform.rpc.session.StandardRpcSessionSerialiser;
import com.playfish.server.Game;
import com.playfish.server.network.facebook.FacebookTokenNetworkUserSession;

public abstract class UserIteratorPayloadGenerator implements PayloadGenerator {
	
	private final Map<Long, String> userSessions = new HashMap<Long, String>();
	private final StandardRpcSessionSerialiser sessionSerializer;
	
	protected UserIteratorPayloadGenerator(StandardRpcSessionSerialiser sessionSerializer){
		this.sessionSerializer = sessionSerializer;
	}

	@Override
	public Map<Long, TestRun> getPayload() throws PayloadGenerationException {
		Map<Long, TestRun> payloads = new HashMap<Long, TestRun>(userSessions.size());
		
		for (Entry<Long, String> userId: userSessions.entrySet()){
			//payloads.put(userId, getPayload(userId., userId));
			payloads.put(userId.getKey(), getPayload(userId.getValue(), userId.getKey()));
		}
		
		return payloads;
	}
	
	public void initGenerator(Iterable<Long> users){
		for (Long userId: users){
			try {
				userSessions.put(userId, getRpcSession(userId));
			} catch (RpcSessionCreationException e) {
				throw new RuntimeException("unable to create dummy session for user: "+userId, e);
			}
		}
	}
	
	private String getRpcSession(Long userId) throws RpcSessionCreationException{
		FacebookTokenNetworkUserSession fbLtSession = new FacebookTokenNetworkUserSession(new FacebookUid(userId), new FacebookUserAccessToken("testToken"));
		
		int truncatedUserId32Bits = (int)(userId & Integer.MAX_VALUE);
		
		RpcSession<FacebookTokenNetworkUserSession> rpcSession = RpcSessionFactory.getInstance().createSession(Game.GAME_ID_MARLIN, "GB", fbLtSession, new PlayfishUid(truncatedUserId32Bits), new byte[0]);
		return sessionSerializer.toExternalString(rpcSession);
	}

	protected abstract TestRun getPayload(String sessionKey, Long userId) throws PayloadGenerationException; 
}
