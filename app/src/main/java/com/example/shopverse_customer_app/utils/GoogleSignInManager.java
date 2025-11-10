package com.example.shopverse_customer_app.utils;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.shopverse_customer_app.BuildConfig;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

/**
 * Manager class for Google Sign-In operations
 * Handles configuration and authentication with Google
 */
public class GoogleSignInManager {

    private static final String TAG = "GoogleSignInManager";

    private final Context context;
    private GoogleSignInClient googleSignInClient;

    public GoogleSignInManager(Context context) {
        this.context = context.getApplicationContext();
        initializeGoogleSignIn();
    }

    /**
     * Initialize Google Sign-In with configuration
     */
    private void initializeGoogleSignIn() {
        Log.d(TAG, "=== Initializing Google Sign-In ===");
        Log.d(TAG, "Client ID from BuildConfig: " + BuildConfig.GOOGLE_CLIENT_ID);

        // Configure Google Sign-In for Supabase
        // We request BOTH ID token and server auth code for better compatibility
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(BuildConfig.GOOGLE_CLIENT_ID)  // Request ID token for Supabase Auth
                .requestServerAuthCode(BuildConfig.GOOGLE_CLIENT_ID)  // Also request server auth code
                .requestEmail()  // Request user's email
                .requestProfile()  // Request user's basic profile
                .build();

        Log.d(TAG, "GoogleSignInOptions created");
        Log.d(TAG, "Requesting ID token: true");
        Log.d(TAG, "Requesting server auth code: true");
        Log.d(TAG, "Requesting email: true");
        Log.d(TAG, "Requesting profile: true");

        googleSignInClient = GoogleSignIn.getClient(context, gso);
        Log.d(TAG, "GoogleSignInClient created: " + (googleSignInClient != null ? "SUCCESS" : "FAILED"));
    }

    /**
     * Get GoogleSignInClient instance
     */
    public GoogleSignInClient getGoogleSignInClient() {
        if (googleSignInClient == null) {
            initializeGoogleSignIn();
        }
        return googleSignInClient;
    }

    /**
     * Get ID token from Google Sign-In result
     */
    public String getIdTokenFromResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            if (account != null) {
                String idToken = account.getIdToken();
                Log.d(TAG, "Successfully retrieved ID token");
                return idToken;
            }
        } catch (ApiException e) {
            Log.e(TAG, "Google sign-in failed", e);
        }
        return null;
    }

    /**
     * Sign out from Google
     */
    public void signOut(OnSignOutListener listener) {
        if (googleSignInClient != null) {
            googleSignInClient.signOut()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "Google sign-out successful");
                            if (listener != null) {
                                listener.onSuccess();
                            }
                        } else {
                            Log.e(TAG, "Google sign-out failed");
                            if (listener != null) {
                                listener.onError("Sign-out failed");
                            }
                        }
                    });
        }
    }

    /**
     * Get last signed-in account
     */
    public GoogleSignInAccount getLastSignedInAccount() {
        return GoogleSignIn.getLastSignedInAccount(context);
    }

    /**
     * Check if user is currently signed in with Google
     */
    public boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(context) != null;
    }

    // Callback interface for sign-out operations
    public interface OnSignOutListener {
        void onSuccess();
        void onError(String error);
    }
}
