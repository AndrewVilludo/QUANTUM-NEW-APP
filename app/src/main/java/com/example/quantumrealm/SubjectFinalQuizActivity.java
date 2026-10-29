package com.example.quantumrealm;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SubjectFinalQuizActivity extends LogoutAppCompatActivity {

    private static final String TAG = "SubjectFinalQuiz";

    private TextView questionText, timerText, scoreText, quizProgressText;
    private ProgressBar quizProgressBar;
    private RadioGroup optionsGroup;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button nextButton;

    private final List<Question> questionList = new ArrayList<>();
    private final Map<Integer, String> selectedByQuizId = new HashMap<>();
    private int currentQuestionIndex = 0;
    private int subjectId;
    private CountDownTimer countDownTimer;
    private static final long TIMER_DURATION = 15000;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private QuizExitGuard exitGuard;

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

        subjectId = getIntent().getIntExtra("subject_id", -1);
        if (subjectId == -1) {
            finish();
            return;
        }

        nextButton.setOnClickListener(v -> checkAnswer());
        exitGuard = new QuizExitGuard(this, this::discardAttemptAndExitToGalaxy);
        loadFinalQuiz();
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (exitGuard.interceptLeaveAttempt(this)) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Music.handleMuteMenuItem(this, item)) {
            return true;
        }
        if (item.getItemId() == R.id.action_profile && exitGuard.interceptLeaveAttempt(this)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void discardAttemptAndExitToGalaxy() {
        exitGuard.setAttemptInProgress(false);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        handler.removeCallbacksAndMessages(null);
        selectedByQuizId.clear();
        currentQuestionIndex = 0;
        ModuleFlowHelper.navigateToLearningGalaxy(this);
    }

    private void loadFinalQuiz() {
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                com.android.volley.Request.Method.GET,
                ApiConfig.api("subject-final-quiz/" + subjectId),
                null,
                response -> {
                    try {
                        if (!response.optBoolean("success", false)) {
                            Toast.makeText(this, response.optString("message", "Quiz locked"), Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            questionList.add(new Question(
                                    obj.getInt("id"),
                                    obj.getInt("lesson_id"),
                                    obj.getString("question"),
                                    obj.getString("answer1"),
                                    obj.getString("answer2"),
                                    obj.getString("answer3"),
                                    obj.getString("answer4"),
                                    obj.getString("correct")
                            ));
                        }
                        if (questionList.isEmpty()) {
                            Toast.makeText(this, "No final quiz questions yet.", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        showNextQuestion();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to parse final quiz", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to load final quiz", Toast.LENGTH_SHORT).show();
                    finish();
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void showNextQuestion() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        resetOptionStyles();
        nextButton.setEnabled(true);

        if (currentQuestionIndex < questionList.size()) {
            exitGuard.setAttemptInProgress(true);
            nextButton.setText(currentQuestionIndex == questionList.size() - 1
                    ? R.string.quiz_submit
                    : R.string.quiz_next);
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
                    selectedByQuizId.put(questionList.get(currentQuestionIndex).getId(), "");
                    currentQuestionIndex++;
                    showNextQuestion();
                }
            }.start();
        } else {
            submitFinalQuiz();
        }
    }

    private void updateProgressUi() {
        int total = questionList.size();
        int displayIndex = Math.min(currentQuestionIndex + 1, total);
        quizProgressText.setText(getString(R.string.quiz_progress, displayIndex, total));
        quizProgressBar.setProgress(total == 0 ? 0 : (displayIndex * 100 / total));
        scoreText.setText("Final quiz");
    }

    private void resetOptionStyles() {
        optionA.setBackgroundResource(R.drawable.quiz_option_background);
        optionB.setBackgroundResource(R.drawable.quiz_option_background);
        optionC.setBackgroundResource(R.drawable.quiz_option_background);
        optionD.setBackgroundResource(R.drawable.quiz_option_background);
    }

    private void checkAnswer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        Question current = questionList.get(currentQuestionIndex);
        int selectedId = optionsGroup.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Pick an answer first", Toast.LENGTH_SHORT).show();
            return;
        }
        RadioButton selected = findViewById(selectedId);
        selectedByQuizId.put(current.getId(), selected.getText().toString());
        currentQuestionIndex++;
        handler.postDelayed(this::showNextQuestion, 400);
    }

    private void submitFinalQuiz() {
        nextButton.setEnabled(false);
        try {
            JSONArray answers = new JSONArray();
            for (Question q : questionList) {
                JSONObject row = new JSONObject();
                row.put("quiz_id", q.getId());
                String selected = selectedByQuizId.getOrDefault(q.getId(), "");
                row.put("selected", selected == null ? "" : selected);
                answers.put(row);
            }

            JSONObject body = new JSONObject();
            body.put("answers", answers);

            Log.d(TAG, "Submitting final quiz subjectId=" + subjectId + " answerCount=" + answers.length());

            AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                    this,
                    com.android.volley.Request.Method.POST,
                    ApiConfig.api("subject-final-quiz/" + subjectId + "/submit"),
                    body,
                    response -> {
                        exitGuard.setAttemptInProgress(false);
                        if (!response.optBoolean("success", true)) {
                            String msg = response.optString("message", "Submit failed");
                            Log.w(TAG, "Submit rejected: " + msg);
                            Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                            nextButton.setEnabled(true);
                            return;
                        }
                        boolean passed = response.optBoolean("passed", false);
                        int score = response.optInt("score", 0);
                        int total = response.optInt("total", questionList.size());
                        String message = response.optString("message", "");
                        if (!message.isEmpty()) {
                            message = score + " / " + total + "\n\n" + message;
                        }
                        new AlertDialog.Builder(this)
                                .setTitle(passed ? "Subject mastered!" : "Final quiz")
                                .setMessage(message)
                                .setCancelable(false)
                                .setPositiveButton("OK", (d, w) -> {
                                    ModuleFlowHelper.backToCourse(this, subjectId);
                                    finish();
                                })
                                .show();
                    },
                    error -> {
                        nextButton.setEnabled(true);
                        String message = parseSubmitError(error);
                        Log.e(TAG, "Submit failed: " + message, error);
                        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
                    }
            );
            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            nextButton.setEnabled(true);
            Log.e(TAG, "Submit error building payload", e);
            Toast.makeText(this, "Submit error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private String parseSubmitError(com.android.volley.VolleyError error) {
        String fallback = "Submit failed";
        if (error.networkResponse == null) {
            return fallback + " (no network response)";
        }
        int status = error.networkResponse.statusCode;
        fallback = fallback + " (HTTP " + status + ")";
        if (error.networkResponse.data != null) {
            try {
                JSONObject body = new JSONObject(new String(error.networkResponse.data));
                String msg = body.optString("message", "");
                if (!msg.isEmpty()) {
                    return msg + " (HTTP " + status + ")";
                }
            } catch (Exception ignored) {
            }
        }
        return fallback;
    }
}
