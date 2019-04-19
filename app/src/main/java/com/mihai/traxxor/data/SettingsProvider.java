package com.mihai.traxxor.data;

import android.content.Context;
import android.content.SharedPreferences;

public class SettingsProvider {

    private static final String KEY_TIME_AT_WORK = "timeAtWork";
    private static final float DEFAULT_TIME_AT_WORK = 6f;

    private SharedPreferences mSharedPrefs;

    public SettingsProvider(Context context) {
        mSharedPrefs = context.getSharedPreferences("settings", Context.MODE_PRIVATE);
    }

    public void setTimeAtWork(float hours) {
        mSharedPrefs.edit().putFloat(KEY_TIME_AT_WORK, hours).apply();
    }

    public float getTimeAtWorkHours() {
        return mSharedPrefs.getFloat(KEY_TIME_AT_WORK, DEFAULT_TIME_AT_WORK);
    }
}
