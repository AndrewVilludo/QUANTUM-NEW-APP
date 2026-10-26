package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class YearActivity extends AppCompatActivity {

    private LinearLayout courseContainer;
    private TextView yearTitleTextView;

    // Base URL for API
    private static final String BASE_URL = "http://ec2-16-176-13-247.ap-southeast-2.compute.amazonaws.com/api/subjects/";

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
        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        // Parse the response JSON to extract courses data
                        JSONArray courses = response.getJSONArray("data");

                        for (int i = 0; i < courses.length(); i++) {
                            JSONObject course = courses.getJSONObject(i);

                            // If the course belongs to the selected year, create a button for it
                            if (course.getInt("year_id") == yearId) {
                                String courseName = course.getString("name");
                                int courseId = course.getInt("id");

                                // Create a button for each course
                                Button courseButton = new Button(this);
                                courseButton.setText(courseName);

                                // Set up the button click listener to open the CourseActivity
                                courseButton.setOnClickListener(v -> openCourseActivity(courseId, courseName));

                                // Add the button to the layout
                                courseContainer.addView(courseButton);
                            }
                        }

                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing course data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    // Show error message if the request fails
                    Toast.makeText(this, "Failed to fetch courses: " + error.toString(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );

        // Add the request to the request queue
        requestQueue.add(request);
    }

    // Open the CourseActivity with the selected course's ID and name
    private void openCourseActivity(int courseId, String courseName) {
        Intent intent = new Intent(this, CourseActivity.class);
        intent.putExtra("course_id", courseId); // Pass course_id
        intent.putExtra("course_name", courseName); // Pass course_name
        startActivity(intent);
    }
}
