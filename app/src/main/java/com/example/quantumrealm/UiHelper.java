package com.example.quantumrealm;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;

public final class UiHelper {

    private UiHelper() {
    }

    public static View inflatePathCard(
            Context context,
            ViewGroup parent,
            String title,
            String subtitle,
            String emoji,
            boolean locked,
            Runnable onUnlockedClick
    ) {
        return inflatePathCard(context, parent, title, subtitle, emoji, locked, false, false, onUnlockedClick);
    }

    public static View inflatePathCard(
            Context context,
            ViewGroup parent,
            String title,
            String subtitle,
            String emoji,
            boolean locked,
            boolean completed,
            Runnable onUnlockedClick
    ) {
        return inflatePathCard(context, parent, title, subtitle, emoji, locked, completed, false, onUnlockedClick);
    }

    public static View inflatePathCard(
            Context context,
            ViewGroup parent,
            String title,
            String subtitle,
            String emoji,
            boolean locked,
            boolean completed,
            boolean isCurrent,
            Runnable onUnlockedClick
    ) {
        View card = LayoutInflater.from(context).inflate(R.layout.item_path_card, parent, false);
        TextView emojiView = card.findViewById(R.id.pathEmoji);
        TextView titleView = card.findViewById(R.id.pathTitle);
        TextView subtitleView = card.findViewById(R.id.pathSubtitle);
        TextView completedBadge = card.findViewById(R.id.pathCompletedBadge);
        TextView currentBadge = card.findViewById(R.id.pathCurrentBadge);
        LottieAnimationView currentLottie = card.findViewById(R.id.pathCurrentLottie);
        ImageView lockIcon = card.findViewById(R.id.pathLockIcon);
        ImageView completeIcon = card.findViewById(R.id.pathCompleteIcon);
        MaterialCardView root = card.findViewById(R.id.pathCardRoot);

        emojiView.setText(completed ? "✅" : emoji);
        titleView.setText(title);

        lockIcon.setVisibility(View.GONE);
        completeIcon.setVisibility(View.GONE);
        if (completedBadge != null) {
            completedBadge.setVisibility(View.GONE);
        }
        if (currentBadge != null) {
            currentBadge.setVisibility(View.GONE);
        }
        if (currentLottie != null) {
            currentLottie.setVisibility(View.GONE);
            currentLottie.cancelAnimation();
        }
        root.clearAnimation();
        root.setScaleX(1f);
        root.setScaleY(1f);
        root.setAlpha(1f);
        root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.game_surface));
        root.setStrokeWidth(completed ? 2 : 1);
        root.setStrokeColor(ContextCompat.getColor(context, completed ? R.color.game_success : R.color.game_stroke));

        if (locked) {
            subtitleView.setText(R.string.module_locked_hint);
            lockIcon.setVisibility(View.VISIBLE);
            root.setAlpha(0.55f);
            root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.game_surface_elevated));
            root.setOnClickListener(v ->
                    Toast.makeText(context, R.string.locked_level, Toast.LENGTH_SHORT).show());
        } else if (completed) {
            subtitleView.setText(subtitle != null && !subtitle.isEmpty()
                    ? subtitle
                    : context.getString(R.string.module_completed));
            subtitleView.setTextColor(ContextCompat.getColor(context, R.color.game_success));
            completeIcon.setVisibility(View.VISIBLE);
            if (completedBadge != null) {
                completedBadge.setVisibility(View.VISIBLE);
            }
            root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.game_success_soft));
            root.setOnClickListener(v -> {
                Toast.makeText(context, R.string.module_completed_toast, Toast.LENGTH_SHORT).show();
                if (onUnlockedClick != null) {
                    onUnlockedClick.run();
                }
            });
        } else {
            subtitleView.setText(subtitle);
            subtitleView.setTextColor(ContextCompat.getColor(
                    context,
                    isCurrent ? R.color.game_accent : R.color.game_text_muted
            ));
            if (isCurrent) {
                emojiView.setVisibility(View.INVISIBLE);
                if (currentLottie != null) {
                    currentLottie.setVisibility(View.VISIBLE);
                    currentLottie.playAnimation();
                }
                if (currentBadge != null) {
                    currentBadge.setVisibility(View.VISIBLE);
                }
                root.setStrokeWidth(2);
                root.setStrokeColor(ContextCompat.getColor(context, R.color.game_accent));
                root.setCardBackgroundColor(ContextCompat.getColor(context, R.color.game_accent_soft));
                startCurrentPulse(root);
            } else {
                emojiView.setVisibility(View.VISIBLE);
            }
            root.setOnClickListener(v -> {
                if (onUnlockedClick != null) {
                    onUnlockedClick.run();
                }
            });
        }
        return card;
    }

    public static View inflatePathBossCard(
            Context context,
            ViewGroup parent,
            String title,
            Runnable onClick
    ) {
        View card = LayoutInflater.from(context).inflate(R.layout.item_path_boss_card, parent, false);
        TextView titleView = card.findViewById(R.id.pathBossTitle);
        MaterialCardView root = card.findViewById(R.id.pathBossCardRoot);
        titleView.setText(title);
        startCurrentPulse(root);
        root.setOnClickListener(v -> {
            if (onClick != null) {
                onClick.run();
            }
        });
        return card;
    }

    private static void startCurrentPulse(MaterialCardView root) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(root, View.SCALE_X, 1f, 1.025f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(root, View.SCALE_Y, 1f, 1.025f);
        scaleX.setDuration(900);
        scaleY.setDuration(900);
        scaleX.setRepeatMode(ValueAnimator.REVERSE);
        scaleY.setRepeatMode(ValueAnimator.REVERSE);
        scaleX.setRepeatCount(ValueAnimator.INFINITE);
        scaleY.setRepeatCount(ValueAnimator.INFINITE);
        scaleX.start();
        scaleY.start();
    }

    public static String emojiForIndex(int index) {
        String[] icons = {"🪐", "🚀", "⭐", "🛸", "🌌", "💫"};
        return icons[index % icons.length];
    }
}
