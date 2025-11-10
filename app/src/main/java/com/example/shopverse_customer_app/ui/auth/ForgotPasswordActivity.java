package com.example.shopverse_customer_app.ui.auth;

import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.viewmodel.AuthViewModel;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Forgot Password Activity
 * Handles password reset request via email
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";

    private TextInputEditText etPhone; // Named etPhone but used for email
    private Button btnSubmit;
    private ProgressBar progressBar;
    private ImageView btnBack;

    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Initialize views
        initViews();

        // Set up listeners
        setupListeners();

        // Observe ViewModel
        observeViewModel();
    }

    private void initViews() {
        etPhone = findViewById(R.id.etPhone);
        btnSubmit = findViewById(R.id.btnSubmit);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        // Submit button click
        btnSubmit.setOnClickListener(v -> {
            Log.d(TAG, "Submit button clicked");
            String email = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

            Log.d(TAG, "Email entered: " + email);

            // Validate email is not empty
            if (email.isEmpty()) {
                Log.w(TAG, "Email is empty");
                Toast.makeText(this, "Vui lòng nhập địa chỉ email", Toast.LENGTH_SHORT).show();
                etPhone.requestFocus();
                return;
            }

            // Validate email format
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Log.w(TAG, "Invalid email format: " + email);
                Toast.makeText(this, "Vui lòng nhập địa chỉ email hợp lệ", Toast.LENGTH_SHORT).show();
                etPhone.requestFocus();
                return;
            }

            Log.d(TAG, "Requesting password reset for email: " + email);
            authViewModel.requestPasswordReset(email);
        });

        // Back button
        btnBack.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });
    }

    private void observeViewModel() {
        // Observe loading state
        authViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                Log.d(TAG, "Loading state changed: " + isLoading);
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                btnSubmit.setEnabled(!isLoading);
            }
        });

        // Observe error messages
        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Log.e(TAG, "Error received: " + error);
                Toast.makeText(this, "Lỗi: " + error, Toast.LENGTH_LONG).show();
                authViewModel.clearError();
            }
        });

        // Observe password reset sent
        authViewModel.getPasswordResetSent().observe(this, sent -> {
            if (sent != null && sent) {
                Log.d(TAG, "Password reset email sent successfully");
                Toast.makeText(this,
                        "✉️ Email đặt lại mật khẩu đã được gửi!\n\nVui lòng kiểm tra hộp thư email của bạn và click vào link để đặt lại mật khẩu.",
                        Toast.LENGTH_LONG).show();

                // Delay finishing to let user see the toast
                btnSubmit.postDelayed(() -> finish(), 2000);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        authViewModel.resetStates();
    }
}
