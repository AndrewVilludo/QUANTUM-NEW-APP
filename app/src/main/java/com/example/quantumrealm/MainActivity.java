package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;

import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    private EditText loginEmail;
    private EditText loginPassword;
    private MaterialButton loginButton;
    private MaterialButton goRegister;
    private TextView forgotPasswordTextView;
    private TextView loginFormError;
    private TextView loginEmailError;
    private TextView loginPasswordError;
    private ProgressBar loginProgress;
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

        MaterialToolbar toolbar = findViewById(R.id.authToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        loginEmail = findViewById(R.id.login_email);
        loginPassword = findViewById(R.id.login_password);
        loginButton = findViewById(R.id.login_button);
        goRegister = findViewById(R.id.go_register);
        forgotPasswordTextView = findViewById(R.id.forgotPasswordTextView);
        loginFormError = findViewById(R.id.login_form_error);
        loginEmailError = findViewById(R.id.login_email_error);
        loginPasswordError = findViewById(R.id.login_password_error);
        loginProgress = findViewById(R.id.login_progress);

        requestQueue = Volley.newRequestQueue(this);

        TextWatcher clearErrorsWatcher = new SimpleTextWatcher(this::clearLoginErrors);
        loginEmail.addTextChangedListener(clearErrorsWatcher);
        loginPassword.addTextChangedListener(clearErrorsWatcher);

        forgotPasswordTextView.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            intent.putExtra(ForgotPasswordActivity.EXTRA_EMAIL, loginEmail.getText().toString().trim());
            startActivity(intent);
            overridePendingTransition(R.anim.auth_slide_in_right, R.anim.auth_slide_out_left);
        });

        loginButton.setOnClickListener(v -> {
            String email = loginEmail.getText().toString().trim();
            String password = loginPassword.getText().toString().trim();

            clearLoginErrors();
            if (email.isEmpty()) {
                showFieldError(loginEmailError, getString(R.string.auth_error_email_required));
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showFieldError(loginEmailError, getString(R.string.auth_error_email_invalid));
                return;
            }
            if (password.isEmpty()) {
                showFieldError(loginPasswordError, getString(R.string.auth_error_password_required));
                return;
            }
            loginUser(email, password);
        });

        goRegister.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, Register.class);
            startActivity(intent);
            overridePendingTransition(R.anim.auth_slide_in_right, R.anim.auth_slide_out_left);
            finish();
        });
    }

    private void clearLoginErrors() {
        hideFieldError(loginFormError);
        hideFieldError(loginEmailError);
        hideFieldError(loginPasswordError);
    }

    private void showFieldError(TextView target, String message) {
        target.setText(message);
        target.setVisibility(View.VISIBLE);
    }

    private void hideFieldError(TextView target) {
        target.setText("");
        target.setVisibility(View.GONE);
    }

    private void setLoginLoading(boolean loading) {
        loginButton.setEnabled(!loading);
        goRegister.setEnabled(!loading);
        loginProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        loginButton.setText(loading ? R.string.auth_loading_login : R.string.auth_login_button);
        loginButton.setAlpha(loading ? 0.88f : 1f);
    }

    private void loginUser(String email, String password) {
        setLoginLoading(true);
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
                                if (response.has("user")) {
                                    JSONObject user = response.getJSONObject("user");
                                    AuthHelper.saveUserName(MainActivity.this, user.optString("name", ""));
                                }

                                runOnUiThread(() -> {
                                    setLoginLoading(false);
                                    Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
                                    Intent intent = new Intent(MainActivity.this, StudyJourney.class);
                                    startActivity(intent);
                                    finish();
                                });
                            } else {
                                runOnUiThread(() -> {
                                    setLoginLoading(false);
                                    showFieldError(loginFormError, "Login failed: Token not found");
                                });
                            }
                        } catch (Exception e) {
                            Log.e("JSON_ERROR", "Parsing error: " + e.getMessage());
                            runOnUiThread(() -> {
                                setLoginLoading(false);
                                showFieldError(loginFormError, "Error: " + e.getMessage());
                            });
                        }
                    },
                    error -> {
                        Log.e("API_ERROR", "Request failed: " + error.toString());
                        runOnUiThread(() -> {
                            setLoginLoading(false);
                            showFieldError(
                                    loginFormError,
                                    VolleyErrorHelper.message(
                                            MainActivity.this,
                                            error,
                                            R.string.auth_error_login_failed
                                    )
                            );
                        });
                    }
            );

            requestQueue.add(request);
        } catch (Exception e) {
            Log.e("ERROR", "Exception: " + e.getMessage());
            setLoginLoading(false);
            showFieldError(loginFormError, "Error: " + e.getMessage());
        }
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

    private static final class SimpleTextWatcher implements TextWatcher {
        private final Runnable onChange;

        SimpleTextWatcher(Runnable onChange) {
            this.onChange = onChange;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            onChange.run();
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    }
}
