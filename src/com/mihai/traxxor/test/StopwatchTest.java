package com.mihai.traxxor.test;

import java.util.Random;

import com.mihai.traxxor.Stopwatch;


public class StopwatchTest {
	private static final String TAG = "StopwatchTest";
	
	public StopwatchTest() throws InterruptedException {
		testLoop();
	}
	
	private boolean testLoop() throws InterruptedException {
		Random r = new Random();
		Stopwatch w = new Stopwatch();
		w.start(); // starting
		while (w.getTimeSinceStart() < 485) {
			Thread.sleep(r.nextInt(5)+1);
			if(w.isActive()) {
				w.stop();
			} else {
				w.start();
			}
		}
		w.DEPstop();
		
		android.util.Log.v(TAG, "starttime " + w.getStartTime());
		android.util.Log.v(TAG, "active " + w.getActivePercentage());
		android.util.Log.v(TAG, "duration " + w.getDuration());
		android.util.Log.v(TAG, "timeSinceStart " + w.getTimeSinceStart());
		
		for (double d : w.getAverages(20)) {
			android.util.Log.v(TAG, ""+d);
		}
		
		return true;
	}
	
	private boolean testStartStop() throws InterruptedException {
		Stopwatch w = new Stopwatch();
		
		// TODO TEST STOPWATCH CLASS
		w.start(); // starting
		android.util.Log.v(TAG, "*");
		Thread.sleep(100); 
		w.stop();
		android.util.Log.v(TAG, "*");
		Thread.sleep(100); 
		w.start();
		android.util.Log.v(TAG, "*");
		Thread.sleep(150);
		w.DEPstop();
		android.util.Log.v(TAG, "*");
		Thread.sleep(100);
		w.start();
		android.util.Log.v(TAG, "*");
		Thread.sleep(100);
		w.start();
		android.util.Log.v(TAG, "*");
		Thread.sleep(100);
		w.stop();
		android.util.Log.v(TAG, "*");
		Thread.sleep(100);
		w.DEPstop();
		android.util.Log.v(TAG, "*");
		Thread.sleep(100);
		
		android.util.Log.v(TAG, ""+w.getStartTime());
		android.util.Log.v(TAG, ""+w.getActivePercentage());
		android.util.Log.v(TAG, ""+w.getDuration());
		
		return true;
	}
	
	public static void main(String[] args) throws InterruptedException {
		new StopwatchTest();
	}
}
