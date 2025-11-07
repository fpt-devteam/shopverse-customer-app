package com.example.shopverse_customer_app.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * OrderItem model representing order_items table
 */
public class OrderItem implements Serializable {

    @SerializedName("order_id")
    private Long orderId;

    @SerializedName("product_id")
    private String productId;

    @SerializedName("discount_id")
    private Long discountId;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("unit_price")
    private double unitPrice;

    public OrderItem() {
    }

    public OrderItem(Long orderId, String productId, int quantity, double unitPrice) {
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public Long getDiscountId() {
        return discountId;
    }

    public void setDiscountId(Long discountId) {
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
