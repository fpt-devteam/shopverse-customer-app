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
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.Order;
import com.example.shopverse_customer_app.data.remote.RetrofitClient;
import com.example.shopverse_customer_app.data.remote.SupabaseRestApi;
import com.example.shopverse_customer_app.ui.auth.LoginActivity;
import com.example.shopverse_customer_app.ui.auth.RegisterActivity;
import com.example.shopverse_customer_app.utils.TokenManager;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
    private LinearLayout menuPurchaseHistory;

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
        menuPurchaseHistory = root.findViewById(R.id.menuPurchaseHistory);

        Log.d(TAG, "initializeViews: layoutNotLoggedIn = " + (layoutNotLoggedIn != null ? "found" : "NULL"));
        Log.d(TAG, "initializeViews: layoutLoggedIn = " + (layoutLoggedIn != null ? "found" : "NULL"));
        Log.d(TAG, "initializeViews: btnLogin = " + (btnLogin != null ? "found" : "NULL"));
        Log.d(TAG, "initializeViews: btnRegister = " + (btnRegister != null ? "found" : "NULL"));
        Log.d(TAG, "initializeViews: btnLogout = " + (btnLogout != null ? "found" : "NULL"));
        Log.d(TAG, "initializeViews: tvUserEmail = " + (tvUserEmail != null ? "found" : "NULL"));
        Log.d(TAG, "initializeViews: menuPurchaseHistory = " + (menuPurchaseHistory != null ? "found" : "NULL"));
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

        // Purchase History click
        if (menuPurchaseHistory != null) {
            menuPurchaseHistory.setOnClickListener(v -> {
                if (tokenManager.isLoggedIn()) {
                    // Navigate to Order History fragment
                    Navigation.findNavController(requireView())
                            .navigate(R.id.action_navigation_account_to_orderHistoryFragment);
                } else {
                    Toast.makeText(requireContext(), "Vui lòng đăng nhập để xem đơn hàng", Toast.LENGTH_SHORT).show();
                }
            });
            Log.d(TAG, "setupClickListeners: Purchase history click listener set");
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

    /**
     * Show order history dialog
     */
    private void showOrderHistory() {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(requireContext(), "Không tìm thấy thông tin người dùng", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(TAG, "showOrderHistory: Loading orders for user " + userId);

        // Show loading dialog
        AlertDialog loadingDialog = new AlertDialog.Builder(requireContext())
                .setTitle("Đang tải...")
                .setMessage("Vui lòng đợi")
                .setCancelable(false)
                .create();
        loadingDialog.show();

        // Call API to get orders
        SupabaseRestApi restApi = RetrofitClient.getInstance().getRestApi();
        restApi.getOrders("*", "eq." + userId, null, "order_date.desc").enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                loadingDialog.dismiss();

                if (response.isSuccessful() && response.body() != null) {
                    List<Order> orders = response.body();
                    Log.d(TAG, "showOrderHistory: Loaded " + orders.size() + " orders");

                    if (orders.isEmpty()) {
                        showEmptyOrdersDialog();
                    } else {
                        displayOrders(orders);
                    }
                } else {
                    Log.e(TAG, "showOrderHistory: Failed to load orders: " + response.code());
                    Toast.makeText(requireContext(), "Không thể tải danh sách đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                loadingDialog.dismiss();
                Log.e(TAG, "showOrderHistory: Error loading orders", t);
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Show empty orders dialog
     */
    private void showEmptyOrdersDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Lịch sử mua hàng")
                .setMessage("Bạn chưa có đơn hàng nào")
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Display orders in a dialog
     */
    private void displayOrders(List<Order> orders) {
        // Create dialog with order list
        StringBuilder orderList = new StringBuilder();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());

        for (int i = 0; i < orders.size(); i++) {
            Order order = orders.get(i);

            orderList.append("Đơn hàng #").append(i + 1).append("\n");
            orderList.append("Mã: ").append(order.getOrderId().substring(0, 8)).append("...\n");
            orderList.append("Tổng tiền: ").append(formatPrice(order.getTotalPrice())).append("\n");
            orderList.append("Trạng thái: ").append(getStatusText(order.getStatus())).append("\n");

            if (order.getOrderDate() != null) {
                try {
                    // Parse ISO date string
                    orderList.append("Ngày đặt: ").append(order.getOrderDate()).append("\n");
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing date", e);
                }
            }

            orderList.append("Địa chỉ: ").append(order.getAddress()).append("\n");

            if (i < orders.size() - 1) {
                orderList.append("\n---\n\n");
            }
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Lịch sử mua hàng (" + orders.size() + " đơn)")
                .setMessage(orderList.toString())
                .setPositiveButton("Đóng", null)
                .show();
    }

    /**
     * Format price to Vietnamese currency
     */
    private String formatPrice(double price) {
        long priceInt = (long) price;
        String formatted = String.format("%,d", priceInt).replace(",", ".");
        return formatted + "₫";
    }

    /**
     * Get status text in Vietnamese
     */
    private String getStatusText(String status) {
        if (status == null) return "Không rõ";

        switch (status.toLowerCase()) {
            case "pending":
                return "Đang xử lý";
            case "completed":
                return "Hoàn thành";
            case "cancelled":
                return "Đã hủy";
            default:
                return status;
        }
    }
}
