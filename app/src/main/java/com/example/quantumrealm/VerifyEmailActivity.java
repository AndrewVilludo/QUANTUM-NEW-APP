package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
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

import org.json.JSONObject;

public class VerifyEmailActivity extends AppCompatActivity {

    public static final String EXTRA_EMAIL = "extra_email";
    public static final String EXTRA_PASSWORD = "extra_password";

    private static final String RESEND_URL = ApiConfig.api("email/verification-notification");

    private String accountEmail;
    private String accountPassword;

    private TextView verifyEmailLabel;
    private TextView verifyFormError;
    private TextView verifySuccess;
    private MaterialButton resendButton;
    private MaterialButton backLoginButton;
    private ProgressBar progress;
    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_email);

        MaterialToolbar toolbar = findViewById(R.id.authToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        toolbar.setNavigationOnClickListener(v -> finishWithLoginTransition());

        accountEmail = getIntent().getStringExtra(EXTRA_EMAIL);
        accountPassword = getIntent().getStringExtra(EXTRA_PASSWORD);

        verifyEmailLabel = findViewById(R.id.verify_email_label);
        verifyFormError = findViewById(R.id.verify_form_error);
        verifySuccess = findViewById(R.id.verify_success);
        resendButton = findViewById(R.id.verify_resend_button);
        backLoginButton = findViewById(R.id.verify_back_login);
        progress = findViewById(R.id.verify_progress);

        requestQueue = Volley.newRequestQueue(this);

        if (accountEmail != null && !accountEmail.isEmpty()) {
            verifyEmailLabel.setText(accountEmail);
        } else {
            verifyEmailLabel.setVisibility(View.GONE);
        }

        resendButton.setOnClickListener(v -> resendVerification());
        backLoginButton.setOnClickListener(v -> finishWithLoginTransition());
    }

    private void resendVerification() {
        if (accountEmail == null || accountEmail.isEmpty()
                || accountPassword == null || accountPassword.isEmpty()) {
            showError(getString(R.string.auth_verify_need_credentials));
            return;
        }

        hideError();
        verifySuccess.setVisibility(View.GONE);
        setLoading(true);

        try {
            JSONObject json = new JSONObject();
            json.put("email", accountEmail);
            json.put("password", accountPassword);

            HostFallbackJsonObjectRequest request = new HostFallbackJsonObjectRequest(
                    this,
                    Request.Method.POST,
                    RESEND_URL,
                    json,
                    response -> runOnUiThread(() -> {
                        setLoading(false);
                        verifySuccess.setText(response.optString(
                                "message",
                                getString(R.string.auth_verify_sent)
                        ));
                        verifySuccess.setVisibility(View.VISIBLE);
                    }),
                    error -> runOnUiThread(() -> {
                        setLoading(false);
                        showError(VolleyErrorHelper.message(
                                VerifyEmailActivity.this,
                                error,
                                R.string.auth_verify_error
                        ));
                    })
            );
            requestQueue.add(request);
        } catch (Exception e) {
            setLoading(false);
            showError(getString(R.string.auth_verify_error));
        }
    }

    private void finishWithLoginTransition() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (accountEmail != null) {
            intent.putExtra(MainActivity.EXTRA_PREFILL_EMAIL, accountEmail);
        }
        startActivity(intent);
        finish();
        overridePendingTransition(R.anim.auth_slide_in_left, R.anim.auth_slide_out_right);
    }

    private void showError(String message) {
        verifyFormError.setText(message);
        verifyFormError.setVisibility(View.VISIBLE);
    }

    private void hideError() {
        verifyFormError.setText("");
        verifyFormError.setVisibility(View.GONE);
    }

    private void setLoading(boolean loading) {
        resendButton.setEnabled(!loading);
        backLoginButton.setEnabled(!loading);
        progress.setVisibility(loading ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        finishWithLoginTransition();
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
