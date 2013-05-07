package com.ahaines.loadtest.report;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ErrorReport implements ReportWriter<Message>{
	
	private final Logger LOG = LoggerFactory.getLogger(ErrorReport.class);
	private final ErrorReportFactory errorReportFactory;
	
	private final Map<Integer, ReportWriter<Message>> errorWriters;
	
	public ErrorReport(ErrorReportFactory errorReportFactory){
		errorWriters = new HashMap<Integer, ReportWriter<Message>>();
		this.errorReportFactory = errorReportFactory;
	}

	@Override
	public void writeStats(Message message) {
		if (message.getStatusCode() < 200 || message.getStatusCode() >= 300){ // only write with the status code is not between 200-299
			LOG.debug("error returned with: "+message.getStatusCode());
			getWriter(message.getStatusCode()).writeStats(message);
		}
	}
	
	private ReportWriter<Message> getWriter(int statusCode) {
		
		ReportWriter<Message> errorWriterForCode = errorWriters.get(statusCode);
		
		if (errorWriterForCode == null){
			errorWriterForCode = errorReportFactory.createReportWriter(statusCode);
			errorWriters.put(statusCode, errorWriterForCode);
		}
		
		return errorWriterForCode;
	}

	@Override
	public void close() throws WriterException {
		for (ReportWriter<Message> writer: errorWriters.values()){
			writer.close();
		}
	}
	
	public static interface ErrorReportFactory{
		
		ReportWriter<Message> createReportWriter(int statusCode);
	}

}