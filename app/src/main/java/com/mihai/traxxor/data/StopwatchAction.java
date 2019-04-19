package com.mihai.traxxor.data;

import android.os.Parcel;
import android.os.Parcelable;

public class StopwatchAction implements Parcelable {
    private long mTimestamp;
    private long mDuration;
    private int mType;

    StopwatchAction(long timestamp, long duration, int type) {
        this.mTimestamp = timestamp;
        this.mDuration = duration;
        this.mType = type;
    }

    long getTimestamp() {
        return mTimestamp;
    }

    long getDuration() {
        return mDuration;
    }

    int getType() {
        return mType;
    }

    // ***************************************************************************
    // Parcelable related code only below this.
    // ***************************************************************************

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mTimestamp);
        out.writeLong(mDuration);
        out.writeInt(mType);
    }

    public static final Parcelable.Creator<StopwatchAction> CREATOR = new Parcelable.Creator<StopwatchAction>() {
        public StopwatchAction createFromParcel(Parcel in) {
            long timestamp = in.readLong();
            long duration = in.readLong();
            int type = in.readInt();

            return new StopwatchAction(
                    timestamp,
                    duration,
                    type);
        }

        public StopwatchAction[] newArray(int size) {
            return new StopwatchAction[size];
        }
    };
}
