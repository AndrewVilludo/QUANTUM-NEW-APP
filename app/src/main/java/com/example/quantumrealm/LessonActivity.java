package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

public class LessonActivity extends LogoutAppCompatActivity {

    private TextView lessonTitleTextView;
    private TextView lessonSlideProgress;
    private TextView lessonSlideContent;
    private MaterialButton lessonPrevButton;
    private MaterialButton lessonNextButton;
    private MaterialButton lessonContinueActivityButton;

    private JSONArray slides = new JSONArray();
    private int slideIndex = 0;

    private JSONObject lesson;
    private int lessonIndex;
    private int moduleId;
    private String moduleName;
    private int subjectId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        lessonTitleTextView = findViewById(R.id.lessonTitleTextView);
        lessonSlideProgress = findViewById(R.id.lessonSlideProgress);
        lessonSlideContent = findViewById(R.id.lessonSlideContent);
        lessonPrevButton = findViewById(R.id.lessonPrevButton);
        lessonNextButton = findViewById(R.id.lessonNextButton);
        lessonContinueActivityButton = findViewById(R.id.lessonContinueActivityButton);

        Intent intent = getIntent();
        try {
            lesson = new JSONObject(intent.getStringExtra(ModuleFlowHelper.EXTRA_LESSON));
            lessonIndex = intent.getIntExtra(ModuleFlowHelper.EXTRA_LESSON_INDEX, 0);
            moduleId = intent.getIntExtra(ModuleFlowHelper.EXTRA_MODULE_ID, -1);
            moduleName = intent.getStringExtra(ModuleFlowHelper.EXTRA_MODULE_NAME);
            subjectId = intent.getIntExtra(ModuleFlowHelper.EXTRA_SUBJECT_ID, -1);

            lessonTitleTextView.setText(lesson.optString("name", moduleName));
            slides = lesson.optJSONArray("data");
            if (slides == null) {
                slides = new JSONArray();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Invalid lesson data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        lessonPrevButton.setOnClickListener(v -> showSlide(slideIndex - 1));
        lessonNextButton.setOnClickListener(v -> {
            if (slideIndex >= slides.length() - 1) {
                openActivity();
            } else {
                showSlide(slideIndex + 1);
            }
        });
        lessonContinueActivityButton.setOnClickListener(v -> openActivity());

        if (slides.length() == 0) {
            lessonSlideContent.setText("No lesson content available yet.");
            lessonContinueActivityButton.setVisibility(View.VISIBLE);
            lessonNextButton.setEnabled(false);
        } else {
            showSlide(0);
        }
    }

    private void showSlide(int index) {
        if (slides.length() == 0) {
            return;
        }
        slideIndex = Math.max(0, Math.min(index, slides.length() - 1));

        try {
            JSONObject slide = slides.getJSONObject(slideIndex);
            String raw = slide.optString("content", "");
            lessonSlideContent.setText(raw.replace("**", "").replace("`", ""));
        } catch (Exception e) {
            lessonSlideContent.setText("");
        }

        lessonSlideProgress.setText("Slide " + (slideIndex + 1) + " of " + slides.length());
        lessonPrevButton.setEnabled(slideIndex > 0);

        boolean onLast = slideIndex >= slides.length() - 1;
        lessonContinueActivityButton.setVisibility(onLast ? View.VISIBLE : View.GONE);
        lessonNextButton.setText(onLast ? "Start activity" : "Next");
    }

    private void openActivity() {
        Intent intent = ModuleFlowHelper.activityIntent(this, lesson, lessonIndex, moduleId, moduleName, subjectId);
        startActivity(intent);
    }
}
