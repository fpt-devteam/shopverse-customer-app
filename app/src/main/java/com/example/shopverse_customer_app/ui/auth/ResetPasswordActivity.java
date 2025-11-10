package com.example.shopverse_customer_app.ui.auth;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.shopverse_customer_app.MainActivity;
import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.User;
import com.example.shopverse_customer_app.data.remote.RetrofitClient;
import com.example.shopverse_customer_app.data.remote.SupabaseAuthApi;
import com.example.shopverse_customer_app.utils.ErrorParser;
import com.google.android.material.textfield.TextInputEditText;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Reset Password Activity
 * Handles password reset from email deep link
 * Deep link format: shopverse://auth/reset?access_token=xxx&refresh_token=xxx&type=recovery
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ResetPasswordActivity";

    private TextInputEditText etNewPassword;
    private TextInputEditText etConfirmPassword;
    private Button btnResetPassword;
    private ProgressBar progressBar;
    private ImageView btnBack;

    private String accessToken;
    private String refreshToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        Log.d(TAG, "=== ResetPasswordActivity Created ===");

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize views
        initViews();

        // Handle deep link
        handleDeepLink(getIntent());

        // Setup listeners
        setupListeners();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    private void initViews() {
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnResetPassword = findViewById(R.id.btnResetPassword);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
    }

    /**
     * Handle deep link from password reset email
     * Supabase sends tokens in URL fragment (after #), not query params
     * Expected format: shopverse://auth/reset#access_token=xxx&refresh_token=xxx&type=recovery
     */
    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();

        if (data != null) {
            String scheme = data.getScheme();
            String host = data.getHost();
            String path = data.getPath();

            Log.d(TAG, "Deep link received");
            Log.d(TAG, "Scheme: " + scheme);
            Log.d(TAG, "Host: " + host);
            Log.d(TAG, "Path: " + path);
            Log.d(TAG, "Full URI: " + data.toString());

            if ("shopverse".equals(scheme) && "auth".equals(host) && "/reset".equals(path)) {
                // Supabase sends tokens in URL FRAGMENT (after #), not query parameters
                String fragment = data.getFragment();
                Log.d(TAG, "URL Fragment: " + (fragment != null ? fragment : "null"));

                if (fragment != null && !fragment.isEmpty()) {
                    // Parse fragment as query parameters
                    // Fragment format: access_token=xxx&refresh_token=xxx&type=recovery
                    String[] params = fragment.split("&");
                    for (String param : params) {
                        String[] keyValue = param.split("=", 2);
                        if (keyValue.length == 2) {
                            String key = keyValue[0];
                            String value = keyValue[1];

                            Log.d(TAG, "Fragment param: " + key + " = " + (value.length() > 20 ? value.substring(0, 20) + "..." : value));

                            switch (key) {
                                case "access_token":
                                    accessToken = value;
                                    break;
                                case "refresh_token":
                                    refreshToken = value;
                                    break;
                                case "type":
                                    Log.d(TAG, "Type: " + value);
                                    break;
                            }
                        }
                    }
                } else {
                    Log.w(TAG, "No URL fragment found, trying query parameters as fallback...");
                    // Fallback to query parameters
                    accessToken = data.getQueryParameter("access_token");
                    refreshToken = data.getQueryParameter("refresh_token");
                }

                Log.d(TAG, "Access token present: " + (accessToken != null && !accessToken.isEmpty()));
                Log.d(TAG, "Refresh token present: " + (refreshToken != null && !refreshToken.isEmpty()));

                if (accessToken == null || accessToken.isEmpty()) {
                    Log.e(TAG, "ERROR: Access token is missing!");
                    Toast.makeText(this, "Link không hợp lệ hoặc đã hết hạn.\n\nVui lòng yêu cầu đặt lại mật khẩu lại.", Toast.LENGTH_LONG).show();
                    finish();
                    return;
                }

                Log.d(TAG, "✅ Recovery token extracted successfully from URL fragment");
                Log.d(TAG, "Access token length: " + accessToken.length());
            } else {
                Log.e(TAG, "Invalid deep link format");
                Toast.makeText(this, "Link không hợp lệ", Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Log.w(TAG, "No deep link data found - activity might have been opened directly");
            // Don't finish here - user might have navigated directly
        }
    }

    private void setupListeners() {
        // Reset password button
        btnResetPassword.setOnClickListener(v -> validateAndResetPassword());

        // Back button
        btnBack.setOnClickListener(v -> finish());
    }

    /**
     * Validate password inputs and submit reset request
     */
    private void validateAndResetPassword() {
        String newPassword = etNewPassword.getText() != null ? etNewPassword.getText().toString() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString() : "";

        Log.d(TAG, "=== Validate Password Reset ===");
        Log.d(TAG, "New password length: " + newPassword.length());
        Log.d(TAG, "Confirm password length: " + confirmPassword.length());

        // Validate password is not empty
        if (newPassword.isEmpty()) {
            Log.w(TAG, "New password is empty");
            Toast.makeText(this, "Vui lòng nhập mật khẩu mới", Toast.LENGTH_SHORT).show();
            etNewPassword.requestFocus();
            return;
        }

        // Validate password length
        if (newPassword.length() < 6) {
            Log.w(TAG, "Password too short");
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show();
            etNewPassword.requestFocus();
            return;
        }

        // Validate passwords match
        if (!newPassword.equals(confirmPassword)) {
            Log.w(TAG, "Passwords don't match");
            Toast.makeText(this, "Mật khẩu xác nhận không khớp", Toast.LENGTH_SHORT).show();
            etConfirmPassword.requestFocus();
            return;
        }

        // Validate access token exists
        if (accessToken == null || accessToken.isEmpty()) {
            Log.e(TAG, "Access token is missing!");
            Toast.makeText(this, "Phiên làm việc không hợp lệ. Vui lòng yêu cầu đặt lại mật khẩu lại.", Toast.LENGTH_LONG).show();
            return;
        }

        Log.d(TAG, "Validation passed - submitting password reset");
        submitPasswordReset(newPassword);
    }

    /**
     * Submit password reset to Supabase using recovery access token
     */
    private void submitPasswordReset(String newPassword) {
        Log.d(TAG, "=== Submitting Password Reset ===");
        Log.d(TAG, "Using access token: " + accessToken.substring(0, Math.min(20, accessToken.length())) + "...");

        // Show loading
        setLoading(true);

        // Create request body
        JsonObject request = new JsonObject();
        request.addProperty("password", newPassword);

        Log.d(TAG, "Request body: " + request.toString());

        // Get Retrofit instance with the recovery access token
        RetrofitClient retrofitClient = RetrofitClient.getInstance();

        // Temporarily set the access token for this request
        retrofitClient.setAccessToken(accessToken);

        SupabaseAuthApi authApi = retrofitClient.getAuthApi();

        Log.d(TAG, "Calling Supabase API: PUT /auth/v1/user");

        authApi.resetPassword(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                setLoading(false);

                Log.d(TAG, "=== Password Reset Response ===");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response message: " + response.message());

                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.d(TAG, "✅ Password reset successful");
                    Log.d(TAG, "User email: " + (user.getEmail() != null ? user.getEmail() : "N/A"));

                    // Clear the temporary token
                    retrofitClient.clearAccessToken();

                    // Show success message
                    Toast.makeText(ResetPasswordActivity.this,
                            "✅ Đặt lại mật khẩu thành công!\n\nVui lòng đăng nhập với mật khẩu mới.",
                            Toast.LENGTH_LONG).show();

                    // Navigate to login screen
                    navigateToLogin();
                } else {
                    Log.e(TAG, "❌ Password reset failed");
                    Log.e(TAG, "Response code: " + response.code());

                    // Try to get error body
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Could not read error body", e);
                    }

                    String errorMsg = ErrorParser.parseError(response);
                    Log.e(TAG, "Parsed error: " + errorMsg);

                    Toast.makeText(ResetPasswordActivity.this,
                            "Lỗi: " + errorMsg,
                            Toast.LENGTH_LONG).show();

                    // Clear the temporary token
                    retrofitClient.clearAccessToken();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                setLoading(false);

                Log.e(TAG, "=== Password Reset Network Error ===");
                Log.e(TAG, "Error type: " + t.getClass().getSimpleName());
                Log.e(TAG, "Error message: " + t.getMessage());
                Log.e(TAG, "Stack trace:", t);

                String errorMsg = ErrorParser.parseError(t);
                Toast.makeText(ResetPasswordActivity.this,
                        "Lỗi kết nối: " + errorMsg,
                        Toast.LENGTH_LONG).show();

                // Clear the temporary token
                retrofitClient.clearAccessToken();
            }
        });
    }

    /**
     * Navigate to login screen after successful password reset
     */
    private void navigateToLogin() {
        Log.d(TAG, "Navigating to login screen");

        // Delay to let user see the success message
        btnResetPassword.postDelayed(() -> {
            Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        }, 2000);
    }

    /**
     * Show/hide loading state
     */
    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnResetPassword.setEnabled(!isLoading);
        etNewPassword.setEnabled(!isLoading);
        etConfirmPassword.setEnabled(!isLoading);
    }
}
