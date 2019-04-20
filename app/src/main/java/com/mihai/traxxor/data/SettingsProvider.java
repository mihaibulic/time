package com.mihai.traxxor.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsProvider {

    private static final String KEY_TIME_TO_LEAVE = "timeAtWork";
    private static final float DEFAULT_TIME_TO_LEAVE_HOURS = 6f;

    private static final String KEY_TIME_TO_GET_UP = "getUpFreq";
    private static final int DEFAULT_TIME_TO_GET_UP_MINUTES = 90;

    private SharedPreferences mSharedPrefs;

    public SettingsProvider(Context context) {
        mSharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public void setTimeToLeave(float hours) {
        mSharedPrefs.edit().putFloat(KEY_TIME_TO_LEAVE, hours).apply();
    }

    public float getTimeToLeaveHours() {
        return mSharedPrefs.getFloat(KEY_TIME_TO_LEAVE, DEFAULT_TIME_TO_LEAVE_HOURS);
    }

    public void setTimeToGetUp(int minutes) {
        mSharedPrefs.edit().putInt(KEY_TIME_TO_GET_UP, minutes).apply();
    }

    public int getTimeToGetUpMinutes() {
        return mSharedPrefs.getInt(KEY_TIME_TO_GET_UP, DEFAULT_TIME_TO_GET_UP_MINUTES);
    }
}
