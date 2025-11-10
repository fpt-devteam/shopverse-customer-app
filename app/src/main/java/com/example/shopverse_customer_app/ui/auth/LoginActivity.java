package com.example.shopverse_customer_app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.lifecycle.ViewModelProvider;

import com.example.shopverse_customer_app.MainActivity;
import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.viewmodel.AuthViewModel;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

/**
 * Login Activity
 * Handles user login with email/password and social login options
 */
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private MaterialButton btnGoogleLogin;
    private TextView tvForgotPassword, tvRegister;
    private ProgressBar progressBar;
    private ImageView btnBack;

    private AuthViewModel authViewModel;
    private GoogleSignInClient googleSignInClient;

    // Activity result launcher for Google Sign-In
    private final ActivityResultLauncher<Intent> googleSignInLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                Log.d(TAG, "Google Sign-In result received");
                Log.d(TAG, "Result code: " + result.getResultCode());
                Log.d(TAG, "RESULT_OK: " + RESULT_OK);
                Log.d(TAG, "RESULT_CANCELED: " + RESULT_CANCELED);

                if (result.getResultCode() == RESULT_OK) {
                    Log.d(TAG, "Result is OK");
                    if (result.getData() != null) {
                        Log.d(TAG, "Intent data is not null");
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(result.getData());
                        handleGoogleSignInResult(task);
                    } else {
                        Log.e(TAG, "Intent data is null!");
                        Toast.makeText(this, "Google Sign-In failed: No data received", Toast.LENGTH_SHORT).show();
                    }
                } else if (result.getResultCode() == RESULT_CANCELED) {
                    Log.w(TAG, "Google Sign-In was canceled by user");
                    Toast.makeText(this, "Google Sign-In canceled", Toast.LENGTH_SHORT).show();
                } else {
                    Log.e(TAG, "Google Sign-In failed with result code: " + result.getResultCode());
                    Toast.makeText(this, "Google Sign-In failed", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Hide action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Initialize ViewModel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Initialize Google Sign-In client
        googleSignInClient = authViewModel.getGoogleSignInClient();

        // Initialize views
        initViews();

        // Set up listeners
        setupListeners();

        // Observe ViewModel
        observeViewModel();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
        progressBar = findViewById(R.id.progressBar);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        // Login button click
        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

            authViewModel.login(email, password);
        });

        // Register link click
        tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Forgot password click
        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
        });

        // Back button
        btnBack.setOnClickListener(v -> finish());

        // Google login - only if button exists in layout
        if (btnGoogleLogin != null) {
            btnGoogleLogin.setOnClickListener(v -> startGoogleSignIn());
        }
    }

    /**
     * Start Google Sign-In flow
     */
    private void startGoogleSignIn() {
        Log.d(TAG, "=== Starting Google Sign-In flow ===");
        Log.d(TAG, "GoogleSignInClient: " + (googleSignInClient != null ? "initialized" : "NULL"));

        if (googleSignInClient == null) {
            Log.e(TAG, "GoogleSignInClient is null! Cannot start sign-in");
            Toast.makeText(this, "Google Sign-In not initialized", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Sign out first to force account picker to appear every time
            Log.d(TAG, "Signing out from Google to show account picker...");
            googleSignInClient.signOut().addOnCompleteListener(this, task -> {
                Log.d(TAG, "Sign out completed, now showing account picker");

                // Now show the account picker
                Intent signInIntent = googleSignInClient.getSignInIntent();
                Log.d(TAG, "Sign-In Intent created: " + signInIntent);
                Log.d(TAG, "Launching Google Sign-In activity...");
                googleSignInLauncher.launch(signInIntent);
            });
        } catch (Exception e) {
            Log.e(TAG, "Error starting Google Sign-In", e);
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Handle Google Sign-In result
     */
    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        Log.d(TAG, "=== Handling Google Sign-In Result ===");
        Log.d(TAG, "Task is successful: " + completedTask.isSuccessful());
        Log.d(TAG, "Task is complete: " + completedTask.isComplete());

        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Log.d(TAG, "GoogleSignInAccount retrieved successfully");

            if (account != null) {
                Log.d(TAG, "Account is not null");
                Log.d(TAG, "Account email: " + account.getEmail());
                Log.d(TAG, "Account display name: " + account.getDisplayName());
                Log.d(TAG, "Account ID: " + account.getId());

                String idToken = account.getIdToken();
                String serverAuthCode = account.getServerAuthCode();

                Log.d(TAG, "ID Token is " + (idToken != null ? "PRESENT" : "NULL"));
                Log.d(TAG, "Server Auth Code is " + (serverAuthCode != null ? "PRESENT" : "NULL"));

                if (idToken != null) {
                    // Log first and last 20 characters of token for debugging
                    int tokenLength = idToken.length();
                    String tokenPreview = tokenLength > 40
                        ? idToken.substring(0, 20) + "..." + idToken.substring(tokenLength - 20)
                        : idToken;
                    Log.d(TAG, "ID Token preview: " + tokenPreview);
                    Log.d(TAG, "ID Token length: " + tokenLength);
                    Log.d(TAG, "Full ID Token: " + idToken);

                    // Authenticate with Supabase using the ID token
                    Log.d(TAG, "Calling authViewModel.loginWithGoogle() with ID token");
                    authViewModel.loginWithGoogle(idToken);
                } else if (serverAuthCode != null) {
                    Log.w(TAG, "ID token is null, but server auth code is present");
                    Log.d(TAG, "Server Auth Code: " + serverAuthCode);
                    Toast.makeText(this, "Got server auth code instead of ID token. Need to exchange it.", Toast.LENGTH_LONG).show();
                    // TODO: You'll need to exchange the server auth code for an ID token
                    // This requires a backend endpoint
                } else {
                    Log.e(TAG, "Both ID token and server auth code are null!");
                    Log.e(TAG, "This means Google Sign-In configuration is incorrect");
                    Toast.makeText(this, "Failed to get credentials from Google. Check configuration.", Toast.LENGTH_LONG).show();
                }
            } else {
                Log.e(TAG, "GoogleSignInAccount is null!");
                Toast.makeText(this, "Failed to get account information", Toast.LENGTH_SHORT).show();
            }
        } catch (ApiException e) {
            Log.e(TAG, "=== Google Sign-In Failed ===");
            Log.e(TAG, "Status code: " + e.getStatusCode());
            Log.e(TAG, "Status message: " + e.getStatusMessage());
            Log.e(TAG, "Error details: " + e.getMessage());
            Log.e(TAG, "Stack trace:", e);

            String errorMessage = "Google sign-in failed (Code: " + e.getStatusCode() + ")";
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error in handleGoogleSignInResult", e);
            Toast.makeText(this, "Unexpected error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void observeViewModel() {
        // Observe loading state
        authViewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                btnLogin.setEnabled(!isLoading);
            }
        });

        // Observe error messages
        authViewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                authViewModel.clearError();
            }
        });

        // Observe login success
        authViewModel.getLoginSuccess().observe(this, success -> {
            if (success != null && success) {
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show();
                navigateToHome();
            }
        });
    }

    private void navigateToHome() {
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        authViewModel.resetStates();
    }
}
