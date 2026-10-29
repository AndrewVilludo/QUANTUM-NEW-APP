package com.example.quantumrealm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

public class CelebrationActivity extends LogoutAppCompatActivity {

    public static final String EXTRA_TITLE = "title";
    public static final String EXTRA_BODY = "body";
    public static final String EXTRA_VARIANT = "variant";
    public static final String EXTRA_ENGAGEMENT_JSON = "engagement_json";
    public static final String EXTRA_REVIEW_BODY = "review_body";
    public static final String EXTRA_PRIMARY_LABEL = "primary_label";
    public static final String EXTRA_SHOW_CERTIFICATE = "show_certificate";
    public static final String EXTRA_STUDENT_NAME = "student_name";

    public static final String VARIANT_XP = "xp";
    public static final String VARIANT_LEVEL_UP = "level_up";
    public static final String VARIANT_QUIZ_PASS = "quiz_pass";
    public static final String VARIANT_GRADUATE = "graduate";
    public static final String VARIANT_BADGE = "badge";
    public static final String VARIANT_YEAR_COMPLETE = "year_complete";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_celebration);

        LottieAnimationView lottie = findViewById(R.id.celebrationLottie);
        TextView titleView = findViewById(R.id.celebrationTitle);
        TextView rankChip = findViewById(R.id.celebrationRankChip);
        ProgressBar rankProgress = findViewById(R.id.celebrationRankProgress);
        TextView xpLine = findViewById(R.id.celebrationXpLine);
        TextView bodyView = findViewById(R.id.celebrationBody);
        MaterialButton primary = findViewById(R.id.celebrationPrimaryButton);
        MaterialButton secondary = findViewById(R.id.celebrationSecondaryButton);
        MaterialButton certificate = findViewById(R.id.celebrationCertificateButton);

        String title = getIntent().getStringExtra(EXTRA_TITLE);
        String body = getIntent().getStringExtra(EXTRA_BODY);
        String variant = getIntent().getStringExtra(EXTRA_VARIANT);
        if (variant == null) {
            variant = VARIANT_XP;
        }

        JSONObject engagement = parseEngagement(getIntent().getStringExtra(EXTRA_ENGAGEMENT_JSON));
        if (engagement != null && !getIntent().hasExtra(EXTRA_VARIANT)) {
            variant = EngagementUiHelper.pickCelebrationVariant(engagement, variant);
        }

        titleView.setText(title != null ? title : getString(R.string.lesson_correct));
        bodyView.setText(body != null ? body : "");

        lottie.setAnimation(EngagementUiHelper.lottieResForVariant(variant));
        lottie.playAnimation();

        CelebrationEffects.playForVariant(this, variant);

        bindEngagementUi(engagement, variant, rankChip, rankProgress, xpLine);

        String primaryLabel = getIntent().getStringExtra(EXTRA_PRIMARY_LABEL);
        if (!TextUtils.isEmpty(primaryLabel)) {
            primary.setText(primaryLabel);
        }

        String reviewBody = getIntent().getStringExtra(EXTRA_REVIEW_BODY);
        if (!TextUtils.isEmpty(reviewBody)) {
            secondary.setVisibility(View.VISIBLE);
            secondary.setOnClickListener(v -> new AlertDialog.Builder(this)
                    .setTitle(R.string.quiz_review_title)
                    .setMessage(reviewBody)
                    .setPositiveButton(android.R.string.ok, null)
                    .show());
        }

        boolean showCert = getIntent().getBooleanExtra(EXTRA_SHOW_CERTIFICATE, false);
        if (showCert) {
            certificate.setVisibility(View.VISIBLE);
            certificate.setOnClickListener(v -> {
                Intent cert = GraduationActivity.intent(this);
                String name = getIntent().getStringExtra(EXTRA_STUDENT_NAME);
                if (!TextUtils.isEmpty(name)) {
                    cert.putExtra("student_name", name);
                }
                startActivity(cert);
            });
        }

        primary.setOnClickListener(v -> finishWithDismiss());
    }

    @Override
    public void onBackPressed() {
        finishWithDismiss();
    }

    private void finishWithDismiss() {
        FeedbackHelper.notifyCelebrationDismissed();
        finish();
    }

    private static JSONObject parseEngagement(String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            return new JSONObject(json);
        } catch (Exception e) {
            return null;
        }
    }

    private void bindEngagementUi(
            JSONObject engagement,
            String variant,
            TextView rankChip,
            ProgressBar rankProgress,
            TextView xpLine
    ) {
        if (engagement == null) {
            return;
        }
        int xpAwarded = engagement.optInt("xp_awarded", 0);
        if (xpAwarded > 0) {
            xpLine.setVisibility(View.VISIBLE);
            xpLine.setText(getString(R.string.engagement_xp_earned, xpAwarded));
        }

        JSONObject rank = engagement.optJSONObject("rank");
        if (rank != null && (VARIANT_LEVEL_UP.equals(variant) || engagement.optBoolean("leveled_up", false))) {
            rankChip.setVisibility(View.VISIBLE);
            rankChip.setText(getString(
                    R.string.profile_rank_format,
                    rank.optString("title", ""),
                    rank.optInt("level", 1)));
            int inLevel = rank.optInt("xp_in_level", 0);
            int forNext = Math.max(1, rank.optInt("xp_for_next_level", 1));
            rankProgress.setVisibility(View.VISIBLE);
            rankProgress.setProgress(0);
            rankProgress.post(() -> rankProgress.setProgress(inLevel * 100 / forNext));
        }
    }

    public static Intent intent(
            Context context,
            String title,
            String body,
            String variant,
            JSONObject engagement
    ) {
        Intent intent = new Intent(context, CelebrationActivity.class);
        intent.putExtra(EXTRA_TITLE, title);
        intent.putExtra(EXTRA_BODY, body);
        intent.putExtra(EXTRA_VARIANT, variant);
        if (engagement != null) {
            intent.putExtra(EXTRA_ENGAGEMENT_JSON, engagement.toString());
        }
        return intent;
    }
}
