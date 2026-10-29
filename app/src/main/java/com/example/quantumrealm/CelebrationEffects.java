package com.example.quantumrealm;

import android.content.Context;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.HapticFeedbackConstants;
import android.view.View;

public final class CelebrationEffects {

    private CelebrationEffects() {
    }

    public static void playForVariant(Context context, String variant) {
        View anchor = new View(context);
        if (CelebrationActivity.VARIANT_LEVEL_UP.equals(variant)
                || CelebrationActivity.VARIANT_GRADUATE.equals(variant)) {
            anchor.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            vibratePattern(context, new long[]{0, 40, 80, 60});
            CelebrationSound.playLevelUp(context);
        } else if (CelebrationActivity.VARIANT_QUIZ_PASS.equals(variant)) {
            anchor.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            CelebrationSound.playQuizComplete(context);
        } else if (CelebrationActivity.VARIANT_BADGE.equals(variant)) {
            anchor.performHapticFeedback(HapticFeedbackConstants.CONTEXT_CLICK);
            CelebrationSound.playSuccess(context);
        } else {
            anchor.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
            CelebrationSound.playSuccess(context);
        }
    }

    public static void quizCorrect(View anchor) {
        if (anchor != null) {
            anchor.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        }
        CelebrationSound.playSuccess(anchor != null ? anchor.getContext() : null);
    }

    public static void quizWrong(View anchor) {
        if (anchor != null) {
            anchor.performHapticFeedback(HapticFeedbackConstants.REJECT);
        }
    }

    private static void vibratePattern(Context context, long[] pattern) {
        if (context == null) {
            return;
        }
        Vibrator vibrator = getVibrator(context);
        if (vibrator == null || !vibrator.hasVibrator()) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1));
        } else {
            vibrator.vibrate(pattern, -1);
        }
    }

    private static Vibrator getVibrator(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager manager = (VibratorManager) context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            return manager != null ? manager.getDefaultVibrator() : null;
        }
        return (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    }
}
