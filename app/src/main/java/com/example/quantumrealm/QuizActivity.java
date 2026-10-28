package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.Request;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class QuizActivity extends LogoutAppCompatActivity {

    private TextView questionText, timerText, scoreText, quizProgressText;
    private ProgressBar quizProgressBar;
    private RadioGroup optionsGroup;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button nextButton;

    private List<Question> questionList = new ArrayList<>();
    private int currentQuestionIndex = 0;
    private int score = 0;
    private List<String> wrongAnswers = new ArrayList<>();

    private CountDownTimer countDownTimer;
    private static final long TIMER_DURATION = 10000;
    private final Handler handler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        questionText = findViewById(R.id.questionText);
        timerText = findViewById(R.id.timerText);
        scoreText = findViewById(R.id.scoreText);
        quizProgressText = findViewById(R.id.quizProgressText);
        quizProgressBar = findViewById(R.id.quizProgressBar);
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
        String url = ApiConfig.api("lesson-quizzes/" + lessonId);

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray jsonArray = response.getJSONArray("data");

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
        resetOptionStyles();
        nextButton.setEnabled(true);

        if (currentQuestionIndex < questionList.size()) {
            Question question = questionList.get(currentQuestionIndex);
            questionText.setText(question.getQuestion());
            optionA.setText(question.getAnswer1());
            optionB.setText(question.getAnswer2());
            optionC.setText(question.getAnswer3());
            optionD.setText(question.getAnswer4());
            optionsGroup.clearCheck();
            updateProgressUi();

            countDownTimer = new CountDownTimer(TIMER_DURATION, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    timerText.setText("⏱ " + millisUntilFinished / 1000 + "s");
                }

                @Override
                public void onFinish() {
                    timerText.setText("Time's up!");
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

    private void updateProgressUi() {
        int total = questionList.size();
        int displayIndex = Math.min(currentQuestionIndex + 1, total);
        quizProgressText.setText(getString(R.string.quiz_progress, displayIndex, total));
        quizProgressBar.setProgress(total == 0 ? 0 : (displayIndex * 100 / total));
        scoreText.setText("XP: " + score);
    }

    private void resetOptionStyles() {
        optionA.setBackgroundResource(R.drawable.quiz_option_background);
        optionB.setBackgroundResource(R.drawable.quiz_option_background);
        optionC.setBackgroundResource(R.drawable.quiz_option_background);
        optionD.setBackgroundResource(R.drawable.quiz_option_background);
    }

    private void checkAnswer() {
        if (countDownTimer != null) countDownTimer.cancel();

        Question current = questionList.get(currentQuestionIndex);
        int selectedId = optionsGroup.getCheckedRadioButtonId();
        String selectedAnswer = "";

        RadioButton selectedOption = null;
        if (selectedId != -1) {
            selectedOption = findViewById(selectedId);
            selectedAnswer = selectedOption.getText().toString();
        }

        if (selectedAnswer.isEmpty()) {
            Toast.makeText(this, "Pick an answer first", Toast.LENGTH_SHORT).show();
            return;
        }

        nextButton.setEnabled(false);
        boolean correct = selectedAnswer.equals(current.getCorrectAnswerText());
        RadioButton correctButton = findButtonForAnswer(current.getCorrectAnswerText());

        if (correct) {
            score++;
            if (selectedOption != null) {
                selectedOption.setBackgroundResource(R.drawable.quiz_option_correct);
            }
            Toast.makeText(this, "Correct! +1 XP", Toast.LENGTH_SHORT).show();
        } else {
            wrongAnswers.add("Q: " + current.getQuestion() + "\nYour Answer: " +
                    selectedAnswer + "\nCorrect: " + current.getCorrectAnswerText());
            if (selectedOption != null) {
                selectedOption.setBackgroundResource(R.drawable.quiz_option_wrong);
            }
            if (correctButton != null) {
                correctButton.setBackgroundResource(R.drawable.quiz_option_correct);
            }
        }

        updateProgressUi();
        currentQuestionIndex++;

        handler.postDelayed(this::showNextQuestion, 700);
    }

    private RadioButton findButtonForAnswer(String answerText) {
        if (optionA.getText().toString().equals(answerText)) return optionA;
        if (optionB.getText().toString().equals(answerText)) return optionB;
        if (optionC.getText().toString().equals(answerText)) return optionC;
        if (optionD.getText().toString().equals(answerText)) return optionD;
        return null;
    }

    private void showFinalScore() {
        StringBuilder result = new StringBuilder();
        result.append("Final XP: ").append(score).append("/").append(questionList.size()).append("\n\n");

        if (!wrongAnswers.isEmpty()) {
            result.append("Review these misses:\n");
            for (String mistake : wrongAnswers) {
                result.append(mistake).append("\n\n");
            }
        } else {
            result.append("Perfect run! 🎉");
        }

        new AlertDialog.Builder(this)
                .setTitle("Quest complete")
                .setMessage(result.toString())
                .setPositiveButton("Back to map", (dialog, which) -> {
                    Intent intent = new Intent(QuizActivity.this, Journey.class);
                    startActivity(intent);
                    finish();
                })
                .show();
    }
}
