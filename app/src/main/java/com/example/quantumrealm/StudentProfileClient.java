package com.example.quantumrealm;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Loads {@code GET /api/student-profile} with auth, timeouts, and readable errors.
 */
public final class StudentProfileClient {

    public interface Callback {
        void onSuccess(JSONObject response);

        void onError(String message);
    }

    private StudentProfileClient() {
    }

    public static void fetch(Context context, Callback callback) {
        if (!AuthHelper.isLoggedIn(context)) {
            callback.onError(context.getString(R.string.journey_login_required));
            return;
        }

        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                context,
                Request.Method.GET,
                ApiConfig.api("student-profile"),
                null,
                response -> {
                    if (response != null && response.optBoolean("success", false)) {
                        callback.onSuccess(response);
                    } else {
                        String message = response != null
                                ? response.optString("message", "")
                                : "";
                        if (message.isEmpty()) {
                            message = context.getString(R.string.profile_load_failed);
                        }
                        callback.onError(message);
                    }
                },
                error -> callback.onError(userMessage(context, error))
        );
        Volley.newRequestQueue(context.getApplicationContext()).add(request);
    }

    public static String userMessage(Context context, VolleyError error) {
        return VolleyErrorHelper.message(context, error, R.string.profile_load_failed);
    }
}
