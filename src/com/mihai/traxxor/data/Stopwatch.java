package com.mihai.traxxor.data;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

import com.mihai.traxxor.R;

public class Stopwatch implements Parcelable {
    private static final String TAG = "Stopwatch";
    private static final boolean DEBUG = true;

    private int mId;
    private String mName;
    private ArrayList<StopwatchAction> mStopwatchActions;
    private boolean mIsStarted;
    private long mLastTime;
    private long mRawDuration;
    private boolean mIsWork;

    @Deprecated
    public Stopwatch() {
    }

    public Stopwatch(int id, String name) {
        mId = id;
        mName = name;
        mStopwatchActions = new ArrayList<StopwatchAction>();
        mIsStarted = false;
        mIsWork = false;
    }

    /**
     * For recreating the {@link Stopwatch} via a Parcel
     */
    public Stopwatch(
            int id,
            String name,
            ArrayList<StopwatchAction> StopwatchActions,
            boolean isStarted,
            long lastTime,
            long rawDuration,
            boolean isWork) {
        mId = id;
        mName = name;
        mStopwatchActions = StopwatchActions;
        mIsStarted = isStarted;
        mLastTime = lastTime;
        mRawDuration = rawDuration;
        mIsWork = isWork;
    }

    public void addAction(long timestamp, long duration, int type) {
        addAction(new StopwatchAction(timestamp, duration, type));
    }

    public void addAction(StopwatchAction action) {
        mStopwatchActions.add(action);
    }

    public boolean stop() {
        boolean sucessful = false;

        int duration = 0;
        if (mIsStarted) {
            long time = getSystemTimeInMs();
            mRawDuration += (int) (time - mLastTime);
            addAction(new StopwatchAction(time, mRawDuration, R.integer.action_type_stop));
            mLastTime = time;
            mIsStarted = false;
            sucessful = true;
        }

        if (DEBUG) {
            if (sucessful) {
                android.util.Log.d(TAG, "STOPPING @ " + mLastTime +
                        " w/ a duration of " + duration + "ms");
            } else {
                android.util.Log.d(TAG, "SKIPPING STOP");
            }
        }

        return sucessful;
    }

    public boolean start() {
        boolean sucessful = false;

        if (!mIsStarted) {
            long time = getSystemTimeInMs();
            mStopwatchActions.add(new StopwatchAction(time, mRawDuration, R.integer.action_type_start));
            mLastTime = time;
            mIsStarted = true;
            sucessful = true;
        }

        if (DEBUG) {
            if (sucessful) {
                android.util.Log.d(TAG, "STARTING @ " + mLastTime + "ms");
            } else {
                android.util.Log.d(TAG, "SKIPPING START");
            }
        }

        return sucessful;
    }

    public boolean reset() {
        mLastTime = getSystemTimeInMs();
        mRawDuration = 0;
        mStopwatchActions.clear();
        mIsStarted = false;
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

    public Long getLastTime() {
        return mLastTime;
    }

    public String getName() {
        return mName;
    }

    public boolean isStarted() {
        return mIsStarted;
    }

    public boolean isWork() {
        return mIsWork;
    }

    public long getRawDuration() {
        return mRawDuration;
    }

    public long getCurrentDuration() {
        return mRawDuration
                + (mIsStarted ? (getSystemTimeInMs() - mLastTime) : 0);
    }

    public ArrayList<StopwatchAction> getStopwatchActions() {
        return mStopwatchActions;
    }

    public void printTimes() {
        for (StopwatchAction a : mStopwatchActions) {
            android.util.Log.i(TAG, "" + a.getDuration());
        }
    }

    public static long getSystemTimeInMs() {
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
        out.writeList(mStopwatchActions);
        out.writeByte((byte) (mIsStarted ? 1 : 0));
        out.writeLong(mLastTime);
        out.writeLong(mRawDuration);
        out.writeByte((byte) (mIsWork ? 1 : 0));
    }

    public static final Parcelable.Creator<Stopwatch> CREATOR = new Parcelable.Creator<Stopwatch>() {
        public Stopwatch createFromParcel(Parcel in) {
            int id = in.readInt();
            String name = in.readString();
            ArrayList<StopwatchAction> StopwatchActions = new ArrayList<StopwatchAction>();
            in.readList(StopwatchActions, getClass().getClassLoader());
            boolean isStarted = (in.readByte() == 1);
            long lastTime = in.readLong();
            long rawDuration = in.readLong();
            boolean isWork = (in.readByte() == 1);

            return new Stopwatch(
                    id,
                    name,
                    StopwatchActions,
                    isStarted,
                    lastTime,
                    rawDuration,
                    isWork);
        }

        public Stopwatch[] newArray(int size) {
            return new Stopwatch[size];
        }
    };
}
