package com.ahaines.loadtest;

import java.io.File;


public class ReportConfig {

	private final File file;
	private final int sampleInterval;
	private final String hostname;
	private final int rampTime;
	private int maxNumThreads;
	private final String reportName;
	private final OutputFormat format;
	private final LoaderType loadType;
	private final int threadDelay;
	private final int timeToRunFor;
	
	public ReportConfig(LoaderType loadType, int sampleInterval, String hostname, int rampTime, int maxNumThreads, OutputFormat format, int threadDelay, int timeToRunFor){
		this.sampleInterval = sampleInterval;
		this.hostname = hostname;
		this.rampTime = rampTime;
		this.maxNumThreads = maxNumThreads;
		this.format = format;
		this.loadType = loadType;
		this.threadDelay = threadDelay;
		this.timeToRunFor = timeToRunFor;
		
		StringBuilder reportName = new StringBuilder();
		reportName.append(maxNumThreads);
		reportName.append(" Threads_");
		reportName.append(rampTime);
		reportName.append(" Ramp Time_");
		reportName.append(sampleInterval);
		reportName.append(" SampleInterval_Against host ");
		reportName.append(threadDelay);
		reportName.append(" Request wait time ");
		reportName.append(hostname);
		reportName.append(" with type "+loadType.name());
		this.reportName = reportName.toString() + loadType.getDescription();
		
		String reportRelativeDir = "reports/"+System.currentTimeMillis()+"_"+loadType.name().replaceAll(" ", "_");
		
		File reportRelativeDirFile = new File(reportRelativeDir);
		if (!reportRelativeDirFile.exists()){
			while (!reportRelativeDirFile.mkdir()){
				File parent = reportRelativeDirFile.getParentFile();
				if (parent == null || !parent.mkdir()){
					throw new IllegalStateException("Unable to create our report dir: "+reportRelativeDirFile.getAbsolutePath());
				}
				
			}
		}
		
		this.file = new File(reportRelativeDir+"/report."+format.name().toLowerCase());
	}
	
	public File getFile(){
		return file;
	}
	
	/**
	 * This value is the sample interval of capturing values. Ie if we want to sample latency as an average over a second (rather then each
	 * individual response back) then this should return 1000.
	 * @return
	 */
	public int getSampleInterval(){
		return sampleInterval;
	}
	
	public String getHostname(){
		return hostname;
	}
	
	public int getRampTime(){
		return rampTime;
	}
	
	public int getMaxNumThreads(){
		return maxNumThreads;
	}

	public String getReportName() {
		return reportName;
	}
	
	public OutputFormat getOutputFormat(){
		return format;
	}
	
	@Override
	public String toString(){
		return getReportName();
	}

	public LoaderType getLoadType() {
		return loadType;
	}

	/**
	 * Length of time that a thread waits for inbetween each request
	 * @return
	 */
	public int getThreadDelay() {
		return threadDelay;
	}

	/**
	 * The time in miliseconds that we apply our load for after the initial ramp up.
	 * @return
	 */
	public int getTimeToRunFor() {
		return timeToRunFor;
	}

	public void setMaxNumThreads(int size) {
		this.maxNumThreads = size;
	}
}
