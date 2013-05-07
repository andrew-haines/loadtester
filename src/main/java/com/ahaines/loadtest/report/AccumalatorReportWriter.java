package com.ahaines.loadtest.report;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public class AccumalatorReportWriter implements ReportWriter<Message>{
	
	private final HashMap<String, Message> aggregatedMap;
	private final ReportWriter<Message> writer;
	private final int interval;
	
	public AccumalatorReportWriter(int interval, ReportWriter<Message> writer){
		aggregatedMap = new HashMap<String, Message>();
		this.writer = writer;
		this.interval = interval;
	}

	@Override
	public void writeStats(Message message) {
		Message aggregatedMessage;
		String key = message.getThreadName();
		if (message.getStatusCode() == 200){
			if (!aggregatedMap.containsKey(key)){
				aggregatedMessage = new Message(key, message.getStatusCode(), 1, message.getStartTime(), message.getRunTime(), message.getConcurrentConnections());
			} else{
				Message previousStat = aggregatedMap.get(key);
					
				int num = previousStat.getRunNum() + 1;
				long accTime = previousStat.getRunTime() + message.getRunTime();
				aggregatedMessage = new Message(key, 200, num, previousStat.getStartTime(), accTime, previousStat.getConcurrentConnections());
			}
			if ((message.getStartTime() + message.getRunTime()) - aggregatedMessage.getStartTime() > interval){
				processAggregatedMessage(aggregatedMessage, message);
				aggregatedMap.remove(key); // clear it to ensure next data point resets
				
			} else{
				// replace what ever value was in the map
				
				aggregatedMap.put(key, aggregatedMessage);
			}
		}
	}
	
	private void processAggregatedMessage(Message aggregatedMessage, Message currentMessage){
		long avRunTime = aggregatedMessage.getRunTime() / aggregatedMessage.getRunNum();
		long avStartTime = (aggregatedMessage.getStartTime() + (currentMessage.getStartTime() + currentMessage.getRunTime())) / 2; // first message start time + last message end time / 2
		
		
		Message writenMessage = new Message(aggregatedMessage.getThreadName(), 200, aggregatedMessage.getRunNum(), avStartTime, avRunTime, currentMessage.getConcurrentConnections());
		writer.writeStats(writenMessage);
		// we need to now actually write this averaged value out
		
	}

	@Override
	public void close() throws WriterException {
		// flush all remaining aggregated messages
		
		List<Entry<String, Message>> entries = new ArrayList<Entry<String,Message>>(aggregatedMap.entrySet());
		Collections.sort(entries, new Comparator<Entry<String, Message>>(){

			@Override
			public int compare(Entry<String, Message> o1, Entry<String, Message> o2) {
				return (int)(o1.getValue().getStartTime() - o2.getValue().getStartTime());
			}
			
		});
		
		for (Entry<String, Message> entry: entries){
			processAggregatedMessage(entry.getValue(), entry.getValue());
		}
		aggregatedMap.clear();
		writer.close();
	}

}