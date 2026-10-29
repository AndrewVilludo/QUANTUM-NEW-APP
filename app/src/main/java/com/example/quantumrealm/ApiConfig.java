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
        return apiWithBase(path, BASE_URL);
    }

    public static String apiWithBase(String path, String baseUrl) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        String normalized = path.startsWith("/") ? path.substring(1) : path;
        if (normalized.startsWith("api/")) {
            return base + "/" + normalized;
        }
        return base + "/api/" + normalized;
    }

    /** Other host the emulator may use when the primary URL cannot connect. */
    public static String alternateBaseUrl() {
        if (BASE_URL.contains("10.0.2.2")) {
            return "http://127.0.0.1:8000";
        }
        if (BASE_URL.contains("127.0.0.1") || BASE_URL.contains("localhost")) {
            return "http://10.0.2.2:8000";
        }
        return null;
    }

    public static String withAlternateHost(String fullUrl) {
        String alternate = alternateBaseUrl();
        if (alternate == null || fullUrl == null || !fullUrl.startsWith(BASE_URL)) {
            return null;
        }
        return alternate + fullUrl.substring(BASE_URL.length());
    }
}
