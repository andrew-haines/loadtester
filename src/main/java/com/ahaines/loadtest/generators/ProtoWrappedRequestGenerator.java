package com.ahaines.loadtest.generators;

import java.util.HashMap;
import java.util.Map;

import com.ahaines.loadtest.request.RequestInfo;
import com.ahaines.loadtest.request.RequestType;
import com.ahaines.loadtest.request.TestRun;
import com.ahaines.loadtest.response.ResponseInfo;
import com.dyuproject.protostuff.ByteString;
import com.dyuproject.protostuff.Message;
import com.playfish.shinobi.io.externalizer.Externalizer;
import com.playfish.shinobi.io.externalizer.ProtostuffIOUtilProtostuffExternalizer;
import com.playfish.shinobi.protostuff.transport.Request;

public abstract class ProtoWrappedRequestGenerator extends NewSessionPayloadGenerator{
	
	private final Externalizer externalizer;
	
	protected ProtoWrappedRequestGenerator(){
		this.externalizer = new ProtostuffIOUtilProtostuffExternalizer();
	}
	
	protected ProtoWrappedRequestGenerator(NewSessionGenerator generator){
		super(generator);
		this.externalizer = new ProtostuffIOUtilProtostuffExternalizer();
	}

	@Override
	public Map<Long, TestRun> getPayload() throws PayloadGenerationException {
		
		Map<Long, TestRun> payloads = new HashMap<Long, TestRun>();
		
		for (final Long userId: getAllUsers()){
			
			payloads.put(userId, new TestRun(){
				
				private final Map<ResponseInfo, RequestInfo> requestCache = new HashMap<ResponseInfo, RequestInfo>();

				@Override
				public boolean hasRequest(ResponseInfo response) {
					RequestInfo requestInfo = getRequestInfo(response, userId);
					if (requestInfo != null){
						requestCache.put(response, requestInfo);
						return true;
					}
					return false;
				}

				@Override
				public RequestInfo getRequest(ResponseInfo response) {
					return requestCache.get(response);
				}
			});
		}
		return payloads;
	}
	
	private RequestInfo getRequestInfo(ResponseInfo responseInfo, Long userId){
		ProtoStuffRequest protoRequest = getProtostuffRequest(responseInfo, userId);
		Request request = new Request();
		byte[] payloadBytes = new byte[]{};
		if (protoRequest.getMessage() != null){
			payloadBytes = getExternalizer().write(protoRequest.getMessage());
		}
		request.setPayload(ByteString.copyFrom(payloadBytes));
		request.setUserSession(getSessionKey(userId));
		request.setManifestVersion(getManifestVersion(userId));
		
		return new RequestInfo(protoRequest.getType(), protoRequest.getPath(), getExternalizer().write(request), protoRequest.getMethod(), "application/protostuff");
	}

	protected abstract ProtoStuffRequest<?> getProtostuffRequest(ResponseInfo responseInfo, Long userId);

	protected Externalizer getExternalizer(){
		return externalizer;
	}
	
	protected static class ProtoStuffRequest<T extends Message<T>> {
		private final T message;
		private final String path;
		private final String method;
		private final RequestType type;
		
		protected ProtoStuffRequest(T message, String path, String method, RequestType requestType){
			this.message = message;
			this.path = path;
			this.method = method;
			this.type = requestType;
		}

		public T getMessage() {
			return message;
		}

		public String getPath() {
			return path;
		}

		public String getMethod() {
			return method;
		}

		public RequestType getType() {
			return type;
		}
	}
}
