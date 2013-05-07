package com.ahaines.loadtest;

import com.ahaines.loadtest.generators.GetSessionPayloadGenerator;
import com.ahaines.loadtest.generators.PayloadGenerator;
import com.ahaines.loadtest.generators.PingPayloadGenerator;
import com.ahaines.loadtest.generators.ProtostuffWrapperUtil;
import com.ahaines.loadtest.generators.StaticPayLoadGenerator;
import com.ahaines.loadtest.testrungraph.testruns.TestDescriptionReLoadTest;

public enum LoaderType {
	
	PING(0, new PingPayloadGenerator("POST", "g/rpc/tailor", "application/octet-stream"), true),
	STATIC_REQUEST(1, new StaticPayLoadGenerator("clipfine-fe-0.0.1-SNAPSHOT/css/stylesheet.css", "GET","*/*"), true),
	PROTOTYPE_GET_SESSION_REQUEST(2, new GetSessionPayloadGenerator("api/game/test/getCurrentRpcSession", "POST"), true),
	TEST_DYNAMIC_XML(3, TestDescriptionReLoadTest.createGraphWalkerTest(ProtostuffWrapperUtil.getProtostuffExternalizer()), false);
	
	private final int id;
	private final PayloadGenerator generator;
	private final boolean concurrentUserRequestsSupported;
	
	LoaderType(int id, PayloadGenerator generator, boolean concurrentUserRequestsSupported){
		this.id = id;
		this.generator = generator;
		this.concurrentUserRequestsSupported = concurrentUserRequestsSupported;
	}
	
	int getId(){
		return id;
	}
	
	PayloadGenerator getPayloadGenerator(){
		return generator;
	}
	
	boolean isConcurrentUserRequestsSupported(){
		return concurrentUserRequestsSupported;
	}
	
	static LoaderType getLoaderType(int id){
		for (LoaderType type: values()){
			if (type.id == id){
				return type;
			}
		}
		throw new IllegalArgumentException("The type: "+id+" is unknown");
	}
	
	public String getDescription(){
		return name()+" to ("+getPayloadGenerator()+")";
	}
}