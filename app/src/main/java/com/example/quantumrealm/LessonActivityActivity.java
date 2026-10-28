package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
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

public class LessonActivityActivity extends LogoutAppCompatActivity {

    private JSONObject lesson;
    private int lessonIndex;
    private int moduleId;
    private String moduleName;
    private int subjectId;

    private LinearLayout activityContainer;
    private JSONArray moduleLessonsCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_activity);

        activityContainer = findViewById(R.id.activityContainer);
        TextView title = findViewById(R.id.activityLessonTitle);
        TextView subtitle = findViewById(R.id.activityLessonSubtitle);

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

        activityContainer.setVisibility(View.VISIBLE);

        JSONArray activity = lesson.optJSONArray("activity");
        if (activity != null && activity.length() > 0) {
            showNoviceQuestion(activity.optJSONObject(0));
        } else {
            Toast.makeText(this, R.string.lesson_no_activity, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void showNoviceQuestion(JSONObject activityItem) {
        activityContainer.removeAllViews();

        MaterialCardView card = buildCard();
        LinearLayout inner = (LinearLayout) card.getChildAt(0);

        TextView badge = new TextView(this);
        badge.setText(R.string.lesson_challenge_badge);
        badge.setTextColor(ContextCompat.getColor(this, R.color.game_accent));
        inner.addView(badge);

        TextView question = new TextView(this);
        question.setText(activityItem.optString("question", "Question"));
        question.setTextAppearance(R.style.TextAppearance_QuantumRealm_Title);
        question.setPadding(0, 16, 0, 0);
        inner.addView(question);

        EditText input = new EditText(this);
        input.setHint(R.string.lesson_answer_hint);
        input.setBackgroundResource(R.drawable.input_field_game);
        input.setPadding(48, 36, 48, 36);
        LinearLayout.LayoutParams inputParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        inputParams.topMargin = 20;
        input.setLayoutParams(inputParams);
        inner.addView(input);

        MaterialButton submit = primaryButton(getString(R.string.lesson_submit_answer));
        submit.setOnClickListener(v -> submitNovice(input.getText().toString().trim()));
        inner.addView(submit);

        activityContainer.addView(card);
        input.requestFocus();
    }

    private void submitNovice(String answer) {
        if (TextUtils.isEmpty(answer)) {
            Toast.makeText(this, R.string.lesson_enter_answer, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            JSONObject body = new JSONObject();
            body.put("lesson_id", lesson.getInt("id"));
            body.put("answer", answer);
            postActivity(ApiConfig.api("lesson-activities/novice"), body);
        } catch (Exception e) {
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
                    boolean correct = response.optBoolean("correct", false);
                    if (correct) {
                        Toast.makeText(this, R.string.lesson_correct, Toast.LENGTH_SHORT).show();
                        refreshModuleLessonsAndAdvance();
                    } else {
                        Toast.makeText(this, response.optString("message", getString(R.string.lesson_incorrect)), Toast.LENGTH_LONG).show();
                        returnToLessonSlides();
                    }
                },
                error -> Toast.makeText(this, "Could not submit answer", Toast.LENGTH_SHORT).show()
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

    private MaterialCardView buildCard() {
        MaterialCardView card = new MaterialCardView(this);
        card.setRadius(getResources().getDimension(R.dimen.game_card_radius));
        card.setCardBackgroundColor(ContextCompat.getColor(this, R.color.game_surface));
        card.setContentPadding(32, 32, 32, 32);
        LinearLayout inner = new LinearLayout(this);
        inner.setOrientation(LinearLayout.VERTICAL);
        card.addView(inner);
        return card;
    }

    private MaterialButton primaryButton(String text) {
        MaterialButton button = new MaterialButton(this, null, R.style.Widget_QuantumRealm_Button_Primary);
        button.setText(text);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = 24;
        button.setLayoutParams(params);
        return button;
    }
}
