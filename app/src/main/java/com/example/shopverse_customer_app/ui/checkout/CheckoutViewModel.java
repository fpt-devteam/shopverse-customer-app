package com.example.shopverse_customer_app.ui.checkout;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.shopverse_customer_app.data.model.CartItem;
import com.example.shopverse_customer_app.data.model.Order;
import com.example.shopverse_customer_app.data.model.OrderItem;
import com.example.shopverse_customer_app.data.model.PaymentResponse;
import com.example.shopverse_customer_app.data.remote.PaymentService;
import com.example.shopverse_customer_app.data.remote.RetrofitClient;
import com.example.shopverse_customer_app.data.remote.SupabaseRestApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CheckoutViewModel extends ViewModel {

    private static final String TAG = "CheckoutViewModel";
    private static final double SHIPPING_COST = 20000.0; // Fixed 20,000 VND

    private final MutableLiveData<List<CartItem>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> shippingAddress = new MutableLiveData<>();
    private final MutableLiveData<Double> subtotal = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> total = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> paymentUrl = new MutableLiveData<>();

    private final SupabaseRestApi restApi;
    private final PaymentService paymentService;

    public CheckoutViewModel() {
        restApi = RetrofitClient.getInstance().getRestApi();
        paymentService = RetrofitClient.getInstance().getPaymentService();
    }

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

    public LiveData<String> getPaymentUrl() {
        return paymentUrl;
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
     * Place order - Complete payment flow
     * Step 1: Create Order
     * Step 2: Create Order Items
     * Step 3: Get Payment Link
     * Step 4: Return payment URL
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

        Double totalPrice = total.getValue();
        if (totalPrice == null) {
            error.setValue("Lỗi tính tổng tiền");
            return;
        }

        loading.setValue(true);
        error.setValue(null);

        // Step 1: Create Order
        Order order = new Order(userId, totalPrice, 0.0, address);

        Log.d(TAG, "Step 1: Creating order for user " + userId);

        restApi.createOrder(order).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    Order createdOrder = response.body().get(0);
                    String orderId = createdOrder.getOrderId();

                    Log.d(TAG, "Order created successfully with ID: " + orderId);

                    // Step 2: Create Order Items
                    createOrderItems(orderId, items);
                } else {
                    loading.postValue(false);
                    error.postValue("Không thể tạo đơn hàng. Mã lỗi: " + response.code());
                    Log.e(TAG, "Failed to create order: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                loading.postValue(false);
                error.postValue("Lỗi kết nối: " + t.getMessage());
                Log.e(TAG, "Network error creating order", t);
            }
        });
    }

    /**
     * Step 2: Create Order Items
     */
    private void createOrderItems(String orderId, List<CartItem> items) {
        Log.d(TAG, "Step 2: Creating order items for order " + orderId);

        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : items) {
            if (cartItem.getProduct() != null) {
                OrderItem orderItem = new OrderItem(
                        orderId,
                        cartItem.getProduct().getProductId(),
                        cartItem.getQuantity(),
                        cartItem.getProduct().getUnitPrice()
                );
                orderItems.add(orderItem);
            }
        }

        restApi.createOrderItems(orderItems).enqueue(new Callback<List<OrderItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<OrderItem>> call, @NonNull Response<List<OrderItem>> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Order items created successfully");

                    // Step 3: Get Payment Link
                    createPaymentLink(orderId);
                } else {
                    loading.postValue(false);
                    error.postValue("Không thể tạo chi tiết đơn hàng. Mã lỗi: " + response.code());
                    Log.e(TAG, "Failed to create order items: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<OrderItem>> call, @NonNull Throwable t) {
                loading.postValue(false);
                error.postValue("Lỗi kết nối: " + t.getMessage());
                Log.e(TAG, "Network error creating order items", t);
            }
        });
    }

    /**
     * Step 3: Create Payment Link via Edge Function
     */
    private void createPaymentLink(String orderId) {
        Log.d(TAG, "Step 3: Creating payment link for order " + orderId);

        PaymentService.PaymentRequest request = new PaymentService.PaymentRequest(orderId);

        paymentService.createPaymentLink(request).enqueue(new Callback<PaymentResponse>() {
            @Override
            public void onResponse(@NonNull Call<PaymentResponse> call, @NonNull Response<PaymentResponse> response) {
                loading.postValue(false);

                if (response.isSuccessful() && response.body() != null) {
                    String url = response.body().getPaymentUrl();

                    if (url != null && !url.isEmpty()) {
                        Log.d(TAG, "Payment link created: " + url);

                        // Step 4: Remove items from cart before opening payment
                        removeItemsFromCart();

                        paymentUrl.postValue(url);
                    } else {
                        error.postValue("Không nhận được link thanh toán");
                        Log.e(TAG, "Payment URL is null or empty");
                    }
                } else {
                    error.postValue("Không thể tạo link thanh toán. Mã lỗi: " + response.code());
                    Log.e(TAG, "Failed to create payment link: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<PaymentResponse> call, @NonNull Throwable t) {
                loading.postValue(false);
                error.postValue("Lỗi kết nối: " + t.getMessage());
                Log.e(TAG, "Network error creating payment link", t);
            }
        });
    }

    /**
     * Step 4: Remove items from cart after payment link is created
     * Fire and forget - we don't care about the response
     */
    private void removeItemsFromCart() {
        List<CartItem> items = cartItems.getValue();
        if (items == null || items.isEmpty()) {
            return;
        }

        Log.d(TAG, "Step 4: Removing " + items.size() + " items from cart");

        for (CartItem item : items) {
            if (item.getProduct() != null) {
                String userId = item.getUserId();
                String productId = item.getProduct().getProductId();

                // Fire and forget - no callback needed
                restApi.deleteCartItem("eq." + userId, "eq." + productId).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "Successfully removed product " + productId + " from cart");
                        } else {
                            Log.w(TAG, "Failed to remove product " + productId + " from cart: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                        Log.w(TAG, "Error removing product " + productId + " from cart", t);
                    }
                });
            }
        }
    }
}
