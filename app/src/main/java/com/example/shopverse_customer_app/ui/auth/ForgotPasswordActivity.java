package com.example.shopverse_customer_app.ui.auth;

import android.os.Bundle;
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
 * Handles password reset request
 */
public class ForgotPasswordActivity extends AppCompatActivity {

    private TextInputEditText etPhone;
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
            String phone = etPhone.getText() != null ? etPhone.getText().toString().trim() : "";

            if (phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số điện thoại hoặc email", Toast.LENGTH_SHORT).show();
                return;
            }

            // For now, assume it's an email (Supabase password reset works with email)
            // In production, you'd need to convert phone to email or use phone-based reset
            String email = phone;
            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Vui lòng nhập địa chỉ email hợp lệ", Toast.LENGTH_SHORT).show();
                return;
            }

            authViewModel.requestPasswordReset(email);
        });

        // Back button
        btnBack.setOnClickListener(v -> finish());
    }

    private void observeViewModel() {
        // Observe loading state
        authViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                btnSubmit.setEnabled(!isLoading);
            }
        });

        // Observe error messages
        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                authViewModel.clearError();
            }
        });

        // Observe password reset sent
        authViewModel.getPasswordResetSent().observe(this, sent -> {
            if (sent != null && sent) {
                Toast.makeText(this, "Email đặt lại mật khẩu đã được gửi. Vui lòng kiểm tra hộp thư của bạn.", Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        authViewModel.resetStates();
    }
}
