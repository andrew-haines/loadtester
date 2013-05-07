package com.ahaines.loadtest.generators;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;

import com.ahaines.loadtest.request.RequestInfo;
import com.ahaines.loadtest.request.RequestType;
import com.ahaines.loadtest.request.StandardRequestTypes;
import com.ahaines.loadtest.request.TestRun;
import com.ahaines.loadtest.response.ResponseInfo;
import com.ahaines.properties.PropertiesSetter;
import com.playfish.platform.rpc.session.StandardRpcSessionSerialiser;
import com.playfish.server.datatype.DatatypeOutput;

public abstract class RpcPayloadGenerator extends UserIteratorPayloadGenerator{
	
	private static final int ENCAPSULATION_NULL = 0x00;

	private final String method;
	private final String path;
	private final String mimeType;
	protected RpcPayloadGenerator(String method, String path, String mimeType) {
		
		super(new StandardRpcSessionSerialiser());
		this.method = method;
		this.path = path;
		this.mimeType = mimeType;
		// load properties from appropriate location to init playfish code...
	}

	@Override
	public TestRun getPayload(String sessionKey, Long userId) throws PayloadGenerationException{

		ByteArrayOutputStream byteout = new ByteArrayOutputStream();
		DatatypeOutput output = new DatatypeOutput(byteout);
	
		int rpcCode = getRpcHandlerCode();
		try{
			output.writeUint8(ENCAPSULATION_NULL);
			output.writeUint8(rpcCode);//0xFE); // ping
			//output.writeUint8(0x03); // GetUserProfile				
			
			System.setProperty("rpcsession.factory.campaign.client.enabled", "false");
			
			
			output.writeString(sessionKey);
			processPayload(output);
		} catch (IOException e){
			throw new PayloadGenerationException("unable to create rpc payload for user: "+userId, e);
		}
		
		final RequestInfo requestInfo = new RequestInfo(new RpcRequestType(rpcCode),path, byteout.toByteArray(), method, mimeType);
		return new TestRun(){

			@Override
			public boolean hasRequest(ResponseInfo response) {
				return true;
			}

			@Override
			public RequestInfo getRequest(ResponseInfo response) {
				return requestInfo;
			}
			
		};
	}

	abstract protected void processPayload(DatatypeOutput output);
	
	abstract int getRpcHandlerCode();
	
	@Override
	public void initGenerator(Iterable<Long> userIds){
		try{
			PropertiesSetter.loadSysPropsFromPropertyDir();
		} catch (IOException e){
			throw new IllegalStateException("Unable to construct this "+getClass().getName()+" object. problem loading properties", e);
		}
		super.initGenerator(userIds);
	}
	
	public static class RpcRequestType implements RequestType {
		private final int code;
		
		public RpcRequestType(int code){
			this.code = code;
		}

		public int getCode() {
			return code;
		}
	}
	
}
