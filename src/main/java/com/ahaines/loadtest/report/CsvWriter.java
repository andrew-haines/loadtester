package com.ahaines.loadtest.report;

import java.io.PrintStream;

public class CsvWriter<MessageType extends Message> extends PrintStreamWriter<MessageType>{
	
	private boolean hasWritenHeader;
	
	public CsvWriter(PrintStream out){
		super(out);
	}

	@Override
	public void writeStats(MessageType message) {
		StringBuilder builder = new StringBuilder();
		
		if (!hasWritenHeader){
			for (Stat stat: message.getValues()){
				builder.append(stat.getKey());
				builder.append(",");
			}
			builder.replace(builder.length()-1, builder.length(), "\n");
			hasWritenHeader = true;
		}
		for (Stat stat: message.getValues()){
			builder.append(stat.getValue());
			builder.append(",");
		}
		
		writeLine(builder.toString());
	}

}
