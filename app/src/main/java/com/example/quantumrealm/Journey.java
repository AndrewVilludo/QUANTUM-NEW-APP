package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import org.json.JSONArray;
import org.json.JSONObject;

public class Journey extends LogoutAppCompatActivity {

    private LinearLayout buttonContainer;
    private TextView journeyStatusText;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        buttonContainer = findViewById(R.id.buttonContainer);
        journeyStatusText = findViewById(R.id.journeyStatusText);
        requestQueue = Volley.newRequestQueue(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadGalaxyYears();
    }

    private void loadGalaxyYears() {
        buttonContainer.removeAllViews();
        buttonContainer.setVisibility(View.GONE);
        journeyStatusText.setVisibility(View.VISIBLE);
        journeyStatusText.setText(R.string.journey_loading);

        if (!AuthHelper.isLoggedIn(this)) {
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

        LayoutInflater inflater = LayoutInflater.from(this);
        buttonContainer.removeAllViews();

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
            ImageView lockIcon = cardView.findViewById(R.id.yearLockIcon);
            nameText.setText(yearName);

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
                subtitleText.setText(R.string.year_locked_hint);
                if (lockIcon != null) {
                    lockIcon.setVisibility(View.VISIBLE);
                }
                if (root != null) {
                    root.setAlpha(0.55f);
                }
                cardView.setOnClickListener(v ->
                        Toast.makeText(Journey.this, R.string.year_locked_hint, Toast.LENGTH_SHORT).show());
            } else if (isCompleted) {
                subtitleText.setText(R.string.year_completed);
                subtitleText.setTextColor(ContextCompat.getColor(this, R.color.game_success));
                cardView.setOnClickListener(v -> openYearActivity(yearId, yearName, description));
            } else {
                subtitleText.setText(R.string.year_enter);
                cardView.setOnClickListener(v -> openYearActivity(yearId, yearName, description));
            }

            buttonContainer.addView(cardView);
        }

        journeyStatusText.setVisibility(View.GONE);
        buttonContainer.setVisibility(View.VISIBLE);
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
