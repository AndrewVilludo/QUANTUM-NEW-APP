package com.example.quantumrealm;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

public final class QuizJuiceHelper {

    private QuizJuiceHelper() {
    }

    public static void flashQuestionCard(MaterialCardView card, boolean correct) {
        if (card == null) {
            return;
        }
        int color = ContextCompat.getColor(
                card.getContext(),
                correct ? R.color.game_success_soft : R.color.game_error_soft);
        card.setCardBackgroundColor(color);
        card.postDelayed(() -> card.setCardBackgroundColor(
                ContextCompat.getColor(card.getContext(), R.color.game_surface)), 220);
    }

    public static void highlightOption(RadioButton selected, boolean correct) {
        if (selected == null) {
            return;
        }
        selected.setBackgroundResource(
                correct ? R.drawable.quiz_option_correct : R.drawable.quiz_option_wrong);
    }

    public static void pulseProgressBar(ProgressBar bar) {
        if (bar == null) {
            return;
        }
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(bar, View.SCALE_Y, 1f, 1.35f, 1f);
        scaleX.setDuration(280);
        scaleX.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleX.start();
    }

    public static void updateStreakDisplay(Context context, TextView scoreText, int streak) {
        if (scoreText == null) {
            return;
        }
        scoreText.setText(context.getString(R.string.quiz_streak_format, streak));
        if (streak == 3) {
            Toast.makeText(context, R.string.quiz_streak_on_fire, Toast.LENGTH_SHORT).show();
        } else if (streak == 5) {
            Toast.makeText(context, R.string.quiz_streak_unstoppable, Toast.LENGTH_SHORT).show();
        }
    }
}
