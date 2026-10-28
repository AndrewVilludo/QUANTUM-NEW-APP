package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

public class ModuleActivity extends LogoutAppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);

        TextView moduleTitleTextView = findViewById(R.id.moduleTitleTextView);
        int moduleId = getIntent().getIntExtra("course_id", -1);
        String moduleName = getIntent().getStringExtra("course_name");
        int subjectId = getIntent().getIntExtra("subject_id", -1);

        if (moduleId == -1 || subjectId == -1) {
            Toast.makeText(this, "Invalid module", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        moduleTitleTextView.setText(moduleName);
        loadModuleProgress(moduleId, moduleName, subjectId);
    }

    private void loadModuleProgress(int moduleId, String moduleName, int subjectId) {
        String url = ApiConfig.api("module-progress/" + moduleId);

        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.GET,
                url,
                null,
                response -> {
                    try {
                        if (!response.optBoolean("success", false)) {
                            Toast.makeText(this, response.optString("message", "Module locked"), Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        JSONArray lessons = response.getJSONArray("data");
                        if (lessons.length() == 0) {
                            Toast.makeText(this, "No lessons in this module.", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }

                        JSONObject moduleMeta = response.optJSONObject("module");
                        if (response.isNull("current_lesson_index")) {
                            boolean quizReady = moduleMeta != null && moduleMeta.optBoolean("module_quiz_ready", false);
                            if (quizReady) {
                                startActivity(ModuleFlowHelper.moduleQuizIntent(this, moduleId, moduleName, subjectId));
                                finish();
                                return;
                            }
                            Toast.makeText(this, "Module complete! Great work.", Toast.LENGTH_LONG).show();
                            ModuleFlowHelper.backToCourse(this, subjectId);
                            finish();
                            return;
                        }

                        int currentIndex = response.optInt("current_lesson_index", 0);
                        JSONObject lesson = lessons.getJSONObject(currentIndex);
                        if (!lesson.optBoolean("can_access", false)) {
                            Toast.makeText(this, "This lesson is locked.", Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }

                        Intent intent = ModuleFlowHelper.slideshowIntent(
                                this, lesson, currentIndex, moduleId, moduleName, subjectId);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        Toast.makeText(this, "Error loading module lessons", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to load module progress", Toast.LENGTH_LONG).show();
                    finish();
                }
        );

        Volley.newRequestQueue(this).add(request);
    }
}
