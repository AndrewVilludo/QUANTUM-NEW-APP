package com.example.quantumrealm;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashSet;
import java.util.Set;

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
    private MaterialCardView microCheckCard;
    private TextView microCheckQuestion;
    private RadioGroup microCheckOptions;
    private MaterialButton microCheckButton;

    private JSONArray slides = new JSONArray();
    private int slideIndex = 0;
    private final Set<Integer> microChecksPassed = new HashSet<>();

    private JSONObject lesson;
    private int lessonIndex;
    private int moduleId;
    private String moduleName;
    private int subjectId;
    private int lessonId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        bindViews();
        applyLessonFromIntent();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        applyLessonFromIntent();
    }

    private void applyLessonFromIntent() {
        slideIndex = 0;
        microChecksPassed.clear();
        loadLessonFromIntent();
        setupPager();
    }

    private void bindViews() {
        lessonTitleTextView = findViewById(R.id.lessonTitleTextView);
        lessonModuleHint = findViewById(R.id.lessonModuleHint);
        lessonSwipeHint = findViewById(R.id.lessonSwipeHint);
        lessonSlideProgress = findViewById(R.id.lessonSlideProgress);
        lessonSlideProgressBar = findViewById(R.id.lessonSlideProgressBar);
        lessonSlidePager = findViewById(R.id.lessonSlidePager);
        lessonDotIndicator = findViewById(R.id.lessonDotIndicator);
        lessonPrevButton = findViewById(R.id.lessonPrevButton);
        lessonNextButton = findViewById(R.id.lessonNextButton);
        microCheckCard = findViewById(R.id.lessonMicroCheckCard);
        microCheckQuestion = findViewById(R.id.lessonMicroCheckQuestion);
        microCheckOptions = findViewById(R.id.lessonMicroCheckOptions);
        microCheckButton = findViewById(R.id.lessonMicroCheckButton);

        lessonPrevButton.setOnClickListener(v -> goToSlide(slideIndex - 1));
        lessonNextButton.setOnClickListener(v -> attemptAdvance());
        microCheckButton.setOnClickListener(v -> submitMicroCheck());
    }

    private void loadLessonFromIntent() {
        Intent intent = getIntent();
        lessonId = -1;
        try {
            lesson = new JSONObject(intent.getStringExtra(ModuleFlowHelper.EXTRA_LESSON));
            lessonIndex = intent.getIntExtra(ModuleFlowHelper.EXTRA_LESSON_INDEX, 0);
            moduleId = intent.getIntExtra(ModuleFlowHelper.EXTRA_MODULE_ID, -1);
            moduleName = intent.getStringExtra(ModuleFlowHelper.EXTRA_MODULE_NAME);
            subjectId = intent.getIntExtra(ModuleFlowHelper.EXTRA_SUBJECT_ID, -1);
            lessonId = lesson.optInt("id", -1);

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
            LessonSlideCache.save(this, lessonId, slides);
        } catch (Exception e) {
            JSONArray cached = LessonSlideCache.load(this, lessonId);
            if (cached != null) {
                slides = cached;
            } else {
                FeedbackHelper.showSnackbar(lessonSlidePager, "Invalid lesson data");
                finish();
            }
        }
    }

    private void setupPager() {
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

        buildDotIndicator();
        if (slides.length() == 0) {
            lessonSwipeHint.setText(R.string.lesson_no_content);
            lessonNextButton.setEnabled(false);
            lessonSlideProgress.setText("");
        } else {
            updateChrome();
        }
    }

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
                if (slide.has("micro_check")) {
                    row.put("micro_check", slide.getJSONObject("micro_check"));
                } else if (slide.has("check_question") && slide.has("check_answer")) {
                    JSONObject micro = new JSONObject();
                    micro.put("question", slide.getString("check_question"));
                    micro.put("correct", slide.getString("check_answer"));
                    JSONArray options = slide.optJSONArray("check_options");
                    if (options == null || options.length() == 0) {
                        options = new JSONArray();
                        options.put("Yes, continue");
                        options.put("Review this slide");
                    }
                    micro.put("options", options);
                    row.put("micro_check", micro);
                } else {
                    int step = i + 1;
                    if (total > 2 && step < total && step % 2 == 0) {
                        JSONObject micro = new JSONObject();
                        micro.put("question", "Quick check: did you understand this slide?");
                        JSONArray options = new JSONArray();
                        options.put("Yes, I got it");
                        options.put("Not yet — I'll re-read");
                        micro.put("options", options);
                        micro.put("correct", "Yes, I got it");
                        row.put("micro_check", micro);
                    }
                }
                out.put(row);
            } catch (Exception ignored) {
            }
        }
        return out;
    }

    private void attemptAdvance() {
        if (slideIndex >= slides.length() - 1) {
            openActivity();
            return;
        }
        if (needsMicroCheck(slideIndex) && !microChecksPassed.contains(slideIndex)) {
            FeedbackHelper.showSnackbar(lessonSlidePager, "Complete the quick check first");
            bindMicroCheckForSlide(slideIndex);
            return;
        }
        goToSlide(slideIndex + 1);
    }

    private boolean needsMicroCheck(int index) {
        try {
            JSONObject slide = slides.getJSONObject(index);
            return slide.has("micro_check");
        } catch (Exception e) {
            return false;
        }
    }

    private void bindMicroCheckForSlide(int index) {
        try {
            JSONObject slide = slides.getJSONObject(index);
            JSONObject check = slide.optJSONObject("micro_check");
            if (check == null) {
                microCheckCard.setVisibility(View.GONE);
                return;
            }
            microCheckCard.setVisibility(View.VISIBLE);
            microCheckQuestion.setText(check.optString("question", ""));
            microCheckOptions.removeAllViews();
            JSONArray options = check.optJSONArray("options");
            if (options != null) {
                for (int i = 0; i < options.length(); i++) {
                    RadioButton rb = new RadioButton(this);
                    rb.setText(options.getString(i));
                    rb.setId(View.generateViewId());
                    microCheckOptions.addView(rb);
                }
            }
        } catch (Exception e) {
            microCheckCard.setVisibility(View.GONE);
        }
    }

    private void submitMicroCheck() {
        int selectedId = microCheckOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            FeedbackHelper.showSnackbar(lessonSlidePager, "Pick an option");
            return;
        }
        RadioButton selected = findViewById(selectedId);
        if (lessonId <= 0) {
            FeedbackHelper.showSnackbar(lessonSlidePager, "Invalid lesson. Re-open from the module map.");
            return;
        }
        try {
            JSONObject slide = slides.getJSONObject(slideIndex);
            int slideStep = slide.optInt("step", slideIndex + 1);

            JSONObject body = new JSONObject();
            body.put("lesson_id", lessonId);
            body.put("slide_step", slideStep);
            body.put("selected", selected.getText().toString());

            AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                    this,
                    Request.Method.POST,
                    ApiConfig.api("lesson-activities/micro-check"),
                    body,
                    response -> {
                        boolean correct = response.optBoolean("correct", false);
                        if (correct) {
                            microChecksPassed.add(slideIndex);
                            microCheckCard.setVisibility(View.GONE);
                            FeedbackHelper.successHaptic(lessonSlidePager);
                            FeedbackHelper.showSnackbar(lessonSlidePager, response.optString("message", "Nice!"));
                        } else {
                            FeedbackHelper.errorHaptic(lessonSlidePager);
                            FeedbackHelper.showSnackbar(lessonSlidePager, response.optString("message", "Try again"));
                        }
                    },
                    error -> {
                        String message = "Could not verify answer";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            try {
                                JSONObject errBody = new JSONObject(new String(error.networkResponse.data));
                                message = errBody.optString("message", message);
                            } catch (Exception ignored) {
                            }
                        }
                        FeedbackHelper.showSnackbar(lessonSlidePager, message);
                    }
            );
            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            FeedbackHelper.showSnackbar(lessonSlidePager, "Error sending check");
        }
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
        bindMicroCheckForSlide(slideIndex);
        if (!needsMicroCheck(slideIndex) || microChecksPassed.contains(slideIndex)) {
            microCheckCard.setVisibility(View.GONE);
        }
    }

    private void animateProgress(int target) {
        ObjectAnimator animator = ObjectAnimator.ofInt(
                lessonSlideProgressBar, "progress", lessonSlideProgressBar.getProgress(), target);
        animator.setDuration(280);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.start();
    }

    private void openActivity() {
        startActivity(ModuleFlowHelper.activityIntent(this, lesson, lessonIndex, moduleId, moduleName, subjectId));
    }
}

