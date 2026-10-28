package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

public class LessonActivityActivity extends LogoutAppCompatActivity {

    private JSONObject lesson;
    private int lessonIndex;
    private int moduleId;
    private String moduleName;
    private int subjectId;

    private JSONArray moduleLessonsCache;

    private TextView challengeTitle;
    private TextView difficultyBadge;
    private TextView questionText;
    private EditText answerInput;
    private MaterialButton submitButton;
    private ProgressBar submitProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_activity);

        TextView title = findViewById(R.id.activityLessonTitle);
        TextView subtitle = findViewById(R.id.activityLessonSubtitle);
        challengeTitle = findViewById(R.id.activityChallengeTitle);
        difficultyBadge = findViewById(R.id.activityDifficultyBadge);
        questionText = findViewById(R.id.activityQuestionText);
        answerInput = findViewById(R.id.activityAnswerInput);
        submitButton = findViewById(R.id.activitySubmitButton);
        submitProgress = findViewById(R.id.activitySubmitProgress);

        try {
            lesson = new JSONObject(getIntent().getStringExtra(ModuleFlowHelper.EXTRA_LESSON));
            lessonIndex = getIntent().getIntExtra(ModuleFlowHelper.EXTRA_LESSON_INDEX, 0);
            moduleId = getIntent().getIntExtra(ModuleFlowHelper.EXTRA_MODULE_ID, -1);
            moduleName = getIntent().getStringExtra(ModuleFlowHelper.EXTRA_MODULE_NAME);
            subjectId = getIntent().getIntExtra(ModuleFlowHelper.EXTRA_SUBJECT_ID, -1);
            title.setText(lesson.optString("name", "Lesson challenge"));
            subtitle.setText(R.string.lesson_challenge_subtitle);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid lesson", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        JSONArray activity = lesson.optJSONArray("activity");
        if (activity != null && activity.length() > 0) {
            bindNoviceQuestion(activity.optJSONObject(0));
        } else {
            Toast.makeText(this, R.string.lesson_no_activity, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void bindNoviceQuestion(JSONObject activityItem) {
        if (activityItem == null) {
            return;
        }

        String activityTitle = activityItem.optString("title", "");
        if (TextUtils.isEmpty(activityTitle)) {
            activityTitle = getString(R.string.lesson_challenge_badge);
        }
        challengeTitle.setText(activityTitle);

        String difficulty = activityItem.optString("difficulty", "novice");
        difficultyBadge.setText(getString(R.string.lesson_activity_difficulty, capitalize(difficulty)));
        questionText.setText(activityItem.optString("question", "Question"));

        submitButton.setOnClickListener(v -> submitNovice(answerInput.getText().toString().trim()));
        answerInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                submitNovice(answerInput.getText().toString().trim());
                return true;
            }
            return false;
        });
        answerInput.requestFocus();
    }

    private static String capitalize(String value) {
        if (value == null || value.isEmpty()) {
            return value;
        }
        return value.substring(0, 1).toUpperCase() + value.substring(1).toLowerCase();
    }

    private void setSubmitting(boolean submitting) {
        submitButton.setEnabled(!submitting);
        submitButton.setText(submitting ? R.string.lesson_submit_checking : R.string.lesson_submit_answer_cta);
        submitProgress.setVisibility(submitting ? View.VISIBLE : View.GONE);
        answerInput.setEnabled(!submitting);
    }

    private void submitNovice(String answer) {
        if (TextUtils.isEmpty(answer)) {
            Toast.makeText(this, R.string.lesson_enter_answer, Toast.LENGTH_SHORT).show();
            answerInput.requestFocus();
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("lesson_id", lesson.getInt("id"));
            body.put("answer", answer);
            setSubmitting(true);
            postActivity(ApiConfig.api("lesson-activities/novice"), body);
        } catch (Exception e) {
            setSubmitting(false);
            Toast.makeText(this, "Error submitting answer", Toast.LENGTH_SHORT).show();
        }
    }

    private void postActivity(String url, JSONObject body) {
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.POST,
                url,
                body,
                response -> {
                    setSubmitting(false);
                    boolean correct = response.optBoolean("correct", false);
                    if (correct) {
                        Toast.makeText(this, R.string.lesson_correct, Toast.LENGTH_SHORT).show();
                        refreshModuleLessonsAndAdvance();
                    } else {
                        Toast.makeText(this, response.optString("message", getString(R.string.lesson_incorrect)), Toast.LENGTH_LONG).show();
                        returnToLessonSlides();
                    }
                },
                error -> {
                    setSubmitting(false);
                    Toast.makeText(this, "Could not submit answer", Toast.LENGTH_SHORT).show();
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void refreshModuleLessonsAndAdvance() {
        String url = ApiConfig.api("module-progress/" + moduleId);
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        moduleLessonsCache = response.getJSONArray("data");
                        ModuleFlowHelper.openNextAfterCompletion(
                                this, moduleId, moduleName, subjectId, moduleLessonsCache, lessonIndex);
                        finish();
                    } catch (Exception e) {
                        ModuleFlowHelper.backToCourse(this, subjectId);
                        finish();
                    }
                },
                error -> {
                    ModuleFlowHelper.backToCourse(this, subjectId);
                    finish();
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void returnToLessonSlides() {
        Intent intent = ModuleFlowHelper.slideshowIntent(this, lesson, lessonIndex, moduleId, moduleName, subjectId);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
