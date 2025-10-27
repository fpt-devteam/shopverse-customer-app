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
import com.example.shopverse_customer_app.utils.TokenManager;
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

    /**
     * Constructor
     */
    public AuthRepository(Context context) {
        this.retrofitClient = RetrofitClient.getInstance();
        this.authApi = retrofitClient.getAuthApi();
        this.restApi = retrofitClient.getRestApi();
        this.tokenManager = new TokenManager(context);

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
        JsonObject request = new JsonObject();
        request.addProperty("email", email);

        authApi.recover(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                    Log.d(TAG, "Password reset email sent");
                } else {
                    String errorMsg = ErrorParser.parseError(response);
                    callback.onError(errorMsg);
                    Log.e(TAG, "Password reset failed: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                String errorMsg = ErrorParser.parseError(t);
                callback.onError(errorMsg);
                Log.e(TAG, "Password reset network error", t);
            }
        });
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
