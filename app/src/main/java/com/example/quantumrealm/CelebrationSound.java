package com.example.quantumrealm;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.ToneGenerator;

public final class CelebrationSound {

    private static final String PREFS = "celebration_prefs";
    private static final String KEY_SFX = "sfx_enabled";

    private CelebrationSound() {
    }

    public static boolean isSfxEnabled(Context context) {
        return prefs(context).getBoolean(KEY_SFX, true);
    }

    public static void setSfxEnabled(Context context, boolean enabled) {
        prefs(context).edit().putBoolean(KEY_SFX, enabled).apply();
    }

    public static void playSuccess(Context context) {
        playTone(context, ToneGenerator.TONE_PROP_ACK, 120);
    }

    public static void playLevelUp(Context context) {
        playTone(context, ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 180);
    }

    public static void playQuizComplete(Context context) {
        playTone(context, ToneGenerator.TONE_PROP_PROMPT, 150);
    }

    private static void playTone(Context context, int tone, int durationMs) {
        if (context == null || !isSfxEnabled(context) || Music.isMuted(context)) {
            return;
        }
        try {
            ToneGenerator generator = new ToneGenerator(AudioManager.STREAM_MUSIC, 70);
            generator.startTone(tone, durationMs);
            generator.release();
        } catch (Exception ignored) {
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
