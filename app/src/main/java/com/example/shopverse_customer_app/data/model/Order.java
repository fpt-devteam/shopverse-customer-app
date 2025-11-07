package com.example.shopverse_customer_app.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Order model representing orders table
 */
public class Order implements Serializable {

    @SerializedName("order_id")
    private Long orderId;

    @SerializedName("user_id")
    private String userId;

    @SerializedName("total_discount")
    private double totalDiscount;

    @SerializedName("total_price")
    private double totalPrice;

    @SerializedName("status")
    private String status; // 'pending', 'completed', 'cancelled'

    @SerializedName("order_date")
    private String orderDate;

    @SerializedName("address")
    private String address;

    public Order() {
    }

    public Order(String userId, double totalPrice, double totalDiscount, String address) {
        this.userId = userId;
        this.totalPrice = totalPrice;
        this.totalDiscount = totalDiscount;
        this.address = address;
        this.status = "pending"; // Default status
    }

    // Getters and Setters
    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public double getTotalDiscount() {
        return totalDiscount;
    }

    public void setTotalDiscount(double totalDiscount) {
        this.totalDiscount = totalDiscount;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
