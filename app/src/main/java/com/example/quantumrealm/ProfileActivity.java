package com.example.quantumrealm;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProfileActivity extends LogoutAppCompatActivity {

    private LinearLayout scoresContainer;
    private TextView nameText;
    private TextView emailText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        nameText = findViewById(R.id.profileNameText);
        emailText = findViewById(R.id.profileEmailText);
        scoresContainer = findViewById(R.id.profileScoresContainer);
        MaterialButton logoutButton = findViewById(R.id.profileLogoutButton);

        logoutButton.setOnClickListener(v -> confirmLogout());
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
