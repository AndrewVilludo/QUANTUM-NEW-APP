package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
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
    private View modeChoiceRow;
    private JSONArray moduleLessonsCache;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson_activity);

        activityContainer = findViewById(R.id.activityContainer);
        modeChoiceRow = findViewById(R.id.modeChoiceRow);
        TextView title = findViewById(R.id.activityLessonTitle);
        MaterialButton novice = findViewById(R.id.modeNoviceButton);
        MaterialButton quiz = findViewById(R.id.modeQuizButton);

        try {
            lesson = new JSONObject(getIntent().getStringExtra(ModuleFlowHelper.EXTRA_LESSON));
            lessonIndex = getIntent().getIntExtra(ModuleFlowHelper.EXTRA_LESSON_INDEX, 0);
            moduleId = getIntent().getIntExtra(ModuleFlowHelper.EXTRA_MODULE_ID, -1);
            moduleName = getIntent().getStringExtra(ModuleFlowHelper.EXTRA_MODULE_NAME);
            subjectId = getIntent().getIntExtra(ModuleFlowHelper.EXTRA_SUBJECT_ID, -1);
            title.setText(lesson.optString("name", "Lesson activity"));
        } catch (Exception e) {
            Toast.makeText(this, "Invalid lesson", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        JSONArray activity = lesson.optJSONArray("activity");
        boolean hasNovice = activity != null && activity.length() > 0;

        novice.setOnClickListener(v -> {
            if (!hasNovice) {
                Toast.makeText(this, "Novice activity not available for this lesson.", Toast.LENGTH_SHORT).show();
                return;
            }
            showNoviceQuestion(activity.optJSONObject(0));
        });

        quiz.setOnClickListener(v -> loadQuizModeQuestion());

        if (!hasNovice) {
            novice.setEnabled(false);
        }
    }

    private void showNoviceQuestion(JSONObject activityItem) {
        modeChoiceRow.setVisibility(View.GONE);
        activityContainer.setVisibility(View.VISIBLE);
        activityContainer.removeAllViews();

        MaterialCardView card = buildCard();
        LinearLayout inner = (LinearLayout) card.getChildAt(0);

        TextView question = new TextView(this);
        question.setText(activityItem.optString("question", "Question"));
        question.setTextAppearance(R.style.TextAppearance_QuantumRealm_Title);
        inner.addView(question);

        EditText input = new EditText(this);
        input.setHint("Type your answer");
        input.setBackgroundResource(R.drawable.input_field_game);
        input.setPadding(48, 36, 48, 36);
        inner.addView(input);

        MaterialButton submit = primaryButton("Submit answer");
        submit.setOnClickListener(v -> submitNovice(input.getText().toString().trim()));
        inner.addView(submit);

        activityContainer.addView(card);
    }

    private void loadQuizModeQuestion() {
        int lessonId = lesson.optInt("id", -1);
        if (lessonId == -1) {
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                ApiConfig.api("lesson-quizzes/" + lessonId),
                null,
                response -> {
                    try {
                        JSONArray data = response.getJSONArray("data");
                        if (data.length() == 0) {
                            Toast.makeText(this, "Quiz mode not available for this lesson.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        showQuizQuestion(data.getJSONObject(0));
                    } catch (Exception e) {
                        Toast.makeText(this, "Could not load quiz question", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> Toast.makeText(this, "Failed to load quiz question", Toast.LENGTH_SHORT).show()
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void showQuizQuestion(JSONObject quiz) {
        modeChoiceRow.setVisibility(View.GONE);
        activityContainer.setVisibility(View.VISIBLE);
        activityContainer.removeAllViews();

        MaterialCardView card = buildCard();
        LinearLayout inner = (LinearLayout) card.getChildAt(0);

        TextView question = new TextView(this);
        question.setText(quiz.optString("question", "Question"));
        question.setTextAppearance(R.style.TextAppearance_QuantumRealm_Title);
        inner.addView(question);

        RadioGroup group = new RadioGroup(this);
        group.setOrientation(LinearLayout.VERTICAL);
        addQuizOption(group, quiz.optString("answer1"));
        addQuizOption(group, quiz.optString("answer2"));
        addQuizOption(group, quiz.optString("answer3"));
        addQuizOption(group, quiz.optString("answer4"));
        inner.addView(group);

        MaterialButton submit = primaryButton("Submit answer");
        submit.setOnClickListener(v -> {
            int checked = group.getCheckedRadioButtonId();
            if (checked == -1) {
                Toast.makeText(this, "Pick an answer", Toast.LENGTH_SHORT).show();
                return;
            }
            RadioButton selected = findViewById(checked);
            submitQuizMode(quiz.optInt("id"), selected.getText().toString());
        });
        inner.addView(submit);

        activityContainer.addView(card);
    }

    private void addQuizOption(RadioGroup group, String text) {
        RadioButton button = new RadioButton(this);
        button.setText(text);
        button.setTextColor(ContextCompat.getColor(this, R.color.game_text_primary));
        button.setBackgroundResource(R.drawable.quiz_option_background);
        button.setPadding(24, 24, 24, 24);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.bottomMargin = 12;
        button.setLayoutParams(params);
        group.addView(button);
    }

    private void submitNovice(String answer) {
        if (TextUtils.isEmpty(answer)) {
            Toast.makeText(this, "Enter an answer", Toast.LENGTH_SHORT).show();
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

    private void submitQuizMode(int quizId, String selected) {
        try {
            JSONObject body = new JSONObject();
            body.put("lesson_id", lesson.getInt("id"));
            body.put("quiz_id", quizId);
            body.put("selected", selected);
            postActivity(ApiConfig.api("lesson-activities/quiz"), body);
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
                        Toast.makeText(this, "Correct! Moving on…", Toast.LENGTH_SHORT).show();
                        refreshModuleLessonsAndAdvance();
                    } else {
                        Toast.makeText(this, response.optString("message", "Incorrect — review the lesson."), Toast.LENGTH_LONG).show();
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
