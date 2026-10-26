package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.airbnb.lottie.LottieAnimationView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class StudyJourney extends AppCompatActivity {

    private JSONObject journeyData;
    private int currentStep = 0;

    private TextView introText;
    private Button quitButton, yesButton, nextButton, startButton;
    private LottieAnimationView lottieAnimationView;
    private LottieAnimationView startAnimation;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_study);

        // UI references
        introText = findViewById(R.id.introText);
        quitButton = findViewById(R.id.quit);
        yesButton = findViewById(R.id.yes_tart);
        nextButton = findViewById(R.id.next);
        startButton = findViewById(R.id.start);
        lottieAnimationView = findViewById(R.id.lottieAnimationView);
        startAnimation = findViewById(R.id.startAnimation);

        // Hide all buttons initially
        quitButton.setVisibility(View.GONE);
        yesButton.setVisibility(View.GONE);
        nextButton.setVisibility(View.GONE);
        startButton.setVisibility(View.GONE);
        startAnimation.setVisibility(View.GONE);

        try {
            String jsonString = loadJSONFromAsset();
            journeyData = new JSONObject(jsonString);
            startJourney();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading journey data", Toast.LENGTH_SHORT).show();
        }

        // Button listeners
        quitButton.setOnClickListener(v -> handleOptionClick("quit"));
        yesButton.setOnClickListener(v -> handleOptionClick("next"));
        nextButton.setOnClickListener(v -> {
            currentStep++;
            proceedToNextStep();
        });
        startButton.setOnClickListener(v -> handleOptionClick("start"));
    }

    private String loadJSONFromAsset() {
        String json;
        try {
            InputStream is = getResources().openRawResource(R.raw.journey);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return json;
    }

    private void startJourney() {
        try {
            JSONObject intro = journeyData.getJSONObject("intro");
            String introMessage = intro.getString("message");
            introText.setText(introMessage);

            // Show Quit & Yes buttons
            quitButton.setVisibility(View.VISIBLE);
            yesButton.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void proceedToNextStep() {
        try {
            JSONArray rules = journeyData.getJSONArray("rules");
            if (currentStep <= rules.length() - 1) {
                String ruleMessage = rules.getJSONObject(currentStep).getString("message");
                introText.setText(ruleMessage);
            } else {
                nextButton.setVisibility(View.GONE);
                startButton.setVisibility(View.VISIBLE);
                startAnimation.setVisibility(View.VISIBLE);
                JSONObject start = journeyData.getJSONObject("start_game");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleOptionClick(String action) {
        switch (action) {
            case "quit":
                Toast.makeText(this, "You chose to quit the game!", Toast.LENGTH_SHORT).show();
                finish();
                break;

            case "next":
                quitButton.setVisibility(View.GONE);
                yesButton.setVisibility(View.GONE);
                nextButton.setVisibility(View.VISIBLE);
                introText.setText("Awesome! Before we get started, let me explain the rules.");
                break;

            case "start":
                Intent intent = new Intent(StudyJourney.this, Journey.class);
                startActivity(intent);
                finish();
                break;


            default:
                Toast.makeText(this, "Unknown action!", Toast.LENGTH_SHORT).show();
        }
    }
}
