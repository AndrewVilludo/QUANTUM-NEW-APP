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

public class CourseActivity extends LogoutAppCompatActivity {

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
        fetchSubjectProgress(courseId, courseName);
    }

    private void fetchSubjectProgress(int subjectId, String courseName) {
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.GET,
                ApiConfig.api("subject-progress/" + subjectId),
                null,
                response -> {
                    try {
                        JSONObject data = response.getJSONObject("data");
                        if (data.optBoolean("final_quiz_unlocked", false) && !data.optBoolean("final_quiz_passed", false)) {
                            moduleContainer.addView(UiHelper.inflatePathCard(
                                    this,
                                    moduleContainer,
                                    "Final subject quiz",
                                    "All modules complete — take the comprehensive quiz",
                                    "🏆",
                                    false,
                                    () -> {
                                        Intent intent = new Intent(CourseActivity.this, SubjectFinalQuizActivity.class);
                                        intent.putExtra("subject_id", subjectId);
                                        startActivity(intent);
                                    }
                            ));
                        }
                    } catch (Exception ignored) {
                    }
                },
                error -> { }
        );
        requestQueue.add(request);
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
                            boolean unlocked = module.optBoolean("is_unlocked", false);
                            boolean completed = module.optBoolean("is_completed", false);
                            boolean quizReady = module.optBoolean("module_quiz_ready", false);
                            int yearId = getIntent().getIntExtra("year_id", 0);
                            int index = moduleContainer.getChildCount();

                            String subtitle = completed
                                    ? getString(R.string.module_completed)
                                    : getString(R.string.module_ready);
                            moduleContainer.addView(UiHelper.inflatePathCard(
                                    this,
                                    moduleContainer,
                                    moduleName,
                                    subtitle,
                                    UiHelper.emojiForIndex(index + 1),
                                    !unlocked,
                                    completed,
                                    () -> openModuleActivity(moduleId, moduleName, yearId)
                            ));

                            if (quizReady && unlocked) {
                                moduleContainer.addView(UiHelper.inflatePathCard(
                                        this,
                                        moduleContainer,
                                        getString(R.string.module_quiz_title, moduleName),
                                        getString(R.string.module_quiz_subtitle),
                                        "📝",
                                        false,
                                        false,
                                        () -> openModuleQuiz(moduleId, moduleName)
                                ));
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

    private void openModuleActivity(int moduleId, String moduleName, int yearId) {
        Intent intent = new Intent(this, ModuleActivity.class);
        intent.putExtra("course_id", moduleId);  // Ensure this is the correct course ID
        intent.putExtra("course_name", moduleName);  // Ensure this is the correct course name
        intent.putExtra("year_id", yearId);
        intent.putExtra("subject_id", getIntent().getIntExtra("course_id", 0));
        startActivity(intent);
    }

    private void openModuleQuiz(int moduleId, String moduleName) {
        int subjectId = getIntent().getIntExtra("course_id", 0);
        startActivity(ModuleFlowHelper.moduleQuizIntent(this, moduleId, moduleName, subjectId));
    }

}
