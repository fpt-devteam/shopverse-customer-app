package com.example.shopverse_customer_app.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.shopverse_customer_app.data.model.AuthResponse;
import com.example.shopverse_customer_app.data.repository.AuthRepository;
import com.example.shopverse_customer_app.utils.FirebaseTokenManager;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;

/**
 * ViewModel for authentication operations
 * Manages UI state and communicates with AuthRepository
 */
public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    // LiveData for UI states
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> passwordResetSent = new MutableLiveData<>();
    private final MutableLiveData<AuthResponse> authResponse = new MutableLiveData<>();

    /**
     * Constructor
     */
    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    /**
     * Login user
     */
    public void login(String email, String password) {
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Email is required");
            return;
        }
        if (password == null || password.trim().isEmpty()) {
            errorMessage.setValue("Password is required");
            return;
        }

        isLoading.setValue(true);
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                isLoading.postValue(false);
                authResponse.postValue(response);
                loginSuccess.postValue(true);
                FirebaseTokenManager.updateToken(getApplication(), response.getUser().getId());
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
                loginSuccess.postValue(false);
            }
        });
    }

    /**
     * Login with Google
     */
    public void loginWithGoogle(String idToken) {
        Log.d("AuthViewModel", "=== loginWithGoogle called ===");
        Log.d("AuthViewModel", "ID Token is " + (idToken != null ? "present" : "NULL"));

        if (idToken == null || idToken.isEmpty()) {
            Log.e("AuthViewModel", "ID Token is null or empty!");
            errorMessage.setValue("Invalid Google sign-in");
            return;
        }

        Log.d("AuthViewModel", "ID Token length: " + idToken.length());
        Log.d("AuthViewModel", "Setting isLoading to true");
        isLoading.setValue(true);

        Log.d("AuthViewModel", "Calling authRepository.loginWithGoogle()");
        authRepository.loginWithGoogle(idToken, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                Log.d("AuthViewModel", "=== Google login SUCCESS ===");
                Log.d("AuthViewModel", "User ID: " + (response.getUser() != null ? response.getUser().getId() : "NULL"));
                Log.d("AuthViewModel", "User Email: " + (response.getUser() != null ? response.getUser().getEmail() : "NULL"));
                Log.d("AuthViewModel", "Access Token present: " + (response.getAccessToken() != null && !response.getAccessToken().isEmpty()));

                isLoading.postValue(false);
                authResponse.postValue(response);
                loginSuccess.postValue(true);

                if (response.getUser() != null) {
                    Log.d("AuthViewModel", "Updating Firebase token for user: " + response.getUser().getId());
                    FirebaseTokenManager.updateToken(getApplication(), response.getUser().getId());
                } else {
                    Log.w("AuthViewModel", "User is null, skipping Firebase token update");
                }
            }

            @Override
            public void onError(String error) {
                Log.e("AuthViewModel", "=== Google login ERROR ===");
                Log.e("AuthViewModel", "Error message: " + error);

                isLoading.postValue(false);
                errorMessage.postValue(error);
                loginSuccess.postValue(false);
            }
        });
    }

    /**
     * Get GoogleSignInClient for starting sign-in flow
     */
    public GoogleSignInClient getGoogleSignInClient() {
        return authRepository.getGoogleSignInClient();
    }

    /**
     * Register new user
     * User must verify email before logging in
     */
    public void register(String email, String password) {
        // Validate input
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Email is required");
            return;
        }
        if (password == null || password.length() < 6) {
            errorMessage.setValue("Password must be at least 6 characters");
            return;
        }

        isLoading.setValue(true);
        authRepository.register(email, password, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                registerSuccess.postValue(true);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
                registerSuccess.postValue(false);
            }
        });
    }

    /**
     * Logout user
     */
    public void logout() {
        isLoading.setValue(true);
        authRepository.logout(new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                logoutSuccess.postValue(true);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                // Still mark as success since tokens are cleared locally
                logoutSuccess.postValue(true);
            }
        });
    }

    /**
     * Request password reset
     */
    public void requestPasswordReset(String email) {
        if (email == null || email.trim().isEmpty()) {
            errorMessage.setValue("Email is required");
            return;
        }

        isLoading.setValue(true);
        authRepository.requestPasswordReset(email, new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                passwordResetSent.postValue(true);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
                passwordResetSent.postValue(false);
            }
        });
    }

    /**
     * Check if user is logged in
     */
    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }

    /**
     * Get current user ID
     */
    public String getCurrentUserId() {
        return authRepository.getCurrentUserId();
    }

    /**
     * Get current user email
     */
    public String getCurrentUserEmail() {
        return authRepository.getCurrentUserEmail();
    }

    // Getters for LiveData
    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public LiveData<Boolean> getLoginSuccess() {
        return loginSuccess;
    }

    public LiveData<Boolean> getRegisterSuccess() {
        return registerSuccess;
    }

    public LiveData<Boolean> getLogoutSuccess() {
        return logoutSuccess;
    }

    public LiveData<Boolean> getPasswordResetSent() {
        return passwordResetSent;
    }

    public LiveData<AuthResponse> getAuthResponse() {
        return authResponse;
    }

    /**
     * Clear error message
     */
    public void clearError() {
        errorMessage.setValue(null);
    }

    /**
     * Reset success states
     */
    public void resetStates() {
        loginSuccess.setValue(null);
        registerSuccess.setValue(null);
        logoutSuccess.setValue(null);
        passwordResetSent.setValue(null);
        errorMessage.setValue(null);
    }
}
