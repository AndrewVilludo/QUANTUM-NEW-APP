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

    private AuthHelper() {
    }

    public static void saveToken(Context context, String token) {
        prefs(context).edit().putString(TOKEN_KEY, token).apply();
    }

    public static String getToken(Context context) {
        return prefs(context).getString(TOKEN_KEY, null);
    }

    public static boolean isLoggedIn(Context context) {
        String token = getToken(context);
        return token != null && !token.isEmpty();
    }

    public static void clearSession(Context context) {
        prefs(context).edit().remove(TOKEN_KEY).apply();
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
