package com.example.shopverse_customer_app.ui.account;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.ui.auth.LoginActivity;
import com.example.shopverse_customer_app.ui.auth.RegisterActivity;
import com.example.shopverse_customer_app.utils.TokenManager;

/**
 * AccountFragment displays user profile and account settings
 * Shows login/register options when not logged in
 * Shows user info and logout option when logged in
 */
public class AccountFragment extends Fragment {

    private static final String TAG = "AccountFragment";
    private TokenManager tokenManager;

    // UI Components
    private LinearLayout layoutNotLoggedIn;
    private LinearLayout layoutLoggedIn;
    private Button btnLogin;
    private Button btnRegister;
    private Button btnLogout;
    private TextView tvUserEmail;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: Fragment created");
        View root = inflater.inflate(R.layout.fragment_account, container, false);

        // Initialize TokenManager
        tokenManager = new TokenManager(requireContext());
        Log.d(TAG, "onCreateView: TokenManager initialized");

        // Initialize UI components
        initializeViews(root);

        // Check login status and update UI
        updateUIBasedOnLoginStatus();

        // Set up click listeners
        setupClickListeners();

        return root;
    }

    /**
     * Initialize all view components
     */
    private void initializeViews(View root) {
        Log.d(TAG, "initializeViews: Initializing UI components");
        layoutNotLoggedIn = root.findViewById(R.id.layoutNotLoggedIn);
        layoutLoggedIn = root.findViewById(R.id.layoutLoggedIn);
        btnLogin = root.findViewById(R.id.btnLogin);
        btnRegister = root.findViewById(R.id.btnRegister);
        btnLogout = root.findViewById(R.id.btnLogout);
        tvUserEmail = root.findViewById(R.id.tvUserEmail);

        Log.d(TAG, "initializeViews: layoutNotLoggedIn = " + (layoutNotLoggedIn != null ? "found" : "NULL"));
        Log.d(TAG, "initializeViews: layoutLoggedIn = " + (layoutLoggedIn != null ? "found" : "NULL"));
        Log.d(TAG, "initializeViews: btnLogin = " + (btnLogin != null ? "found" : "NULL"));
        Log.d(TAG, "initializeViews: btnRegister = " + (btnRegister != null ? "found" : "NULL"));
        Log.d(TAG, "initializeViews: btnLogout = " + (btnLogout != null ? "found" : "NULL"));
        Log.d(TAG, "initializeViews: tvUserEmail = " + (tvUserEmail != null ? "found" : "NULL"));
    }

    /**
     * Update UI based on user login status
     */
    private void updateUIBasedOnLoginStatus() {
        boolean isLoggedIn = tokenManager.isLoggedIn();
        String accessToken = tokenManager.getAccessToken();
        String userEmail = tokenManager.getUserEmail();

        Log.d(TAG, "updateUIBasedOnLoginStatus: ===== CHECKING LOGIN STATUS =====");
        Log.d(TAG, "updateUIBasedOnLoginStatus: isLoggedIn = " + isLoggedIn);
        Log.d(TAG, "updateUIBasedOnLoginStatus: accessToken = " + (accessToken != null ? "EXISTS (length: " + accessToken.length() + ")" : "NULL"));
        Log.d(TAG, "updateUIBasedOnLoginStatus: userEmail = " + (userEmail != null ? userEmail : "NULL"));

        if (isLoggedIn) {
            // User is logged in - show logged in layout (which includes logout button)
            Log.d(TAG, "updateUIBasedOnLoginStatus: USER IS LOGGED IN - Showing logged in UI");

            layoutNotLoggedIn.setVisibility(View.GONE);
            layoutLoggedIn.setVisibility(View.VISIBLE);

            Log.d(TAG, "updateUIBasedOnLoginStatus: layoutLoggedIn is now VISIBLE (includes logout button)");

            // Logout button visibility is controlled by parent layoutLoggedIn
            if (btnLogout != null) {
                Log.d(TAG, "updateUIBasedOnLoginStatus: btnLogout found - visibility = " + btnLogout.getVisibility() + " (0=VISIBLE, 4=INVISIBLE, 8=GONE)");
            } else {
                Log.e(TAG, "updateUIBasedOnLoginStatus: ERROR - btnLogout is NULL!");
            }

            // Display user email
            if (userEmail != null && !userEmail.isEmpty()) {
                tvUserEmail.setText(userEmail);
                Log.d(TAG, "updateUIBasedOnLoginStatus: User email displayed: " + userEmail);
            } else {
                tvUserEmail.setText("Người dùng Shopverse");
                Log.d(TAG, "updateUIBasedOnLoginStatus: Default user text displayed");
            }
        } else {
            // User is not logged in - show login/register options
            Log.d(TAG, "updateUIBasedOnLoginStatus: USER IS NOT LOGGED IN - Showing login/register UI");

            layoutNotLoggedIn.setVisibility(View.VISIBLE);
            layoutLoggedIn.setVisibility(View.GONE);

            Log.d(TAG, "updateUIBasedOnLoginStatus: Login/Register buttons shown, logout section hidden");
        }

        Log.d(TAG, "updateUIBasedOnLoginStatus: ===== STATUS CHECK COMPLETE =====");
    }

    /**
     * Set up click listeners for all interactive elements
     */
    private void setupClickListeners() {
        Log.d(TAG, "setupClickListeners: Setting up click listeners");

        // Login button click
        btnLogin.setOnClickListener(v -> {
            Log.d(TAG, "setupClickListeners: Login button clicked");
            Intent intent = new Intent(requireContext(), LoginActivity.class);
            startActivity(intent);
        });

        // Register button click
        btnRegister.setOnClickListener(v -> {
            Log.d(TAG, "setupClickListeners: Register button clicked");
            Intent intent = new Intent(requireContext(), RegisterActivity.class);
            startActivity(intent);
        });

        // Logout button click
        if (btnLogout != null) {
            btnLogout.setOnClickListener(v -> handleLogout());
            Log.d(TAG, "setupClickListeners: Logout button click listener set");
        } else {
            Log.e(TAG, "setupClickListeners: ERROR - Cannot set logout click listener, btnLogout is NULL!");
        }
    }

    /**
     * Handle logout functionality
     * Clears all tokens and navigates to home
     */
    private void handleLogout() {
        Log.d(TAG, "handleLogout: Logout button clicked");

        // Clear all authentication tokens
        tokenManager.clearTokens();
        Log.d(TAG, "handleLogout: All tokens cleared");

        // Show success message
        Toast.makeText(requireContext(), "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show();

        // Navigate to home/dashboard
        Navigation.findNavController(requireView()).navigate(R.id.navigation_dashboard);
        Log.d(TAG, "handleLogout: Navigated to dashboard");

        // Update UI to show logged out state
        updateUIBasedOnLoginStatus();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Fragment resumed");
        // Update UI when fragment resumes (e.g., after returning from login)
        updateUIBasedOnLoginStatus();
    }
}
