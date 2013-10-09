package com.mihai.traxxor.data;

import android.os.Parcel;
import android.os.Parcelable;

import com.mihai.traxxor.R;

public class StopwatchAction implements Parcelable {
    public long mTimestamp;
    public long mDuration;
    public int mType;

    public StopwatchAction(long timestamp, long duration, int type) {
        this.mTimestamp = timestamp;
        this.mDuration = duration;
        this.mType = type;
    }

    public int describeContents() {
        return R.integer.describe_contents_action;
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
