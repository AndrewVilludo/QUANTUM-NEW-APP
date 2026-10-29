package com.example.quantumrealm;

import android.content.Context;
import android.content.SharedPreferences;

public final class JourneyProgressPrefs {

    private static final String PREFS = "journey_progress_prefs";
    private static final String KEY_INITIALIZED = "initialized";

    private JourneyProgressPrefs() {
    }

    public static boolean isInitialized(Context context) {
        return prefs(context).getBoolean(KEY_INITIALIZED, false);
    }

    public static void markInitialized(Context context) {
        prefs(context).edit().putBoolean(KEY_INITIALIZED, true).apply();
    }

    public static boolean wasYearCompleted(Context context, int yearId) {
        return prefs(context).getBoolean(key(yearId), false);
    }

    public static void setYearCompleted(Context context, int yearId, boolean completed) {
        prefs(context).edit().putBoolean(key(yearId), completed).apply();
    }

    private static String key(int yearId) {
        return "year_completed_" + yearId;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
