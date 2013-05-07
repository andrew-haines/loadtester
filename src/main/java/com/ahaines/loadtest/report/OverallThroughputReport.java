package com.ahaines.loadtest.report;

import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class OverallThroughputReport implements ReportWriter<Message>{
	
	private final ReportWriter<AggregatedMessage> delegate;
	private final Set<String> seenThreads;
	private AggregatedMessage aggregatedMessage;
	private final long timeInterval;
	private int numAggregatedSamples = 0;
	private PriorityQueue<Message> messageBuffer = new PriorityQueue<Message>(); // buffer used to ensure that message are aggregated in the order they started at.
	private static final int BUFFER_SIZE = 1000;
	
	public OverallThroughputReport(int timeInterval, ReportWriter<AggregatedMessage> delegate){
		this.delegate = delegate;
		
		this.seenThreads = new HashSet<String>();
		this.timeInterval = timeInterval;
	}
	
	public OverallThroughputReport(ReportWriter<AggregatedMessage> delegate){
		this(1000, delegate);// 1 second interval...
	}

	@Override
	public void writeStats(Message message){
		messageBuffer.add(message);
		
		if (messageBuffer.size() > BUFFER_SIZE){
			for (int i = 0; i < BUFFER_SIZE/2; i++){
				processMessage(messageBuffer.poll());
			}
		}
	}
	
	public void processMessage(Message message) {
		seenThreads.add(message.getThreadName());
		
		int numberOfThreads = seenThreads.size();
		
		if (aggregatedMessage == null){
			aggregatedMessage = new AggregatedMessage(++numAggregatedSamples, message.getStartTime(), message.getStatusCode());
		}
		
		// now check if we are ready to persist an aggregated sample...
		long deltaSinceLastSample = message.getStartTime() - aggregatedMessage.getStartTime();
		
		if (deltaSinceLastSample > timeInterval){
			
			delegate.writeStats(aggregatedMessage);
			aggregatedMessage = new AggregatedMessage(++numAggregatedSamples, message.getStartTime(), message.getStatusCode());
		} 
		aggregatedMessage.addSample(message.getRunTime(), numberOfThreads, message.getStatusCode());
		
	}

	@Override
	public void close() throws WriterException {
		
		//flush buffer
		while (!messageBuffer.isEmpty()){
			processMessage(messageBuffer.poll());
		}
		
		//process last message if there is one.
		
		if (aggregatedMessage != null){
			delegate.writeStats(aggregatedMessage);
		}
		delegate.close();
	}

}

