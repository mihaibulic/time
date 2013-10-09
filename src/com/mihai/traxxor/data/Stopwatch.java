package com.mihai.traxxor.data;

import java.util.LinkedList;

import android.os.Parcel;
import android.os.Parcelable;

import com.mihai.traxxor.R;

public class Stopwatch implements Parcelable {
    private static final String TAG = "Stopwatch";
    private static final boolean DEBUG = true;

    private int mId;
    private String mName;
    private long mCreationTime;
    private LinkedList<StopwatchAction> mStopwatchActions;
    private boolean mIsStarted;
    private long mLastTime;
    private long mDuration;

    @Deprecated
    public Stopwatch() {
    }

    public Stopwatch(int id, String name) {
        mId = id;
        mName = name;
        mCreationTime = getSystemTimeInMs();
        mStopwatchActions = new LinkedList<StopwatchAction>();
        mIsStarted = false;
    }

    /**
     * For recreating the {@link Stopwatch} via a Parcel
     */
    public Stopwatch(
            int id,
            String name,
            long creationTime,
            LinkedList<StopwatchAction> StopwatchActions,
            boolean isStarted,
            long lastTime,
            long duration) {
        mId = id;
        mName = name;
        mCreationTime = creationTime;
        mStopwatchActions = StopwatchActions;
        mIsStarted = isStarted;
        mLastTime = lastTime;
        mDuration = duration;
    }

    public boolean stop() {
        boolean sucessful = false;

        int duration = 0;
        if (mIsStarted) {
            long time = getSystemTimeInMs();
            mDuration += (int) (time - mLastTime);
            mStopwatchActions.add(new StopwatchAction(time, mDuration, R.integer.action_type_stop));
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
            mStopwatchActions.add(new StopwatchAction(time, mDuration, R.integer.action_type_start));
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
        mCreationTime = -1;
        mLastTime = getSystemTimeInMs();
        mDuration = 0;
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

    public String getName() {
        return mName;
    }

    public boolean isStarted() {
        return mIsStarted;
    }

    public long getCreationTime() {
        return mCreationTime;
    }

    public long getTimeSinceCreation() {
        return getSystemTimeInMs() - mCreationTime;
    }

    public long getDuration() {
        return mDuration
                + (mIsStarted ? (getSystemTimeInMs() - mLastTime) : 0);
    }

    public LinkedList<StopwatchAction> getStopwatchActions() {
        return mStopwatchActions;
    }

    public void printTimes() {
        for (StopwatchAction a : mStopwatchActions) {
            android.util.Log.i(TAG, "" + a.mDuration);
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
        out.writeLong(mCreationTime);
        out.writeList(mStopwatchActions);
        out.writeByte((byte) (mIsStarted ? 1 : 0));
        out.writeLong(mLastTime);
        out.writeLong(mDuration);
    }

    public static final Parcelable.Creator<Stopwatch> CREATOR = new Parcelable.Creator<Stopwatch>() {
        public Stopwatch createFromParcel(Parcel in) {
            int id = in.readInt();
            String name = in.readString();
            long creationTime = in.readLong();
            LinkedList<StopwatchAction> StopwatchActions = new LinkedList<StopwatchAction>();
            in.readList(StopwatchActions, null);
            boolean isStarted = (in.readByte() == 1);
            long lastTime = in.readLong();
            long duration = in.readLong();

            return new Stopwatch(
                    id,
                    name,
                    creationTime,
                    StopwatchActions,
                    isStarted,
                    lastTime,
                    duration);
        }

        public Stopwatch[] newArray(int size) {
            return new Stopwatch[size];
        }
    };
}
