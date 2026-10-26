package com.example.quantumrealm;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * GET/POST JSON request with Sanctum Bearer token from login.
 */
public class AuthenticatedJsonObjectRequest extends JsonObjectRequest {

    private static final String PREFS_NAME = "APP_PREFS";
    private static final String TOKEN_KEY = "auth_token";

    private final Context context;

    public AuthenticatedJsonObjectRequest(
            Context context,
            int method,
            String url,
            JSONObject jsonRequest,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener
    ) {
        super(method, url, jsonRequest, listener, errorListener);
        this.context = context.getApplicationContext();
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        String token = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(TOKEN_KEY, null);
        if (token != null && !token.isEmpty()) {
            headers.put("Authorization", "Bearer " + token);
        }
        return headers;
    }
}
