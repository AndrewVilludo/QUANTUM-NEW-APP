package com.example.quantumrealm;

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

import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ModuleQuizActivity extends LogoutAppCompatActivity {

    private TextView questionText, timerText, scoreText, quizProgressText;
    private ProgressBar quizProgressBar;
    private RadioGroup optionsGroup;
    private RadioButton optionA, optionB, optionC, optionD;
    private Button nextButton;

    private final List<Question> questionList = new ArrayList<>();
    private final Map<Integer, String> selectedByQuizId = new HashMap<>();
    private int currentQuestionIndex = 0;
    private int correctStreak = 0;
    private int moduleId;
    private int subjectId;
    private String moduleName;
    private CountDownTimer countDownTimer;
    private static final long TIMER_DURATION = 15000;
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

        moduleId = getIntent().getIntExtra(ModuleFlowHelper.EXTRA_MODULE_ID, -1);
        subjectId = getIntent().getIntExtra(ModuleFlowHelper.EXTRA_SUBJECT_ID, -1);
        moduleName = getIntent().getStringExtra(ModuleFlowHelper.EXTRA_MODULE_NAME);
        if (moduleId == -1 || subjectId == -1) {
            finish();
            return;
        }

        nextButton.setOnClickListener(v -> checkAnswer());
        loadModuleQuiz();
    }

    private void loadModuleQuiz() {
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                com.android.volley.Request.Method.GET,
                ApiConfig.api("module-quiz/" + moduleId),
                null,
                response -> {
                    try {
                        if (!response.optBoolean("success", false)) {
                            Toast.makeText(this, response.optString("message", "Quiz locked"), Toast.LENGTH_LONG).show();
                            finish();
                            return;
                        }
                        if (response.optBoolean("already_passed", false)) {
                            Toast.makeText(this, "You already passed this module quiz.", Toast.LENGTH_SHORT).show();
                            ModuleFlowHelper.backToCourse(this, subjectId);
                            finish();
                            return;
                        }
                        JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject obj = data.getJSONObject(i);
                            questionList.add(new Question(
                                    obj.getInt("id"),
                                    obj.optInt("lesson_id", 0),
                                    obj.getString("question"),
                                    obj.getString("answer1"),
                                    obj.getString("answer2"),
                                    obj.getString("answer3"),
                                    obj.getString("answer4"),
                                    obj.getString("correct")
                            ));
                        }
                        if (questionList.isEmpty()) {
                            Toast.makeText(this, "No module quiz questions yet.", Toast.LENGTH_SHORT).show();
                            finish();
                            return;
                        }
                        showNextQuestion();
                    } catch (Exception e) {
                        Toast.makeText(this, "Failed to parse module quiz", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                },
                error -> {
                    Toast.makeText(this, "Failed to load module quiz", Toast.LENGTH_SHORT).show();
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
            submitModuleQuiz();
        }
    }

    private void updateProgressUi() {
        int total = questionList.size();
        int displayIndex = Math.min(currentQuestionIndex + 1, total);
        quizProgressText.setText(getString(R.string.quiz_progress, displayIndex, total));
        quizProgressBar.setProgress(total == 0 ? 0 : (displayIndex * 100 / total));
        scoreText.setText(moduleName != null ? moduleName : "Module quiz");
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
        String selectedText = selected.getText().toString();
        selectedByQuizId.put(current.getId(), selectedText);
        if (selectedText.equals(current.getCorrectAnswerText())) {
            correctStreak++;
            FeedbackHelper.successHaptic(optionsGroup);
        } else {
            correctStreak = 0;
            FeedbackHelper.errorHaptic(optionsGroup);
        }
        scoreText.setText(getString(R.string.quiz_streak_format, correctStreak));
        currentQuestionIndex++;
        handler.postDelayed(this::showNextQuestion, 400);
    }

    private void submitModuleQuiz() {
        try {
            JSONArray answers = new JSONArray();
            for (Question q : questionList) {
                JSONObject row = new JSONObject();
                row.put("quiz_id", q.getId());
                row.put("selected", selectedByQuizId.getOrDefault(q.getId(), ""));
                answers.put(row);
            }

            JSONObject body = new JSONObject();
            body.put("answers", answers);

            AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                    this,
                    com.android.volley.Request.Method.POST,
                    ApiConfig.api("module-quiz/" + moduleId + "/submit"),
                    body,
                    response -> {
                        boolean passed = response.optBoolean("passed", false);
                        int score = response.optInt("score", 0);
                        int total = response.optInt("total", questionList.size());
                        int percent = response.optInt("percent", 0);
                        String title = passed ? "Module quiz passed!" : "Module quiz score";
                        String scoreLine = "Your score: " + score + " / " + total + " (" + percent + "%)";
                        StringBuilder review = new StringBuilder(scoreLine);
                        review.append("\n\n").append(getString(R.string.quiz_review_title)).append("\n");
                        try {
                            JSONArray rows = response.optJSONArray("review");
                            if (rows != null) {
                                for (int i = 0; i < rows.length(); i++) {
                                    JSONObject row = rows.getJSONObject(i);
                                    review.append("\n• ");
                                    if (row.optBoolean("is_correct", false)) {
                                        review.append("Correct");
                                    } else {
                                        review.append("Wrong — answer: ").append(row.optString("correct", ""));
                                    }
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        JSONObject engagement = response.optJSONObject("engagement");
                        if (engagement != null) {
                            review.append("\n\n+").append(engagement.optInt("xp_awarded", 0)).append(" Quantum XP");
                        }
                        review.append("\n\n").append(response.optString("message", ""));
                        if (passed) {
                            FeedbackHelper.celebrate(this, title, review.toString(), () -> {
                                ModuleFlowHelper.backToCourse(this, subjectId);
                                finish();
                            });
                        } else {
                            new AlertDialog.Builder(this)
                                    .setTitle(title)
                                    .setMessage(review.toString())
                                    .setCancelable(false)
                                    .setPositiveButton("OK", (d, w) -> {
                                        ModuleFlowHelper.restartModuleFromFirstLesson(
                                                this, moduleId, moduleName, subjectId);
                                        finish();
                                    })
                                    .show();
                        }
                    },
                    error -> Toast.makeText(this, "Submit failed", Toast.LENGTH_SHORT).show()
            );
            Volley.newRequestQueue(this).add(request);
        } catch (Exception e) {
            Toast.makeText(this, "Submit error", Toast.LENGTH_SHORT).show();
        }
    }
}
