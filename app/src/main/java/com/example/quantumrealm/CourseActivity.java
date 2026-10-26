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
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class CourseActivity extends AppCompatActivity {

    private LinearLayout moduleContainer;
    private TextView courseTitleTextView;

    private static final String BASE_URL = ApiConfig.api("subject-modules/");

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        courseTitleTextView = findViewById(R.id.courseTitleTextView);
        moduleContainer = findViewById(R.id.moduleContainer);
        requestQueue = Volley.newRequestQueue(this);

        // ✅ FIX: Correctly get course_id and course_name from intent
        Intent intent = getIntent();
        int courseId = intent.getIntExtra("course_id", 0);
        String courseName = intent.getStringExtra("course_name");

        courseTitleTextView.setText(courseName);

        // ✅ Use correct course_id in API call
        fetchModulesFromApi(courseId);
    }

    private void fetchModulesFromApi(int courseId) {
        String url = BASE_URL + courseId;

        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray modules = response.getJSONArray("data");

                        for (int i = 0; i < modules.length(); i++) {
                            JSONObject module = modules.getJSONObject(i);

                            String moduleName = module.getString("name");
                            int moduleId = module.getInt("id");

                            Button moduleButton = new Button(this);
                            moduleButton.setText(moduleName);

                            moduleButton.setOnClickListener(v -> {
                                int yearId = getIntent().getIntExtra("year_id", 0); // optional
                                openModuleActivity(moduleId, moduleName, yearId);
                            });

                            moduleContainer.addView(moduleButton);
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

    private void openModuleActivity(int moduleId, String moduleName, int yearId) {
        Intent intent = new Intent(this, ModuleActivity.class);
        intent.putExtra("course_id", moduleId);  // Ensure this is the correct course ID
        intent.putExtra("course_name", moduleName);  // Ensure this is the correct course name
        intent.putExtra("year_id", yearId);
        intent.putExtra("subject_id", getIntent().getIntExtra("course_id", 0));
        startActivity(intent);
    }

}
