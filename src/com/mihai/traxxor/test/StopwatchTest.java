package com.mihai.traxxor.test;

import java.util.Random;

import com.mihai.traxxor.data.Stopwatch;


public class StopwatchTest {
    private static final String TAG = "StopwatchTest";

    public StopwatchTest() throws InterruptedException {
        testLoop();
    }

    private boolean testLoop() throws InterruptedException {
        Random r = new Random();
        Stopwatch w = new Stopwatch();
        w.start(); // starting
        while (w.getTimeSinceCreation() < 485) {
            Thread.sleep(r.nextInt(5) + 1);
            if (w.isStarted()) {
                w.stop();
            } else {
                w.start();
            }
        }

        android.util.Log.v(TAG, "starttime " + w.getCreationTime());
        android.util.Log.v(TAG, "duration " + w.getDuration());
        android.util.Log.v(TAG, "timeSinceStart " + w.getTimeSinceCreation());

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
        w.stop();
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
        w.stop();
        android.util.Log.v(TAG, "*");
        Thread.sleep(100);

        android.util.Log.v(TAG, "" + w.getCreationTime());
        android.util.Log.v(TAG, "" + w.getDuration());

        return true;
    }

    public static void main(String[] args) throws InterruptedException {
        new StopwatchTest();
    }
}
