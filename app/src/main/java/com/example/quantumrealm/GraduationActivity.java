package com.example.quantumrealm;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.print.PrintAttributes;
import android.print.PrintDocumentAdapter;
import android.print.PrintManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.Volley;
import com.google.android.material.button.MaterialButton;

public class GraduationActivity extends LogoutAppCompatActivity {

    private WebView webView;
    private String certificateHtml = "";

    public static Intent intent(Context context) {
        return new Intent(context, GraduationActivity.class);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graduation);

        webView = findViewById(R.id.graduationWebView);
        TextView subtitle = findViewById(R.id.graduationSubtitle);
        MaterialButton printButton = findViewById(R.id.graduationPrintButton);
        MaterialButton doneButton = findViewById(R.id.graduationDoneButton);

        webView.getSettings().setJavaScriptEnabled(false);
        webView.setWebViewClient(new WebViewClient());

        String name = getIntent().getStringExtra("student_name");
        if (name != null && !name.isEmpty()) {
            subtitle.setText(getString(R.string.graduation_subtitle_named, name));
        }

        printButton.setOnClickListener(v -> printCertificate());
        doneButton.setOnClickListener(v -> {
            ModuleFlowHelper.navigateToLearningGalaxy(this);
            finish();
        });

        loadCertificate();
    }

    private void loadCertificate() {
        AuthenticatedJsonObjectRequest request = new AuthenticatedJsonObjectRequest(
                this,
                Request.Method.GET,
                ApiConfig.api("graduation/certificate"),
                null,
                response -> {
                    if (!response.optBoolean("success", false)) {
                        Toast.makeText(this, R.string.graduation_load_failed, Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    certificateHtml = response.optString("html", "");
                    if (certificateHtml.isEmpty()) {
                        Toast.makeText(this, R.string.graduation_not_ready, Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    webView.loadDataWithBaseURL(null, certificateHtml, "text/html", "UTF-8", null);
                },
                error -> {
                    Toast.makeText(this, R.string.graduation_load_failed, Toast.LENGTH_LONG).show();
                    finish();
                }
        );
        Volley.newRequestQueue(this).add(request);
    }

    private void printCertificate() {
        if (certificateHtml.isEmpty()) {
            Toast.makeText(this, R.string.graduation_not_ready, Toast.LENGTH_SHORT).show();
            return;
        }
        PrintManager printManager = (PrintManager) getSystemService(PRINT_SERVICE);
        if (printManager == null) {
            Toast.makeText(this, R.string.graduation_print_unavailable, Toast.LENGTH_SHORT).show();
            return;
        }
        PrintDocumentAdapter adapter = webView.createPrintDocumentAdapter("QuantumRealmCertificate");
        printManager.print("Quantum Realm Certificate", adapter, new PrintAttributes.Builder().build());
    }
}
