package com.example.quantumrealm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public final class AuthHelper {

    public static final String PREFS_NAME = "APP_PREFS";
    public static final String TOKEN_KEY = "auth_token";
    public static final String USER_NAME_KEY = "user_name";
    public static final String RANK_TITLE_KEY = "rank_title";
    private static final String RANK_SYNC_AT_KEY = "rank_title_synced_at";

    private AuthHelper() {
    }

    public static void saveToken(Context context, String token) {
        prefs(context).edit().putString(TOKEN_KEY, token).apply();
    }

    public static void saveUserName(Context context, String name) {
        prefs(context).edit().putString(USER_NAME_KEY, name != null ? name.trim() : "").apply();
    }

    public static String getUserName(Context context) {
        String name = prefs(context).getString(USER_NAME_KEY, "");
        return name != null && !name.isEmpty() ? name : "Explorer";
    }

    public static String getToken(Context context) {
        return prefs(context).getString(TOKEN_KEY, null);
    }

    public static boolean isLoggedIn(Context context) {
        String token = getToken(context);
        return token != null && !token.isEmpty();
    }

    public static void clearSession(Context context) {
        prefs(context).edit()
                .remove(TOKEN_KEY)
                .remove(USER_NAME_KEY)
                .remove(RANK_TITLE_KEY)
                .remove(RANK_SYNC_AT_KEY)
                .apply();
    }

    public static void saveRankTitle(Context context, String title) {
        String normalized = title != null ? title.trim() : "";
        prefs(context).edit()
                .putString(RANK_TITLE_KEY, normalized)
                .putLong(RANK_SYNC_AT_KEY, System.currentTimeMillis())
                .apply();
    }

    public static String getRankTitle(Context context) {
        String title = prefs(context).getString(RANK_TITLE_KEY, "");
        if (title != null && !title.isEmpty()) {
            return title;
        }
        return context.getString(R.string.nav_header_tier_fallback);
    }

    public static boolean shouldSyncRankTitle(Context context, long minIntervalMs) {
        if (!isLoggedIn(context)) {
            return false;
        }
        long lastSync = prefs(context).getLong(RANK_SYNC_AT_KEY, 0L);
        return System.currentTimeMillis() - lastSync >= minIntervalMs;
    }

    /**
     * Loads rank title from {@code GET /api/student-profile} for the drawer subtitle.
     */
    public static void refreshRankTitleFromServer(AppCompatActivity activity, Runnable onComplete) {
        if (!isLoggedIn(activity)) {
            if (onComplete != null) {
                activity.runOnUiThread(onComplete);
            }
            return;
        }

        StudentProfileClient.fetch(activity, new StudentProfileClient.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                JSONObject dashboard = response.optJSONObject("dashboard");
                if (dashboard != null) {
                    JSONObject rank = dashboard.optJSONObject("rank");
                    if (rank != null) {
                        saveRankTitle(activity, rank.optString("title", ""));
                    }
                }
                if (onComplete != null) {
                    activity.runOnUiThread(onComplete);
                }
            }

            @Override
            public void onError(String message) {
                if (onComplete != null) {
                    activity.runOnUiThread(onComplete);
                }
            }
        });
    }

    /** Revokes token on server (best effort), clears local session, returns to login. */
    public static void logout(AppCompatActivity activity) {
        String url = ApiConfig.api("logout");
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                activity,
                Request.Method.POST,
                url,
                new JSONObject(),
                response -> {
                },
                error -> {
                }
        );
        Volley.newRequestQueue(activity).add(request);

        clearSession(activity);

        Intent intent = new Intent(activity, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        activity.startActivity(intent);
        activity.finish();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
}
