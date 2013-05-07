package com.ahaines.loadtest.report;

import java.io.PrintStream;

public class PrintStreamWriter<MessageType extends Message> implements ReportWriter<MessageType> {
	
	private final PrintStream out;
	protected int lineNum = 0;
	
	public PrintStreamWriter(PrintStream out) {
		this.out = out;
	}

	@Override
	public void writeStats(MessageType message) {
		writeLine(message.toString());
	}
	
	protected void writeLine(String line){
		out.println(line);
	}

	@Override
	public void close() throws WriterException {
		out.close();
	}
}