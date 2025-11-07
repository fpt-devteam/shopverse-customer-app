package com.example.shopverse_customer_app.ui.orderhistory;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.shopverse_customer_app.data.model.Order;
import com.example.shopverse_customer_app.data.remote.RetrofitClient;
import com.example.shopverse_customer_app.data.remote.SupabaseRestApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * ViewModel for Order History screen
 */
public class OrderHistoryViewModel extends ViewModel {

    private static final String TAG = "OrderHistoryViewModel";

    private final MutableLiveData<List<Order>> orders = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> currentStatus = new MutableLiveData<>(null); // null = all

    private final SupabaseRestApi restApi;

    public OrderHistoryViewModel() {
        restApi = RetrofitClient.getInstance().getRestApi();
    }

    public LiveData<List<Order>> getOrders() {
        return orders;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getCurrentStatus() {
        return currentStatus;
    }

    /**
     * Load orders by status
     * @param userId User ID
     * @param status Order status ("pending", "paid", "shipped", "completed", "cancelled") or null for all
     */
    public void loadOrders(String userId, String status) {
        if (userId == null) {
            error.setValue("User ID is required");
            return;
        }

        loading.setValue(true);
        error.setValue(null);
        currentStatus.setValue(status);

        // Build status filter
        String statusFilter = null;
        if (status != null && !status.trim().isEmpty()) {
            statusFilter = "eq." + status;
        }

        Log.d(TAG, "Loading orders for user " + userId + " with status: " + status);

        restApi.getOrders("*", "eq." + userId, statusFilter, "order_date.desc")
                .enqueue(new Callback<List<Order>>() {
                    @Override
                    public void onResponse(@NonNull Call<List<Order>> call, @NonNull Response<List<Order>> response) {
                        loading.setValue(false);

                        if (response.isSuccessful() && response.body() != null) {
                            orders.setValue(response.body());
                            Log.d(TAG, "Loaded " + response.body().size() + " orders");
                        } else {
                            error.setValue("Không thể tải danh sách đơn hàng. Mã lỗi: " + response.code());
                            Log.e(TAG, "Failed to load orders: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<List<Order>> call, @NonNull Throwable t) {
                        loading.setValue(false);
                        error.setValue("Lỗi kết nối: " + t.getMessage());
                        Log.e(TAG, "Network error loading orders", t);
                    }
                });
    }

    /**
     * Load all orders (no status filter)
     */
    public void loadAllOrders(String userId) {
        loadOrders(userId, null);
    }
}
