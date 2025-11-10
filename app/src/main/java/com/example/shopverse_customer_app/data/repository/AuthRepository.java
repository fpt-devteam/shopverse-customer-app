package com.example.shopverse_customer_app.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.shopverse_customer_app.data.model.AuthResponse;
import com.example.shopverse_customer_app.data.model.LoginRequest;
import com.example.shopverse_customer_app.data.model.Profile;
import com.example.shopverse_customer_app.data.model.RegisterRequest;
import com.example.shopverse_customer_app.data.remote.RetrofitClient;
import com.example.shopverse_customer_app.data.remote.SupabaseAuthApi;
import com.example.shopverse_customer_app.data.remote.SupabaseRestApi;
import com.example.shopverse_customer_app.utils.ErrorParser;
import com.example.shopverse_customer_app.utils.GoogleSignInManager;
import com.example.shopverse_customer_app.utils.TokenManager;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository class for authentication operations
 * Handles communication with Supabase Auth API and token management
 */
public class AuthRepository {

    private static final String TAG = "AuthRepository";

    private final SupabaseAuthApi authApi;
    private final SupabaseRestApi restApi;
    private final TokenManager tokenManager;
    private final RetrofitClient retrofitClient;
    private final GoogleSignInManager googleSignInManager;

    /**
     * Constructor
     */
    public AuthRepository(Context context) {
        this.retrofitClient = RetrofitClient.getInstance();
        this.authApi = retrofitClient.getAuthApi();
        this.restApi = retrofitClient.getRestApi();
        this.tokenManager = new TokenManager(context);
        this.googleSignInManager = new GoogleSignInManager(context);

        // Load saved access token and set it in AuthInterceptor
        loadSavedToken();
    }

    /**
     * Load saved access token from TokenManager and set in AuthInterceptor
     * This ensures the token persists across app restarts
     */
    private void loadSavedToken() {
        String savedToken = tokenManager.getAccessToken();
        if (savedToken != null && !savedToken.isEmpty()) {
            retrofitClient.setAccessToken(savedToken);
            Log.d(TAG, "Loaded saved access token into AuthInterceptor");
        }
    }

    /**
     * Register new user
     * User must verify email before logging in
     */
    public void register(String email, String password, SimpleCallback callback) {
        RegisterRequest request = new RegisterRequest(email, password);

        // Signup user - email verification required
        authApi.signup(request).enqueue(new Callback<com.example.shopverse_customer_app.data.model.User>() {
            @Override
            public void onResponse(@NonNull Call<com.example.shopverse_customer_app.data.model.User> call, @NonNull Response<com.example.shopverse_customer_app.data.model.User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    com.example.shopverse_customer_app.data.model.User user = response.body();
                    Log.d(TAG, "Signup successful for user: " + user.getEmail() + ". Email verification required.");

                    // Return success - user must verify email before login
                    callback.onSuccess();
                } else {
                    String errorMsg = ErrorParser.parseError(response);
                    callback.onError(errorMsg);
                    Log.e(TAG, "Signup failed: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<com.example.shopverse_customer_app.data.model.User> call, @NonNull Throwable t) {
                String errorMsg = ErrorParser.parseError(t);
                callback.onError(errorMsg);
                Log.e(TAG, "Signup network error", t);
            }
        });
    }

    /**
     * Login user
     */
    public void login(String email, String password, AuthCallback callback) {
        LoginRequest request = new LoginRequest(email, password);

        authApi.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    saveAuthSession(authResponse);
                    callback.onSuccess(authResponse);
                    Log.d(TAG, "Login successful: " + authResponse);
                } else {
                    String errorMsg = ErrorParser.parseError(response);
                    callback.onError(errorMsg);
                    Log.e(TAG, "Login failed: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                String errorMsg = ErrorParser.parseError(t);
                callback.onError(errorMsg);
                Log.e(TAG, "Login network error", t);
            }
        });
    }

    /**
     * Logout user
     */
    public void logout(SimpleCallback callback) {
        // Clear tokens from TokenManager
        tokenManager.clearTokens();

        // Clear token from RetrofitClient AuthInterceptor
        retrofitClient.clearAccessToken();

        callback.onSuccess();
        Log.d(TAG, "Logout successful - tokens cleared");
    }

    /**
     * Refresh access token
     */
