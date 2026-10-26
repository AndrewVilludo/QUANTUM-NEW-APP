package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MiniGame extends AppCompatActivity {

    private Spinner languageSpinner;
    private Button resetButton, nextButton;
    private TextView questionText, outputTextView;
    private LinearLayout blockSourceLayout, dropZoneLayout;

    private List<CodeBlock> codeBlocks = new ArrayList<>();
    private List<Integer> solutionOrder = new ArrayList<>();
    private String expectedOutput = "";

    private DragAndDropHandler dragAndDropHandler;

    private JSONObject jsonData;
    private int currentQuestionIndex = 0;

    private List<String> languageNames = new ArrayList<>();
    private JSONArray languagesArray;
    private JSONObject selectedLanguage;
    private JSONArray questionsArray;

    private int nextCourseId = -1;
    private String userEmail = "";

    private int yearId = -1;
    private String yearName = "";
    private int nextSubjectId = -1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mini);

        // Initialize intent data
        Intent intent = getIntent();
        nextSubjectId = intent.getIntExtra("next_subject_id", -1); // 🔧 Add this
        nextCourseId = intent.getIntExtra("next_course_id", -1);
        userEmail = intent.getStringExtra("user_email");

        yearId = getIntent().getIntExtra("year_id", -1);
        yearName = getIntent().getStringExtra("year_name");


        // View bindings
        languageSpinner = findViewById(R.id.languageSpinner);
        resetButton = findViewById(R.id.resetButton);
        dropZoneLayout = findViewById(R.id.dropZoneLayout);
        blockSourceLayout = findViewById(R.id.blockSourceLayout);
        questionText = findViewById(R.id.questionText);
        outputTextView = findViewById(R.id.outputTextView);

        nextButton = new Button(this);
        nextButton.setText("Next");
        ((LinearLayout) findViewById(R.id.rootLayout)).addView(nextButton);
        nextButton.setEnabled(false);

        // Set listeners
        resetButton.setOnClickListener(v -> resetGame());
        nextButton.setOnClickListener(v -> loadNextQuestion());

        try {
            String json = loadJSONFromAsset("programming_blocks.json");
            jsonData = new JSONObject(json);
            languagesArray = jsonData.getJSONArray("languages");

            for (int i = 0; i < languagesArray.length(); i++) {
                languageNames.add(languagesArray.getJSONObject(i).getString("name"));
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, languageNames);
            languageSpinner.setAdapter(adapter);

            languageSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    try {
                        selectedLanguage = languagesArray.getJSONObject(position);
                        questionsArray = selectedLanguage.getJSONArray("questions");
                        currentQuestionIndex = 0;
                        loadQuestion(currentQuestionIndex);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadNextQuestion() {
        currentQuestionIndex++;
        if (currentQuestionIndex < questionsArray.length()) {
            loadQuestion(currentQuestionIndex);
        } else {
            currentQuestionIndex = 0;
            loadQuestion(currentQuestionIndex);
        }
    }

    private void loadQuestion(int index) {
        try {
            resetGame();
            JSONObject questionObj = questionsArray.getJSONObject(index);

            questionText.setText(questionObj.getString("question"));

            solutionOrder.clear();
            JSONArray solutionArray = questionObj.getJSONArray("solution_order");
            for (int i = 0; i < solutionArray.length(); i++) {
                solutionOrder.add(solutionArray.getInt(i));
            }

            codeBlocks.clear();
            JSONArray blocksArray = questionObj.getJSONArray("blocks");
            for (int i = 0; i < blocksArray.length(); i++) {
                JSONObject block = blocksArray.getJSONObject(i);
                int id = block.getInt("id");
                String text = block.getString("text");
                codeBlocks.add(new CodeBlock(id, text));
            }

            expectedOutput = questionObj.optString("expected_output", "");

            BlockRandomizer.randomizeBlocks(codeBlocks);

            dragAndDropHandler = new DragAndDropHandler(
                    this,
                    dropZoneLayout,
                    blockSourceLayout,
                    solutionOrder,
                    expectedOutput,
                    outputTextView
            );

            blockSourceLayout.removeAllViews();
            dropZoneLayout.removeAllViews();

            for (CodeBlock codeBlock : codeBlocks) {
                TextView blockView = createBlockView(codeBlock);
                blockSourceLayout.addView(blockView);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private TextView createBlockView(CodeBlock block) {
        TextView textView = new TextView(this);
        textView.setText(block.text);
        textView.setTextSize(16);
        textView.setBackgroundResource(R.drawable.block_background);
        textView.setPadding(16, 16, 16, 16);
        textView.setTextColor(getResources().getColor(android.R.color.black));
        textView.setTag(block.id);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(8, 8, 8, 8);
        textView.setLayoutParams(params);

        textView.setOnTouchListener(dragAndDropHandler.new BlockTouchListener());

        return textView;
    }

    private void resetGame() {
        if (dragAndDropHandler != null) dragAndDropHandler.reset();
        blockSourceLayout.removeAllViews();

        for (CodeBlock codeBlock : codeBlocks) {
            TextView blockView = createBlockView(codeBlock);
            blockSourceLayout.addView(blockView);
        }

        nextButton.setEnabled(false);
    }

    private String loadJSONFromAsset(String filename) throws IOException {
        InputStream is = getAssets().open(filename);
        int size = is.available();
        byte[] buffer = new byte[size];
        is.read(buffer);
        is.close();
        return new String(buffer, StandardCharsets.UTF_8);
    }

    public void checkAnswer(boolean isCorrect) {
        if (isCorrect && currentQuestionIndex == questionsArray.length() - 1) {
            // Log for debugging
            Log.d("MiniGame", "Answer is correct. Redirecting to YearActivity...");

            // Unlock the next course
            YearActivity.unlockCourse(this, userEmail, nextCourseId);

            // Unlock the next subject
            YearActivity.unlockSubject(this, userEmail, nextSubjectId);

            // Redirect to YearActivity
            Intent intent = new Intent(MiniGame.this, YearActivity.class);
            intent.putExtra("user_email", userEmail);
            intent.putExtra("year_id", yearId);
            intent.putExtra("year_name", yearName);
            startActivity(intent);
            finish(); // Close the current activity
        }
    }


}
