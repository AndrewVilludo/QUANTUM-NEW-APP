package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class StudyJourney extends LogoutAppCompatActivity {

    private ViewPager2 onboardingPager;
    private ProgressBar onboardingProgressBar;
    private LinearLayout onboardingDotIndicator;
    private MaterialButton primaryButton;
    private MaterialButton skipButton;

    private List<OnboardingSlideAdapter.Slide> slides = new ArrayList<>();

    @Override
    protected boolean showNavigateUp() {
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        onboardingPager = findViewById(R.id.onboardingPager);
        onboardingProgressBar = findViewById(R.id.onboardingProgressBar);
        onboardingDotIndicator = findViewById(R.id.onboardingDotIndicator);
        primaryButton = findViewById(R.id.onboardingPrimaryButton);
        skipButton = findViewById(R.id.onboardingSkipButton);

        primaryButton.setOnClickListener(v -> onPrimaryAction());
        skipButton.setOnClickListener(v -> goToGalaxy());

        buildSlides(AuthHelper.getUserName(this));
        maybeRefreshNameFromApi();
    }

    private void maybeRefreshNameFromApi() {
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.GET,
                ApiConfig.api("profile"),
                null,
                response -> {
                    try {
                        JSONObject user = response.optJSONObject("user");
                        if (user != null) {
                            String name = user.optString("name", "");
                            if (!name.isEmpty()) {
                                AuthHelper.saveUserName(this, name);
                                rebuildFirstSlide(name);
                            }
                        }
                    } catch (Exception ignored) {
                    }
                },
                error -> {
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void buildSlides(String studentName) {
        slides = new ArrayList<>(Arrays.asList(
                new OnboardingSlideAdapter.Slide(
                        "👋",
                        getString(R.string.onboarding_welcome_title, studentName),
                        getString(R.string.onboarding_welcome_body)),
                new OnboardingSlideAdapter.Slide(
                        "📚",
                        getString(R.string.onboarding_lessons_title),
                        getString(R.string.onboarding_lessons_body)),
                new OnboardingSlideAdapter.Slide(
                        "📝",
                        getString(R.string.onboarding_quiz_title),
                        getString(R.string.onboarding_quiz_body)),
                new OnboardingSlideAdapter.Slide(
                        "🚀",
                        getString(R.string.onboarding_launch_title),
                        getString(R.string.onboarding_launch_body))
        ));
        bindPager();
    }

    private void rebuildFirstSlide(String studentName) {
        if (slides.isEmpty()) {
            return;
        }
        slides.set(0, new OnboardingSlideAdapter.Slide(
                "👋",
                getString(R.string.onboarding_welcome_title, studentName),
                getString(R.string.onboarding_welcome_body)));
        onboardingPager.getAdapter().notifyItemChanged(0);
    }

    private void bindPager() {
        OnboardingSlideAdapter adapter = new OnboardingSlideAdapter(slides);
        onboardingPager.setAdapter(adapter);
        onboardingPager.setOffscreenPageLimit(1);
        onboardingPager.setPageTransformer(new MarginPageTransformer(24));
        buildDots();
        onboardingPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                updateChrome(position);
            }
        });
        updateChrome(0);
    }

    private void buildDots() {
        onboardingDotIndicator.removeAllViews();
        float density = getResources().getDisplayMetrics().density;
        int margin = (int) (6 * density);
        for (int i = 0; i < slides.size(); i++) {
            View dot = new View(this);
            int size = (int) (10 * density);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
            params.setMargins(margin, 0, margin, 0);
            dot.setLayoutParams(params);
            dot.setBackgroundResource(R.drawable.lesson_dot_inactive);
            onboardingDotIndicator.addView(dot);
        }
    }

    private void updateDots(int index) {
        for (int i = 0; i < onboardingDotIndicator.getChildCount(); i++) {
            onboardingDotIndicator.getChildAt(i).setBackgroundResource(
                    i == index ? R.drawable.lesson_dot_active : R.drawable.lesson_dot_inactive);
        }
    }

    private void updateChrome(int index) {
        int total = slides.size();
        int step = index + 1;
        onboardingProgressBar.setProgress(total == 0 ? 0 : (step * 100 / total));
        updateDots(index);
        boolean last = index >= total - 1;
        primaryButton.setText(last ? R.string.onboarding_enter_galaxy : R.string.onboarding_continue);
        skipButton.setVisibility(last ? View.GONE : View.VISIBLE);
    }

    private void onPrimaryAction() {
        int index = onboardingPager.getCurrentItem();
        if (index >= slides.size() - 1) {
            goToGalaxy();
        } else {
            onboardingPager.setCurrentItem(index + 1, true);
        }
    }

    private void goToGalaxy() {
        startActivity(new Intent(this, Journey.class));
        finish();
    }
}
