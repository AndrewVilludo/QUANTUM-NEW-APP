package com.example.quantumrealm;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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

public class ModuleActivity extends AppCompatActivity {

    private LinearLayout moduleContainer;
    private TextView moduleTitleTextView;

    private int courseId;
    private String courseName;
    private int yearId;
    private RequestQueue requestQueue;

    private static final String BASE_URL = "http://ec2-16-176-13-247.ap-southeast-2.compute.amazonaws.com/api/module-lessons/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);

        moduleContainer = findViewById(R.id.moduleContainer);
        moduleTitleTextView = findViewById(R.id.moduleTitleTextView);

        requestQueue = Volley.newRequestQueue(this);

        courseId = getIntent().getIntExtra("course_id", -1);
        courseName = getIntent().getStringExtra("course_name");
        yearId = getIntent().getIntExtra("year_id", -1);

        Log.d("ModuleActivity", "courseId: " + courseId + ", yearId: " + yearId);

        if (courseId == -1 || yearId == -1) {
            Toast.makeText(this, "Invalid course or year ID", Toast.LENGTH_SHORT).show();
            return;
        }

        moduleTitleTextView.setText(courseName);

        fetchLessonsFromApi(courseId, yearId);
    }

    private void fetchLessonsFromApi(int courseId, int yearId) {
        String url = BASE_URL + courseId + "?year_id=" + yearId;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray modules = response.getJSONArray("data");

                        if (modules.length() == 0) {
                            Toast.makeText(this, "No lessons found for the given course and year.", Toast.LENGTH_SHORT).show();
                        } else {
                            for (int i = 0; i < modules.length(); i++) {
                                JSONObject module = modules.getJSONObject(i);

                                String moduleName = module.getString("name");
                                int moduleId = module.getInt("id");

                                // Display module name
                                TextView moduleNameView = new TextView(this);
                                moduleNameView.setText("Module: " + moduleName);
                                moduleNameView.setTextSize(18);
                                moduleNameView.setTextColor(Color.BLACK);
                                moduleContainer.addView(moduleNameView);

                                // Extract and display content
                                JSONArray contentArray = module.getJSONArray("data");
                                for (int j = 0; j < contentArray.length(); j++) {
                                    String content = contentArray.getJSONObject(j).getString("content");

                                    TextView contentTextView = new TextView(this);
                                    contentTextView.setText(content);
                                    contentTextView.setPadding(16, 8, 16, 8);
                                    moduleContainer.addView(contentTextView);
                                }

                                // Add Novice and Expert buttons
                                Button noviceButton = new Button(this);
                                noviceButton.setText("Novice");
                                noviceButton.setBackgroundColor(Color.parseColor("#4CAF50"));
                                noviceButton.setTextColor(Color.WHITE);


                                moduleContainer.addView(noviceButton);

                                // Handle button clicks
                                noviceButton.setOnClickListener(v -> openLessonActivity(moduleId, moduleName, "Novice"));
                            }
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing modules: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch modules: " + error.toString(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );

        requestQueue.add(request);
    }

    private void openLessonActivity(int moduleId, String moduleName, String userLevel) {
        Intent intent = new Intent(this, LessonActivity.class);
        intent.putExtra("module_id", moduleId);
        intent.putExtra("module_name", moduleName);
        intent.putExtra("user_level", userLevel);
        startActivity(intent);
    }
}
