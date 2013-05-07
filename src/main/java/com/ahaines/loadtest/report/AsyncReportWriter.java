package com.ahaines.loadtest.report;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class AsyncReportWriter<MessageType extends Message> implements ReportWriter<MessageType>, Runnable{

	private BlockingQueue<MessageType> messageQueue;
	private volatile boolean active = true;
	private final ReportWriter<MessageType> writer;
	private final CountDownLatch finishedProcessingQueue = new CountDownLatch(1);
	
	private static final Logger LOG = LoggerFactory.getLogger(AsyncReportWriter.class);
	
	public AsyncReportWriter(ReportWriter<MessageType> writer){
		this.writer = writer;
		Thread threadWriter = new Thread(this);
		threadWriter.setPriority(Thread.MIN_PRIORITY); // give it min priority
		messageQueue = new LinkedBlockingQueue<MessageType>();
		threadWriter.start();
	}
	
	@Override
	public void run() {
		try {
			while(active || !messageQueue.isEmpty()){ // keep consuming messages if we are active or the queue still has elements left
				/* timeout after a second to prevent race condition where queue is empty and active is true, this then waits on the queue. 
				 * Then active is set to false but we indefinitely wait on the poll. time out will allow loop to check predicate and exit normally
				 */
				MessageType nextMessage = messageQueue.poll(1, TimeUnit.SECONDS); 
				if (nextMessage != null){
					writer.writeStats(nextMessage);
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		finally{
			finishedProcessingQueue.countDown();
		}
	}
	
	public void close() throws WriterException{
		// ensure queue is empty
		try {
			active = false;
			finishedProcessingQueue.await();
		} catch (InterruptedException e) {
			throw new WriterException("unable to countdown queue", e);
		} finally{
			writer.close();
		}
	}
	
	@Override
	public void writeStats(MessageType message) {
		if (active){
			messageQueue.add(message);
		} else{
			LOG.info("Skipping message :"+message.toString()+" as writer has been shut down");
		}
	}
}
