package com.ahaines.loadtest.generators;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.ahaines.loadtest.request.RequestInfo;
import com.ahaines.loadtest.request.RequestType;
import com.dyuproject.protostuff.ByteString;
import com.dyuproject.protostuff.ByteStringUtils;
import com.dyuproject.protostuff.Message;
import com.playfish.marlin.protostuff.transport.TransportAuditChange;
import com.playfish.marlin.protostuff.transport.TransportAuditChangeBatch;
import com.playfish.marlin.protostuff.transport.TransportAuditChangeRequest;
import com.playfish.marlin.protostuff.transport.TransportAuditChangeType;
import com.playfish.shinobi.io.externalizer.Externalizer;
import com.playfish.shinobi.io.externalizer.JsonIOUtilProtostuffExternalizer;
import com.playfish.shinobi.io.externalizer.ProtobufIOUtilProtostuffExternalizer;
import com.playfish.shinobi.io.externalizer.ProtostuffIOUtilProtostuffExternalizer;
import com.playfish.shinobi.io.externalizer.xml.XmlIOUtilProtostuffExternalizer;
import com.playfish.shinobi.protostuff.transport.Request;
import com.playfish.shinobi.protostuff.transport.Response;

public class ProtostuffWrapperUtil {

	public static final String PROTOSTUFF_MIME_TYPE = "application/protostuff";

	private static final String AUDIT_CHANGE_PATH = "api/game/user/processAuditAndSave";
	
	private final Externalizer externalizer;
	private final String mimeType;
	
	private ProtostuffWrapperUtil(Externalizer externalizer, String mimeType){
		this.externalizer = externalizer;
		this.mimeType = mimeType;
	}
	
	public String getMimeType(){
		return mimeType;
	}

	public <T extends Message<T>> Request wrapRequest(T message, String sessionKey, String manifestVersion) {
		Request request = new Request();
		byte[] payloadBytes = new byte[]{};
		if (message != null){
			payloadBytes = externalizer.write(message);
		}
		request.setPayload(ByteString.copyFrom(payloadBytes));
		request.setUserSession(sessionKey);
		request.setManifestVersion(manifestVersion);
		
		return request;
	}
	
	public <T extends Message<T>> byte[] wrapRequestAndExternalize(T message, String sessionKey, String manifestVersion){
		Request req = wrapRequest(message, sessionKey, manifestVersion);
		
		 return externalizer.write(req);
	}

	public <T extends Message<T>> T unWrapAndInternalize(byte[] responseContent,T message) {
		Response wrappedResponse = new Response();
		
		externalizer.read(responseContent, wrappedResponse);
		return externalizer.read(ByteStringUtils.getBytesWithoutCopy(wrappedResponse.getPayload()), message);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public TransportAuditChangeRequest addToAuditBatch(int checkoutVersion, int saveVersion, Map<TransportAuditChangeType, ? extends Message> auditMessages){
		TransportAuditChangeRequest request = new TransportAuditChangeRequest();
		request.setCheckoutVersion(checkoutVersion);
		TransportAuditChangeBatch batch = new TransportAuditChangeBatch();
		batch.setSaveVersion(saveVersion);
		List<TransportAuditChange> changes = new ArrayList<TransportAuditChange>();
		for (Entry<TransportAuditChangeType, ? extends Message> actualAudit: auditMessages.entrySet()){
			TransportAuditChange change = new TransportAuditChange();
			change.setType(actualAudit.getKey());
			change.setPayload(ByteStringUtils.createByteStringWithNoCopyOrEncode(externalizer.write(actualAudit.getValue())));
			changes.add(change);
		}
		batch.setAuditChangeList(changes);
		request.setAuditChangeBatchList(Arrays.asList(batch));
		return request;
	}
	
	public RequestInfo addToAuditBatchAndWrap(RequestType requestType, int checkoutVersion, int saveVersion, String sessionKey, String manifestVersion, @SuppressWarnings("rawtypes") Map<TransportAuditChangeType, ? extends Message> auditMessages){
		byte[] payload = wrapRequestAndExternalize(addToAuditBatch(checkoutVersion, saveVersion, auditMessages), sessionKey, manifestVersion);
		
		return new RequestInfo(requestType, AUDIT_CHANGE_PATH, payload, "POST", getMimeType());
	}
	
	public static ProtostuffWrapperUtil getProtostuffExternalizer(){
		return new ProtostuffWrapperUtil(new ProtostuffIOUtilProtostuffExternalizer(), "application/protostuff");
	}
	
	public static ProtostuffWrapperUtil getJSONExternalizer(){
		return new ProtostuffWrapperUtil(new JsonIOUtilProtostuffExternalizer(), "application/json");
	}
	
	public static ProtostuffWrapperUtil getXMLExternalizer(){
		return new ProtostuffWrapperUtil(new XmlIOUtilProtostuffExternalizer(), "application/xml");
	}
	
	public static ProtostuffWrapperUtil getProtobuffExternalizer(){
		return new ProtostuffWrapperUtil(new ProtobufIOUtilProtostuffExternalizer(), "application/protobuf");
	}

}
