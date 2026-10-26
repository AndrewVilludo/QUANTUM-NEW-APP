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

    private int moduleId;
    private String moduleName;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_module);

        moduleContainer = findViewById(R.id.moduleContainer);
        moduleTitleTextView = findViewById(R.id.moduleTitleTextView);

        requestQueue = Volley.newRequestQueue(this);

        moduleId = getIntent().getIntExtra("course_id", -1);
        moduleName = getIntent().getStringExtra("course_name");

        Log.d("ModuleActivity", "moduleId: " + moduleId);

        if (moduleId == -1) {
            Toast.makeText(this, "Invalid module ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        moduleTitleTextView.setText(moduleName);
        fetchLessonsFromApi(moduleId);
    }

    private void fetchLessonsFromApi(int moduleId) {
        String url = ApiConfig.api("module-lessons/" + moduleId);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray lessons = response.getJSONArray("data");

                        if (lessons.length() == 0) {
                            Toast.makeText(this, "No lessons found for this module.", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        for (int i = 0; i < lessons.length(); i++) {
                            JSONObject lesson = lessons.getJSONObject(i);

                            String lessonName = lesson.getString("name");
                            int lessonId = lesson.getInt("id");

                            TextView lessonNameView = new TextView(this);
                            lessonNameView.setText(lessonName);
                            lessonNameView.setTextSize(18);
                            lessonNameView.setTextColor(Color.BLACK);
                            lessonNameView.setPadding(16, 24, 16, 8);
                            moduleContainer.addView(lessonNameView);

                            Button noviceButton = new Button(this);
                            noviceButton.setText("Novice");
                            noviceButton.setBackgroundColor(Color.parseColor("#4CAF50"));
                            noviceButton.setTextColor(Color.WHITE);
                            noviceButton.setOnClickListener(v ->
                                    openLessonActivity(moduleId, lessonName));
                            moduleContainer.addView(noviceButton);

                            Button quizButton = new Button(this);
                            quizButton.setText("Quiz");
                            quizButton.setBackgroundColor(Color.parseColor("#2196F3"));
                            quizButton.setTextColor(Color.WHITE);
                            quizButton.setOnClickListener(v -> openQuizActivity(lessonId));
                            moduleContainer.addView(quizButton);
                        }
                    } catch (Exception e) {
                        Toast.makeText(this, "Error parsing lessons: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to fetch lessons: " + error.toString(), Toast.LENGTH_LONG).show();
                    error.printStackTrace();
                }
        );

        requestQueue.add(request);
    }

    private void openLessonActivity(int moduleId, String moduleName) {
        Intent intent = new Intent(this, LessonActivity.class);
        intent.putExtra("module_id", moduleId);
        intent.putExtra("module_name", moduleName);
        startActivity(intent);
    }

    private void openQuizActivity(int lessonId) {
        Intent intent = new Intent(this, QuizActivity.class);
        intent.putExtra("lesson_id", lessonId);
        startActivity(intent);
    }
}
