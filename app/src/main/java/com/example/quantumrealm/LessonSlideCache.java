package com.example.quantumrealm;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;

public final class LessonSlideCache {

    private static final String PREFS = "lesson_slide_cache";

    private LessonSlideCache() {
    }

    public static void save(Context context, int lessonId, JSONArray slides) {
        if (slides == null) {
            return;
        }
        prefs(context).edit()
                .putString(key(lessonId), slides.toString())
                .apply();
    }

    public static JSONArray load(Context context, int lessonId) {
        String raw = prefs(context).getString(key(lessonId), null);
        if (raw == null) {
            return null;
        }
        try {
            return new JSONArray(raw);
        } catch (Exception e) {
            return null;
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    private static String key(int lessonId) {
        return "lesson_" + lessonId + "_slides";
    }
}
