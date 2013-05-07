package com.ahaines.loadtest.request;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ahaines.loadtest.report.Message;
import com.ahaines.loadtest.report.ReportWriter;
import com.ahaines.loadtest.response.ResponseInfo;
import com.ahaines.loadtest.response.StandardResponseTypes;

public class HttpRequest implements Runnable{
	
	private static final String HEX_LIST = "0123456789ABCDEF";

	private final Long userId;
	private final TestRun testRun;
	private final String hostname;
	private final int port;
	private final Thread thread;
	
	private static final Logger LOG = LoggerFactory.getLogger(HttpRequest.class);
	
	private static final AtomicInteger threadCount = new AtomicInteger(0);
	private final CountDownLatch startLatch = new CountDownLatch(1);
	private final CountDownLatch finishedSleepingLatch;
	private final CountDownLatch endLatch;
	private final long waitTime;
	private final ReportWriter<Message> writer;
	private volatile AtomicBoolean active;
	private final long globalStartTime;
	private volatile float successRate = 0;
	private final int threadDelay;
	
	public HttpRequest(Long userId, TestRun testRun, String hostname, int port, long waitTime, ReportWriter<Message> writer, CountDownLatch startedLatch, CountDownLatch finishedLatch, long globalStartTime, int threadDelay) throws HttpRequestException{
		this.userId = userId;
		this.testRun = testRun;
		this.hostname = hostname;
		this.thread = new Thread(this, "Request Thread "+threadCount.addAndGet(1)+" for "+userId);
		this.waitTime = waitTime;
		this.writer = writer;
		this.finishedSleepingLatch = startedLatch;
		this.endLatch = finishedLatch;
		this.active = new AtomicBoolean(true);
		this.globalStartTime = globalStartTime;
		this.threadDelay = threadDelay;
		this.port = port;
		thread.start();
	}

	public void start() throws HttpRequestException{
		startLatch.countDown();
	}
	
	private String convertToHexString(byte[] value){
		
		StringBuffer payload = new StringBuffer(""+value.length);
		payload.append("-");
		
		// convert to hex
		
		for (byte next: value){
			payload.append(HEX_LIST.charAt((next>>4) & 0xF));
			payload.append(HEX_LIST.charAt ((next) & 0xF));
		}
		
		return payload.toString();
	}

	@Override
	public synchronized void run() {
		try {

			startLatch.await();
			
			String threadName = thread.getName();
			
			try{
				Thread.sleep(waitTime);
			} finally{
				finishedSleepingLatch.countDown();
			}
			
			LOG.debug("Starting thread: "+thread.getName()+" for user: "+userId+" at "+hostname.toString());
			
			int loopNum = 0;
			int successes = 0;
			ResponseInfo response = new ResponseInfo.StandardResponseInfo(StandardRequestTypes.INITIAL_REQUEST,StandardResponseTypes.NO_PREVIOUS_RESPONSE, 0);
			while (active.get() && testRun.hasRequest(response)){
				
				RequestInfo requestInfo = testRun.getRequest(response);
				// build url objects
				
				URL url;
				if (requestInfo.getMethod().equals("GET")){
					url = new URL("http://"+hostname+":"+port+"/"+requestInfo.getPath()+"?msg="+convertToHexString(requestInfo.getPayload()));
				} else{
					url = new URL("http://"+hostname+":"+port+"/"+requestInfo.getPath());
				}
				
				LOG.debug(getName()+" making request to: {} with type: {}", url.toExternalForm(), requestInfo.getRequestType());
				//make connection
				long runStartTime = System.currentTimeMillis();
				InputStream in = null;
				int statusCode = -1; // default to bad in the case of an exception
				try{
					HttpURLConnection conn = (HttpURLConnection)url.openConnection();
					conn.setRequestMethod(requestInfo.getMethod());
					
					conn.setRequestProperty("Content-Type", requestInfo.getMimeType());
					conn.setRequestProperty("Accept", requestInfo.getMimeType());
					conn.setDoOutput(true);
					conn.setConnectTimeout(10000); //set timeout to 10 seconds
					conn.setReadTimeout(10000);
					response = new ResponseInfo.StandardResponseInfo(requestInfo.getRequestType(), StandardResponseTypes.UNKNOWN_ERROR, loopNum+1);
					try{
						if (requestInfo.getMethod().equals("POST")){
						 //Send request
					      DataOutputStream wr = new DataOutputStream (conn.getOutputStream ());
					      wr.write(requestInfo.getPayload());
					      wr.flush ();
					      wr.close ();
						}
						statusCode = conn.getResponseCode();
					    
					    if (statusCode == HttpURLConnection.HTTP_OK){
					    	in = conn.getInputStream();
					    	
					    	ByteArrayOutputStream content = new ByteArrayOutputStream();
					    	byte[] buffer = new byte[1024];
					    	
					    	int bytesRead;
					    	
					    	while((bytesRead = in.read(buffer)) != -1){
					    		if (bytesRead != buffer.length){
					    			byte[] trimmedBuffer = new byte[bytesRead];
					    			System.arraycopy(buffer, 0, trimmedBuffer, 0, bytesRead);
					    			buffer = trimmedBuffer;
					    		}
					    		content.write(buffer);
					    	}
					    	
					    	response = new ResponseInfo(requestInfo.getRequestType(), StandardResponseTypes.RESPONSE_OK, content.toByteArray(), loopNum+1);
						    successes++;
					    }
					} catch(java.net.ConnectException e){
						statusCode = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
					} catch (java.net.SocketTimeoutException e){
						statusCode = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
					} catch (java.net.SocketException e){
						statusCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
					}
					    
				} catch (IOException ioe){
					LOG.error("unable to complete request", ioe);
				} finally{
					long runTime = (System.currentTimeMillis() - runStartTime);
                                        Message message = new Message(threadName, statusCode, loopNum++, runStartTime - globalStartTime, runTime, (int)finishedSleepingLatch.getCount());
                                        writer.writeStats(message);

					if (in != null){ // close connection
						in.close();
					}
				}
				if (threadDelay != -1){
					Thread.sleep(threadDelay);
				}
			}
			if (successes != 0){
				successRate = (float)successes / (float)loopNum;
			}
			
		} catch (Throwable e) {
			e.printStackTrace();
		} finally {
			endLatch.countDown();
		}
	}
	
	public float getSuccessRate(){
		return successRate;
	}
	
	public void shutdown() throws InterruptedException{
		active.set(false);
	}

	public String getName() {
		return thread.getName();
	}
}
