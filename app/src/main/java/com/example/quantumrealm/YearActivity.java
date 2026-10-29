package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class YearActivity extends LogoutAppCompatActivity {

    private LinearLayout courseContainer;
    private TextView yearTitleTextView;

    // Base URL for API
    private static final String BASE_URL = ApiConfig.api("subjects/");

    private RequestQueue requestQueue;

    public static void unlockSubject(MiniGame miniGame, String userEmail, int nextSubjectId) {
    }

    public static void unlockCourse(MiniGame miniGame, String userEmail, int nextCourseId) {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_year);

        yearTitleTextView = findViewById(R.id.yearTitleTextView);
        courseContainer = findViewById(R.id.courseContainer);
        requestQueue = Volley.newRequestQueue(this);

        // Retrieve year information from the Intent
        Intent intent = getIntent();
        int yearId = intent.getIntExtra("year_id", 0);
        String yearName = intent.getStringExtra("year_name");

        // Display the year name on the screen
        yearTitleTextView.setText(yearName);

        // Fetch courses for the selected year
        fetchCoursesFromApi(yearId);
    }

    // Fetch courses for the selected year from the API
    private void fetchCoursesFromApi(int yearId) {
        String url = BASE_URL + yearId;

        // Create the API request
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.GET, url, null,
                response -> {
                    try {
                        if (!response.optBoolean("success", true)) {
                            Toast.makeText(this, response.optString("message", "Could not load subjects."), Toast.LENGTH_LONG).show();
                            return;
                        }
                        // Parse the response JSON to extract courses data
                        JSONArray courses = response.getJSONArray("data");

                        for (int i = 0; i < courses.length(); i++) {
                            JSONObject course = courses.getJSONObject(i);

                            // If the course belongs to the selected year, create a button for it
                            if (course.getInt("year_id") == yearId) {
                                String courseName = course.getString("name");
                                int courseId = course.getInt("id");
                                String mission = course.optString("mission", "");

                                int index = courseContainer.getChildCount();
                                String subtitle = mission.isEmpty()
                                        ? "Subject quest · tap to open"
                                        : mission;
                                courseContainer.addView(UiHelper.inflatePathCard(
                                        this,
                                        courseContainer,
                                        courseName,
                                        subtitle,
                                        UiHelper.emojiForIndex(index),
                                        false,
                                        () -> openCourseActivity(courseId, courseName, mission)
                                ));
                            }
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing course data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    String message = "Failed to fetch subjects.";
                    if (error.networkResponse != null && error.networkResponse.data != null) {
                        try {
                            JSONObject body = new JSONObject(new String(error.networkResponse.data));
                            message = body.optString("message", message);
                        } catch (Exception ignored) {
                        }
                    }
                    Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );

        // Add the request to the request queue
        requestQueue.add(request);
    }

    // Open the CourseActivity with the selected course's ID and name
    private void openCourseActivity(int courseId, String courseName, String mission) {
        Intent intent = new Intent(this, CourseActivity.class);
        intent.putExtra("course_id", courseId);
        intent.putExtra("course_name", courseName);
        intent.putExtra("course_mission", mission);
        intent.putExtra("year_id", getIntent().getIntExtra("year_id", 0));
        startActivity(intent);
    }
}
