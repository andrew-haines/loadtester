package com.ahaines.loadtest.report;

import java.io.FileNotFoundException;

import com.ahaines.loadtest.ReportConfig;

public interface ReportWriterFactory {

	ReportWriter<Message> createReportWriter(ReportConfig config) throws FileNotFoundException, WriterException;
}
