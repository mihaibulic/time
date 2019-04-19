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

    @Deprecated
    public Stopwatch() {
    }

    public Stopwatch(int id, String name) {
        mId = id;
        mName = name;
        mStopwatchActions = new ArrayList<>();
        mIsStarted = false;
    }

    /**
     * For recreating the {@link Stopwatch} via a Parcel
     */
    Stopwatch(
            int id,
            String name,
            ArrayList<StopwatchAction> StopwatchActions,
            boolean isStarted,
            long lastTime,
            long rawDuration) {
        mId = id;
        mName = name;
        mStopwatchActions = StopwatchActions;
        mIsStarted = isStarted;
        mLastTime = lastTime;
        mRawDuration = rawDuration;
    }

    void addAction(long timestamp, long duration, int type) {
        addAction(new StopwatchAction(timestamp, duration, type));
    }

    private void addAction(StopwatchAction action) {
        mStopwatchActions.add(action);
    }

    public boolean stop() {
        boolean successful = false;

        int duration = 0;
        if (mIsStarted) {
            long time = getSystemTimeInMs();
            mRawDuration += (int) (time - mLastTime);
            addAction(new StopwatchAction(time, mRawDuration, R.integer.action_type_stop));
            mLastTime = time;
            mIsStarted = false;
            successful = true;
        }

        if (DEBUG) {
            if (successful) {
                android.util.Log.d(TAG, "STOPPING @ " + mLastTime +
                        " w/ a duration of " + duration + "ms");
            } else {
                android.util.Log.d(TAG, "SKIPPING STOP");
            }
        }

        return successful;
    }

    public boolean start() {
        boolean successful = false;

        if (!mIsStarted) {
            long time = getSystemTimeInMs();
            mStopwatchActions.add(new StopwatchAction(time, mRawDuration, R.integer.action_type_start));
            mLastTime = time;
            mIsStarted = true;
            successful = true;
        }

        if (DEBUG) {
            if (successful) {
                android.util.Log.d(TAG, "STARTING @ " + mLastTime + "ms");
            } else {
                android.util.Log.d(TAG, "SKIPPING START");
            }
        }

        return successful;
    }

    public boolean reset() {
        mLastTime = getSystemTimeInMs();
        mRawDuration = 0;
        mStopwatchActions.clear();
        mIsStarted = false;

        if (DEBUG) {
            android.util.Log.d(TAG, "RESETTING @ " + mLastTime  + "ms");
        }

        return true;
    }

    public int getId() {
    	return mId;
    }

    Long getLastTime() {
        return mLastTime;
    }

    public String getName() {
        return mName;
    }

    public boolean isStarted() {
        return mIsStarted;
    }

    long getRawDuration() {
        return mRawDuration;
    }

    public long getCurrentDuration() {
        return mRawDuration
                + (mIsStarted ? (getSystemTimeInMs() - mLastTime) : 0);
    }

    ArrayList<StopwatchAction> getStopwatchActions() {
        return mStopwatchActions;
    }

//    public void printTimes() {
//        for (StopwatchAction a : mStopwatchActions) {
//            android.util.Log.i(TAG, "" + a.getDuration());
//        }
//    }

    static long getSystemTimeInMs() {
        // Used System.nanoTime()/NS_PER_MS before to prevent mistakes when clock changes
        // This however leads to mistakes as the arbitrary start time changes when the main
        // activity is killed to recoup memory.
        return System.currentTimeMillis();
    }

    // ***************************************************************************
    // Parcelable related code only below this.
    // ***************************************************************************

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(mId);
        out.writeString(mName);
        out.writeList(mStopwatchActions);
        out.writeByte((byte) (mIsStarted ? 1 : 0));
        out.writeLong(mLastTime);
        out.writeLong(mRawDuration);
    }

    public static final Parcelable.Creator<Stopwatch> CREATOR = new Parcelable.Creator<Stopwatch>() {
        public Stopwatch createFromParcel(Parcel in) {
            int id = in.readInt();
            String name = in.readString();
            ArrayList<StopwatchAction> StopwatchActions = new ArrayList<>();
            in.readList(StopwatchActions, getClass().getClassLoader());
            boolean isStarted = (in.readByte() == 1);
            long lastTime = in.readLong();
            long rawDuration = in.readLong();

            return new Stopwatch(
                    id,
                    name,
                    StopwatchActions,
                    isStarted,
                    lastTime,
                    rawDuration);
        }

        public Stopwatch[] newArray(int size) {
            return new Stopwatch[size];
        }
    };
}
