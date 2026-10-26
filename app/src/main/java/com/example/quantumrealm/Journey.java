package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Journey extends AppCompatActivity {

    private LinearLayout buttonContainer;
    private static final String API_URL = "http://ec2-16-176-13-247.ap-southeast-2.compute.amazonaws.com/api/years";
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_journey);

        buttonContainer = findViewById(R.id.buttonContainer);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Fetch the year levels from API
        fetchYearLevelsFromAPI();
    }

    private void fetchYearLevelsFromAPI() {
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.GET, API_URL, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            JSONArray yearLevels = response.getJSONArray("data");

                            LayoutInflater inflater = LayoutInflater.from(Journey.this);

                            // Loop through each year level
                            for (int i = 0; i < yearLevels.length(); i++) {
                                JSONObject year = yearLevels.getJSONObject(i);
                                String yearName = year.getString("name");
                                int yearId = year.getInt("id");

                                // Inflate the custom layout
                                View cardView = inflater.inflate(R.layout.item_year_card, buttonContainer, false);

                                // Set the year name
                                TextView nameText = cardView.findViewById(R.id.yearName);
                                nameText.setText(yearName);

                                // Set planet animation dynamically
                                LottieAnimationView planetAnimation = cardView.findViewById(R.id.planetAnimation);
                                int animationResId = getResources().getIdentifier("planet" + yearId, "raw", getPackageName());
                                if (animationResId != 0) {
                                    planetAnimation.setAnimation(animationResId);
                                    planetAnimation.playAnimation(); // optional
                                } else {
                                    Log.e("LottieError", "planet" + yearId + ".json not found in res/raw");
                                    // Optional fallback animation or error handling
                                }

                                // Set click listener to open YearActivity
                                cardView.setOnClickListener(v -> openYearActivity(yearId, yearLevels));

                                // Add to the layout
                                buttonContainer.addView(cardView);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Log.e("JSONError", "Error parsing response");
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("VolleyError", "Error fetching data from API: " + error.getMessage());
                    }
                });

        // Add request to the request queue
        requestQueue.add(jsonObjectRequest);
    }

    private void openYearActivity(int yearId, JSONArray yearLevels) {
        Intent intent = new Intent(this, com.example.quantumrealm.YearActivity.class);
        try {
            JSONObject year = yearLevels.getJSONObject(yearId - 1);
            intent.putExtra("year_id", yearId);
            intent.putExtra("year_name", year.getString("name"));
            intent.putExtra("year_description", year.getString("description"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        startActivity(intent);
    }
}
