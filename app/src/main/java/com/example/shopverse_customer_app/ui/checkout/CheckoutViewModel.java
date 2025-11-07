package com.example.shopverse_customer_app.ui.checkout;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.shopverse_customer_app.data.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CheckoutViewModel extends ViewModel {

    private static final String TAG = "CheckoutViewModel";
    private static final double SHIPPING_COST = 20000.0; // Fixed 20,000 VND

    private final MutableLiveData<List<CartItem>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> shippingAddress = new MutableLiveData<>();
    private final MutableLiveData<Double> subtotal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> total = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> orderSuccess = new MutableLiveData<>();

    public LiveData<List<CartItem>> getCartItems() {
        return cartItems;
    }

    public LiveData<String> getShippingAddress() {
        return shippingAddress;
    }

    public LiveData<Double> getSubtotal() {
        return subtotal;
    }

    public LiveData<Double> getTotal() {
        return total;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getOrderSuccess() {
        return orderSuccess;
    }

    public double getShippingCost() {
        return SHIPPING_COST;
    }

    /**
     * Initialize checkout with selected cart items
     */
    public void initialize(List<CartItem> selectedItems) {
        this.cartItems.setValue(selectedItems);
        calculateTotals();
        Log.d(TAG, "Checkout initialized with " + selectedItems.size() + " items");
    }

    /**
     * Set shipping address
     */
    public void setShippingAddress(String address) {
        this.shippingAddress.setValue(address);
    }

    /**
     * Calculate subtotal and total
     */
    private void calculateTotals() {
        List<CartItem> items = cartItems.getValue();
        if (items == null || items.isEmpty()) {
            subtotal.setValue(0.0);
            total.setValue(SHIPPING_COST);
            return;
        }

        double sum = 0.0;
        for (CartItem item : items) {
            if (item.getProduct() != null) {
                sum += item.getProduct().getUnitPrice() * item.getQuantity();
            }
        }

        subtotal.setValue(sum);
        total.setValue(sum + SHIPPING_COST);

        Log.d(TAG, "Subtotal: " + sum + ", Total: " + (sum + SHIPPING_COST));
    }

    /**
     * Place order
     */
    public void placeOrder(String userId) {
        String address = shippingAddress.getValue();
        if (address == null || address.trim().isEmpty()) {
            error.setValue("Vui lòng nhập địa chỉ giao hàng");
            return;
        }

        List<CartItem> items = cartItems.getValue();
        if (items == null || items.isEmpty()) {
            error.setValue("Giỏ hàng trống");
            return;
        }

        loading.setValue(true);
        error.setValue(null);

        // TODO: Implement actual API call to create order
        // For now, simulate success after 1 second
        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            loading.postValue(false);
            orderSuccess.postValue(true);
            Log.d(TAG, "Order placed successfully");
        }, 1000);

        Log.d(TAG, "Placing order for user: " + userId + ", address: " + address);
    }
}
