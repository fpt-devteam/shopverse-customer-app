package com.example.shopverse_customer_app;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import android.view.View;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavOptions;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.shopverse_customer_app.databinding.ActivityMainBinding;
import com.google.firebase.FirebaseApp;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ActivityMainBinding binding;
    private NavController navController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FirebaseApp.initializeApp(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        // Setup toolbar (hide title, only show it for specific fragments)
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        View root = findViewById(R.id.container);
        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dashboard, R.id.navigation_maps, R.id.navigation_cart, R.id.navigation_account)
                .build();
        navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            Log.d(TAG, "Destination changed to: " + destination.getLabel());
            Log.d(TAG, "Destination ID: " + destination.getId());

            if (destination.getId() == R.id.navigation_checkout ||
                destination.getId() == R.id.productDetailFragment ||
                destination.getId() == R.id.orderHistoryFragment) {
                binding.navView.setVisibility(View.GONE);
                Log.d(TAG, "Bottom navigation hidden for: " + destination.getLabel());
            } else {
                binding.navView.setVisibility(View.VISIBLE);
                Log.d(TAG, "Bottom navigation visible for: " + destination.getLabel());
            }
        });

        NavigationUI.setupWithNavController(binding.navView, navController);
        Log.d(TAG, "Bottom navigation setup complete");

        // Add listener to debug navigation clicks
        binding.navView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Log.d(TAG, "=== Bottom Nav Item Clicked ===");
            Log.d(TAG, "Item ID clicked: " + itemId);
            Log.d(TAG, "Current destination: " + (navController.getCurrentDestination() != null
                ? navController.getCurrentDestination().getId() : "NULL"));

            // Let NavigationUI handle the navigation
            boolean handled = NavigationUI.onNavDestinationSelected(item, navController);

            Log.d(TAG, "Navigation handled: " + handled);
            Log.d(TAG, "New destination: " + (navController.getCurrentDestination() != null
                ? navController.getCurrentDestination().getId() : "NULL"));

            return handled;
        });

        // Handle deep link from payment callback
        handleDeepLink(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleDeepLink(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Activity resumed");
        // Ensure bottom navigation is synced with current destination
        syncBottomNavigationWithNavController();
    }

    /**
     * Sync bottom navigation selection with current nav controller destination
     */
    private void syncBottomNavigationWithNavController() {
        if (navController != null && binding != null && binding.navView != null) {
            int currentDestinationId = navController.getCurrentDestination() != null
                ? navController.getCurrentDestination().getId()
                : R.id.navigation_dashboard;

            Log.d(TAG, "syncBottomNavigationWithNavController: Current destination ID = " + currentDestinationId);
            Log.d(TAG, "syncBottomNavigationWithNavController: Current bottom nav selection = " + binding.navView.getSelectedItemId());

            // Only update if they don't match to avoid unnecessary updates
            if (binding.navView.getSelectedItemId() != currentDestinationId) {
                Log.d(TAG, "syncBottomNavigationWithNavController: Syncing bottom nav...");

                // Update bottom navigation selection to match current destination
                binding.navView.post(() -> {
                    binding.navView.setSelectedItemId(currentDestinationId);
                    Log.d(TAG, "syncBottomNavigationWithNavController: Bottom nav updated to " + currentDestinationId);
                });
            } else {
                Log.d(TAG, "syncBottomNavigationWithNavController: Already in sync, no update needed");
            }
        }
    }

    /**
     * Handle deep link for payment callback
     * URI format: shopverse://payment?status=success&orderCode=xxx&orderId=xxx
     */
    private void handleDeepLink(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            String scheme = data.getScheme();
            String host = data.getHost();

            Log.d(TAG, "Deep link received - Scheme: " + scheme + ", Host: " + host);
            Log.d(TAG, "Full URI: " + data.toString());

            if ("shopverse".equals(scheme) && "payment".equals(host)) {
                // Extract query parameters
                String status = data.getQueryParameter("status");
                String orderCode = data.getQueryParameter("orderCode");
                String orderId = data.getQueryParameter("orderId");

                Log.d(TAG, "Payment callback - Status: " + status + ", OrderCode: " + orderCode + ", OrderId: " + orderId);

                // Handle payment result
                if ("success".equalsIgnoreCase(status)) {
                    handlePaymentSuccess(orderId, orderCode);
                } else if ("cancel".equalsIgnoreCase(status)) {
                    handlePaymentCancel(orderId, orderCode);
                } else {
                    handlePaymentFailure(orderId, orderCode);
                }
            }
        }
    }

    /**
     * Handle successful payment
     */
    private void handlePaymentSuccess(String orderId, String orderCode) {
        Log.d(TAG, "Payment successful - OrderId: " + orderId + ", OrderCode: " + orderCode);

        Toast.makeText(this, "Thanh toán thành công!\nMã đơn hàng: " + orderCode, Toast.LENGTH_LONG).show();

        // Navigate to Account tab to show orders
        binding.navView.setSelectedItemId(R.id.navigation_account);
    }

    /**
     * Handle cancelled payment
     */
    private void handlePaymentCancel(String orderId, String orderCode) {
        Log.d(TAG, "Payment cancelled - OrderId: " + orderId + ", OrderCode: " + orderCode);

        Toast.makeText(this, "Đã hủy thanh toán", Toast.LENGTH_SHORT).show();

        // Navigate to Account tab
        binding.navView.setSelectedItemId(R.id.navigation_account);
    }

    /**
     * Handle failed payment
     */
    private void handlePaymentFailure(String orderId, String orderCode) {
        Log.e(TAG, "Payment failed - OrderId: " + orderId + ", OrderCode: " + orderCode);

        Toast.makeText(this, "Thanh toán thất bại. Vui lòng thử lại.", Toast.LENGTH_LONG).show();

        // Navigate to Account tab
        binding.navView.setSelectedItemId(R.id.navigation_account);
    }

    // Removed toolbar menu - cart is now in bottom navigation

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}