package com.example.shopverse_customer_app.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * OrderItem model representing order_items table
 */
public class OrderItem implements Serializable {

    @SerializedName("order_id")
    private String orderId; // UUID from orders table

    @SerializedName("product_id")
    private String productId; // UUID from products table

    @SerializedName("discount_id")
    private String discountId; // UUID from discounts table (nullable)

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("unit_price")
    private double unitPrice;

    public OrderItem() {
    }

    public OrderItem(String orderId, String productId, int quantity, double unitPrice) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getDiscountId() {
        return discountId;
    }

    public void setDiscountId(String discountId) {
        this.discountId = discountId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }
}
