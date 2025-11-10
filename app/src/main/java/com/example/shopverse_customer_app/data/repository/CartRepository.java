package com.example.shopverse_customer_app.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.shopverse_customer_app.data.model.CartItem;
import com.example.shopverse_customer_app.data.remote.RetrofitClient;
import com.example.shopverse_customer_app.data.remote.SupabaseRestApi;
import com.example.shopverse_customer_app.utils.ErrorParser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Repository for cart operations
 */
public class CartRepository {

    private static final String TAG = "CartRepository";
    private final SupabaseRestApi restApi;

    public CartRepository() {
        this.restApi = RetrofitClient.getInstance().getRestApi();
    }

    /**
     * Get all cart items for a user
     * Returns items in reverse chronological order (newest first)
     *
     * @param userId User ID (UUID string)
     * @param callback Callback with list of cart items
     */
    public void getCartItems(String userId, CartItemsCallback callback) {
        String select = "*,products(*)";
        String userFilter = "eq." + userId;
        String order = "updated_at.desc"; // Order by updated_at descending (newest first)

        Log.d(TAG, "Fetching cart items for user: " + userId + " with order: " + order);

        restApi.getCartItems(select, userFilter, order).enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<CartItem>> call,
                                   @NonNull Response<List<CartItem>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    Log.d(TAG, "Loaded " + response.body().size() + " cart items");
                } else {
                    String error = ErrorParser.parseError(response);
                    callback.onError(error);
                    Log.e(TAG, "Failed to load cart items: " + error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CartItem>> call, @NonNull Throwable t) {
                String error = ErrorParser.parseError(t);
                callback.onError(error);
                Log.e(TAG, "Network error loading cart items", t);
            }
        });
    }

    /**
     * Add item to cart
     * Note: Will fail if item already exists (UNIQUE constraint on user_id + product_id)
     * Consider checking existence first or handling the error
     *
     * @param cartItem Cart item to add (must have userId, productId, quantity)
     * @param callback Callback with created cart item
     */
    public void addToCart(CartItem cartItem, CartItemCallback callback) {
        Log.d(TAG, "Attempting INSERT - CartItem: " + cartItem.toString());

        restApi.addToCart(cartItem).enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<CartItem>> call,
                                   @NonNull Response<List<CartItem>> response) {
                int bodySize = (response.body() != null) ? response.body().size() : 0;
                Log.d(TAG, "INSERT Response Code: " + response.code() + ", Body size: " + bodySize);

                if (response.isSuccessful()) {
                    // Success - Supabase returns array with the inserted item
                    if (response.body() != null && !response.body().isEmpty()) {
                        callback.onSuccess(response.body().get(0));
                        Log.d(TAG, "Added item to cart successfully");
                    } else {
                        // Fallback: success but no body
                        callback.onSuccess(cartItem);
                        Log.d(TAG, "Added item to cart successfully (no body)");
                    }
                } else {
                    String error = ErrorParser.parseError(response);
                    callback.onError(error);
                    Log.e(TAG, "Failed to add to cart - Code: " + response.code() + ", Error: " + error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CartItem>> call, @NonNull Throwable t) {
                String error = ErrorParser.parseError(t);
                callback.onError(error);
                Log.e(TAG, "Network error adding to cart: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Update cart item quantity using composite primary key
     *
     * @param userId User ID (UUID string)
     * @param productId Product ID (UUID string)
     * @param newQuantity New quantity value
     * @param callback Callback with updated cart item
     */
    public void updateCartItemQuantity(String userId, String productId, int newQuantity,
                                       CartItemCallback callback) {
        String userFilter = "eq." + userId;
        String productFilter = "eq." + productId;

        CartItem updateData = new CartItem();
        updateData.setQuantity(newQuantity);

        Log.d(TAG, "Attempting UPDATE - userId: " + userId + ", productId: " + productId + ", quantity: " + newQuantity);

        restApi.updateCartItem(userFilter, productFilter, updateData)
                .enqueue(new Callback<List<CartItem>>() {
            @Override
            public void onResponse(@NonNull Call<List<CartItem>> call,
                                   @NonNull Response<List<CartItem>> response) {
                int bodySize = (response.body() != null) ? response.body().size() : 0;
                Log.d(TAG, "UPDATE Response Code: " + response.code() + ", Body size: " + bodySize);

                if (response.isSuccessful()) {
                    if (response.body() != null && !response.body().isEmpty()) {
                        // Got the updated item back
                        callback.onSuccess(response.body().get(0));
                        Log.d(TAG, "Updated cart item quantity to " + newQuantity);
                    } else if (response.code() == 200 || response.code() == 204) {
                        // Update was successful but no data returned (or empty array)
                        // Create a cart item with the updated quantity
                        CartItem updatedItem = new CartItem(userId, productId, newQuantity);
                        callback.onSuccess(updatedItem);
                        Log.d(TAG, "Updated cart item successfully (no body returned)");
                    } else {
                        callback.onError("Không thể cập nhật giỏ hàng");
                        Log.e(TAG, "UPDATE returned success but unexpected state");
                    }
                } else {
                    String error = ErrorParser.parseError(response);
                    callback.onError(error);
                    Log.e(TAG, "Failed to update cart item - Code: " + response.code() + ", Error: " + error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<CartItem>> call, @NonNull Throwable t) {
                String error = ErrorParser.parseError(t);
                callback.onError(error);
                Log.e(TAG, "Network error updating cart item: " + t.getMessage(), t);
            }
        });
    }

    /**
     * Delete cart item using composite primary key
     *
     * @param userId User ID (UUID string)
     * @param productId Product ID (UUID string)
     * @param callback Simple callback
     */
    public void deleteCartItem(String userId, String productId, SimpleCallback callback) {
        String userFilter = "eq." + userId;
        String productFilter = "eq." + productId;

        restApi.deleteCartItem(userFilter, productFilter).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                    Log.d(TAG, "Deleted cart item successfully");
                } else {
                    String error = ErrorParser.parseError(response);
                    callback.onError(error);
                    Log.e(TAG, "Failed to delete cart item: " + error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                String error = ErrorParser.parseError(t);
                callback.onError(error);
                Log.e(TAG, "Network error deleting cart item", t);
            }
        });
    }

    /**
     * Add to cart or update quantity if item already exists
     * This is a convenience method that handles the UNIQUE constraint
     *
     * @param userId User ID (UUID string)
     * @param productId Product ID (UUID string)
     * @param quantity Quantity to add (or set if updating)
     * @param callback Callback with cart item
     */
    public void addOrUpdateCartItem(String userId, String productId, int quantity,
                                    CartItemCallback callback) {
        CartItem newItem = new CartItem(userId, productId, quantity);

        Log.d(TAG, "Adding to cart - userId: " + userId + ", productId: " + productId + ", quantity: " + quantity);

        // Try to add first
        addToCart(newItem, new CartItemCallback() {
            @Override
            public void onSuccess(CartItem cartItem) {
                callback.onSuccess(cartItem);
            }

            @Override
            public void onError(String error) {
                // If add fails (likely due to duplicate), try update instead
                if (error.contains("duplicate") || error.contains("unique") ||
                    error.contains("already exists")) {
                    updateCartItemQuantity(userId, productId, quantity, callback);
                } else {
                    callback.onError(error);
                }
            }
        });
    }

    // Callback interfaces
    public interface CartItemsCallback {
        void onSuccess(List<CartItem> cartItems);
        void onError(String error);
    }

    public interface CartItemCallback {
        void onSuccess(CartItem cartItem);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
}
