package com.example.quantumrealm;

import android.app.Activity;
import android.content.Intent;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

import org.json.JSONObject;

public final class FeedbackHelper {

    private static Runnable pendingCelebrationDismiss;

    private FeedbackHelper() {
    }

    public static void notifyCelebrationDismissed() {
        if (pendingCelebrationDismiss != null) {
            pendingCelebrationDismiss.run();
            pendingCelebrationDismiss = null;
        }
    }

    public static void successHaptic(View anchor) {
        if (anchor != null) {
            anchor.performHapticFeedback(HapticFeedbackConstants.CONFIRM);
        }
    }

    public static void errorHaptic(View anchor) {
        if (anchor != null) {
            anchor.performHapticFeedback(HapticFeedbackConstants.REJECT);
        }
    }

    public static void showSnackbar(View root, String message) {
        if (root == null) {
            return;
        }
        Snackbar.make(root, message, Snackbar.LENGTH_SHORT).show();
    }

    public static void showSnackbar(View root, int messageResId) {
        if (root == null) {
            return;
        }
        Snackbar.make(root, messageResId, Snackbar.LENGTH_SHORT).show();
    }

    public static void celebrate(Activity activity, String title, String body, Runnable onDismiss) {
        celebrate(activity, title, body, CelebrationActivity.VARIANT_XP, null, null, onDismiss);
    }

    public static void celebrate(
            Activity activity,
            String title,
            String body,
            JSONObject engagement,
            Runnable onDismiss
    ) {
        String variant = EngagementUiHelper.pickCelebrationVariant(engagement, CelebrationActivity.VARIANT_XP);
        celebrate(activity, title, body, variant, engagement, null, onDismiss);
    }

    public static void celebrate(
            Activity activity,
            String title,
            String body,
            String variant,
            JSONObject engagement,
            String reviewBody,
            Runnable onDismiss
    ) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        pendingCelebrationDismiss = onDismiss;
        Intent intent = CelebrationActivity.intent(activity, title, body, variant, engagement);
        if (reviewBody != null && !reviewBody.isEmpty()) {
            intent.putExtra(CelebrationActivity.EXTRA_REVIEW_BODY, reviewBody);
        }
        activity.startActivity(intent);
    }

    public static void celebrateWithExtras(
            Activity activity,
            Intent celebrationIntent,
            Runnable onDismiss
    ) {
        if (activity == null || activity.isFinishing()) {
            return;
        }
        pendingCelebrationDismiss = onDismiss;
        activity.startActivity(celebrationIntent);
    }
}
