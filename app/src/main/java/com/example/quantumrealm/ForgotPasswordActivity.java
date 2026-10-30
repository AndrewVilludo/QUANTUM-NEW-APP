package com.example.quantumrealm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONObject;

public class ForgotPasswordActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";

    private static final String FORGOT_PASSWORD_URL = ApiConfig.api("forgot-password");

    private TextInputEditText emailField;
    private MaterialButton sendButton;
    private MaterialButton goLogin;
    private TextView formError;
    private TextView emailError;
    private ProgressBar progress;
    private MaterialCardView formCard;
    private MaterialCardView successCard;
    private TextView successMessage;
    private TextView cmsPathHint;
    private MaterialButton openCmsButton;
    private TextView mailhogHint;
    private RequestQueue requestQueue;
    private String cmsForgotPasswordUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        MaterialToolbar toolbar = findViewById(R.id.authToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> {
            onBackPressed();
            overridePendingTransition(R.anim.auth_slide_in_left, R.anim.auth_slide_out_right);
        });

        requestQueue = Volley.newRequestQueue(this);

        emailField = findViewById(R.id.forgot_email);
        sendButton = findViewById(R.id.forgot_send_button);
        goLogin = findViewById(R.id.go_login);
        formError = findViewById(R.id.forgot_form_error);
        emailError = findViewById(R.id.forgot_email_error);
        progress = findViewById(R.id.forgot_progress);
        formCard = findViewById(R.id.forgot_form_card);
        successCard = findViewById(R.id.forgot_success_card);
        successMessage = findViewById(R.id.forgot_success_message);
        cmsPathHint = findViewById(R.id.forgot_cms_path_hint);
        openCmsButton = findViewById(R.id.forgot_open_cms_button);
        mailhogHint = findViewById(R.id.forgot_mailhog_hint);

        String prefilledEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        if (prefilledEmail != null && !prefilledEmail.isEmpty()) {
            emailField.setText(prefilledEmail);
        }

        if (BuildConfig.DEBUG) {
            mailhogHint.setVisibility(View.VISIBLE);
        }

        emailField.addTextChangedListener(new SimpleTextWatcher(this::clearErrors));

        sendButton.setOnClickListener(v -> submitResetRequest());

        goLogin.setOnClickListener(v -> {
            Intent intent = new Intent(ForgotPasswordActivity.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.auth_slide_in_left, R.anim.auth_slide_out_right);
            finish();
        });

        openCmsButton.setOnClickListener(v -> openCmsForgotPasswordPage());
    }

    private void openCmsForgotPasswordPage() {
        String url = cmsForgotPasswordUrl != null && !cmsForgotPasswordUrl.isEmpty()
                ? ApiConfig.rewriteCmsUrlForDevice(cmsForgotPasswordUrl)
                : ApiConfig.rewriteCmsUrlForDevice(ApiConfig.BASE_URL + "/forgot-password");
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }

    private void submitResetRequest() {
        String email = emailField.getText() != null ? emailField.getText().toString().trim() : "";
        clearErrors();

        if (email.isEmpty()) {
            showFieldError(emailError, getString(R.string.auth_error_email_required));
            return;
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showFieldError(emailError, getString(R.string.auth_error_email_invalid));
            return;
        }

        setLoading(true);
        try {
            JSONObject json = new JSONObject();
            json.put("email", email);

            HostFallbackJsonObjectRequest request = new HostFallbackJsonObjectRequest(
                    this,
                    Request.Method.POST, FORGOT_PASSWORD_URL, json,
                    response -> runOnUiThread(() -> {
                        setLoading(false);
                        cmsForgotPasswordUrl = response.optString("forgot_password_url", "");
                        showSuccessState(response.optString(
                                "message",
                                getString(R.string.auth_forgot_success_body)
                        ));
                    }),
                    error -> runOnUiThread(() -> {
                        setLoading(false);
                        String msg = VolleyErrorHelper.message(
                                ForgotPasswordActivity.this,
                                error,
                                R.string.auth_forgot_error_generic
                        );
                        if (error.networkResponse != null && error.networkResponse.statusCode == 422) {
                            showFieldError(emailError, msg);
                        } else {
                            showFieldError(formError, msg);
                        }
                    })
            );

            requestQueue.add(request);
        } catch (Exception e) {
            setLoading(false);
            showFieldError(formError, getString(R.string.auth_forgot_error_generic));
        }
    }

    private void showSuccessState(String message) {
        successMessage.setText(message);
        formCard.setVisibility(View.GONE);
        successCard.setVisibility(View.VISIBLE);
        sendButton.setEnabled(false);
        emailField.setEnabled(false);

        String deviceCmsBase = ApiConfig.rewriteCmsUrlForDevice(
                cmsForgotPasswordUrl != null && !cmsForgotPasswordUrl.isEmpty()
                        ? cmsForgotPasswordUrl
                        : ApiConfig.BASE_URL + "/forgot-password");
        cmsPathHint.setText(getString(R.string.auth_forgot_cms_path_hint, deviceCmsBase + "/reset-password/…"));
        cmsPathHint.setVisibility(View.VISIBLE);
        openCmsButton.setVisibility(View.VISIBLE);

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) goLogin.getLayoutParams();
        params.topToBottom = successCard.getId();
        goLogin.setLayoutParams(params);
    }

    private void clearErrors() {
        hideFieldError(formError);
        hideFieldError(emailError);
    }

    private void showFieldError(TextView target, String message) {
        target.setText(message);
        target.setVisibility(View.VISIBLE);
    }

    private void hideFieldError(TextView target) {
        target.setText("");
        target.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        sendButton.setEnabled(!loading);
        goLogin.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
        sendButton.setText(loading ? R.string.auth_loading_forgot : R.string.auth_forgot_button);
        sendButton.setAlpha(loading ? 0.88f : 1f);
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
