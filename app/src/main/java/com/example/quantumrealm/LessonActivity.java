package com.example.quantumrealm;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LessonActivity extends AppCompatActivity {

    private LinearLayout lessonContainer;
    private TextView lessonTitleTextView;

    private JSONArray activityArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lesson);

        lessonContainer = findViewById(R.id.lessonContainer);
        lessonTitleTextView = findViewById(R.id.lessonTitleTextView);

        Intent intent = getIntent();
        int moduleId = intent.getIntExtra("module_id", -1);
        String moduleName = intent.getStringExtra("module_name");

        lessonTitleTextView.setText(moduleName);

        if (moduleId != -1) {
            fetchLessonData(moduleId);
        }
    }

    private void fetchLessonData(int moduleId) {
        String url = "http://ec2-16-176-13-247.ap-southeast-2.compute.amazonaws.com/api/module-lessons/" + moduleId;

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray dataArray = response.getJSONArray("data");
                        if (dataArray.length() > 0) {
                            JSONObject lessonObject = dataArray.getJSONObject(0);

                            // Only fetch activities now (no content display as per your instruction)
                            activityArray = lessonObject.getJSONArray("activity");

                            // Display all questions directly
                            displayQuestions();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing lesson data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Failed to fetch lesson data", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }

    private void displayQuestions() {
        lessonContainer.removeAllViews(); // Clear any previously displayed questions

        boolean questionFound = false;  // To track if we find any questions

        for (int i = 0; i < activityArray.length(); i++) {
            try {
                JSONObject activity = activityArray.getJSONObject(i);

                // Create a TextView for the question
                TextView questionText = new TextView(this);
                questionText.setText("Q: " + activity.getString("question"));
                questionText.setTextColor(Color.DKGRAY);
                questionText.setPadding(20, 10, 20, 10);
                lessonContainer.addView(questionText);

                // Create an EditText for answer input
                EditText answerInput = new EditText(this);
                answerInput.setHint("Enter your answer here");
                answerInput.setPadding(20, 10, 20, 10);
                lessonContainer.addView(answerInput);

                // Create a Submit button for the answer
                Button submitButton = new Button(this);
                submitButton.setText("Submit Answer");
                submitButton.setBackgroundColor(Color.parseColor("#2196F3"));
                submitButton.setTextColor(Color.WHITE);
                submitButton.setPadding(20, 10, 20, 10);

                submitButton.setOnClickListener(v -> {
                    String userAnswer = answerInput.getText().toString().trim();
                    if (TextUtils.isEmpty(userAnswer)) {
                        Toast.makeText(this, "Please enter an answer", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            String correctAnswer = activity.getString("correct_answer");
                            if (userAnswer.equalsIgnoreCase(correctAnswer)) {
                                Toast.makeText(this, "Correct!", Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(LessonActivity.this, SplashActivity.class);
                                startActivity(intent);
                            } else {
                                Toast.makeText(this, "Incorrect! The correct answer is: " + correctAnswer, Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });

                lessonContainer.addView(submitButton);

                questionFound = true; // Mark that we've found at least one question

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // If no questions are found, display a message
        if (!questionFound) {
            TextView noQuestionText = new TextView(this);
            noQuestionText.setText("No questions available.");
            noQuestionText.setTextColor(Color.RED);
            noQuestionText.setPadding(20, 10, 20, 10);
            lessonContainer.addView(noQuestionText);
        }
    }
}
