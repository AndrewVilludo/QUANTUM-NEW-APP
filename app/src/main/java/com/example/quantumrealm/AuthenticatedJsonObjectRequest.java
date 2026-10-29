package com.example.quantumrealm;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * GET/POST JSON request with Sanctum Bearer token from login.
 */
public class AuthenticatedJsonObjectRequest extends HostFallbackJsonObjectRequest {

    public AuthenticatedJsonObjectRequest(
            Context context,
            int method,
            String url,
            JSONObject jsonRequest,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener
    ) {
        super(context, method, url, jsonRequest, listener, errorListener);
    }

    private AuthenticatedJsonObjectRequest(
            Context context,
            int method,
            String url,
            JSONObject jsonRequest,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener,
            boolean fallbackAttempted
    ) {
        super(context, method, url, jsonRequest, listener, errorListener, fallbackAttempted);
    }

    @Override
    protected HostFallbackJsonObjectRequest createRetry(String alternateUrl) {
        return new AuthenticatedJsonObjectRequest(
                context,
                getMethod(),
                alternateUrl,
                requestBody,
                successListener,
                failureListener,
                true
        );
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        if (getMethod() == com.android.volley.Request.Method.POST
                || getMethod() == com.android.volley.Request.Method.PUT
                || getMethod() == com.android.volley.Request.Method.PATCH) {
            headers.put("Content-Type", "application/json; charset=utf-8");
        }
        String token = AuthHelper.getToken(context);
        if (token != null && !token.isEmpty()) {
            headers.put("Authorization", "Bearer " + token);
        }
        return headers;
    }
}
