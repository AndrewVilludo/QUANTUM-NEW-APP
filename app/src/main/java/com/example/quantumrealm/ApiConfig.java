package com.example.quantumrealm;

/**
 * Central API base URL. Override via gradle.properties {@code API_BASE_URL}.
 * <ul>
 *   <li>Android emulator → {@code http://10.0.2.2:8000}</li>
 *   <li>Physical device on same Wi‑Fi → {@code http://YOUR_PC_LAN_IP:8000}</li>
 * </ul>
 */
public final class ApiConfig {

    private ApiConfig() {
    }

    public static final String BASE_URL = BuildConfig.API_BASE_URL;

    public static String api(String path) {
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        if (normalized.startsWith("api/")) {
            return BASE_URL + "/" + normalized;
        }
        return BASE_URL + "/api/" + normalized;
    }
}
