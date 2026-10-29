package com.example.quantumrealm;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

public class AchievementsActivity extends LogoutAppCompatActivity {

    public static final String EXTRA_BADGES_JSON = "badges_json";

    private LinearLayout container;
    private TextView subtitle;
    private TextView progressLabel;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);
        setupAppDrawer();

        container = findViewById(R.id.achievementsContainer);
        subtitle = findViewById(R.id.achievementsSubtitle);
        progressLabel = findViewById(R.id.achievementsProgressLabel);
        progressBar = findViewById(R.id.achievementsProgressBar);

        String cached = getIntent().getStringExtra(EXTRA_BADGES_JSON);
        if (!TextUtils.isEmpty(cached)) {
            bindBadges(cached);
        } else {
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

    private void loadFromApi() {
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.GET,
                ApiConfig.api("student-profile"),
                null,
                response -> {
                    if (!response.optBoolean("success", false)) {
                        Toast.makeText(this, R.string.profile_load_failed, Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    JSONArray badges = response.optJSONArray("badges");
                    if (badges == null) {
                        Toast.makeText(this, R.string.achievements_empty, Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    bindBadges(badges.toString());
                },
                error -> {
                    Toast.makeText(this, R.string.profile_load_failed, Toast.LENGTH_SHORT).show();
                    finish();
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void bindBadges(String badgesJson) {
        try {
            JSONArray badges = new JSONArray(badgesJson);
            if (badges.length() == 0) {
                Toast.makeText(this, R.string.achievements_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            int earned = 0;
            for (int i = 0; i < badges.length(); i++) {
                JSONObject row = badges.optJSONObject(i);
                if (row != null && row.optBoolean("earned", false)) {
                    earned++;
                }
            }
            int total = badges.length();
            int percent = total == 0 ? 0 : (earned * 100 / total);
            progressLabel.setText(getString(R.string.achievements_progress_format, earned, total));
            progressBar.setProgress(percent);
            subtitle.setText(getString(R.string.achievements_subtitle_count, earned, total));

            container.removeAllViews();
            LayoutInflater inflater = LayoutInflater.from(this);
            for (int i = 0; i < badges.length(); i++) {
                JSONObject badge = badges.optJSONObject(i);
                if (badge == null) {
                    continue;
                }
                View row = inflateBadgeCard(inflater, badge);
                container.addView(row);
                row.setAlpha(0f);
                row.setTranslationY(28f);
                row.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(340)
                        .setStartDelay(i * 70L)
                        .start();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.profile_load_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private View inflateBadgeCard(LayoutInflater inflater, JSONObject badge) {
        View row = inflater.inflate(R.layout.item_achievement_badge, container, false);
        TextView emoji = row.findViewById(R.id.achievementEmoji);
        LottieAnimationView lottie = row.findViewById(R.id.achievementLottie);
        TextView title = row.findViewById(R.id.achievementTitle);
        TextView teaser = row.findViewById(R.id.achievementTeaser);
        TextView status = row.findViewById(R.id.achievementStatusBadge);
        MaterialCardView card = row.findViewById(R.id.achievementCard);

        boolean earned = badge.optBoolean("earned", false);
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
            row.setAlpha(0.88f);
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
