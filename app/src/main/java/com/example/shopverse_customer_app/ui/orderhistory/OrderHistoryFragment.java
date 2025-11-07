package com.example.shopverse_customer_app.ui.orderhistory;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.Order;
import com.example.shopverse_customer_app.utils.TokenManager;

/**
 * Fragment for displaying order history with status filtering
 */
public class OrderHistoryFragment extends Fragment implements OrderAdapter.OnOrderClickListener {

    private static final String TAG = "OrderHistoryFragment";

    private OrderHistoryViewModel viewModel;
    private TokenManager tokenManager;
    private OrderAdapter orderAdapter;

    private View backButton;
    private RecyclerView ordersRecyclerView;
    private View loadingProgressBar;
    private View emptyStateLayout;

    // Status tabs
    private TextView tabAll;
    private TextView tabPending;
    private TextView tabPaid;
    private TextView tabShipped;
    private TextView tabCompleted;
    private TextView tabCancelled;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_order_history, container, false);

        viewModel = new ViewModelProvider(this).get(OrderHistoryViewModel.class);
        tokenManager = new TokenManager(requireContext());

        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        observeViewModel();

        // Load all orders initially
        loadOrders(null);

        return view;
    }

    private void initializeViews(View view) {
        backButton = view.findViewById(R.id.backButton);
        ordersRecyclerView = view.findViewById(R.id.ordersRecyclerView);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);

        tabAll = view.findViewById(R.id.tabAll);
        tabPending = view.findViewById(R.id.tabPending);
        tabPaid = view.findViewById(R.id.tabPaid);
        tabShipped = view.findViewById(R.id.tabShipped);
        tabCompleted = view.findViewById(R.id.tabCompleted);
        tabCancelled = view.findViewById(R.id.tabCancelled);
    }

    private void setupRecyclerView() {
        orderAdapter = new OrderAdapter(this);
        ordersRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void setupListeners() {
        // Back button
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());

        // Status tabs
        tabAll.setOnClickListener(v -> {
            loadOrders(null);
            updateTabStyles(tabAll);
        });

        tabPending.setOnClickListener(v -> {
            loadOrders("pending");
            updateTabStyles(tabPending);
        });

        tabPaid.setOnClickListener(v -> {
            loadOrders("paid");
            updateTabStyles(tabPaid);
        });

        tabShipped.setOnClickListener(v -> {
            loadOrders("shipped");
            updateTabStyles(tabShipped);
        });

        tabCompleted.setOnClickListener(v -> {
            loadOrders("completed");
            updateTabStyles(tabCompleted);
        });

        tabCancelled.setOnClickListener(v -> {
            loadOrders("cancelled");
            updateTabStyles(tabCancelled);
        });
    }

    private void observeViewModel() {
        // Observe orders
        viewModel.getOrders().observe(getViewLifecycleOwner(), orders -> {
            if (orders != null) {
                orderAdapter.setOrders(orders);

                // Show/hide empty state
                if (orders.isEmpty()) {
                    ordersRecyclerView.setVisibility(View.GONE);
                    emptyStateLayout.setVisibility(View.VISIBLE);
                } else {
                    ordersRecyclerView.setVisibility(View.VISIBLE);
                    emptyStateLayout.setVisibility(View.GONE);
                }

                Log.d(TAG, "Orders updated: " + orders.size());
            }
        });

        // Observe loading
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                ordersRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            }
        });

        // Observe error
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    private void loadOrders(String status) {
        String userId = tokenManager.getUserId();
        if (userId == null) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            return;
        }

        viewModel.loadOrders(userId, status);
        Log.d(TAG, "Loading orders with status: " + (status != null ? status : "all"));
    }

    private void updateTabStyles(TextView selectedTab) {
        // Reset all tabs
        resetTabStyle(tabAll);
        resetTabStyle(tabPending);
        resetTabStyle(tabPaid);
        resetTabStyle(tabShipped);
        resetTabStyle(tabCompleted);
        resetTabStyle(tabCancelled);

        // Highlight selected tab
        selectedTab.setTextColor(getResources().getColor(R.color.white, null));
        selectedTab.setBackgroundColor(getResources().getColor(R.color.red_500, null));
    }

    private void resetTabStyle(TextView tab) {
        tab.setTextColor(getResources().getColor(android.R.color.black, null));
        tab.setBackgroundColor(0xFFE0E0E0); // Light gray
    }

    @Override
    public void onOrderClick(Order order) {
        // TODO: Navigate to order detail screen
        Toast.makeText(getContext(), "Order clicked: " + order.getOrderId(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Order clicked: " + order.getOrderId());
    }
}
