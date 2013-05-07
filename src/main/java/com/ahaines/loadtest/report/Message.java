package com.ahaines.loadtest.report;

import java.util.ArrayList;
import java.util.List;

public class Message implements Comparable<Message>{
	private final String threadName;
	private final int statusCode;
	private final int runNum;
	private final long startTime;
	protected long runTime;
	private final int concurrentConnections;
	
	public Message(String threadName, int statusCode, int runNum, long startTime, long runTime, int concurrentConnections){
		this.threadName = threadName;
		this.statusCode = statusCode;
		this.runNum = runNum;
		this.startTime = startTime;
		this.runTime = runTime;
		this.concurrentConnections = concurrentConnections;
	}

	public String getThreadName() {
		return threadName;
	}
	
	public int getConcurrentConnections(){
		return concurrentConnections;
	}

	public int getStatusCode() {
		return statusCode;
	}

	public int getRunNum() {
		return runNum;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getRunTime() {
		return runTime;
	}
	
	public long getEndTime(){
		return startTime + runTime;
	}
	
	public String toString(){
		StringBuilder builder = new StringBuilder(" Thread: \"");

		builder.append(getThreadName());
		builder.append("\" statusCode: \"");
		builder.append(getStatusCode());
		builder.append("\" runNum: \"");
		builder.append(getRunNum());
		builder.append("\" startTime: \"");
		builder.append(getStartTime());
		builder.append("\" runTime: \"");
		builder.append(getRunTime());
		builder.append("\" endTime: \"");
		builder.append(getEndTime());
		builder.append("\"");
		return builder.toString();
	}
	
	public List<Stat> getValues(){
		List<Stat> stats = new ArrayList<Stat>();
		
		stats.add(new Stat("threadName", getThreadName()));
		stats.add(new Stat("statusCode", ""+getStatusCode()));
		stats.add(new Stat("runNum", ""+getRunNum()));
		stats.add(new Stat("startTime", ""+getStartTime()));
		stats.add(new Stat("runTime", ""+getRunTime()));
		
		return stats;
	}

	@Override
	public int compareTo(Message o) {
		return (int)(this.getStartTime() - o.getStartTime());
	}
}

class Stat{
	private final String key;
	private final String value;
	
	Stat(String key, String value){
		this.key = key;
		this.value = value;
	}
	
	public String getKey(){
		return key;
	}
	
	public String getValue(){
		return value;
	}
}