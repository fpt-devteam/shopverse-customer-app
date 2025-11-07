package com.example.shopverse_customer_app.ui.checkout;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.CartItem;
import com.example.shopverse_customer_app.utils.TokenManager;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class CheckoutFragment extends Fragment {

    private static final String TAG = "CheckoutFragment";
    private static final String ARG_CART_ITEMS = "cart_items";

    private CheckoutViewModel viewModel;
    private TokenManager tokenManager;
    private CheckoutProductAdapter productAdapter;

    private View shippingAddressContainer;
    private TextView shippingAddressText;
    private RecyclerView checkoutProductsRecyclerView;
    private TextView subtotalText;
    private TextView shippingCostText;
    private TextView totalText;
    private MaterialButton placeOrderButton;
    private ProgressBar loadingProgressBar;

    public static CheckoutFragment newInstance(ArrayList<CartItem> cartItems) {
        CheckoutFragment fragment = new CheckoutFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CART_ITEMS, cartItems);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_checkout, container, false);

        viewModel = new ViewModelProvider(this).get(CheckoutViewModel.class);
        tokenManager = new TokenManager(requireContext());

        initializeViews(view);
        setupRecyclerView();
        setupListeners();
        observeViewModel();

        // Initialize with cart items from arguments
        if (getArguments() != null) {
            ArrayList<CartItem> cartItems = (ArrayList<CartItem>) getArguments().getSerializable(ARG_CART_ITEMS);
            if (cartItems != null && !cartItems.isEmpty()) {
                viewModel.initialize(cartItems);
            }
        }

        // Load user's address from TokenManager or database
        loadUserAddress();

        return view;
    }

    private void initializeViews(View view) {
        shippingAddressContainer = view.findViewById(R.id.shippingAddressContainer);
        shippingAddressText = view.findViewById(R.id.shippingAddressText);
        checkoutProductsRecyclerView = view.findViewById(R.id.checkoutProductsRecyclerView);
        subtotalText = view.findViewById(R.id.subtotalText);
        shippingCostText = view.findViewById(R.id.shippingCostText);
        totalText = view.findViewById(R.id.totalText);
        placeOrderButton = view.findViewById(R.id.placeOrderButton);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        View backButton = view.findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> Navigation.findNavController(v).navigateUp());
    }

    private void setupRecyclerView() {
        productAdapter = new CheckoutProductAdapter();
        checkoutProductsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        checkoutProductsRecyclerView.setAdapter(productAdapter);
    }

    private void setupListeners() {
        // Shipping address click → open dialog to edit
        shippingAddressContainer.setOnClickListener(v -> showAddressDialog());

        // Place order button
        placeOrderButton.setOnClickListener(v -> {
            String userId = tokenManager.getUserId();
            if (userId != null) {
                viewModel.placeOrder(userId);
            } else {
                Toast.makeText(getContext(), "Vui lòng đăng nhập", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void observeViewModel() {
        // Observe cart items
        viewModel.getCartItems().observe(getViewLifecycleOwner(), cartItems -> {
            if (cartItems != null) {
                productAdapter.setCartItems(cartItems);
            }
        });

        // Observe shipping address
        viewModel.getShippingAddress().observe(getViewLifecycleOwner(), address -> {
            if (address != null && !address.isEmpty()) {
                shippingAddressText.setText(address);
            } else {
                shippingAddressText.setText("Nhấn để thêm địa chỉ giao hàng");
            }
        });

        // Observe subtotal
        viewModel.getSubtotal().observe(getViewLifecycleOwner(), subtotal -> {
            if (subtotal != null) {
                subtotalText.setText(formatPrice(subtotal));
            }
        });

        // Observe total
        viewModel.getTotal().observe(getViewLifecycleOwner(), total -> {
            if (total != null) {
                totalText.setText(formatPrice(total));
            }
        });

        // Display shipping cost
        shippingCostText.setText(formatPrice(viewModel.getShippingCost()));

        // Observe loading
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                placeOrderButton.setEnabled(!isLoading);
            }
        });

        // Observe error
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_LONG).show();
            }
        });

        // Observe order success
        viewModel.getOrderSuccess().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                showOrderSuccessDialog();
            }
        });
    }

    private void loadUserAddress() {
        // TODO: Load from database
        // For now, check if there's a saved address in TokenManager or use placeholder
        String savedAddress = null; // tokenManager.getAddress() if you implement it
        if (savedAddress != null && !savedAddress.isEmpty()) {
            viewModel.setShippingAddress(savedAddress);
        }
    }

    private void showAddressDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Địa chỉ giao hàng");

        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        input.setHint("VD: 123 Lê Lợi, Quận 1, TP.HCM");
        input.setMinLines(3);
        input.setPadding(50, 30, 50, 30);

        // Pre-fill with current address
        String currentAddress = viewModel.getShippingAddress().getValue();
        if (currentAddress != null && !currentAddress.isEmpty()) {
            input.setText(currentAddress);
        }

        builder.setView(input);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String address = input.getText().toString().trim();
            if (!address.isEmpty()) {
                viewModel.setShippingAddress(address);
                // TODO: Save to database
            } else {
                Toast.makeText(getContext(), "Vui lòng nhập địa chỉ", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.create().show();
    }

    private void showOrderSuccessDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đặt hàng thành công!")
                .setMessage("Đơn hàng của bạn đã được đặt thành công. Cảm ơn bạn đã mua hàng!")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Navigate back to home or orders page
                    Navigation.findNavController(requireView()).navigateUp();
                })
                .setCancelable(false)
                .show();
    }

    private String formatPrice(double price) {
        long priceInt = (long) price;
        String formatted = String.format("%,d", priceInt).replace(",", ".");
        return formatted + "₫";
    }
}
