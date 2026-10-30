package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.navigation.NavigationView;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.json.JSONArray;
import org.json.JSONObject;

public class Journey extends LogoutAppCompatActivity {

    private LinearLayout buttonContainer;
    private TextView journeyStatusText;
    private SwipeRefreshLayout journeySwipeRefresh;
    private RequestQueue requestQueue;
    private boolean pendingYearCelebration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);
        setupAppDrawer();

        buttonContainer = findViewById(R.id.buttonContainer);
        journeyStatusText = findViewById(R.id.journeyStatusText);
        journeySwipeRefresh = findViewById(R.id.journeySwipeRefresh);
        requestQueue = Volley.newRequestQueue(this);
        journeySwipeRefresh.setOnRefreshListener(() -> {
            loadGalaxyYears();
            journeySwipeRefresh.setRefreshing(false);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        NavigationView navView = findViewById(R.id.app_nav_view);
        if (navView != null) {
            AppDrawerHelper.refreshHeader(this, navView);
        }
        if (AuthHelper.shouldSyncRankTitle(this, 60_000L)) {
            AuthHelper.refreshRankTitleFromServer(this, () -> {
                NavigationView nav = findViewById(R.id.app_nav_view);
                if (nav != null) {
                    AppDrawerHelper.refreshHeader(Journey.this, nav);
                }
            });
        }
        loadGalaxyYears();
    }

    private void showYearCompleteCelebrationIfNeeded() {
        pendingYearCelebration = false;
        FeedbackHelper.celebrate(
                this,
                getString(R.string.year_complete_celebration_title),
                getString(R.string.year_complete_celebration_body),
                CelebrationActivity.VARIANT_YEAR_COMPLETE,
                null,
                null,
                null);
    }

    private void loadGalaxyYears() {
        journeyStatusText.setVisibility(View.VISIBLE);
        journeyStatusText.setText(R.string.journey_loading);
        buttonContainer.removeAllViews();
        buttonContainer.setVisibility(View.GONE);

        if (!AuthHelper.isLoggedIn(this)) {
            buttonContainer.removeAllViews();
            buttonContainer.setVisibility(View.GONE);
            journeyStatusText.setVisibility(View.VISIBLE);
            journeyStatusText.setText(R.string.journey_login_required);
            Toast.makeText(this, R.string.journey_login_required, Toast.LENGTH_LONG).show();
            return;
        }

        fetchGalaxyYears();
    }

    private void fetchGalaxyYears() {
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.GET,
                ApiConfig.api("years/galaxy"),
                null,
                response -> {
                    if (!response.optBoolean("success", true)) {
                        showLoadError(response.optString("message", getString(R.string.journey_load_failed)));
                        return;
                    }
                    try {
                        renderYearCards(response.getJSONArray("data"));
                    } catch (Exception e) {
                        Log.e("Journey", "Parse galaxy years", e);
                        fetchPublicYearsFallback();
                    }
                },
                error -> {
                    Log.e("Journey", "Galaxy years request failed: " + error);
                    fetchPublicYearsFallback();
                }
        );
        requestQueue.add(request);
    }

    /** Fallback when galaxy endpoint is unavailable (older API or server issue). */
    private void fetchPublicYearsFallback() {
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.GET,
                ApiConfig.api("years"),
                null,
                response -> {
                    try {
                        JSONArray years = response.getJSONArray("data");
                        JSONArray enriched = new JSONArray();
                        for (int i = 0; i < years.length(); i++) {
                            JSONObject year = years.getJSONObject(i);
                            JSONObject row = new JSONObject();
                            row.put("id", year.getInt("id"));
                            row.put("name", year.getString("name"));
                            row.put("description", year.optString("description", ""));
                            row.put("is_unlocked", i == 0);
                            row.put("is_completed", false);
                            enriched.put(row);
                        }
                        renderYearCards(enriched);
                    } catch (Exception e) {
                        Log.e("Journey", "Parse public years", e);
                        showLoadError(getString(R.string.journey_load_failed));
                    }
                },
                error -> {
                    Log.e("Journey", "Public years request failed: " + error);
                    showLoadError(getString(R.string.journey_server_unreachable));
                }
        );
        requestQueue.add(request);
    }

    private void renderYearCards(JSONArray yearLevels) throws Exception {
        if (yearLevels.length() == 0) {
            showLoadError(getString(R.string.journey_no_years));
            return;
        }

        buttonContainer.removeAllViews();
        journeyStatusText.setVisibility(View.GONE);
        buttonContainer.setVisibility(View.VISIBLE);
        LayoutInflater inflater = LayoutInflater.from(this);

        if (!JourneyProgressPrefs.isInitialized(this)) {
            for (int i = 0; i < yearLevels.length(); i++) {
                JSONObject seed = yearLevels.getJSONObject(i);
                JourneyProgressPrefs.setYearCompleted(
                        this,
                        seed.getInt("id"),
                        seed.optBoolean("is_completed", false));
            }
            JourneyProgressPrefs.markInitialized(this);
        }

        for (int i = 0; i < yearLevels.length(); i++) {
            JSONObject year = yearLevels.getJSONObject(i);
            String yearName = year.getString("name");
            int yearId = year.getInt("id");
            boolean isUnlocked = year.optBoolean("is_unlocked", i == 0);
            boolean isCompleted = year.optBoolean("is_completed", false);
            String description = year.optString("description", "");

            View cardView = inflater.inflate(R.layout.item_year_card, buttonContainer, false);
            MaterialCardView root = cardView.findViewById(R.id.yearCardRoot);

            TextView nameText = cardView.findViewById(R.id.yearName);
            TextView subtitleText = cardView.findViewById(R.id.yearSubtitle);
            ProgressBar yearProgressBar = cardView.findViewById(R.id.yearProgressBar);
            ImageView lockIcon = cardView.findViewById(R.id.yearLockIcon);
            nameText.setText(yearName);

            int modulesCompleted = year.optInt("modules_completed", 0);
            int modulesTotal = year.optInt("modules_total", 0);
            int progressPercent = year.optInt("progress_percent", 0);
            if (isUnlocked && !isCompleted && modulesTotal > 0 && yearProgressBar != null) {
                yearProgressBar.setVisibility(View.VISIBLE);
                yearProgressBar.setProgress(progressPercent);
                subtitleText.setText(getString(
                        R.string.year_progress_modules,
                        modulesCompleted,
                        modulesTotal,
                        progressPercent));
            }

            if (isCompleted && !JourneyProgressPrefs.wasYearCompleted(this, yearId)) {
                JourneyProgressPrefs.setYearCompleted(this, yearId, true);
                pendingYearCelebration = true;
            } else if (!isCompleted) {
                JourneyProgressPrefs.setYearCompleted(this, yearId, false);
            }

            LottieAnimationView planetAnimation = cardView.findViewById(R.id.planetAnimation);
            int animationResId = getResources().getIdentifier("planet" + yearId, "raw", getPackageName());
            if (animationResId != 0) {
                planetAnimation.setAnimation(animationResId);
                planetAnimation.playAnimation();
            }

            if (lockIcon != null) {
                lockIcon.setVisibility(View.GONE);
            }
            if (root != null) {
                root.setAlpha(1f);
            }

            if (!isUnlocked) {
                String unlockMessage = year.optString("unlock_message", "");
                if (unlockMessage.isEmpty()) {
                    unlockMessage = getString(R.string.year_locked_hint);
                }
                subtitleText.setText(unlockMessage);
                if (lockIcon != null) {
                    lockIcon.setVisibility(View.VISIBLE);
                }
                if (root != null) {
                    root.setAlpha(0.55f);
                }
                final String lockedToast = unlockMessage;
                cardView.setOnClickListener(v ->
                        Toast.makeText(Journey.this, lockedToast, Toast.LENGTH_SHORT).show());
            } else if (isCompleted) {
                subtitleText.setText(R.string.year_completed);
                subtitleText.setTextColor(ContextCompat.getColor(this, R.color.game_success));
                if (root != null) {
                    root.setStrokeWidth(4);
                    root.setStrokeColor(ContextCompat.getColor(this, R.color.game_success));
                }
                cardView.setOnClickListener(v -> openYearActivity(yearId, yearName, description));
            } else {
                subtitleText.setText(R.string.year_enter);
                cardView.setOnClickListener(v -> openYearActivity(yearId, yearName, description));
            }

            buttonContainer.addView(cardView);
        }

        journeyStatusText.setVisibility(View.GONE);
        buttonContainer.setVisibility(View.VISIBLE);

        if (pendingYearCelebration) {
            journeyStatusText.post(this::showYearCompleteCelebrationIfNeeded);
        }
    }

    private void showLoadError(String message) {
        journeyStatusText.setVisibility(View.VISIBLE);
        journeyStatusText.setText(message);
        buttonContainer.setVisibility(View.GONE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void openYearActivity(int yearId, String yearName, String description) {
        Intent intent = new Intent(this, YearActivity.class);
        intent.putExtra("year_id", yearId);
        intent.putExtra("year_name", yearName);
        intent.putExtra("year_description", description);
        startActivity(intent);
    }
}
