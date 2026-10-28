package com.example.quantumrealm;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

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
        return inflatePathCard(context, parent, title, subtitle, emoji, locked, false, onUnlockedClick);
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
        View card = LayoutInflater.from(context).inflate(R.layout.item_path_card, parent, false);
        TextView emojiView = card.findViewById(R.id.pathEmoji);
        TextView titleView = card.findViewById(R.id.pathTitle);
        TextView subtitleView = card.findViewById(R.id.pathSubtitle);
        TextView completedBadge = card.findViewById(R.id.pathCompletedBadge);
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
            subtitleView.setText(R.string.module_completed);
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
            subtitleView.setTextColor(ContextCompat.getColor(context, R.color.game_text_muted));
            root.setOnClickListener(v -> {
                if (onUnlockedClick != null) {
                    onUnlockedClick.run();
                }
            });
        }
        return card;
    }

    public static String emojiForIndex(int index) {
        String[] icons = {"🪐", "🚀", "⭐", "🛸", "🌌", "💫"};
        return icons[index % icons.length];
    }
}
