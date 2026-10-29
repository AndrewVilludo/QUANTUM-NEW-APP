package com.example.quantumrealm;

import android.content.Context;

import androidx.annotation.StringRes;

import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Maps Volley failures to user-facing messages (connection vs HTTP/body errors).
 */
public final class VolleyErrorHelper {

    private VolleyErrorHelper() {
    }

    public static String message(Context context, VolleyError error, @StringRes int httpFallbackRes) {
        if (error == null || error.networkResponse == null) {
            return context.getString(R.string.auth_error_server_unreachable);
        }

        byte[] data = error.networkResponse.data;
        if (data != null && data.length > 0) {
            String parsed = parseJsonMessage(new String(data));
            if (parsed != null && !parsed.isEmpty()) {
                return parsed;
            }
        }

        int status = error.networkResponse.statusCode;
        if (status == 401 || status == 422) {
            return context.getString(httpFallbackRes);
        }
        return context.getString(R.string.auth_error_request_failed, status);
    }

    private static String parseJsonMessage(String responseBody) {
        try {
            JSONObject body = new JSONObject(responseBody);
            JSONObject errors = body.optJSONObject("errors");
            if (errors != null) {
                JSONArray names = errors.names();
                if (names != null) {
                    for (int i = 0; i < names.length(); i++) {
                        JSONArray fieldErrors = errors.optJSONArray(names.getString(i));
                        if (fieldErrors != null && fieldErrors.length() > 0) {
                            return fieldErrors.getString(0);
                        }
                    }
                }
            }
            String message = body.optString("message", "");
            if (!message.isEmpty()) {
                return message;
            }
        } catch (Exception ignored) {
        }
        return null;
    }
}
