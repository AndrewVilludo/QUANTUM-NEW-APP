package com.example.quantumrealm;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

public class AchievementsActivity extends LogoutAppCompatActivity {

    private static final String TAG = "AchievementsActivity";

    public static final String EXTRA_BADGES_JSON = "badges_json";

    private LinearLayout container;
    private TextView subtitle;
    private TextView progressLabel;
    private ProgressBar progressBar;
    private ProgressBar achievementsLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
        setupAppDrawer();

        container = findViewById(R.id.achievementsContainer);
        subtitle = findViewById(R.id.achievementsSubtitle);
        progressLabel = findViewById(R.id.achievementsProgressLabel);
        progressBar = findViewById(R.id.achievementsProgressBar);
        achievementsLoading = findViewById(R.id.achievementsLoading);

        String cached = getIntent().getStringExtra(EXTRA_BADGES_JSON);
        if (!TextUtils.isEmpty(cached)) {
            bindBadges(cached);
        } else {
            setLoading(true);
            loadFromApi();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.achievements_page_title);
        }
    }

    private void setLoading(boolean loading) {
        if (achievementsLoading != null) {
            achievementsLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        }
    }

    private void loadFromApi() {
        StudentProfileClient.fetch(this, new StudentProfileClient.Callback() {
            @Override
            public void onSuccess(JSONObject response) {
                setLoading(false);
                JSONArray badges = extractBadges(response);
                bindBadges(badges);
            }

            @Override
            public void onError(String message) {
                setLoading(false);
                Log.e(TAG, "Achievements load failed: " + message);
                showLoadError(message);
            }
        });
    }

    private static JSONArray extractBadges(JSONObject response) {
        JSONArray badges = response.optJSONArray("badges");
        if (badges != null) {
            return badges;
        }
        JSONObject dashboard = response.optJSONObject("dashboard");
        if (dashboard != null) {
            return dashboard.optJSONArray("badges");
        }
        return new JSONArray();
    }

    private void bindBadges(String badgesJson) {
        try {
            bindBadges(new JSONArray(badgesJson));
        } catch (Exception e) {
            Log.e(TAG, "Invalid badges JSON", e);
            showLoadError(null);
        }
    }

    private void bindBadges(JSONArray badges) {
        try {
            container.removeAllViews();

            if (badges.length() == 0) {
                showEmptyState();
                return;
            }

            int earned = 0;
            for (int i = 0; i < badges.length(); i++) {
                JSONObject row = badges.optJSONObject(i);
                if (row != null && jsonBoolean(row, "earned")) {
                    earned++;
                }
            }
            int total = badges.length();
            int percent = total == 0 ? 0 : (earned * 100 / total);
            progressLabel.setText(getString(R.string.achievements_progress_format, earned, total));
            progressBar.setProgress(percent);
            subtitle.setText(getString(R.string.achievements_subtitle_count, earned, total));

            LayoutInflater inflater = LayoutInflater.from(this);
            for (int i = 0; i < badges.length(); i++) {
                JSONObject badge = badges.optJSONObject(i);
                if (badge == null) {
                    continue;
                }
                View row = inflateBadgeCard(inflater, badge);
                container.addView(row);
                revealBadgeRow(row, i);
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to bind achievements", e);
            showLoadError(null);
        }
    }

    private void revealBadgeRow(View row, int index) {
        row.setAlpha(1f);
        row.setTranslationY(20f);
        row.animate()
                .translationY(0f)
                .setDuration(300)
                .setStartDelay(index * 60L)
                .start();
    }

    private void showEmptyState() {
        progressBar.setProgress(0);
        progressLabel.setText(getString(R.string.achievements_progress_format, 0, 0));
        subtitle.setText(R.string.achievements_subtitle_default);
        TextView message = new TextView(this);
        message.setText(R.string.achievements_empty);
        message.setTextAppearance(R.style.TextAppearance_QuantumRealm_Subtitle);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        message.setPadding(0, pad, 0, 0);
        container.addView(message);
    }

    private void showLoadError(String serverMessage) {
        container.removeAllViews();
        progressBar.setProgress(0);
        subtitle.setText(R.string.achievements_subtitle_default);
        TextView message = new TextView(this);
        message.setText(
                serverMessage != null && !serverMessage.isEmpty()
                        ? serverMessage
                        : getString(R.string.achievements_load_failed));
        message.setTextAppearance(R.style.TextAppearance_QuantumRealm_Subtitle);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        message.setPadding(0, pad, 0, 0);
        container.addView(message);
        Toast.makeText(
                this,
                serverMessage != null && !serverMessage.isEmpty()
                        ? serverMessage
                        : getString(R.string.achievements_load_failed),
                Toast.LENGTH_LONG).show();
    }

    private static boolean jsonBoolean(JSONObject object, String key) {
        if (object == null || !object.has(key) || object.isNull(key)) {
            return false;
        }
        Object value = object.opt(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue() != 0;
        }
        if (value instanceof String) {
            return "true".equalsIgnoreCase((String) value) || "1".equals(value);
        }
        return false;
    }

    private View inflateBadgeCard(LayoutInflater inflater, JSONObject badge) {
        View row = inflater.inflate(R.layout.item_achievement_badge, container, false);
        TextView emoji = row.findViewById(R.id.achievementEmoji);
        LottieAnimationView lottie = row.findViewById(R.id.achievementLottie);
        TextView title = row.findViewById(R.id.achievementTitle);
        TextView teaser = row.findViewById(R.id.achievementTeaser);
        TextView status = row.findViewById(R.id.achievementStatusBadge);
        MaterialCardView card = row.findViewById(R.id.achievementCard);

        boolean earned = jsonBoolean(badge, "earned");
        emoji.setText(badge.optString("emoji", "✨"));
        title.setText(badge.optString("label", "Achievement"));

        String description = badge.optString("description", "");
        String howTo = badge.optString("how_to_earn", "");
        if (earned && !description.isEmpty()) {
            teaser.setText(description);
        } else if (!howTo.isEmpty()) {
            teaser.setText(howTo);
        } else {
            teaser.setText(R.string.achievements_tap_for_details);
        }

        if (earned) {
            status.setText(R.string.achievement_status_earned);
            card.setStrokeColor(ContextCompat.getColor(this, R.color.game_success));
            card.setStrokeWidth(3);
            lottie.setVisibility(View.VISIBLE);
            lottie.setAnimation(R.raw.green_circle);
            lottie.playAnimation();
            emoji.setAlpha(0.35f);
        } else {
            status.setText(R.string.achievement_status_locked);
            card.setStrokeColor(ContextCompat.getColor(this, R.color.game_text_secondary));
            card.setStrokeWidth(1);
            emoji.setAlpha(0.45f);
        }

        row.setOnClickListener(v -> {
            v.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).withEndAction(() ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start());
            if (earned) {
                CelebrationEffects.playForVariant(this, CelebrationActivity.VARIANT_BADGE);
            } else {
                FeedbackHelper.errorHaptic(v);
            }
            showBadgeDetail(badge, earned);
        });

        return row;
    }

    private void showBadgeDetail(JSONObject badge, boolean earned) {
        String title = badge.optString("label", "");
        String emoji = badge.optString("emoji", "✨");
        StringBuilder body = new StringBuilder();
        body.append(emoji).append("  ");
        if (earned) {
            body.append(getString(R.string.achievement_status_earned)).append("\n\n");
            String description = badge.optString("description", "");
            if (!description.isEmpty()) {
                body.append(description);
            }
        } else {
            body.append(getString(R.string.achievement_status_locked)).append("\n\n");
            body.append(getString(R.string.achievement_detail_how_to)).append("\n");
            body.append(badge.optString("how_to_earn", getString(R.string.achievements_keep_exploring)));
        }

        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(body.toString().trim())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
