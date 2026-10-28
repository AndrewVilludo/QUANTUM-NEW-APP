package com.example.quantumrealm;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONObject;

public class LessonActivity extends LogoutAppCompatActivity {

    private TextView lessonTitleTextView;
    private TextView lessonModuleHint;
    private TextView lessonSwipeHint;
    private TextView lessonSlideProgress;
    private ProgressBar lessonSlideProgressBar;
    private ViewPager2 lessonSlidePager;
    private LinearLayout lessonDotIndicator;
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
        lessonModuleHint = findViewById(R.id.lessonModuleHint);
        lessonSwipeHint = findViewById(R.id.lessonSwipeHint);
        lessonSlideProgress = findViewById(R.id.lessonSlideProgress);
        lessonSlideProgressBar = findViewById(R.id.lessonSlideProgressBar);
        lessonSlidePager = findViewById(R.id.lessonSlidePager);
        lessonDotIndicator = findViewById(R.id.lessonDotIndicator);
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
            if (moduleName != null && !moduleName.isEmpty()) {
                lessonModuleHint.setVisibility(View.VISIBLE);
                lessonModuleHint.setText(getString(R.string.lesson_module_hint, moduleName));
            }

            slides = lesson.optJSONArray("data");
            if (slides == null) {
                slides = new JSONArray();
            }
            slides = normalizeSlidesClientSide(slides, lesson.optString("name", "Lesson"));
        } catch (Exception e) {
            Toast.makeText(this, "Invalid lesson data", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        LessonSlidePagerAdapter adapter = new LessonSlidePagerAdapter(slides);
        lessonSlidePager.setAdapter(adapter);
        lessonSlidePager.setOffscreenPageLimit(1);
        lessonSlidePager.setPageTransformer(new MarginPageTransformer(32));
        lessonSlidePager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                slideIndex = position;
                updateChrome();
            }
        });

        lessonPrevButton.setOnClickListener(v -> goToSlide(slideIndex - 1));
        lessonNextButton.setOnClickListener(v -> {
            if (slideIndex >= slides.length() - 1) {
                openActivity();
            } else {
                goToSlide(slideIndex + 1);
            }
        });
        lessonContinueActivityButton.setVisibility(View.GONE);

        buildDotIndicator();

        if (slides.length() == 0) {
            lessonSwipeHint.setText(R.string.lesson_no_content);
            lessonNextButton.setEnabled(false);
            lessonSlideProgress.setText("");
        } else {
            updateChrome();
        }
    }

    /** Fallback when API has not yet normalized slides (older cache). */
    private JSONArray normalizeSlidesClientSide(JSONArray raw, String lessonName) {
        if (raw.length() == 0) {
            return raw;
        }
        try {
            JSONObject first = raw.getJSONObject(0);
            if (first.has("step") && first.has("title")) {
                return raw;
            }
        } catch (Exception ignored) {
        }

        JSONArray out = new JSONArray();
        String[] emojis = {"📖", "💡", "🎯", "🔍", "✨", "🚀", "🧩", "⭐"};
        int total = raw.length();
        for (int i = 0; i < total; i++) {
            try {
                JSONObject slide = raw.getJSONObject(i);
                JSONObject row = new JSONObject();
                row.put("step", i + 1);
                row.put("total", total);
                row.put("emoji", emojis[i % emojis.length]);
                row.put("title", slide.optString("title", lessonName + " — Part " + (i + 1)));
                row.put("content", slide.optString("content", ""));
                out.put(row);
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    private void buildDotIndicator() {
        lessonDotIndicator.removeAllViews();
        int count = slides.length();
        if (count <= 1) {
            lessonDotIndicator.setVisibility(View.GONE);
            return;
        }
        lessonDotIndicator.setVisibility(View.VISIBLE);
        float density = getResources().getDisplayMetrics().density;
        int margin = (int) (6 * density);
        for (int i = 0; i < count; i++) {
            View dot = new View(this);
            int size = (int) (10 * density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.lesson_dot_inactive);
            lessonDotIndicator.addView(dot);
        }
    }

    private void updateDotIndicator() {
        for (int i = 0; i < lessonDotIndicator.getChildCount(); i++) {
            View dot = lessonDotIndicator.getChildAt(i);
            dot.setBackgroundResource(i == slideIndex
                    ? R.drawable.lesson_dot_active
                    : R.drawable.lesson_dot_inactive);
        }
    }

    private void goToSlide(int index) {
        if (slides.length() == 0) {
            return;
        }
        int target = Math.max(0, Math.min(index, slides.length() - 1));
        lessonSlidePager.setCurrentItem(target, true);
    }

    private void updateChrome() {
        int total = slides.length();
        if (total == 0) {
            return;
        }

        int step = slideIndex + 1;
        lessonSlideProgress.setText(getString(R.string.lesson_progress_label, step, total));
        lessonPrevButton.setEnabled(slideIndex > 0);

        boolean onLast = slideIndex >= total - 1;
        lessonNextButton.setText(onLast ? R.string.lesson_start_activity : R.string.lesson_next);
        lessonSwipeHint.setVisibility(slideIndex == 0 && total > 1 ? View.VISIBLE : View.GONE);

        int progress = (int) ((step / (float) total) * 100);
        animateProgress(progress);
        updateDotIndicator();
    }

    private void animateProgress(int target) {
        ObjectAnimator animator = ObjectAnimator.ofInt(
                lessonSlideProgressBar, "progress", lessonSlideProgressBar.getProgress(), target);
        animator.setDuration(280);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    private void openActivity() {
        Intent intent = ModuleFlowHelper.activityIntent(this, lesson, lessonIndex, moduleId, moduleName, subjectId);
        startActivity(intent);
    }
}
