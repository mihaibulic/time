package com.mihai.traxxor;

import java.util.LinkedList;

import android.os.Parcel;
import android.os.Parcelable;

public class Stopwatch implements Parcelable {
    private static final String TAG = "Stopwatch";
    private static final boolean DEBUG = true;

    private int mId;
    private String mName;
    private long mStartTime;
    private long mLastTime;
    private long mDuration;
    private LinkedList<Integer> mResumeSuspendTimes = new LinkedList<Integer>();
    private boolean mIsStarted = false;
    private boolean mIsActive = false;

    @Deprecated
    public Stopwatch() {
    }

    public Stopwatch(int id, String name) {
        mId = id;
        mName = name;
    }

    /**
     * For recreating the {@link Stopwatch} via a Parcel
     */
    public Stopwatch(
            int id,
            String name,
            long startTime,
            long lastTime,
            long duration,
            LinkedList<Integer> resumeSuspendTimes,
            boolean isStarted,
            boolean isActive) {
        mId = id;
        mName = name;
        mStartTime = startTime;
        mLastTime = lastTime;
        mDuration = duration;
        mResumeSuspendTimes = resumeSuspendTimes;
        mIsStarted = isStarted;
        mIsActive = isActive;
    }

    public boolean start(boolean shouldActivate) {
        boolean sucessful = false;

        if (!mIsStarted) {
            reset();
            mStartTime = getSystemTimeInMs();
            sucessful = mIsStarted = true;
            if (shouldActivate) {
                sucessful = resume();
            }
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
        if (mIsStarted && mIsActive) {
            long time = getSystemTimeInMs();
            duration = (int) (time - mLastTime);
            mResumeSuspendTimes.addLast(duration);
            mDuration += duration;
            mLastTime = time;
            mIsActive = false;
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

        if (mIsStarted && !mIsActive) {
            long time = getSystemTimeInMs();
            mResumeSuspendTimes.addLast((int) (time - mLastTime));
            mLastTime = time;
            mIsActive = true;
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
        return (mIsStarted ? (mIsActive ? suspend() : resume()) : start(true));
    }

    public boolean stop() {
        boolean sucessful = false;

        if (mIsStarted) {
            suspend();
            mIsStarted = false;
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
        mIsStarted = mIsActive = false;
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
        return mIsStarted;
    }

    public boolean isActive() {
        return mIsActive;
    }
    public long getStartTime() {
        return mStartTime;
    }

    public long getTimeSinceStart() {
        return getEndTime() - mStartTime;
    }

    public long getDuration() {
        return mDuration
                + (mIsActive ? (getSystemTimeInMs() - mLastTime) : 0);
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
        return mIsStarted ? getSystemTimeInMs() : mLastTime;
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
        // Used System.nanoTime()/NS_PER_MS before to prevent mistakes when clock changes
        // This however leads to mistakes as the arbitrary start time changes when the main
        // activity is killed to recoup memory.
        return System.currentTimeMillis();
    }

    // ***************************************************************************
    // Parcelable related code only below this.
    // ***************************************************************************

    public int describeContents() {
        return R.integer.describe_contents_stopwatch;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mId);
        out.writeString(mName);
        out.writeLong(mStartTime);
        out.writeLong(mLastTime);
        out.writeLong(mDuration);
        out.writeList(mResumeSuspendTimes);
        out.writeByte((byte) (mIsStarted ? 1 : 0));
        out.writeByte((byte) (mIsActive ? 1 : 0));
    }

    public static final Parcelable.Creator<Stopwatch> CREATOR = new Parcelable.Creator<Stopwatch>() {
        public Stopwatch createFromParcel(Parcel in) {
            int id = in.readInt();
            String name = in.readString();
            long startTime = in.readLong();
            long lastTime = in.readLong();
            long duration = in.readLong();
            LinkedList<Integer> resumeSuspendTimes = new LinkedList<Integer>();
            in.readList(resumeSuspendTimes, null);
            boolean isStarted = (in.readByte() == 1);
            boolean isActive = (in.readByte() == 1);

            return new Stopwatch(
                    id,
                    name,
                    startTime,
                    lastTime,
                    duration,
                    resumeSuspendTimes,
                    isStarted,
                    isActive);
        }

        public Stopwatch[] newArray(int size) {
            return new Stopwatch[size];
        }
    };
}
