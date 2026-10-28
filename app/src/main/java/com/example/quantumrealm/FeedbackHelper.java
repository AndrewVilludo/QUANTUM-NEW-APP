package com.example.quantumrealm;

import android.app.Activity;
import android.view.HapticFeedbackConstants;
import android.view.View;

import androidx.appcompat.app.AlertDialog;

import com.google.android.material.snackbar.Snackbar;

public final class FeedbackHelper {

    private FeedbackHelper() {
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
        if (activity == null || activity.isFinishing()) {
            return;
        }
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(body)
                .setCancelable(false)
                .setPositiveButton(R.string.celebration_continue, (d, w) -> {
                    if (onDismiss != null) {
                        onDismiss.run();
                    }
                })
                .show();
    }
}
