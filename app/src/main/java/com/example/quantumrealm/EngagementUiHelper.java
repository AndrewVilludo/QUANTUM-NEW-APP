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

    public static String variantFromCelebration(JSONObject celebration, JSONObject engagement) {
        if (celebration != null && celebration.has("variant")) {
            return celebration.optString("variant", CelebrationActivity.VARIANT_XP);
        }
        return pickCelebrationVariant(engagement, CelebrationActivity.VARIANT_XP);
    }

    public static String pickCelebrationVariant(JSONObject engagement, String fallback) {
        if (engagement != null && engagement.optBoolean("leveled_up", false)) {
            return CelebrationActivity.VARIANT_LEVEL_UP;
        }
        return fallback != null ? fallback : CelebrationActivity.VARIANT_XP;
    }

    public static int lottieResForVariant(String variant) {
        if (CelebrationActivity.VARIANT_LEVEL_UP.equals(variant)) {
            return R.raw.blue_planet;
        }
        if (CelebrationActivity.VARIANT_QUIZ_PASS.equals(variant)) {
            return R.raw.module_lotties;
        }
        if (CelebrationActivity.VARIANT_GRADUATE.equals(variant)) {
            return R.raw.intro;
        }
        if (CelebrationActivity.VARIANT_BADGE.equals(variant)) {
            return R.raw.white;
        }
        if (CelebrationActivity.VARIANT_YEAR_COMPLETE.equals(variant)) {
            return R.raw.journey;
        }
        return R.raw.green_circle;
    }
}
