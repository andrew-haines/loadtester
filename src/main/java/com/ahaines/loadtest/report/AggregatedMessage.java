package com.ahaines.loadtest.report;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

public class AggregatedMessage extends Message {
	
	private int numExecutingThreads = 0;
	private int numSamples = 0;
	private long maxRunTime = 0;
	private final Map<Integer, CodeCount> codeCounts;
	
	public AggregatedMessage(int aggregatedSampleNumber, long startTime, int statusCode) {
		super("aggregatedMessage", statusCode, aggregatedSampleNumber, startTime, 0, 0); // zero runtime as addSample below will add it accumalately
		this.codeCounts = new TreeMap<Integer, CodeCount>();
		addStatusCodeCount(statusCode);
	}
	
	private void addStatusCodeCount(int statusCode) {
		CodeCount count = codeCounts.get(statusCode);
		
		if (count == null){
			count = new CodeCount(statusCode);
			codeCounts.put(statusCode, count);
		}
		count.incrementCount();
	}

	@Override
	public int getConcurrentConnections() {
		return numExecutingThreads;
	}

	public int getHitRate(){
		return numSamples;
	}
	
	public long getMaximumResponseTime(){
		return maxRunTime;
	}
	
	@Override
	public int getStatusCode() {
		return Collections.max(codeCounts.values()).statusCode;
	}

	public long getAverageResponseTime(){
		return runTime / numSamples;
	}
	
	public void addSample(long responseTime, int currentlyExecutingThreads, int statusCode){
		this.numExecutingThreads = currentlyExecutingThreads;
		numSamples++;
		runTime += responseTime;
		if (maxRunTime < responseTime){
			this.maxRunTime = responseTime;
		}
	}

	@Override
	public long getRunTime() {
		return getAverageResponseTime();
	}

	@Override
	public List<Stat> getValues() {
		List<Stat> stats = super.getValues();
		
		stats.add(new Stat("numExecutingThreads", ""+numExecutingThreads));
		stats.add(new Stat("hitRate", ""+getHitRate()));
		stats.add(new Stat("maxRunTime", ""+getMaximumResponseTime()));
		
		return stats;
	}

	@Override
	public String toString() {
		String mes =  super.toString();
		StringBuilder builder = new StringBuilder(mes);
		builder.append("hitRate: \"");
		builder.append(getHitRate());
		builder.append("\" numExecutingThreads: \"");
		builder.append(numExecutingThreads);
		builder.append("\" maxRunTime: \"");
		builder.append(getMaximumResponseTime());
		
		return builder.toString();
	}
	private static class CodeCount implements Comparable<CodeCount>{
		private final int statusCode;
		private final AtomicInteger count;
		
		private CodeCount(int statusCode){
			this.statusCode = statusCode;
			this.count = new AtomicInteger(0);
		}
		
		private void incrementCount(){
			count.incrementAndGet();
		}

		@Override
		public int compareTo(CodeCount o) {
			return count.get() - o.count.get();
		}
	}
}
