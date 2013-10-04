package com.mihai.traxxor.util;

import android.content.Context;
import android.content.SharedPreferences;

public class Util {

	private static final String PREFS = "traxxor_shared_prefs";
	
	public static SharedPreferences getPrefs(Context ctx) {
		return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
	}
}
