package com.example.quantumrealm;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Blocks back/up/profile navigation during an in-progress quiz attempt until the user confirms exit.
 */
public final class QuizExitGuard {

    private final OnBackPressedCallback backCallback;
    private final Runnable onConfirmExit;
    private AlertDialog visibleDialog;

    public QuizExitGuard(AppCompatActivity activity, Runnable onConfirmExit) {
        this.onConfirmExit = onConfirmExit;
        backCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                promptExit(activity);
            }
        };
        activity.getOnBackPressedDispatcher().addCallback(activity, backCallback);
    }

    public void setAttemptInProgress(boolean inProgress) {
        backCallback.setEnabled(inProgress);
    }

    /** @return true if the leave attempt was consumed (dialog shown). */
    public boolean interceptLeaveAttempt(AppCompatActivity activity) {
        if (!backCallback.isEnabled()) {
            return false;
        }
        promptExit(activity);
        return true;
    }

    private void promptExit(AppCompatActivity activity) {
        if (activity.isFinishing()) {
            return;
        }
        if (visibleDialog != null && visibleDialog.isShowing()) {
            return;
        }
        visibleDialog = new AlertDialog.Builder(activity)
                .setMessage(R.string.quiz_exit_confirm_message)
                .setNegativeButton(R.string.quiz_exit_confirm_cancel, null)
                .setPositiveButton(R.string.quiz_exit_confirm_exit, (dialog, which) -> onConfirmExit.run())
                .create();
        visibleDialog.show();
    }
}
