package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class CourseActivity extends LogoutAppCompatActivity {

    private LinearLayout moduleContainer;
    private TextView courseTitleTextView;
    private TextView courseMissionText;
    private SwipeRefreshLayout courseSwipeRefresh;

    private static final String BASE_URL = ApiConfig.api("subject-modules/");

    private RequestQueue requestQueue;
    private int subjectId;
    private String courseName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_course);

        courseTitleTextView = findViewById(R.id.courseTitleTextView);
        courseMissionText = findViewById(R.id.courseMissionText);
        courseSwipeRefresh = findViewById(R.id.courseSwipeRefresh);
        moduleContainer = findViewById(R.id.moduleContainer);
        requestQueue = Volley.newRequestQueue(this);

        Intent intent = getIntent();
        subjectId = intent.getIntExtra("course_id", 0);
        courseName = intent.getStringExtra("course_name");

        courseTitleTextView.setText(courseName);
        String mission = intent.getStringExtra("course_mission");
        if (mission != null && !mission.isEmpty()) {
            courseMissionText.setText(getString(R.string.subject_mission_prefix, mission));
        } else {
            courseMissionText.setVisibility(View.GONE);
        }
        courseSwipeRefresh.setOnRefreshListener(() -> {
            loadModulePath();
            courseSwipeRefresh.setRefreshing(false);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadModulePath();
    }

    private void loadModulePath() {
        moduleContainer.removeAllViews();
        fetchModulesFromApi(subjectId);
        fetchSubjectProgress(subjectId);
    }

    private void fetchSubjectProgress(int subjectId) {
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
                                    "30 questions from all modules — need 70% to pass",
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

                            String subtitle;
                            if (completed) {
                                subtitle = getString(R.string.module_completed);
                            } else if (quizReady && unlocked) {
                                subtitle = getString(R.string.module_quiz_pending);
                            } else {
                                subtitle = getString(R.string.module_ready);
                            }

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
                            } else if (completed) {
                                moduleContainer.addView(UiHelper.inflatePathCard(
                                        this,
                                        moduleContainer,
                                        getString(R.string.module_quiz_title, moduleName),
                                        getString(R.string.module_quiz_passed),
                                        "📝",
                                        false,
                                        true,
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
        intent.putExtra("course_id", moduleId);
        intent.putExtra("course_name", moduleName);
        intent.putExtra("year_id", yearId);
        intent.putExtra("subject_id", subjectId);
        startActivity(intent);
    }

    private void openModuleQuiz(int moduleId, String moduleName) {
        startActivity(ModuleFlowHelper.moduleQuizIntent(this, moduleId, moduleName, subjectId));
    }

}
