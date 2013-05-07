package com.ahaines.loadtest.report;

public interface ReportWriter<MessageType extends Message> {
	
	abstract void writeStats(MessageType message);
	
	public void close() throws WriterException;
}
