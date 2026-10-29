package com.example.quantumrealm;

import android.content.Context;

import org.json.JSONObject;

public final class EngagementUiHelper {

    private EngagementUiHelper() {
    }

    public static String appendEngagementSummary(Context context, String base, JSONObject engagement) {
        if (engagement == null) {
            return base;
        }
        StringBuilder out = new StringBuilder(base != null ? base : "");
        int xpAwarded = engagement.optInt("xp_awarded", 0);
        if (xpAwarded > 0) {
            if (out.length() > 0) {
                out.append("\n\n");
            }
            out.append(context.getString(R.string.engagement_xp_earned, xpAwarded));
        }
        if (engagement.optBoolean("leveled_up", false)) {
            JSONObject rank = engagement.optJSONObject("rank");
            if (rank != null) {
                if (out.length() > 0) {
                    out.append("\n\n");
                }
                out.append(context.getString(
                        R.string.engagement_level_up,
                        rank.optString("title", ""),
                        rank.optInt("level", 1)));
            }
        }
        return out.toString();
    }

    public static String celebrationTitle(Context context, JSONObject celebration, int defaultTitleRes) {
        if (celebration != null && celebration.has("title")) {
            return celebration.optString("title", context.getString(defaultTitleRes));
        }
        return context.getString(defaultTitleRes);
    }
}
