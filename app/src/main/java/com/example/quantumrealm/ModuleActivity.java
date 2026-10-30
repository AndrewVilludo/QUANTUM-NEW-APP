package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

public class ModuleActivity extends LogoutAppCompatActivity {

    private LinearLayout moduleContainer;
    private MaterialButton continueButton;
    private JSONArray lessonsCache;
    private int moduleId;
    private String moduleName;
    private int subjectId;
    private Integer currentLessonIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);

        TextView moduleTitleTextView = findViewById(R.id.moduleTitleTextView);
        moduleContainer = findViewById(R.id.moduleContainer);
        continueButton = findViewById(R.id.moduleContinueButton);

        moduleId = getIntent().getIntExtra("course_id", -1);
        moduleName = getIntent().getStringExtra("course_name");
        subjectId = getIntent().getIntExtra("subject_id", -1);

        if (moduleId == -1 || subjectId == -1) {
            FeedbackHelper.showSnackbar(moduleContainer, "Invalid module");
            finish();
            return;
        }

        moduleTitleTextView.setText(moduleName);
        continueButton.setOnClickListener(v -> continueCurrentQuest());
        loadModuleProgress(false);
    }

    private void loadModuleProgress(boolean autoJumpIfRequested) {
        String url = ApiConfig.api("module-progress/" + moduleId);

        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (!response.optBoolean("success", false)) {
                            FeedbackHelper.showSnackbar(moduleContainer, response.optString("message", "Module locked"));
                            finish();
                            return;
                        }

                        lessonsCache = response.getJSONArray("data");
                        if (lessonsCache.length() == 0) {
                            FeedbackHelper.showSnackbar(moduleContainer, "No lessons in this module.");
                            finish();
                            return;
                        }

                        JSONObject moduleMeta = response.optJSONObject("module");
                        currentLessonIndex = response.isNull("current_lesson_index")
                                ? null
                                : response.optInt("current_lesson_index", 0);

                        renderLessonPath(lessonsCache, moduleMeta);

                        if (autoJumpIfRequested && getIntent().getBooleanExtra("auto_continue", false)) {
                            continueCurrentQuest();
                            return;
                        }

                        if (currentLessonIndex != null) {
                            continueButton.setVisibility(View.VISIBLE);
                        } else {
                            boolean quizReady = moduleMeta != null && moduleMeta.optBoolean("module_quiz_ready", false);
                            continueButton.setVisibility(quizReady ? View.VISIBLE : View.GONE);
                            if (quizReady) {
                                continueButton.setText(R.string.module_quiz_subtitle);
                            }
                        }
                    } catch (Exception e) {
                        FeedbackHelper.showSnackbar(moduleContainer, "Error loading module lessons");
                        finish();
                    }
                },
                error -> {
                    FeedbackHelper.showSnackbar(moduleContainer, "Failed to load module progress");
                    finish();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }

    private void renderLessonPath(JSONArray lessons, JSONObject moduleMeta) throws Exception {
        moduleContainer.removeAllViews();
        boolean quizReady = moduleMeta != null && moduleMeta.optBoolean("module_quiz_ready", false);
        boolean moduleComplete = moduleMeta != null && moduleMeta.optBoolean("is_completed", false);

        for (int i = 0; i < lessons.length(); i++) {
            JSONObject lesson = lessons.getJSONObject(i);
            String lessonName = lesson.optString("name", "Lesson " + (i + 1));
            boolean completed = lesson.optBoolean("is_completed", false);
            boolean canAccess = lesson.optBoolean("can_access", false);
            boolean locked = !completed && !canAccess;
            boolean isCurrent = currentLessonIndex != null && currentLessonIndex == i;

            String subtitle = buildLessonPathSubtitle(lesson, completed, isCurrent, locked);

            int index = i;
            moduleContainer.addView(UiHelper.inflatePathCard(
                    this,
                    moduleContainer,
                    lessonName,
                    subtitle,
                    completed ? "✅" : UiHelper.emojiForIndex(i),
                    locked,
                    completed,
                    isCurrent,
                    () -> openLesson(lesson, index)
            ));
        }

        if (quizReady && !moduleComplete) {
            moduleContainer.addView(UiHelper.inflatePathBossCard(
                    this,
                    moduleContainer,
                    getString(R.string.module_quiz_title, moduleName),
                    () -> startActivity(ModuleFlowHelper.moduleQuizIntent(this, moduleId, moduleName, subjectId))
            ));
        }
    }

    private String buildLessonPathSubtitle(JSONObject lesson, boolean completed, boolean isCurrent, boolean locked) {
        if (locked) {
            return getString(R.string.lesson_status_locked);
        }
        String blurb = lesson.optString("map_blurb", "").trim();
        if (completed) {
            if (!blurb.isEmpty()) {
                return blurb;
            }
            return getString(R.string.lesson_status_completed);
        }
        if (isCurrent) {
            if (!blurb.isEmpty()) {
                return getString(R.string.lesson_status_current) + " · " + blurb;
            }
            return getString(R.string.lesson_status_current);
        }
        if (!blurb.isEmpty()) {
            return blurb;
        }
        return getString(R.string.module_ready);
    }

    private void openLesson(JSONObject lesson, int index) {
        try {
            if (!lesson.optBoolean("can_access", false) && !lesson.optBoolean("is_completed", false)) {
                FeedbackHelper.showSnackbar(moduleContainer, R.string.lesson_status_locked);
                return;
            }
            Intent intent = ModuleFlowHelper.slideshowIntent(this, lesson, index, moduleId, moduleName, subjectId);
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        } catch (Exception e) {
            FeedbackHelper.showSnackbar(moduleContainer, "Could not open lesson");
        }
    }

    private void continueCurrentQuest() {
        try {
            if (currentLessonIndex != null && lessonsCache != null) {
                JSONObject lesson = lessonsCache.getJSONObject(currentLessonIndex);
                Intent intent = ModuleFlowHelper.slideshowIntent(
                        this, lesson, currentLessonIndex, moduleId, moduleName, subjectId);
                intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                return;
            }
            if (lessonsCache != null) {
                Intent gate = ModuleFlowHelper.questContinueIntent(this, moduleId, moduleName, subjectId);
                gate.putExtra(QuestContinueActivity.EXTRA_MODE, QuestContinueActivity.MODE_QUIZ_GATE);
                startActivity(gate);
            }
        } catch (Exception e) {
            FeedbackHelper.showSnackbar(moduleContainer, "Nothing to continue yet");
        }
    }
}
