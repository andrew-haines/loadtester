package com.ahaines.loadtest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ahaines.loadtest.generators.PayloadGenerator;
import com.ahaines.loadtest.report.AsyncReportWriter;
import com.ahaines.loadtest.report.Message;
import com.ahaines.loadtest.report.ReportWriter;
import com.ahaines.loadtest.request.HttpRequest;
import com.ahaines.loadtest.request.TestRun;

public class LoadTester {
	
	private final static int DEFAULT_TIME_INTERVAL = 500;
	private final static Logger LOG = LoggerFactory.getLogger(LoadTester.class);
	
	private final ReportConfig config;
	
	LoadTester(ReportConfig config){
		this.config = config;
	}
	
	public void start() throws Exception{
		// get Payload
		LOG.debug("Starting load test");
		ReportWriter<Message> writer = null;
		
		List<HttpRequest> requests = new ArrayList<HttpRequest>();
		CountDownLatch shutdownLatch = new CountDownLatch(config.getMaxNumThreads());
		
		PayloadGenerator generator = config.getLoadType().getPayloadGenerator();
		
		List<Long> userIds = new LinkedList<Long>();
		
		for (int i = 0; i < 1000; i++){
			userIds.add(1000L+i);
		}
		generator.initGenerator(userIds);
		
		Map<Long, TestRun> payloads = generator.getPayload();
		
		/*
		 * if we cant make concurrent requests of the same user and the number of users we have is less then the required 
		 * thread size then we need to limit the thread count to how many users we have
		 */
		if (!config.getLoadType().isConcurrentUserRequestsSupported() && payloads.size() < config.getMaxNumThreads()){
			System.out.println("Unable to make a request of "+config.getMaxNumThreads()+" threads as we dont have enough users and this type of request ("+config.getLoadType().name()+") does not support concurrent user requests.\n\nSetting thread count to the number of users we have data for: "+payloads.size());
			config.setMaxNumThreads(payloads.size());
		}
		
		// now pass this data on to each worker thread
		List<Entry<Long, TestRun>> payloadSet = new ArrayList<Entry<Long,TestRun>>(payloads.entrySet());
		
		CountDownLatch latch = new CountDownLatch(config.getMaxNumThreads());
		
		writer = config.getOutputFormat().getReportWriterFactory().createReportWriter(config);
		
		System.out.println("Report file: "+config.getFile().getAbsolutePath());
		// wrap in async writer and accumalate if needed

		writer = new AsyncReportWriter<Message>(writer);
		
		LOG.debug("Starting "+config);
		long startTime = System.currentTimeMillis();
		try{
			for (int i = 0; i < config.getMaxNumThreads(); i++){	
				Entry<Long, TestRun> payload = payloadSet.get(i % payloads.size());
				long waitTime = (TimeUnit.MILLISECONDS.convert(config.getRampTime(), TimeUnit.SECONDS) / config.getMaxNumThreads()) * i;
				HttpRequest request = new HttpRequest(payload.getKey(), payload.getValue(), config.getHostname(), 9001, waitTime, writer, latch, shutdownLatch, startTime, config.getThreadDelay());
				requests.add(request);
				request.start();
			}
			
			LOG.debug("Finished setting up threads. Waiting for them to all start");
			
			latch.await(); // wait for tasks to finish
			LOG.debug("all threads started, waiting for run of: "+config.getTimeToRunFor()+" seconds");
			Thread.sleep(TimeUnit.MILLISECONDS.convert(config.getTimeToRunFor(), TimeUnit.SECONDS));
			
		} finally{
			
			System.out.println("Shutting down threads");
			for (HttpRequest request: requests){
				request.shutdown();
			}
			if (requests.size() > 0){
				shutdownLatch.await();
				for (HttpRequest request: requests){
					LOG.debug("request "+request.getName()+" stopped with "+request.getSuccessRate()+" success rate");
				}
			}
			System.out.println("Shutting down writers");
			if (writer != null){
				writer.close();
			}
			System.out.println("Finished runtime: "+(System.currentTimeMillis() - startTime));
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args.length != 7 && args.length != 8){
			printUsageAndExit(null);
		}
		try{
			String hostname = args[0];
			LoaderType type = LoaderType.getLoaderType(Integer.valueOf(args[1]));
			int threads = Integer.valueOf(args[2]);
			int rampUpTime = Integer.valueOf(args[3]);
			int timeToRunFor = Integer.valueOf(args[4]);
			OutputFormat format = OutputFormat.getOutputFormat(Integer.valueOf(args[5]));
			int delayThread = Integer.parseInt(args[6]);
			ReportConfig reportConfig;
			if (args.length == 8){
				int interval = Integer.valueOf(args[7]);
				reportConfig = new ReportConfig(type, interval, hostname, rampUpTime, threads, format, delayThread, timeToRunFor);
				
			} else{
				reportConfig = new ReportConfig(type, DEFAULT_TIME_INTERVAL, hostname, rampUpTime, threads, format, delayThread, timeToRunFor);
			}
			LoadTester tester = new LoadTester(reportConfig);
			
			tester.start();
			
		} catch (Throwable t){
			printUsageAndExit(t);
		}
	}
	
	private static void printUsageAndExit(Throwable t) {
		StringBuilder builder = new StringBuilder("USAGE java LoadTester hostname load_test_type:{");
		try{
			
			for (int i = 0; i < LoaderType.values().length; i++){
				LoaderType type = LoaderType.values()[i];
				builder.append(type.getId());
				builder.append("=");
				builder.append(type.name());
				if (i != LoaderType.values().length){
					builder.append(",");
				}
			}
		} catch (NoClassDefFoundError e){ // if there was a problem loading the enum then calling values will get this error
			builder.append("UNKNOWN");
		}
		builder.append("}> threads ramp_up_time run_for output_format:{");
		for (int i = 0; i < OutputFormat.values().length; i++){
			OutputFormat format = OutputFormat.values()[i];
			builder.append(format.getId());
			builder.append("=");
			builder.append(format.name());
			if (i != OutputFormat.values().length){
				builder.append(",");
			}
		}
		builder.append("}> threadSleepTime, [accInterval]");
		System.out.println(builder.toString());
		if (t != null){
			t.printStackTrace();
		}
		System.exit(-1);
	}

}
