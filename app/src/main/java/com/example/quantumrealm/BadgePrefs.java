package com.example.quantumrealm;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

public final class BadgePrefs {

    private static final String PREFS = "badge_prefs";
    private static final String KEY_EARNED = "earned_ids";
    private static final String KEY_INITIALIZED = "initialized";

    private BadgePrefs() {
    }

    public static Set<String> loadEarned(Context context) {
        return new HashSet<>(prefs(context).getStringSet(KEY_EARNED, new HashSet<>()));
    }

    public static void saveEarned(Context context, Set<String> ids) {
        prefs(context).edit().putStringSet(KEY_EARNED, new HashSet<>(ids)).apply();
    }

    /** Returns badge id that was newly earned, or null. */
    public static String syncNewBadge(Context context, JSONArray badges) {
        if (badges == null) {
            return null;
        }
        SharedPreferences storage = prefs(context);
        Set<String> known = loadEarned(context);
        Set<String> updated = new HashSet<>(known);
        String newlyEarned = null;
        boolean initialized = storage.getBoolean(KEY_INITIALIZED, false);
        try {
            for (int i = 0; i < badges.length(); i++) {
                JSONObject row = badges.optJSONObject(i);
                if (row == null || !row.optBoolean("earned", false)) {
                    continue;
                }
                String id = row.optString("id", "");
                if (id.isEmpty()) {
                    continue;
                }
                if (initialized && !known.contains(id) && newlyEarned == null) {
                    newlyEarned = id;
                }
                updated.add(id);
            }
        } catch (Exception ignored) {
        }
        saveEarned(context, updated);
        storage.edit().putBoolean(KEY_INITIALIZED, true).apply();
        return newlyEarned;
    }

    public static String labelForBadge(JSONArray badges, String badgeId) {
        if (badges == null || badgeId == null) {
            return "";
        }
        for (int i = 0; i < badges.length(); i++) {
            JSONObject row = badges.optJSONObject(i);
            if (row != null && badgeId.equals(row.optString("id", ""))) {
                return row.optString("label", badgeId);
            }
        }
        return badgeId;
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
