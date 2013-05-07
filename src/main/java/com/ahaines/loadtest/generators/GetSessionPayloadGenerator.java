package com.ahaines.loadtest.generators;

import java.io.IOException;

import com.ahaines.loadtest.request.RequestType;
import com.ahaines.loadtest.response.ResponseInfo;
import com.playfish.platform.facebook.FacebookUserAccessToken;
import com.playfish.platform.network.facebook.FacebookUid;
import com.playfish.platform.network.playfish.PlayfishUid;
import com.playfish.platform.rpc.session.RpcSession;
import com.playfish.platform.rpc.session.RpcSessionCreationException;
import com.playfish.platform.rpc.session.RpcSessionFactory;
import com.playfish.platform.rpc.session.StandardRpcSessionSerialiser;
import com.playfish.server.Game;
import com.playfish.server.network.facebook.FacebookTokenNetworkUserSession;

public class GetSessionPayloadGenerator extends ProtoWrappedRequestGenerator{
	
	public static enum RequestTypes implements RequestType {
		GET_SESSION;
	}
	
	private final String path;
	private final String method;
	protected GetSessionPayloadGenerator(String path, String method, StandardRpcSessionSerialiser sessionSerializer){
		super(new StaticSessionGenerator(sessionSerializer));
		System.setProperty("rpcsession.factory.campaign.client.enabled", "false");
		System.setProperty("platform.identityService.enabled", "true");
		this.path = path;
		this.method = method;
	}
	
	public GetSessionPayloadGenerator(String path, String method){
		this(path, method, new StandardRpcSessionSerialiser());
	}

	@Override
	public String toString() {
		return method+" request to "+path;
	}

	@Override
	protected ProtoStuffRequest<?> getProtostuffRequest(ResponseInfo responseInfo, Long userId) {
		return new ProtoStuffRequest(null, path, method, RequestTypes.GET_SESSION);
	}
	
	private static class StaticSessionGenerator implements NewSessionGenerator {

		private final StandardRpcSessionSerialiser sessionSerializer;
		
		private StaticSessionGenerator(StandardRpcSessionSerialiser sessionSerializer){
			this.sessionSerializer = sessionSerializer;
		}
		@Override
		public SessionDetails getSessionDetails(long userId) throws IOException, RpcSessionCreationException {
			FacebookTokenNetworkUserSession fbLtSession = new FacebookTokenNetworkUserSession(new FacebookUid(userId), new FacebookUserAccessToken("testToken"));
			
			int truncatedUserId32Bits = (int)(userId & Integer.MAX_VALUE);
			
			RpcSession<FacebookTokenNetworkUserSession> rpcSession = RpcSessionFactory.getInstance().createSession(Game.GAME_ID_MARLIN, "GB", fbLtSession, new PlayfishUid(truncatedUserId32Bits), new byte[0]);
			final String rpcSessionStr = sessionSerializer.toExternalString(rpcSession);
			
			return new SessionDetails(){

				@Override
				public String getSessionKey() {
					return rpcSessionStr;
				}

				@Override
				public String getManifestVersion() {
					return "manifest.xml";
				}
				
			};
		}
		
	}
}