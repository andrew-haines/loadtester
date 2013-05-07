package com.ahaines.loadtest.report;

import java.util.LinkedList;
import java.util.Queue;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

import com.ahaines.loadtest.MessageGenerator;

public class AccumalatedReportWriterTest {
	
	private AccumalatorReportWriter writer;
	private ListReportWriterTester resultantWriter;
	private final static int DEFAULT_INTERVAL = 100;

	@Before
	public void beforeTest(){
		resultantWriter = new ListReportWriterTester();
		writer = new AccumalatorReportWriter(DEFAULT_INTERVAL, resultantWriter);
	}
	
	@Test
	public void testAccumulate() throws WriterException{
		
		for (Message message: MessageGenerator.getMessages()){
			writer.writeStats(message);
		}
		writer.close();
		
		assertThat("underlying writer should have been closed", resultantWriter.isClosed(), is(equalTo(true)));
		
		Queue<Message> accumalatedMessages = resultantWriter.getSeenMessages();
		
		assertThat("there was not the corect amount of messages seen", accumalatedMessages.size(), is(equalTo(10)));
		assertMessage(accumalatedMessages.poll(), MessageGenerator.THREAD_3_NAME, 200L, 155L, 1, 200); // have a look at first message
		assertMessage(accumalatedMessages.poll(), MessageGenerator.THREAD_1_NAME, 38L, 60L, 3, 200); // have a look at second message
		assertMessage(accumalatedMessages.poll(), MessageGenerator.THREAD_2_NAME, 47L, 105L, 2, 200); // have a look at third message
		assertMessage(accumalatedMessages.poll(), MessageGenerator.THREAD_3_NAME, 51L, 140L, 2, 200); // have a look at fourth message
		assertMessage(accumalatedMessages.poll(), MessageGenerator.THREAD_2_NAME, 112L, 287L, 2, 200); // have a look at next message
		assertMessage(accumalatedMessages.poll(), MessageGenerator.THREAD_1_NAME, 26L, 217L, 4, 200); // have a look at next message
		assertMessage(accumalatedMessages.poll(), MessageGenerator.THREAD_4_NAME, 65L, 275L, 2, 200); // have a look at next message
		assertMessage(accumalatedMessages.poll(), MessageGenerator.THREAD_3_NAME, 30L, 235L, 1, 200); // have a look at next message
		assertMessage(accumalatedMessages.poll(), MessageGenerator.THREAD_4_NAME, 50L, 380L, 1, 200); // have a look at next message
		assertMessage(accumalatedMessages.poll(), MessageGenerator.THREAD_2_NAME, 20L, 430L, 1, 200); // have a look at next message
	}
	
	private void assertMessage(Message message, String expectedThreadName, long averageRunTime, long averageStartTime, int runNum, int statusCode){
		assertThat("message was not thread 1", message.getThreadName(), is(equalTo(expectedThreadName)));
		assertThat("message was not averaged correctly", message.getRunTime(), is(averageRunTime));
		assertThat("message was not with the expected run number", message.getRunNum(), is(equalTo(runNum)));
		assertThat("message was not with expected start time", message.getStartTime(), is(equalTo(averageStartTime)));
		assertThat("message was not with expected status code", message.getStatusCode(), is(equalTo(statusCode)));
	}
}

class ListReportWriterTester implements ReportWriter<Message>{
	
	private final Queue<Message> queue;
	private boolean isClosed = false;
	
	ListReportWriterTester() {
		queue = new LinkedList<Message>();
	}

	Queue<Message> getSeenMessages(){
		return queue;
	}
	
	boolean isClosed(){
		return isClosed;
	}
	
	@Override
	public void writeStats(Message message) {
		queue.add(message);
		
	}

	@Override
	public void close() throws WriterException {
		this.isClosed = true;
	}
	
}
