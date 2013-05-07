package com.ahaines.loadtest;

import java.util.ArrayList;
import java.util.List;

import com.ahaines.loadtest.report.Message;

public class MessageGenerator {
	
	public static String THREAD_1_NAME = "threadName1";
	public static String THREAD_2_NAME = "threadName2";
	public static String THREAD_3_NAME = "threadName3";
	public static String THREAD_4_NAME = "threadName4";

	public static List<Message> getMessages(){
		
		List<Message> messages = new ArrayList<Message>();
		messages.add(new Message(THREAD_1_NAME, 200, 1, 0, 50, 1));
		messages.add(new Message(THREAD_1_NAME, 200, 2, 50, 25, 1));
		messages.add(new Message(THREAD_2_NAME, 200, 1, 50, 25, 2));
		messages.add(new Message(THREAD_3_NAME, 200, 1, 55, 200, 3));
		messages.add(new Message(THREAD_1_NAME, 200, 3, 80, 40, 3));
		messages.add(new Message(THREAD_2_NAME, 200, 2, 90, 70, 3));
		messages.add(new Message(THREAD_3_NAME, 200, 2, 80, 32, 3));
		messages.add(new Message(THREAD_1_NAME, 200, 4, 150, 30, 3));
		messages.add(new Message(THREAD_2_NAME, 200, 3, 170, 25, 3));
		messages.add(new Message(THREAD_1_NAME, 200, 5, 190, 15, 4));
		messages.add(new Message(THREAD_3_NAME, 200, 3, 130, 71, 3));
		messages.add(new Message(THREAD_4_NAME, 200, 3, 200, 90, 4));
		messages.add(new Message(THREAD_2_NAME, 200, 4, 205, 200, 4));
		messages.add(new Message(THREAD_3_NAME, 200, 4, 220, 30, 4));
		messages.add(new Message(THREAD_1_NAME, 200, 6, 220, 5, 4));
		messages.add(new Message(THREAD_1_NAME, 200, 7, 230, 55, 4));
		messages.add(new Message(THREAD_4_NAME, 200, 3, 310, 40, 4));
		messages.add(new Message(THREAD_2_NAME, 200, 5, 420, 20, 4));
		messages.add(new Message(THREAD_4_NAME, 200, 4, 355, 50, 4));

		return messages;
	}
}
