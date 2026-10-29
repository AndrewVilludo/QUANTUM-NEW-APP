package com.example.quantumrealm;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Filter;
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
import com.google.android.material.textfield.MaterialAutoCompleteTextView;

import org.json.JSONObject;

public class Register extends AppCompatActivity {

    private EditText editTextUsername;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;
    private MaterialButton buttonRegister;
    private MaterialButton goLogin;
    private MaterialAutoCompleteTextView yearLevelAutoCompleteTextView;
    private String[] yearLevels;
    private String selectedYearLevel = "";
    private TextView registerFormError;
    private TextView registerEmailError;
    private TextView registerPasswordError;
    private TextView registerYearError;
    private ProgressBar registerProgress;

    private static final String REGISTER_URL = ApiConfig.api("register");

    private RequestQueue requestQueue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        MaterialToolbar toolbar = findViewById(R.id.authToolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        requestQueue = Volley.newRequestQueue(this);

        yearLevelAutoCompleteTextView = findViewById(R.id.YearlevelAutoCompleteTextView);
        yearLevels = getResources().getStringArray(R.array.auth_year_levels);
        setupYearLevelDropdown();

        editTextUsername = findViewById(R.id.Username);
        editTextEmail = findViewById(R.id.Email);
        editTextPassword = findViewById(R.id.Password);
        editTextConfirmPassword = findViewById(R.id.ConfirmPassword);
        buttonRegister = findViewById(R.id.register_button);
        goLogin = findViewById(R.id.go_login);
        registerFormError = findViewById(R.id.register_form_error);
        registerEmailError = findViewById(R.id.register_email_error);
        registerPasswordError = findViewById(R.id.register_password_error);
        registerYearError = findViewById(R.id.register_year_error);
        registerProgress = findViewById(R.id.register_progress);

        TextWatcher clearErrorsWatcher = new SimpleTextWatcher(this::clearRegisterErrors);
        editTextUsername.addTextChangedListener(clearErrorsWatcher);
        editTextEmail.addTextChangedListener(clearErrorsWatcher);
        editTextPassword.addTextChangedListener(clearErrorsWatcher);
        editTextConfirmPassword.addTextChangedListener(clearErrorsWatcher);
        buttonRegister.setOnClickListener(v -> {
            String username = editTextUsername.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();
            String password = editTextPassword.getText().toString().trim();
            String confirmPassword = editTextConfirmPassword.getText().toString().trim();
            clearRegisterErrors();

            String yearLevel = resolveSelectedYearLevel();
            if (yearLevel.isEmpty()) {
                showFieldError(registerYearError, getString(R.string.auth_error_year_required));
                return;
            }
            if (!isAllowedYearLevel(yearLevel)) {
                showFieldError(registerYearError, getString(R.string.auth_error_year_invalid));
                return;
            }
            if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                showFieldError(registerFormError, getString(R.string.auth_error_fields_required));
                return;
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showFieldError(registerEmailError, getString(R.string.auth_error_email_invalid));
                return;
            }
            if (!password.equals(confirmPassword)) {
                showFieldError(registerPasswordError, getString(R.string.auth_error_password_mismatch));
                return;
            }
            registerUser(username, email, password, confirmPassword, yearLevel);
        });

        goLogin.setOnClickListener(v -> {
            Intent intent = new Intent(Register.this, MainActivity.class);
            startActivity(intent);
            overridePendingTransition(R.anim.auth_slide_in_left, R.anim.auth_slide_out_right);
            finish();
        });
    }

    private void setupYearLevelDropdown() {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this,
                R.layout.item_auth_dropdown,
                yearLevels
        ) {
            @Override
            public Filter getFilter() {
                return new YearLevelDropdownFilter(this, yearLevels);
            }
        };
        yearLevelAutoCompleteTextView.setAdapter(adapter);
        yearLevelAutoCompleteTextView.setKeyListener(null);
        yearLevelAutoCompleteTextView.setOnClickListener(v -> yearLevelAutoCompleteTextView.showDropDown());
        yearLevelAutoCompleteTextView.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                yearLevelAutoCompleteTextView.showDropDown();
            }
        });
        yearLevelAutoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            if (position >= 0 && position < yearLevels.length) {
                selectedYearLevel = yearLevels[position];
                yearLevelAutoCompleteTextView.setText(selectedYearLevel, false);
                clearRegisterErrors();
            }
        });
    }

    private String resolveSelectedYearLevel() {
        if (!selectedYearLevel.isEmpty()) {
            return selectedYearLevel;
        }
        String displayed = yearLevelAutoCompleteTextView.getText().toString().trim();
        if (isAllowedYearLevel(displayed)) {
            selectedYearLevel = displayed;
            return selectedYearLevel;
        }
        return "";
    }

    private boolean isAllowedYearLevel(String value) {
        if (value == null || value.isEmpty()) {
            return false;
        }
        for (String level : yearLevels) {
            if (level.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private void clearRegisterErrors() {
        hideFieldError(registerFormError);
        hideFieldError(registerEmailError);
        hideFieldError(registerPasswordError);
        hideFieldError(registerYearError);
    }

    private void showFieldError(TextView target, String message) {
        target.setText(message);
        target.setVisibility(View.VISIBLE);
    }

    private void hideFieldError(TextView target) {
        target.setText("");
        target.setVisibility(View.GONE);
    }

    private void setRegisterLoading(boolean loading) {
        buttonRegister.setEnabled(!loading);
        goLogin.setEnabled(!loading);
        registerProgress.setVisibility(loading ? View.VISIBLE : View.GONE);
        buttonRegister.setText(loading ? R.string.auth_loading_register : R.string.auth_register_button);
        buttonRegister.setAlpha(loading ? 0.88f : 1f);
    }

    private void registerUser(String username, String email, String password, String confirmPassword, String yearLevel) {
        setRegisterLoading(true);
        try {
            JSONObject json = new JSONObject();
            json.put("name", username);
            json.put("email", email);
            json.put("password", password);
            json.put("password_confirmation", confirmPassword);
            json.put("year_level", yearLevel);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST, REGISTER_URL, json,
                    response -> {
                        setRegisterLoading(false);
                        String message = response.optString("message", "Registered successfully");
                        Toast.makeText(Register.this, message, Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(Register.this, MainActivity.class);
                        startActivity(intent);
                        overridePendingTransition(R.anim.auth_slide_in_left, R.anim.auth_slide_out_right);
                        finish();
                    },
                    error -> {
                        setRegisterLoading(false);
                        showFieldError(
                                registerFormError,
                                VolleyErrorHelper.message(
                                        Register.this,
                                        error,
                                        R.string.auth_error_register_failed
                                )
                        );
                    }
            );

            requestQueue.add(request);
        } catch (Exception e) {
            setRegisterLoading(false);
            showFieldError(registerFormError, "Error: " + e.getMessage());
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

    /** Keeps the exposed dropdown list fixed to the four year options (no free-text filtering). */
    private static final class YearLevelDropdownFilter extends Filter {
        private final ArrayAdapter<String> adapter;
        private final String[] options;

        YearLevelDropdownFilter(ArrayAdapter<String> adapter, String[] options) {
            this.adapter = adapter;
            this.options = options;
        }

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults results = new FilterResults();
            results.values = options;
            results.count = options.length;
            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            adapter.notifyDataSetChanged();
        }
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
