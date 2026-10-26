package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class QuizSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz_summary);

        TextView summaryText = findViewById(R.id.summaryText);
        Button returnButton = findViewById(R.id.returnButton);

        int score = getIntent().getIntExtra("score", 0);
        ArrayList<String> wrongAnswers = getIntent().getStringArrayListExtra("wrongAnswers");

        StringBuilder summary = new StringBuilder();
        summary.append("📝 Quiz Summary\n\n");
        summary.append("✅ Score: ").append(score).append("\n\n");

        if (wrongAnswers != null && !wrongAnswers.isEmpty()) {
            summary.append("❌ Incorrect Answers:\n");
            for (String wrong : wrongAnswers) {
                summary.append("• ").append(wrong).append("\n");
            }
        } else {
            summary.append("🎉 Perfect score! No mistakes at all!");
        }

        summaryText.setText(summary.toString());

        returnButton.setOnClickListener(v -> {
            Intent intent = new Intent(QuizSummaryActivity.this, Journey.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
