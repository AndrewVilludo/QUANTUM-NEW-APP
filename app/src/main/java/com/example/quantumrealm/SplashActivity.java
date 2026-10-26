package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    private TextView startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splashactivity);

        startButton = findViewById(R.id.startButton);

        startButton.setOnClickListener(v -> {
            // 🔧 You must pass a valid lesson_id to QuizActivity.
            // For now, we'll hardcode it as "1" (you can change it dynamically later)
            int validLessonId = 1; // Replace this with actual valid lesson ID from your API

            Log.d("SplashActivity", "Sending lesson_id: " + validLessonId);

            Intent intent = new Intent(SplashActivity.this, QuizActivity.class);
            intent.putExtra("lesson_id", validLessonId); // Pass lesson_id
            startActivity(intent);
        });
    }
}
