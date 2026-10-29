package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.text.TextUtils;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;

import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProfileActivity extends LogoutAppCompatActivity {

    private LinearLayout scoresContainer;
    private TextView nameText;
    private TextView emailText;
    private MaterialCardView dashboardCard;
    private TextView rankText;
    private ProgressBar rankProgressBar;
    private TextView rankProgressText;
    private TextView xpText;
    private TextView streakText;
    private TextView dailyGoalText;
    private TextView nextLevelText;
    private LinearLayout xpRewardsContainer;
    private MaterialCardView nextLevelCard;
    private MaterialCardView titleTiersEntry;
    private MaterialCardView achievementsEntry;
    private TextView achievementsSummary;
    private JSONArray badgesSnapshot = new JSONArray();
    private MaterialButton continueQuestButton;
    private MaterialButton certificateButton;
    private SwitchMaterial sfxSwitch;
    private JSONObject continueQuest;
    private JSONArray xpRewardLines = new JSONArray();
    private JSONObject nextLevelSnapshot;
    private JSONObject rankLadderSnapshot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameText = findViewById(R.id.profileNameText);
        emailText = findViewById(R.id.profileEmailText);
        scoresContainer = findViewById(R.id.profileScoresContainer);
        MaterialButton logoutButton = findViewById(R.id.profileLogoutButton);
        certificateButton = findViewById(R.id.profileCertificateButton);
        dashboardCard = findViewById(R.id.profileDashboardCard);
        rankText = findViewById(R.id.profileRankText);
        rankProgressBar = findViewById(R.id.profileRankProgressBar);
        rankProgressText = findViewById(R.id.profileRankProgressText);
        xpText = findViewById(R.id.profileXpText);
        streakText = findViewById(R.id.profileStreakText);
        dailyGoalText = findViewById(R.id.profileDailyGoalText);
        nextLevelText = findViewById(R.id.profileNextLevelText);
        nextLevelCard = findViewById(R.id.profileNextLevelCard);
        xpRewardsContainer = findViewById(R.id.profileXpRewardsContainer);
        titleTiersEntry = findViewById(R.id.profileTitleTiersEntry);
        achievementsEntry = findViewById(R.id.profileAchievementsEntry);
        achievementsSummary = findViewById(R.id.profileAchievementsSummary);
        continueQuestButton = findViewById(R.id.profileContinueQuestButton);
        sfxSwitch = findViewById(R.id.profileSfxSwitch);
        sfxSwitch.setChecked(CelebrationSound.isSfxEnabled(this));
        sfxSwitch.setOnCheckedChangeListener((button, checked) ->
                CelebrationSound.setSfxEnabled(this, checked));

        continueQuestButton.setOnClickListener(v ->
                ModuleFlowHelper.continueQuestFromDashboard(this, continueQuest));
        nextLevelCard.setOnClickListener(v -> {
            if (nextLevelSnapshot != null) {
                showNextLevelDetailDialog(nextLevelSnapshot);
            }
        });
        titleTiersEntry.setOnClickListener(v -> openTitleTiersScreen());
        achievementsEntry.setOnClickListener(v -> openAchievementsScreen());
        logoutButton.setOnClickListener(v -> confirmLogout());
        certificateButton.setOnClickListener(v -> {
            Intent intent = GraduationActivity.intent(this);
            intent.putExtra("student_name", nameText.getText().toString());
            startActivity(intent);
        });
        loadProfile();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.profile_title);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mute_only_menu, menu);
        Music.bindMuteMenuItem(this, menu.findItem(R.id.action_mute_music));
        return true;
    }

    private void openTitleTiersScreen() {
        Intent intent = new Intent(this, TitleTiersActivity.class);
        if (rankLadderSnapshot != null) {
            intent.putExtra(TitleTiersActivity.EXTRA_RANK_LADDER, rankLadderSnapshot.toString());
        }
        startActivity(intent);
    }

    private void openAchievementsScreen() {
        Intent intent = new Intent(this, AchievementsActivity.class);
        if (badgesSnapshot.length() > 0) {
            intent.putExtra(AchievementsActivity.EXTRA_BADGES_JSON, badgesSnapshot.toString());
        }
        startActivity(intent);
    }

    private void loadProfile() {
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.GET,
                ApiConfig.api("student-profile"),
                null,
                response -> {
                    try {
                        if (!response.optBoolean("success", false)) {
                            Toast.makeText(this, R.string.profile_load_failed, Toast.LENGTH_SHORT).show();
                            return;
                        }
                        JSONObject user = response.getJSONObject("user");
                        nameText.setText(user.optString("name", "Student"));
                        emailText.setText(user.optString("email", ""));
                        bindDashboard(response.optJSONObject("dashboard"));

                        JSONObject graduation = response.optJSONObject("graduation");
                        boolean showCertificate = graduation != null && graduation.optBoolean("graduated", false);
                        certificateButton.setVisibility(showCertificate ? View.VISIBLE : View.GONE);

                        JSONArray badges = response.optJSONArray("badges");
                        bindAchievementsEntry(badges);
                        final String newBadgeId = BadgePrefs.syncNewBadge(this, badges);
                        if (newBadgeId != null) {
                            final String label = BadgePrefs.labelForBadge(badges, newBadgeId);
                            getWindow().getDecorView().post(() -> FeedbackHelper.celebrate(
                                    ProfileActivity.this,
                                    getString(R.string.badge_unlock_title),
                                    label,
                                    CelebrationActivity.VARIANT_BADGE,
                                    null,
                                    null,
                                    null));
                        }

                        scoresContainer.removeAllViews();
                        JSONArray subjects = response.optJSONArray("quiz_scores_by_subject");
                        if (subjects == null || subjects.length() == 0) {
                            addEmptyScoresMessage();
                            return;
                        }

                        LayoutInflater inflater = LayoutInflater.from(this);
                        for (int i = 0; i < subjects.length(); i++) {
                            JSONObject subject = subjects.getJSONObject(i);
                            addSubjectSection(inflater, subject);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, R.string.profile_load_failed, Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, R.string.profile_load_failed, Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void bindDashboard(JSONObject dashboard) {
        if (dashboard == null) {
            dashboardCard.setVisibility(View.GONE);
            return;
        }
        dashboardCard.setVisibility(View.VISIBLE);
        JSONObject rank = dashboard.optJSONObject("rank");
        if (rank != null) {
            rankText.setText(getString(
                    R.string.profile_rank_format,
                    rank.optString("title", ""),
                    rank.optInt("level", 1)));
            int xpInLevel = rank.optInt("xp_in_level", 0);
            int xpForNext = Math.max(1, rank.optInt("xp_for_next_level", 1));
            rankProgressBar.setProgress(xpForNext == 0 ? 0 : (xpInLevel * 100 / xpForNext));
            rankProgressText.setText(getString(R.string.profile_rank_progress_format, xpInLevel, xpForNext));
        } else {
            rankText.setText("");
            rankProgressBar.setProgress(0);
            rankProgressText.setText("");
        }
        xpText.setText(getString(R.string.profile_xp_format, dashboard.optInt("quantum_xp", 0)));
        streakText.setText(getString(R.string.profile_streak_format, dashboard.optInt("daily_streak", 0)));
        dailyGoalText.setText(getString(
                R.string.profile_daily_goal_format,
                dashboard.optInt("lessons_completed_today", 0),
                dashboard.optInt("daily_lesson_goal", 1)));
        bindProgression(dashboard.optJSONObject("rank_ladder"), rank);
        continueQuest = dashboard.optJSONObject("continue_quest");
        continueQuestButton.setVisibility(continueQuest != null ? View.VISIBLE : View.GONE);
    }

    private void bindProgression(JSONObject ladder, JSONObject rank) {
        xpRewardsContainer.removeAllViews();
        rankLadderSnapshot = ladder;

        if (ladder == null) {
            nextLevelCard.setVisibility(View.GONE);
            titleTiersEntry.setVisibility(View.GONE);
            return;
        }

        try {
            if (rank != null) {
                ladder.put("rank", rank);
            }
        } catch (Exception ignored) {
        }

        titleTiersEntry.setVisibility(View.VISIBLE);

        JSONObject next = ladder.optJSONObject("next_level");
        nextLevelSnapshot = next;
        if (next != null) {
            nextLevelCard.setVisibility(View.VISIBLE);
            nextLevelText.setText(getString(
                    R.string.profile_next_level_format,
                    next.optString("title", ""),
                    next.optInt("level", 1),
                    next.optInt("xp_remaining", 0),
                    next.optInt("total_xp_required", 0)));
        } else {
            nextLevelCard.setVisibility(View.GONE);
        }

        JSONArray rewards = ladder.optJSONArray("xp_rewards");
        xpRewardLines = rewards != null ? rewards : new JSONArray();
        if (rewards != null) {
            for (int i = 0; i < rewards.length(); i++) {
                JSONObject row = rewards.optJSONObject(i);
                if (row == null) {
                    continue;
                }
                TextView line = new TextView(this);
                line.setText(getString(
                        R.string.profile_xp_reward_row,
                        row.optString("label", ""),
                        row.optString("xp", "")));
                line.setTextAppearance(R.style.TextAppearance_QuantumRealm_Subtitle);
                xpRewardsContainer.addView(line);
            }
        }
    }

    private void showNextLevelDetailDialog(JSONObject level) {
        if (level == null) {
            return;
        }

        int levelNum = level.optInt("level", 1);
        String title = level.optString("title", "");

        StringBuilder body = new StringBuilder();
        body.append(getString(R.string.profile_level_detail_status,
                getString(R.string.profile_rank_state_upcoming))).append("\n\n");
        String requirement = level.optString("requirement", "");
        if (!TextUtils.isEmpty(requirement)) {
            body.append(getString(R.string.profile_level_detail_requirement, requirement)).append("\n\n");
        }
        String reward = level.optString("reward", "");
        if (!TextUtils.isEmpty(reward)) {
            body.append(getString(R.string.profile_level_detail_reward, reward)).append("\n\n");
        }
        int xpRemaining = level.optInt("xp_remaining", 0);
        if (xpRemaining > 0) {
            body.append(getString(R.string.profile_level_detail_xp_remaining, xpRemaining)).append("\n\n");
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
                .setTitle(getString(R.string.profile_level_detail_title, levelNum, title))
                .setMessage(body.toString().trim())
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }

    private void bindAchievementsEntry(JSONArray badges) {
        if (achievementsEntry == null || achievementsSummary == null) {
            return;
        }
        if (badges == null || badges.length() == 0) {
            achievementsEntry.setVisibility(View.GONE);
            badgesSnapshot = new JSONArray();
            return;
        }
        badgesSnapshot = badges;
        achievementsEntry.setVisibility(View.VISIBLE);
        int earned = 0;
        for (int i = 0; i < badges.length(); i++) {
            JSONObject row = badges.optJSONObject(i);
            if (row != null && row.optBoolean("earned", false)) {
                earned++;
            }
        }
        achievementsSummary.setText(getString(
                R.string.profile_achievements_summary,
                earned,
                badges.length()));
    }

    private void addEmptyScoresMessage() {
        TextView empty = new TextView(this);
        empty.setText(R.string.profile_no_scores);
        empty.setTextAppearance(R.style.TextAppearance_QuantumRealm_Subtitle);
        scoresContainer.addView(empty);
    }

    private void addSubjectSection(LayoutInflater inflater, JSONObject subject) throws Exception {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(getResources().getDimension(R.dimen.game_card_radius));
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.game_surface));
        card.setContentPadding(24, 24, 24, 24);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.bottomMargin = (int) getResources().getDimension(R.dimen.game_card_margin);
        card.setLayoutParams(cardParams);

        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);

        TextView subjectHeader = new TextView(this);
        subjectHeader.setText(subject.getString("subject_name"));
        subjectHeader.setTextAppearance(R.style.TextAppearance_QuantumRealm_Title);
        subjectHeader.setTextSize(18f);
        inner.addView(subjectHeader);

        JSONArray modules = subject.optJSONArray("module_quizzes");
        if (modules != null) {
            for (int m = 0; m < modules.length(); m++) {
                JSONObject mod = modules.getJSONObject(m);
                inner.addView(inflateScoreRow(
                        inflater,
                        mod.getString("module_name"),
                        mod.getInt("score"),
                        mod.getInt("total"),
                        mod.getInt("percent"),
                        mod.getBoolean("passed"),
                        false
                ));
            }
        }

        JSONObject finalQuiz = subject.optJSONObject("final_quiz");
        if (finalQuiz != null) {
            inner.addView(inflateScoreRow(
                    inflater,
                    getString(R.string.profile_final_quiz_label),
                    finalQuiz.getInt("score"),
                    finalQuiz.getInt("total"),
                    finalQuiz.getInt("percent"),
                    finalQuiz.getBoolean("passed"),
                    true
            ));
        }

        if ((modules == null || modules.length() == 0) && finalQuiz == null) {
            TextView none = new TextView(this);
            none.setText(R.string.profile_subject_no_scores);
            none.setTextAppearance(R.style.TextAppearance_QuantumRealm_Subtitle);
            none.setPadding(0, 8, 0, 0);
            inner.addView(none);
        }

        card.addView(inner);
        scoresContainer.addView(card);
    }

    private View inflateScoreRow(
            LayoutInflater inflater,
            String title,
            int score,
            int total,
            int percent,
            boolean passed,
            boolean isFinal
    ) {
        View row = inflater.inflate(R.layout.item_profile_score_row, scoresContainer, false);
        TextView titleView = row.findViewById(R.id.scoreRowTitle);
        TextView detailView = row.findViewById(R.id.scoreRowDetail);

        titleView.setText(title);
        String status = passed
                ? getString(R.string.profile_quiz_passed)
                : getString(R.string.profile_quiz_not_passed);
        detailView.setText(getString(R.string.profile_score_format, score, total, percent, status));
        if (isFinal) {
            titleView.setTextColor(ContextCompat.getColor(this, R.color.game_accent));
        }
        if (passed) {
            detailView.setTextColor(ContextCompat.getColor(this, R.color.game_success));
        }

        return row;
    }
}
