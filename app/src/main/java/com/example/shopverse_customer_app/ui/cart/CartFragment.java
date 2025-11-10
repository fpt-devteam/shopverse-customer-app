package com.example.shopverse_customer_app.ui.cart;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.CartItem;
import com.example.shopverse_customer_app.databinding.FragmentCartBinding;
import com.example.shopverse_customer_app.utils.TokenManager;

import java.text.NumberFormat;
import java.util.Locale;

/**
 * Fragment for displaying shopping cart
 */
public class CartFragment extends Fragment implements CartAdapter.OnCartItemListener {

    private static final String TAG = "CartFragment";

    private FragmentCartBinding binding;
    private CartViewModel cartViewModel;
    private CartAdapter cartAdapter;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tokenManager = new TokenManager(requireContext());
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);

        setupRecyclerView();
        setupListeners();
        observeViewModel();

        // Load cart items for current user
        String userId = tokenManager.getUserId();
        if (userId != null && !userId.isEmpty()) {
            cartViewModel.loadCartItems(userId);
        } else {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this);
        binding.recyclerViewCart.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewCart.setAdapter(cartAdapter);
    }

    private void setupListeners() {
        // Select all checkbox
        binding.checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (buttonView.isPressed()) { // Only trigger if user clicked, not programmatic change
                cartViewModel.toggleSelectAll(isChecked);
            }
        });

        binding.buttonBuy.setOnClickListener(v -> {
            java.util.List<CartItem> selectedItems = cartViewModel.getSelectedItems();
            if (selectedItems.isEmpty()) {
                Toast.makeText(getContext(), "Vui lòng chọn sản phẩm để mua", Toast.LENGTH_SHORT).show();
            } else {
                Bundle bundle = new Bundle();
                bundle.putSerializable("cart_items", new java.util.ArrayList<>(selectedItems));
                Navigation.findNavController(v).navigate(R.id.action_navigation_cart_to_navigation_checkout, bundle);
            }
        });
    }

    private void observeViewModel() {
        // Observe cart items
        cartViewModel.getCartItems().observe(getViewLifecycleOwner(), cartItems -> {
            if (cartItems != null) {
                cartAdapter.setCartItems(cartItems);

                // Show/hide empty state
                if (cartItems.isEmpty()) {
                    binding.emptyCartLayout.setVisibility(View.VISIBLE);
                    binding.recyclerViewCart.setVisibility(View.GONE);
                } else {
                    binding.emptyCartLayout.setVisibility(View.GONE);
                    binding.recyclerViewCart.setVisibility(View.VISIBLE);
                }
            }
        });

        // Observe loading state
        cartViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe errors
        cartViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });

        // Observe total amount
        cartViewModel.getTotalAmount().observe(getViewLifecycleOwner(), totalAmount -> {
            if (totalAmount != null) {
                String formattedTotal = formatPrice(totalAmount);
                binding.textTotalAmount.setText(formattedTotal);
            }
        });

        // Observe "select all" state
        cartViewModel.getAllSelected().observe(getViewLifecycleOwner(), allSelected -> {
            if (allSelected != null) {
                binding.checkboxSelectAll.setOnCheckedChangeListener(null);
                binding.checkboxSelectAll.setChecked(allSelected);
                binding.checkboxSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (buttonView.isPressed()) {
                        cartViewModel.toggleSelectAll(isChecked);
                    }
                });
            }
        });
    }

    @Override
    public void onIncreaseQuantity(CartItem cartItem) {
        cartViewModel.increaseQuantity(cartItem);
    }

    @Override
    public void onDecreaseQuantity(CartItem cartItem) {
        cartViewModel.decreaseQuantity(cartItem);
    }

    @Override
    public void onDeleteItem(CartItem cartItem) {
        // Show confirmation dialog
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Xóa sản phẩm")
                .setMessage("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    cartViewModel.deleteCartItem(cartItem);
                })
                .setNegativeButton("Hủy", null)
                .show();
    }

    @Override
    public void onItemSelectionChanged(CartItem cartItem) {
        cartViewModel.toggleItemSelection(cartItem);
    }

    /**
     * Format price to Vietnamese currency format
     */
    private String formatPrice(double price) {
        NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
        return format.format(price) + "đ";
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: Refreshing cart");

        // Refresh cart items when fragment resumes (e.g., after adding item from product detail)
        String userId = tokenManager.getUserId();
        if (userId != null && !userId.isEmpty()) {
            cartViewModel.loadCartItems(userId);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
