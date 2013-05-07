package com.ahaines.loadtest.report;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;

import com.ahaines.loadtest.MessageGenerator;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class OverallThroughputReportWriterTest {
	
	private OverallThroughputReport writer;
	private ListAggReportWriterTester resultantWriter;
	private final static int DEFAULT_INTERVAL = 100;

	@Before
	public void beforeTest(){
		resultantWriter = new ListAggReportWriterTester();
		writer = new OverallThroughputReport(DEFAULT_INTERVAL, resultantWriter);
	}
	
	@Test
	public void testThroughput() throws WriterException{
		List<Message> messages = MessageGenerator.getMessages();
		
		for (Message message: messages){
			writer.writeStats(message);
		}
		
		writer.close();
		
		Queue<AggregatedMessage> aggMessages = resultantWriter.getSeenMessages();
		
		assertThat("The number of aggregated messages is not as expected", aggMessages.size(), is(equalTo(4)));
		AggregatedMessage message = aggMessages.poll();
		assertThat("first message should have the first added start time", message.getStartTime(), is(equalTo(0L)));
		assertThat("first message has an unexpected hit rate", message.getHitRate(), is(equalTo(7)));
		assertThat("first message has an unexpected av response time", message.getAverageResponseTime(), is(equalTo(63L)));
		assertThat("first message has an unexpected max response time", message.getMaximumResponseTime(), is(equalTo(200L)));
		assertThat("first message has an unexpected no of threads", message.getConcurrentConnections(), is(equalTo(3)));
		
		message = aggMessages.poll();
		assertThat("first message should have the first added start time", message.getStartTime(), is(equalTo(130L)));
		assertThat("first message has an unexpected hit rate", message.getHitRate(), is(equalTo(9)));
		assertThat("first message has an unexpected av response time", message.getAverageResponseTime(), is(equalTo(57L)));
		assertThat("first message has an unexpected max response time", message.getMaximumResponseTime(), is(equalTo(200L)));
		assertThat("first message has an unexpected no of threads", message.getConcurrentConnections(), is(equalTo(4)));
		
		message = aggMessages.poll();
		assertThat("first message should have the first added start time", message.getStartTime(), is(equalTo(310L)));
		assertThat("first message has an unexpected hit rate", message.getHitRate(), is(equalTo(2)));
		assertThat("first message has an unexpected av response time", message.getAverageResponseTime(), is(equalTo(45L)));
		assertThat("first message has an unexpected max response time", message.getMaximumResponseTime(), is(equalTo(50L)));
		assertThat("first message has an unexpected no of threads", message.getConcurrentConnections(), is(equalTo(4)));
		
		message = aggMessages.poll();
		assertThat("first message should have the first added start time", message.getStartTime(), is(equalTo(420L)));
		assertThat("first message has an unexpected hit rate", message.getHitRate(), is(equalTo(1)));
		assertThat("first message has an unexpected av response time", message.getAverageResponseTime(), is(equalTo(20L)));
		assertThat("first message has an unexpected max response time", message.getMaximumResponseTime(), is(equalTo(20L)));
		assertThat("first message has an unexpected no of threads", message.getConcurrentConnections(), is(equalTo(4)));
	}
}
class ListAggReportWriterTester implements ReportWriter<AggregatedMessage>{
	
	private final Queue<AggregatedMessage> queue;
	private boolean isClosed = false;
	
	ListAggReportWriterTester() {
		queue = new LinkedList<AggregatedMessage>();
	}

	Queue<AggregatedMessage> getSeenMessages(){
		return queue;
	}
	
	boolean isClosed(){
		return isClosed;
	}
	
	@Override
	public void writeStats(AggregatedMessage message) {
		queue.add(message);
		
	}

	@Override
	public void close() throws WriterException {
		this.isClosed = true;
	}
	
}