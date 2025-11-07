package com.example.shopverse_customer_app.ui.cart;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.shopverse_customer_app.data.model.CartItem;
import com.example.shopverse_customer_app.data.repository.CartRepository;

import java.util.ArrayList;
import java.util.List;

public class CartViewModel extends ViewModel {

    private static final String TAG = "CartViewModel";

    private final CartRepository cartRepository;

    private final MutableLiveData<List<CartItem>> cartItems = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> success = new MutableLiveData<>();
    private final MutableLiveData<Double> totalAmount = new MutableLiveData<>(0.0);
    private final MutableLiveData<Boolean> allSelected = new MutableLiveData<>(false);

    public CartViewModel() {
        cartRepository = new CartRepository();
    }

    // LiveData getters
    public LiveData<List<CartItem>> getCartItems() {
        return cartItems;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getSuccess() {
        return success;
    }

    public LiveData<Double> getTotalAmount() {
        return totalAmount;
    }

    public LiveData<Boolean> getAllSelected() {
        return allSelected;
    }

    /**
     * Load cart items for user
     *
     * @param userId User ID (UUID string)
     */
    public void loadCartItems(String userId) {
        loading.setValue(true);
        error.setValue(null);

        cartRepository.getCartItems(userId, new CartRepository.CartItemsCallback() {
            @Override
            public void onSuccess(List<CartItem> items) {
                loading.postValue(false);
                cartItems.postValue(items);
                calculateTotal();
                Log.d(TAG, "Loaded " + items.size() + " cart items");
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
                Log.e(TAG, "Error loading cart items: " + errorMsg);
            }
        });
    }

    /**
     * Increase quantity of cart item
     */
    public void increaseQuantity(CartItem cartItem) {
        // Check stock availability
        if (cartItem.getProduct() != null && cartItem.getQuantity() >= cartItem.getProduct().getStock()) {
            error.setValue("Không đủ hàng trong kho");
            return;
        }

        int newQuantity = cartItem.getQuantity() + 1;
        updateQuantity(cartItem, newQuantity);
    }

    /**
     * Decrease quantity of cart item
     */
    public void decreaseQuantity(CartItem cartItem) {
        int newQuantity = cartItem.getQuantity() - 1;
        if (newQuantity < 1) {
            error.setValue("Số lượng tối thiểu là 1");
            return;
        }
        updateQuantity(cartItem, newQuantity);
    }

    /**
     * Update cart item quantity
     */
    private void updateQuantity(CartItem cartItem, int newQuantity) {
        loading.setValue(true);
        error.setValue(null); // Clear previous errors

        cartRepository.updateCartItemQuantity(
                cartItem.getUserId(),
                cartItem.getProductId(),
                newQuantity,
                new CartRepository.CartItemCallback() {
            @Override
            public void onSuccess(CartItem updatedItem) {
                loading.postValue(false);
                // Update local list
                List<CartItem> currentItems = cartItems.getValue();
                if (currentItems != null) {
                    for (int i = 0; i < currentItems.size(); i++) {
                        CartItem item = currentItems.get(i);
                        if (item.getUserId().equals(cartItem.getUserId()) &&
                            item.getProductId().equals(cartItem.getProductId())) {
                            currentItems.get(i).setQuantity(newQuantity);
                            break;
                        }
                    }
                    cartItems.postValue(currentItems);
                    calculateTotal();
                }
                Log.d(TAG, "Updated quantity to " + newQuantity);
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
                Log.e(TAG, "Error updating quantity: " + errorMsg);
            }
        });
    }

    /**
     * Delete cart item
     */
    public void deleteCartItem(CartItem cartItem) {
        loading.setValue(true);

        cartRepository.deleteCartItem(
                cartItem.getUserId(),
                cartItem.getProductId(),
                new CartRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                loading.postValue(false);
                // Remove from local list
                List<CartItem> currentItems = cartItems.getValue();
                if (currentItems != null) {
                    currentItems.removeIf(item ->
                        item.getUserId().equals(cartItem.getUserId()) &&
                        item.getProductId().equals(cartItem.getProductId())
                    );
                    cartItems.postValue(currentItems);
                    calculateTotal();
                }
                Log.d(TAG, "Deleted cart item successfully");
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
                Log.e(TAG, "Error deleting cart item: " + errorMsg);
            }
        });
    }

    /**
     * Toggle selection of a cart item
     */
    public void toggleItemSelection(CartItem cartItem) {
        List<CartItem> currentItems = cartItems.getValue();
        if (currentItems != null) {
            for (CartItem item : currentItems) {
                if (item.getUserId().equals(cartItem.getUserId()) &&
                    item.getProductId().equals(cartItem.getProductId())) {
                    item.setSelected(!item.isSelected());
                    break;
                }
            }
            cartItems.postValue(currentItems);
            updateAllSelectedState();
            calculateTotal();
        }
    }

    /**
     * Select/Deselect all items
     */
    public void toggleSelectAll(boolean selectAll) {
        List<CartItem> currentItems = cartItems.getValue();
        if (currentItems != null) {
            for (CartItem item : currentItems) {
                item.setSelected(selectAll);
            }
            cartItems.postValue(currentItems);
            allSelected.postValue(selectAll);
            calculateTotal();
        }
    }

    /**
     * Update "all selected" state based on individual items
     */
    private void updateAllSelectedState() {
        List<CartItem> currentItems = cartItems.getValue();
        if (currentItems != null && !currentItems.isEmpty()) {
            boolean allItemsSelected = true;
            for (CartItem item : currentItems) {
                if (!item.isSelected()) {
                    allItemsSelected = false;
                    break;
                }
            }
            allSelected.postValue(allItemsSelected);
        } else {
            allSelected.postValue(false);
        }
    }

    /**
     * Calculate total amount for selected items
     */
    private void calculateTotal() {
        List<CartItem> currentItems = cartItems.getValue();
        double total = 0.0;

        if (currentItems != null) {
            for (CartItem item : currentItems) {
                if (item.isSelected() && item.getProduct() != null) {
                    total += item.getSubtotal();
                }
            }
        }

        totalAmount.postValue(total);
    }

    /**
     * Get list of selected cart items
     */
    public List<CartItem> getSelectedItems() {
        List<CartItem> selectedItems = new ArrayList<>();
        List<CartItem> currentItems = cartItems.getValue();

        if (currentItems != null) {
            for (CartItem item : currentItems) {
                if (item.isSelected()) {
                    selectedItems.add(item);
                }
            }
        }

        return selectedItems;
    }

    /**
     * Retry loading cart items
     */
    public void retry(String userId) {
        loadCartItems(userId);
    }

    /**
     * Add or update item in cart
     * Convenience method for adding products to cart from product detail screen
     *
     * @param userId User ID (UUID string)
     * @param productId Product ID (UUID string)
     * @param quantity Quantity to add
     */
    public void addOrUpdateCartItem(String userId, String productId, int quantity) {
        loading.setValue(true);
        error.setValue(null);
        success.setValue(null);

        cartRepository.addOrUpdateCartItem(userId, productId, quantity,
                new CartRepository.CartItemCallback() {
            @Override
            public void onSuccess(CartItem cartItem) {
                loading.postValue(false);
                success.postValue("Đã thêm vào giỏ hàng");
                // Reload cart items to get fresh data
                loadCartItems(userId);
                Log.d(TAG, "Added/updated cart item successfully");
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
                Log.e(TAG, "Error adding/updating cart item: " + errorMsg);
            }
        });
    }
}
