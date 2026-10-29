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

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class TitleTiersActivity extends LogoutAppCompatActivity {

    public static final String EXTRA_RANK_LADDER = "rank_ladder_json";

    private static final String[] TIER_EMOJIS = {
            "🌱", "⚡", "🧭", "🏃", "🐛", "⚔️", "🚀", "🏗️", "🔬", "🛡️", "🌌", "✨"
    };

    private LinearLayout container;
    private TextView subtitle;
    private JSONArray xpRewardLines = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_title_tiers);
        setupAppDrawer();

        container = findViewById(R.id.titleTiersContainer);
        subtitle = findViewById(R.id.titleTiersSubtitle);

        String ladderJson = getIntent().getStringExtra(EXTRA_RANK_LADDER);
        if (!TextUtils.isEmpty(ladderJson)) {
            bindFromLadder(ladderJson);
        } else {
            loadFromApi();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.title_tiers_page_title);
        }
    }

    private void loadFromApi() {
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.GET,
                ApiConfig.api("student-profile"),
                null,
                response -> {
                    try {
                        if (!response.optBoolean("success", false)) {
                            Toast.makeText(this, R.string.profile_load_failed, Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        JSONObject dashboard = response.optJSONObject("dashboard");
                        JSONObject ladder = dashboard != null ? dashboard.optJSONObject("rank_ladder") : null;
                        if (ladder == null) {
                            Toast.makeText(this, R.string.title_tiers_empty, Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        bindFromLadder(ladder.toString());
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.profile_load_failed, Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                error -> {
                    Toast.makeText(this, R.string.profile_load_failed, Toast.LENGTH_SHORT).show();
                    finish();
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void bindFromLadder(String ladderJson) {
        try {
            JSONObject ladder = new JSONObject(ladderJson);
            JSONArray rewards = ladder.optJSONArray("xp_rewards");
            xpRewardLines = rewards != null ? rewards : new JSONArray();

            subtitle.setText(R.string.title_tiers_subtitle_default);

            container.removeAllViews();
            JSONArray bands = ladder.optJSONArray("title_bands");
            if (bands == null || bands.length() == 0) {
                Toast.makeText(this, R.string.title_tiers_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            LayoutInflater inflater = LayoutInflater.from(this);
            for (int i = 0; i < bands.length(); i++) {
                JSONObject band = bands.optJSONObject(i);
                if (band == null) {
                    continue;
                }
                View card = inflateTierCard(inflater, band, i);
                container.addView(card);
                card.setAlpha(0f);
                card.setTranslationY(24f);
                card.animate()
                        .alpha(1f)
                        .translationY(0f)
                        .setDuration(320)
                        .setStartDelay(i * 60L)
                        .start();
            }
        } catch (Exception e) {
            Toast.makeText(this, R.string.profile_load_failed, Toast.LENGTH_SHORT).show();
        }
    }

    private View inflateTierCard(LayoutInflater inflater, JSONObject band, int index) {
        View row = inflater.inflate(R.layout.item_title_tier_card, container, false);
        TextView emoji = row.findViewById(R.id.titleTierEmoji);
        TextView name = row.findViewById(R.id.titleTierName);
        TextView levels = row.findViewById(R.id.titleTierLevels);
        TextView badge = row.findViewById(R.id.titleTierStatusBadge);
        ProgressBar progress = row.findViewById(R.id.titleTierProgress);
        TextView xpLine = row.findViewById(R.id.titleTierXpLine);
        com.google.android.material.card.MaterialCardView card = row.findViewById(R.id.titleTierCard);

        emoji.setText(TIER_EMOJIS[index % TIER_EMOJIS.length]);
        name.setText(band.optString("title", ""));

        int min = band.optInt("min_level", 1);
        String levelRange;
        if (band.isNull("max_level")) {
            levelRange = getString(R.string.profile_rank_band_max_open, min);
        } else {
            levelRange = getString(
                    R.string.profile_rank_band_range,
                    min,
                    band.optInt("max_level", min));
        }
        levels.setText(getString(R.string.title_tier_levels_format, levelRange));

        String state = band.optString("state", "locked");
        int badgeLabel;
        int strokeColor;
        float alpha = 1f;
        if ("current".equals(state)) {
            badgeLabel = R.string.title_tier_state_current;
            strokeColor = R.color.game_accent;
            card.setCardElevation(10f * getResources().getDisplayMetrics().density);
        } else if ("unlocked".equals(state)) {
            badgeLabel = R.string.title_tier_state_unlocked;
            strokeColor = R.color.game_success;
        } else {
            badgeLabel = R.string.title_tier_state_locked;
            strokeColor = R.color.game_text_secondary;
            alpha = 0.92f;
        }
        badge.setText(badgeLabel);
        row.setAlpha(alpha);
        card.setStrokeColor(ContextCompat.getColor(this, strokeColor));
        card.setStrokeWidth(state.equals("current") ? 3 : 1);

        progress.setProgress(band.optInt("progress_percent", 0));

        int xpAtMin = band.optInt("total_xp_at_min", 0);
        int xpRemaining = band.optInt("xp_remaining", 0);
        if ("locked".equals(state) && xpRemaining > 0) {
            xpLine.setText(getString(R.string.title_tier_xp_locked, xpAtMin, xpRemaining));
        } else if ("current".equals(state)) {
            xpLine.setText(getString(R.string.title_tier_xp_current, xpAtMin, progress.getProgress()));
        } else {
            xpLine.setText(getString(R.string.title_tier_xp_unlocked, xpAtMin));
        }

        row.setOnClickListener(v -> {
            v.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).withEndAction(() ->
                    v.animate().scaleX(1f).scaleY(1f).setDuration(120).start());
            showTierDetailDialog(band, levelRange);
        });

        return row;
    }

    private void showTierDetailDialog(JSONObject band, String levelRange) {
        String state = band.optString("state", "locked");
        int statusRes;
        if ("current".equals(state)) {
            statusRes = R.string.title_tier_state_current;
        } else if ("unlocked".equals(state)) {
            statusRes = R.string.title_tier_state_unlocked;
        } else {
            statusRes = R.string.title_tier_state_locked;
        }

        StringBuilder body = new StringBuilder();
        body.append(getString(R.string.title_tier_detail_levels, levelRange)).append("\n\n");
        body.append(getString(R.string.profile_level_detail_status, getString(statusRes))).append("\n\n");
        body.append(getString(R.string.profile_level_detail_requirement,
                band.optString("requirement", ""))).append("\n\n");
        body.append(getString(R.string.profile_level_detail_reward,
                band.optString("reward", ""))).append("\n\n");

        int xpRemaining = band.optInt("xp_remaining", 0);
        if ("locked".equals(state) && xpRemaining > 0) {
            body.append(getString(R.string.profile_level_detail_xp_remaining, xpRemaining)).append("\n\n");
        } else if ("unlocked".equals(state) || "current".equals(state)) {
            body.append(getString(R.string.profile_level_detail_xp_met)).append("\n\n");
        }

        body.append(getString(R.string.profile_level_detail_earn_heading)).append("\n");
        for (int i = 0; i < xpRewardLines.length(); i++) {
            JSONObject row = xpRewardLines.optJSONObject(i);
            if (row == null) {
                continue;
            }
            body.append("• ")
                    .append(getString(
                            R.string.profile_xp_reward_row,
                            row.optString("label", ""),
                            row.optString("xp", "")))
                    .append("\n");
        }

        new AlertDialog.Builder(this)
                .setTitle(band.optString("title", ""))
                .setMessage(body.toString().trim())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
