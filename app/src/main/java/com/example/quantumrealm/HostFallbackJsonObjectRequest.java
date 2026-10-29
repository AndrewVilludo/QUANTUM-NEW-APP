package com.example.quantumrealm;

import android.content.Context;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

/**
 * Retries once on connection failure using {@link ApiConfig#withAlternateHost(String)}.
 * Emulator dev often needs either {@code 10.0.2.2} or {@code 127.0.0.1} with {@code adb reverse}.
 */
public class HostFallbackJsonObjectRequest extends JsonObjectRequest {

    private static final int TIMEOUT_MS = 45_000;

    protected final Context context;
    protected final JSONObject requestBody;
    protected final Response.Listener<JSONObject> successListener;
    protected final Response.ErrorListener failureListener;
    private final boolean fallbackAttempted;

    public HostFallbackJsonObjectRequest(
            Context context,
            int method,
            String url,
            JSONObject jsonRequest,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener
    ) {
        this(context, method, url, jsonRequest, listener, errorListener, false);
    }

    protected HostFallbackJsonObjectRequest(
            Context context,
            int method,
            String url,
            JSONObject jsonRequest,
            Response.Listener<JSONObject> listener,
            Response.ErrorListener errorListener,
            boolean fallbackAttempted
    ) {
        super(method, url, jsonRequest, listener, errorListener);
        this.context = context.getApplicationContext();
        this.requestBody = jsonRequest;
        this.successListener = listener;
        this.failureListener = errorListener;
        this.fallbackAttempted = fallbackAttempted;
        setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
    }

    protected HostFallbackJsonObjectRequest createRetry(String alternateUrl) {
        return new HostFallbackJsonObjectRequest(
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
    public void deliverError(VolleyError error) {
        if (!fallbackAttempted && VolleyErrorHelper.isConnectionFailure(error)) {
            String alternateUrl = ApiConfig.withAlternateHost(getUrl());
            if (alternateUrl != null) {
                Volley.newRequestQueue(context).add(createRetry(alternateUrl));
                return;
            }
        }
        super.deliverError(error);
    }
}
