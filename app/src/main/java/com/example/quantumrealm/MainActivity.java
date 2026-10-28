package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText loginEmail;
    private EditText loginPassword;
    private Button loginButton;
    private Button goRegister;
    private CheckBox showLoginPassword;
    private TextView forgotPasswordTextView;
    private RequestQueue requestQueue;
    private static final String LOGIN_URL = ApiConfig.api("login");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (AuthHelper.isLoggedIn(this)) {
            startActivity(new Intent(this, StudyJourney.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        goRegister = findViewById(R.id.go_register);
        showLoginPassword = findViewById(R.id.show_login_password);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);

        requestQueue = Volley.newRequestQueue(this); // Initialize Volley RequestQueue

        showLoginPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                loginPassword.setTransformationMethod(null);
            } else {
                loginPassword.setTransformationMethod(new PasswordTransformationMethod());
            }
        });

        forgotPasswordTextView.setOnClickListener(v -> showForgotPasswordDialog());

        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(MainActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else {
                loginUser(email, password);
            }
        });

        goRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
            finish();
        });
    }

    private void loginUser(String email, String password) {
        try {
            JSONObject json = new JSONObject();
            json.put("email", email);
            json.put("password", password);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, LOGIN_URL, json,
                    response -> {
                        Log.d("API_RESPONSE", "Response: " + response.toString());

                        try {
                            if (response.has("token")) {
                                String token = response.getString("token");
                                AuthHelper.saveToken(MainActivity.this, token);

                                runOnUiThread(() -> {
                                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, StudyJourney.class);
                                    startActivity(intent);
                                    finish();
                                });
                            } else {
                                runOnUiThread(() ->
                                        Toast.makeText(MainActivity.this, "Login failed: Token not found", Toast.LENGTH_SHORT).show());
                            }
                        } catch (Exception e) {
                            Log.e("JSON_ERROR", "Parsing error: " + e.getMessage());
                            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    },
                    error -> {
                        Log.e("API_ERROR", "Request failed: " + error.toString());
                        Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                    }
            );

            requestQueue.add(request);
        } catch (Exception e) {
            Log.e("ERROR", "Exception: " + e.getMessage());
            Toast.makeText(MainActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }


    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Reset Password");

        final EditText input = new EditText(this);
        input.setHint("Enter your registered email");
        builder.setView(input);

        builder.setPositiveButton("Send Email", (dialog, which) -> {
            String email = input.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please enter your email", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(MainActivity.this, "Please enter a valid email address", Toast.LENGTH_SHORT).show();
            } else {
                sendPasswordResetEmail(email);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void sendPasswordResetEmail(String email) {
        Toast.makeText(MainActivity.this, "Password reset request sent", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.auth_menu, menu);
        Music.bindMuteMenuItem(this, menu.findItem(R.id.action_mute_music));
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Music.bindMuteMenuItem(this, menu.findItem(R.id.action_mute_music));
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (Music.handleMuteMenuItem(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
