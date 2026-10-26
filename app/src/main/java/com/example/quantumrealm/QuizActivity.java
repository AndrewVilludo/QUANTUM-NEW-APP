package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends AppCompatActivity {

    private TextView questionText, timerText, scoreText;
    private RadioGroup optionsGroup;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button nextButton;

    private List<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private List<String> wrongAnswers = new ArrayList<>();

    private CountDownTimer countDownTimer;
    private static final long TIMER_DURATION = 10000; // 10 seconds per question

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionText = findViewById(R.id.questionText);
        timerText = findViewById(R.id.timerText);
        scoreText = findViewById(R.id.scoreText);
        optionsGroup = findViewById(R.id.optionsGroup);
        optionA = findViewById(R.id.optionA);
        optionB = findViewById(R.id.optionB);
        optionC = findViewById(R.id.optionC);
        optionD = findViewById(R.id.optionD);
        nextButton = findViewById(R.id.nextButton);

        int lessonId = getIntent().getIntExtra("lesson_id", -1);

        if (lessonId != -1) {
            fetchQuizData(lessonId);
        } else {
            Toast.makeText(this, "Invalid lesson ID", Toast.LENGTH_SHORT).show();
            finish();
        }

        nextButton.setOnClickListener(v -> checkAnswer());
    }

    private void fetchQuizData(int lessonId) {
        // Correct the URL format
        String url = "http://ec2-16-176-13-247.ap-southeast-2.compute.amazonaws.com/lesson/" + lessonId + "/quizzes";

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray jsonArray = response.getJSONArray("data");

                        // Parse the quiz data into Question objects
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject obj = jsonArray.getJSONObject(i);
                            Question q = new Question(
                                    obj.getInt("id"),
                                    lessonId,
                                    obj.getString("question"),
                                    obj.getString("answer1"),
                                    obj.getString("answer2"),
                                    obj.getString("answer3"),
                                    obj.getString("answer4"),
                                    obj.getString("correct")
                            );
                            questionList.add(q);
                        }

                        // If questions are available, show the next question
                        if (!questionList.isEmpty()) {
                            showNextQuestion();
                        } else {
                            Toast.makeText(this, "No quizzes found for this lesson.", Toast.LENGTH_SHORT).show();
                            finish();
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to parse quiz data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    error.printStackTrace();
                    Toast.makeText(this, "Failed to fetch quiz data", Toast.LENGTH_SHORT).show();
                });

        Volley.newRequestQueue(this).add(request);
    }


    private void showNextQuestion() {
        if (countDownTimer != null) countDownTimer.cancel();

        if (currentQuestionIndex < questionList.size()) {
            Question question = questionList.get(currentQuestionIndex);
            questionText.setText(question.getQuestion());
            optionA.setText(question.getAnswer1());
            optionB.setText(question.getAnswer2());
            optionC.setText(question.getAnswer3());
            optionD.setText(question.getAnswer4());
            optionsGroup.clearCheck();
            scoreText.setText("Score: " + score);

            countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timerText.setText("Time Left: " + millisUntilFinished / 1000 + "s");
                }

                @Override
                public void onFinish() {
                    timerText.setText("Time's Up!");
                    wrongAnswers.add("Q: " + question.getQuestion() +
                            "\nYour Answer: No answer (Timeout)" +
                            "\nCorrect: " + question.getCorrectAnswerText());
                    currentQuestionIndex++;
                    showNextQuestion();
                }
            }.start();

        } else {
            showFinalScore();
        }
    }

    private void checkAnswer() {
        if (countDownTimer != null) countDownTimer.cancel();

        Question current = questionList.get(currentQuestionIndex);
        int selectedId = optionsGroup.getCheckedRadioButtonId();
        String selectedAnswer = "";

        if (selectedId != -1) {
            RadioButton selectedOption = findViewById(selectedId);
            selectedAnswer = selectedOption.getText().toString();
        }

        if (selectedAnswer.equals(current.getCorrectAnswerText())) {
            score++;
        } else {
            wrongAnswers.add("Q: " + current.getQuestion() + "\nYour Answer: " +
                    selectedAnswer + "\nCorrect: " + current.getCorrectAnswerText());
        }

        currentQuestionIndex++;
        showNextQuestion();
    }

    private void showFinalScore() {
        StringBuilder result = new StringBuilder();
        result.append("Final Score: ").append(score).append("/").append(questionList.size()).append("\n\n");

        if (!wrongAnswers.isEmpty()) {
            result.append("Incorrect Answers:\n");
            for (String mistake : wrongAnswers) {
                result.append(mistake).append("\n\n");
            }
        } else {
            result.append("Perfect Score! 🎉");
        }

        new AlertDialog.Builder(this)
                .setTitle("Quiz Finished")
                .setMessage(result.toString())
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent = new Intent(QuizActivity.this, Journey.class);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}