//    public void refreshToken(AuthCallback callback) {
//        String refreshToken = tokenManager.getRefreshToken();
//
//        if (refreshToken == null) {
//            callback.onError("No refresh token available");
//            return;
//        }
//
//        authApi.refreshToken(refreshToken).enqueue(new Callback<AuthResponse>() {
//            @Override
//            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
//                if (response.isSuccessful() && response.body() != null) {
//                    AuthResponse authResponse = response.body();
//                    saveAuthSession(authResponse);
//                    callback.onSuccess(authResponse);
//                    Log.d(TAG, "Token refresh successful");
//                } else {
//                    callback.onError("Token refresh failed");
//                    Log.e(TAG, "Token refresh failed");
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
//                callback.onError("Network error: " + t.getMessage());
//                Log.e(TAG, "Token refresh network error", t);
//            }
//        });
//    }

    /**
     * Request password reset
     */
    public void requestPasswordReset(String email, SimpleCallback callback) {
        Log.d(TAG, "=== Requesting Password Reset ===");
        Log.d(TAG, "Email: " + email);

        JsonObject request = new JsonObject();
        request.addProperty("email", email);

        Log.d(TAG, "Request body: " + request.toString());
        Log.d(TAG, "Calling Supabase Auth API: POST /auth/v1/recover");

        authApi.recover(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                Log.d(TAG, "=== Password Reset Response ===");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response message: " + response.message());

                if (response.isSuccessful()) {
                    Log.d(TAG, "✅ Password reset email sent successfully to: " + email);
                    callback.onSuccess();
                } else {
                    Log.e(TAG, "❌ Password reset failed");
                    Log.e(TAG, "Response code: " + response.code());

                    // Try to get error body
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Could not read error body", e);
                    }

                    String errorMsg = ErrorParser.parseError(response);
                    Log.e(TAG, "Parsed error: " + errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "=== Password Reset Network Error ===");
                Log.e(TAG, "Error type: " + t.getClass().getSimpleName());
                Log.e(TAG, "Error message: " + t.getMessage());
                Log.e(TAG, "Stack trace:", t);

                String errorMsg = ErrorParser.parseError(t);
                Log.e(TAG, "Parsed error: " + errorMsg);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Sign in with Google using Supabase Auth
     * Uses Google ID token to authenticate with Supabase
     */
    public void loginWithGoogle(String idToken, AuthCallback callback) {
        Log.d(TAG, "=== loginWithGoogle in AuthRepository ===");
        Log.d(TAG, "ID Token is " + (idToken != null ? "present" : "NULL"));

        if (idToken == null || idToken.isEmpty()) {
            Log.e(TAG, "ID Token is null or empty!");
            callback.onError("Invalid Google ID token");
            return;
        }

        // Create request body for Supabase Auth
        JsonObject request = new JsonObject();
        request.addProperty("id_token", idToken);
        request.addProperty("provider", "google");

        Log.d(TAG, "Request body created: " + request.toString());
        Log.d(TAG, "Calling Supabase Auth API endpoint: /auth/v1/token?grant_type=id_token");

        // Call Supabase Auth API to sign in with Google
        authApi.signInWithGoogle(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call, @NonNull Response<AuthResponse> response) {
                Log.d(TAG, "=== Supabase Auth Response Received ===");
                Log.d(TAG, "Response code: " + response.code());
                Log.d(TAG, "Response message: " + response.message());
                Log.d(TAG, "Response is successful: " + response.isSuccessful());

                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    Log.d(TAG, "=== Google Sign-In SUCCESS ===");
                    Log.d(TAG, "Access token present: " + (authResponse.getAccessToken() != null && !authResponse.getAccessToken().isEmpty()));
                    Log.d(TAG, "Refresh token present: " + (authResponse.getRefreshToken() != null && !authResponse.getRefreshToken().isEmpty()));
                    Log.d(TAG, "User present: " + (authResponse.getUser() != null));
                    if (authResponse.getUser() != null) {
                        Log.d(TAG, "User ID: " + authResponse.getUser().getId());
                        Log.d(TAG, "User Email: " + authResponse.getUser().getEmail());
                    }

                    Log.d(TAG, "Saving auth session...");
                    saveAuthSession(authResponse);
                    Log.d(TAG, "Calling callback.onSuccess()");
                    callback.onSuccess(authResponse);
                } else {
                    Log.e(TAG, "=== Google Sign-In FAILED ===");
                    Log.e(TAG, "Response code: " + response.code());

                    // Log response body if error
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(TAG, "Error body: " + errorBody);
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Could not read error body", e);
                    }

                    String errorMsg = ErrorParser.parseError(response);
                    Log.e(TAG, "Error message: " + errorMsg);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "=== Supabase Auth Network Error ===");
                Log.e(TAG, "Error type: " + t.getClass().getSimpleName());
                Log.e(TAG, "Error message: " + t.getMessage());
                Log.e(TAG, "Stack trace:", t);

                String errorMsg = ErrorParser.parseError(t);
                Log.e(TAG, "Parsed error message: " + errorMsg);
                callback.onError(errorMsg);
            }
        });
    }

    /**
     * Get GoogleSignInClient for starting sign-in flow
     */
    public GoogleSignInClient getGoogleSignInClient() {
        return googleSignInManager.getGoogleSignInClient();
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return tokenManager.isLoggedIn();
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        return tokenManager.getUserId();
    }

    /**
     * Get current user email
     */
    public String getCurrentUserEmail() {
        return tokenManager.getUserEmail();
    }

    /**
     * Get authorization header
     */
    public String getAuthorizationHeader() {
        return tokenManager.getAuthorizationHeader();
    }

    /**
     * Save authentication session to TokenManager and RetrofitClient
     */
    private void saveAuthSession(AuthResponse authResponse) {
        // Save to TokenManager (encrypted storage)
        tokenManager.saveAuthSession(
                authResponse.getAccessToken(),
                authResponse.getRefreshToken(),
                authResponse.getUser().getId(),
                authResponse.getUser().getEmail(),
                authResponse.getExpiresIn()
        );

        // Set access token in RetrofitClient AuthInterceptor for immediate use
        retrofitClient.setAccessToken(authResponse.getAccessToken());
    }

    /**
     * Create user profile in profiles table
     */
    private void createUserProfile(String userId, String email) {
        Profile profile = new Profile(userId, email);
        profile.setRole("customer");

        // AuthInterceptor will add the bearer token automatically
        restApi.createProfile(profile).enqueue(new Callback<Profile>() {
            @Override
            public void onResponse(@NonNull Call<Profile> call, @NonNull Response<Profile> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "User profile created successfully");
                } else {
                    String errorMsg = ErrorParser.parseError(response);
                    Log.e(TAG, "Failed to create user profile: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Profile> call, @NonNull Throwable t) {
                String errorMsg = ErrorParser.parseError(t);
                Log.e(TAG, "Profile creation network error: " + errorMsg, t);
            }
        });
    }

    // Callback interfaces
    public interface AuthCallback {
        void onSuccess(AuthResponse response);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
}
