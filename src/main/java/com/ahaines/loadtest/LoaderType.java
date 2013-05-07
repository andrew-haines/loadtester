package com.ahaines.loadtest;

import com.ahaines.loadtest.generators.PayloadGenerator;
import com.ahaines.loadtest.generators.StaticPayLoadGenerator;
public enum LoaderType {
	
	STATIC_REQUEST(1, new StaticPayLoadGenerator("clipfine-fe-0.0.1-SNAPSHOT/css/stylesheet.css", "GET","*/*"), true);
	
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