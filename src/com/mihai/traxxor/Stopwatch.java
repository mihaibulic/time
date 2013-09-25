package com.mihai.traxxor;

import java.util.LinkedList;

public class Stopwatch {
    private static final String TAG = "Stopwatch";
    private static final boolean DEBUG = true;
    private static final int NS_PER_MS = 1000000; // 1,000,000,000 / 1000
    
    private String mName;
    private int mId;
    private long mStartTime;
    private long mLastTime;
    private long mDuration;
    private LinkedList<Integer> mResumeSuspendTimes = new LinkedList<Integer>();
    private boolean mStarted = false;
    private boolean mActive = false;
    
    @Deprecated
    public Stopwatch() {
    }
    
    public Stopwatch(int id, String name) {
        mId = id;
         mName = name;
    }
    
    public boolean start() {
        boolean sucessful = false;
        
        if (!mStarted) {
            reset();
            mStartTime = getSystemTimeInMs();
            mStarted = true;
            sucessful = resume();
        }

        if (DEBUG) {
            if (sucessful) {
                android.util.Log.d(TAG, "STARTING @ " + mStartTime + "ms");
            } else {
                android.util.Log.d(TAG, "SKIPPING START");
            }
        }
        
        return sucessful;
    }

    public boolean suspend() {
        boolean sucessful = false;
        
        int duration = 0;
        if(mStarted && mActive) {
            long time = getSystemTimeInMs();
            duration = (int) (time - mLastTime);
            mResumeSuspendTimes.addLast(duration);
            mDuration += duration; 
            mLastTime = time;
            mActive = false;
            sucessful = true;
        }

        if (DEBUG) {
            if (sucessful) {
                android.util.Log.d(TAG, "SUSPENDING @ " + mLastTime + 
                        " w/ a duration of " + duration + "ms");
            } else {
                android.util.Log.d(TAG, "SKIPPING SUSPEND");
            }
        }
        
        return sucessful;
    }

    public boolean resume() {
        boolean sucessful = false;
        
        if(mStarted && !mActive) {
            long time = getSystemTimeInMs();
            mResumeSuspendTimes.addLast((int) (time - mLastTime));
            mLastTime = time;
            mActive = true;
            sucessful = true;
        }

        if (DEBUG) {
            if (sucessful) {
                android.util.Log.d(TAG, "RESUMING @ " + mLastTime + "ms");
            } else {
                android.util.Log.d(TAG, "SKIPPING RESUME");
            }
        }
        
        return sucessful;
    }

    public boolean toggle() {
        return (mStarted ? (mActive ? suspend() : resume()) : start());
    }

    public boolean stop() {
        boolean sucessful = false;
        
        if(mStarted) {
            suspend();
            mStarted = false;
            sucessful = true;
        } 
        
        if (DEBUG) {
            if (sucessful) {
                android.util.Log.d(TAG, "STOPING @ " + mLastTime + "ms");
            } else {
                android.util.Log.d(TAG, "SKIPPING STOP");
            }
        }

        return sucessful;
    }
    
    public boolean reset() {
        mStartTime = -1;
        mLastTime = getSystemTimeInMs();
        mDuration = 0;
        mResumeSuspendTimes.clear();
        mStarted = mActive = false;
        boolean sucessful = true;
        
        if (DEBUG) {
            if (sucessful) {
                android.util.Log.d(TAG, "RESETTING @ " + mLastTime  + "ms");
            } else {
                android.util.Log.d(TAG, "SKIPPING RESET");
            }
        }
        
        return sucessful;
    }

    public int getId() {
    	return mId;
    }
    
    public String getName() {
        return mName;
    }

    public boolean isStarted() {
        return mStarted;
    }

    public boolean isActive() {
        return mActive;
    }
    public long getStartTime() {
        return mStartTime;
    }
    
    public long getTimeSinceStart() {
        return getEndTime() - mStartTime;
    }
    
    public long getDuration() {
        return mDuration
                + (mActive ? (getSystemTimeInMs() - mLastTime) : 0); 
    }
    
    public double getActivePercentage() {
        long endTime = getEndTime();
        long duration = getDuration();
        if (DEBUG) {
            android.util.Log.d(TAG, "Time active calculation: " + 
                    duration + " / (" + endTime + " - " + mStartTime + ")");
        }
        return ((double) duration) / (endTime - mStartTime); 
    }
    
    private long getEndTime() {
        return mStarted ? getSystemTimeInMs() : mLastTime;
    }
    
    public double[] getAverages(final int windowSizeInMs) {
        int length = (int) Math.ceil((double) getTimeSinceStart() / windowSizeInMs);
        double[] times = new double[length];

        if (DEBUG) {
            android.util.Log.d(TAG, "Slots needed: " + length);
            android.util.Log.d(TAG, "%Active | activeWindowLength | windowLength | overflow:");
        }
        
        long window = 0;
        long activeWindow = 0;
        boolean running = false;
        int cur = 0;
        for (int time : mResumeSuspendTimes) {
            window += time;
            if (running) {
                activeWindow += time;
            }
            
            long overflow = window - windowSizeInMs;
            if (overflow >= 0) {
                window -= overflow;
                if (running) {
                    activeWindow -= overflow;
                }
                
                times[cur++] = (double) activeWindow / window;
                if (DEBUG) {
                    android.util.Log.d(TAG, 
                            times[cur-1] + " | " + activeWindow + " | " + window + " | " + overflow);
                }
                
                window = overflow;
                activeWindow = (running ? overflow : 0);
            }
            
            running = !running;
        }
        
        if (window > 0 ) {
            times[cur++] = (double) activeWindow / window;
            if (DEBUG) {
                android.util.Log.d(TAG, times[cur-1] + " | " + activeWindow + " | " + window);
            }
        }

        return times;
    }

    public void printTimes() {
        for (int time : mResumeSuspendTimes) {
            android.util.Log.i(TAG, ""+time);
        }
    }

    private long getSystemTimeInMs() {
        return (int) (System.nanoTime()/NS_PER_MS);
    }
}
