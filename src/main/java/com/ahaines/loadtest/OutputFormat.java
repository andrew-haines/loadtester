package com.ahaines.loadtest;

import java.io.FileNotFoundException;
import java.io.PrintStream;

import com.ahaines.loadtest.report.CsvWriter;
import com.ahaines.loadtest.report.GraphWriter;
import com.ahaines.loadtest.report.Message;
import com.ahaines.loadtest.report.PrintStreamWriter;
import com.ahaines.loadtest.report.ReportWriter;
import com.ahaines.loadtest.report.ReportWriterFactory;
import com.ahaines.loadtest.report.WriterException;
import com.ahaines.loadtest.report.XmlWriter;

public enum OutputFormat{
	PRINTSTREAM(0, new ReportWriterFactory(){

		public ReportWriter<Message> createReportWriter(ReportConfig config) throws FileNotFoundException {
			if (config.getFile() == null){
				return new PrintStreamWriter<Message>(System.out);
			} else{
				PrintStream out = new PrintStream(config.getFile());
				return new PrintStreamWriter<Message>(out);
			}
		}
		
	}),
	XML(1, new ReportWriterFactory(){

		public ReportWriter<Message> createReportWriter(ReportConfig config) throws FileNotFoundException, WriterException {
			return new XmlWriter<Message>(config.getFile());

		}
	}),
	CSV(2, new ReportWriterFactory(){
		
		public ReportWriter<Message> createReportWriter(ReportConfig config) throws FileNotFoundException{
			PrintStream out = new PrintStream(config.getFile());
			return new CsvWriter<Message>(out);
		}
	}),
	
	PLOT_GRAPH(3, new ReportWriterFactory(){

		public ReportWriter<Message> createReportWriter(ReportConfig config) throws FileNotFoundException, WriterException {
			return new GraphWriter(config);
		}
		
	});
	
	private final int id;
	private final ReportWriterFactory factory;
	
	OutputFormat(int id, ReportWriterFactory factory){
		this.id = id;
		this.factory = factory;
	}
	
	public int getId(){
		return id;
	}
	
	public ReportWriterFactory getReportWriterFactory(){
		return factory;
	}

	public static OutputFormat getOutputFormat(Integer id) {
		for (OutputFormat type: values()){
			if (type.id == id){
				return type;
			}
		}
		throw new IllegalArgumentException("The type: "+id+" is unknown");
	}
}