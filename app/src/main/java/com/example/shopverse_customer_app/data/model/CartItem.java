package com.example.shopverse_customer_app.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Cart item model
 * Database table: cart_items
 *
 * Schema:
 * - user_id (BIGINT, PK, FK to users)
 * - product_id (BIGINT, PK, FK to products)
 * - quantity (INTEGER, NOT NULL, CHECK > 0)
 *
 * Primary Key: (user_id, product_id)
 */
public class CartItem implements Serializable {

    @SerializedName("user_id")
    private String userId; // UUID from auth.users

    @SerializedName("product_id")
    private String productId; // UUID from products table

    @SerializedName("quantity")
    private int quantity;

    // Nested product object when using joins
    @SerializedName("products")
    private Product product;

    // UI state (not from database) - transient so it won't be serialized to API
    private transient boolean isSelected = false;

    public CartItem() {
    }

    public CartItem(String userId, String productId, int quantity) {
        this.userId = userId;
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters and Setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    /**
     * Calculate subtotal for this cart item
     */
    public double getSubtotal() {
        if (product != null) {
            return product.getUnitPrice() * quantity;
        }
        return 0.0;
    }

    /**
     * Check if product is available in stock for current quantity
     */
    public boolean isAvailable() {
        return product != null && product.isActive() && product.getStock() >= quantity;
    }

    /**
     * Get composite key as string for identification
     */
    public String getCompositeKey() {
        return userId + "_" + productId;
    }

    @Override
    public String toString() {
        return "CartItem{" +
                "userId=" + userId +
                ", productId=" + productId +
                ", quantity=" + quantity +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return userId.equals(cartItem.userId) && productId.equals(cartItem.productId);
    }

    @Override
    public int hashCode() {
        int result = userId.hashCode();
        result = 31 * result + productId.hashCode();
        return result;
    }
}
