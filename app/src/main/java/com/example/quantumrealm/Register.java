package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class Register extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private Button buttonRegister;
    private Button go_login;
    private CheckBox checkBoxShowPassword;
    private AutoCompleteTextView yearLevelAutoCompleteTextView;

    private static final String REGISTER_URL = ApiConfig.api("register");

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Initialize Volley RequestQueue
        requestQueue = Volley.newRequestQueue(this);

        // Bind views
        yearLevelAutoCompleteTextView = findViewById(R.id.YearlevelAutoCompleteTextView);
        String[] yearLevels = new String[]{"1st year", "2nd year", "3rd year", "4th year"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, yearLevels);
        yearLevelAutoCompleteTextView.setAdapter(adapter);

        editTextUsername = findViewById(R.id.Username);
        editTextEmail = findViewById(R.id.Email);
        editTextPassword = findViewById(R.id.Password);
        editTextConfirmPassword = findViewById(R.id.ConfirmPassword);
        buttonRegister = findViewById(R.id.register_button);
        go_login = findViewById(R.id.go_login);
        checkBoxShowPassword = findViewById(R.id.showPassword);

        // Show/hide password functionality
        checkBoxShowPassword.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                editTextPassword.setTransformationMethod(null);
                editTextConfirmPassword.setTransformationMethod(null);
            } else {
                editTextPassword.setTransformationMethod(new PasswordTransformationMethod());
                editTextConfirmPassword.setTransformationMethod(new PasswordTransformationMethod());
            }
        });

        // Register button onClick listener
        buttonRegister.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();
            String yearLevel = yearLevelAutoCompleteTextView.getText().toString().trim();


            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || yearLevel.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(Register.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(Register.this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            } else if (!password.equals(confirmPassword)) {
                Toast.makeText(Register.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                registerUser(username, email, password, confirmPassword, yearLevel);
            }

        });

        // Go back to login activity
        go_login.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, MainActivity.class);
            startActivity(intent);
            finish();
        });
    }

    // Method to register user using API call via Volley
    private void registerUser(String username, String email, String password, String confirmPassword, String yearLevel) {
        try {
            JSONObject json = new JSONObject();
            json.put("name", username);
            json.put("email", email);
            json.put("password", password);
            json.put("password_confirmation", confirmPassword); // Laravel expects this
            json.put("year_level", yearLevel);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, REGISTER_URL, json,
                    response -> {
                        String message = response.optString("message", "Registered successfully");
                        Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(Register.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                    },
                    error -> {
                        String errorMessage = "Registration failed";
                        if (error.networkResponse != null && error.networkResponse.data != null) {
                            String responseBody = new String(error.networkResponse.data);
                            errorMessage += ": " + responseBody;
                        }
                        Toast.makeText(Register.this, errorMessage, Toast.LENGTH_LONG).show();
                    }
            );

            requestQueue.add(request);
        } catch (Exception e) {
            Toast.makeText(Register.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

}
