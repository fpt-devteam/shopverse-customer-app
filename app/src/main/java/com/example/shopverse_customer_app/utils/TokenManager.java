package com.example.shopverse_customer_app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * TokenManager handles secure storage of authentication tokens
 * Uses EncryptedSharedPreferences for secure local storage
 */
public class TokenManager {

    private static final String PREFS_NAME = "shopverse_auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";

    private final SharedPreferences sharedPreferences;

    /**
     * Constructor - initializes EncryptedSharedPreferences
     */
    public TokenManager(Context context) {
        try {
            // Create or retrieve MasterKey for encryption
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Initialize EncryptedSharedPreferences
            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to initialize EncryptedSharedPreferences", e);
        }
    }

    /**
     * Save access token
     */
    public void saveAccessToken(String token) {
        sharedPreferences.edit().putString(KEY_ACCESS_TOKEN, token).apply();
    }

    /**
     * Get access token
     */
    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    /**
     * Save refresh token
     */
    public void saveRefreshToken(String token) {
        sharedPreferences.edit().putString(KEY_REFRESH_TOKEN, token).apply();
    }

    /**
     * Get refresh token
     */
    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    /**
     * Save user ID
     */
    public void saveUserId(String userId) {
        sharedPreferences.edit().putString(KEY_USER_ID, userId).apply();
    }

    /**
     * Get user ID
     */
    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    /**
     * Save user email
     */
    public void saveUserEmail(String email) {
        sharedPreferences.edit().putString(KEY_USER_EMAIL, email).apply();
    }

    /**
     * Get user email
     */
    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    /**
     * Save token expiry timestamp
     */
    public void saveTokenExpiry(long expiryTimestamp) {
        sharedPreferences.edit().putLong(KEY_TOKEN_EXPIRY, expiryTimestamp).apply();
    }

    /**
     * Get token expiry timestamp
     */
    public long getTokenExpiry() {
        return sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0);
    }

    /**
     * Check if user is logged in (has valid access token)
     */
    public boolean isLoggedIn() {
        String accessToken = getAccessToken();
        return accessToken != null && !accessToken.isEmpty();
    }

    /**
     * Check if token is expired
     */
    public boolean isTokenExpired() {
        long expiry = getTokenExpiry();
        return expiry > 0 && System.currentTimeMillis() >= expiry;
    }

    /**
     * Get Authorization header value
     */
    public String getAuthorizationHeader() {
        String token = getAccessToken();
        return token != null ? "Bearer " + token : null;
    }

    /**
     * Clear all authentication data (logout)
     */
    public void clearTokens() {
        sharedPreferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_TOKEN_EXPIRY)
                .apply();
    }

    /**
     * Save complete authentication session
     */
    public void saveAuthSession(String accessToken, String refreshToken, String userId, String email, long expiresIn) {
        long expiryTimestamp = System.currentTimeMillis() + (expiresIn * 1000);

        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, email)
                .putLong(KEY_TOKEN_EXPIRY, expiryTimestamp)
                .apply();
    }
}
