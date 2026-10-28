package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.material.button.MaterialButton;

public class QuestContinueActivity extends LogoutAppCompatActivity {

    public static final String EXTRA_MODE = "quest_mode";
    public static final String MODE_QUIZ_GATE = "quiz_gate";
    public static final String MODE_NEXT_LESSON = "next_lesson";

    public static final String EXTRA_NEXT_LESSON = "next_lesson_json";
    public static final String EXTRA_NEXT_LESSON_INDEX = "next_lesson_index";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_continue);

        TextView title = findViewById(R.id.questContinueTitle);
        TextView message = findViewById(R.id.questContinueMessage);
        MaterialButton primary = findViewById(R.id.questContinuePrimary);
        MaterialButton moduleMap = findViewById(R.id.questContinueModuleMap);
        MaterialButton subject = findViewById(R.id.questContinueSubject);

        Intent intent = getIntent();
        int moduleId = intent.getIntExtra(ModuleFlowHelper.EXTRA_MODULE_ID, -1);
        String moduleName = intent.getStringExtra(ModuleFlowHelper.EXTRA_MODULE_NAME);
        int subjectId = intent.getIntExtra(ModuleFlowHelper.EXTRA_SUBJECT_ID, -1);
        String mode = intent.getStringExtra(EXTRA_MODE);

        if (moduleId == -1 || subjectId == -1) {
            finish();
            return;
        }

        if (MODE_NEXT_LESSON.equals(mode)) {
            title.setText(R.string.quest_continue_next_title);
            message.setText(getString(R.string.quest_continue_next_message, moduleName));
            primary.setText(R.string.quest_continue_next_lesson);
            primary.setOnClickListener(v -> {
                try {
                    String lessonJson = intent.getStringExtra(EXTRA_NEXT_LESSON);
                    int nextIndex = intent.getIntExtra(EXTRA_NEXT_LESSON_INDEX, 0);
                    Intent next = ModuleFlowHelper.slideshowIntent(
                            this,
                            new org.json.JSONObject(lessonJson),
                            nextIndex,
                            moduleId,
                            moduleName,
                            subjectId);
                    next.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(next);
                    finish();
                } catch (Exception e) {
                    FeedbackHelper.showSnackbar(primary, R.string.quest_continue_error);
                }
            });
        } else {
            title.setText(R.string.quest_continue_quiz_title);
            message.setText(getString(R.string.quest_continue_quiz_message, moduleName));
            primary.setText(R.string.quest_continue_to_quiz);
            primary.setOnClickListener(v -> {
                Intent quiz = ModuleFlowHelper.moduleQuizIntent(this, moduleId, moduleName, subjectId);
                quiz.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(quiz);
                finish();
            });
        }

        moduleMap.setOnClickListener(v -> {
            Intent map = new Intent(this, ModuleActivity.class);
            map.putExtra("course_id", moduleId);
            map.putExtra("course_name", moduleName);
            map.putExtra("subject_id", subjectId);
            map.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(map);
            finish();
        });

        subject.setOnClickListener(v -> {
            ModuleFlowHelper.backToCourse(this, subjectId);
            finish();
        });
    }
}
