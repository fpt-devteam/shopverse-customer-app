package com.example.shopverse_customer_app.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

/**
 * Product model representing products table in Supabase
 */
public class Product implements Serializable {

    @SerializedName("product_id")
    private String productId;

    @SerializedName("category_id")
    private String categoryId;

    @SerializedName("brand_id")
    private String brandId;

    @SerializedName("product_media")
    private List<String> productMedia;

    @SerializedName("product_name")
    private String productName;

    @SerializedName("stock")
    private int stock;

    @SerializedName("unit_price")
    private double unitPrice;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status; // 'active' or 'inactive'

    // Optional: Nested brand and category objects if using joins
    @SerializedName("brands")
    private Brand brand;

    @SerializedName("categories")
    private Category category;

    // Constructors
    public Product() {
    }

    public Product(String productId, String productName, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
    }

    // Getters and Setters
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public List<String> getProductMedia() {
        return productMedia;
    }

    public void setProductMedia(List<String> productMedia) {
        this.productMedia = productMedia;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Brand getBrand() {
        return brand;
    }

    public void setBrand(Brand brand) {
        this.brand = brand;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * Get first product image URL
     */
    public String getFirstImageUrl() {
        if (productMedia != null && !productMedia.isEmpty()) {
            return productMedia.get(0);
        }
        return null;
    }

    /**
     * Check if product is in stock
     */
    public boolean isInStock() {
        return stock > 0;
    }

    /**
     * Check if product is active
     */
    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", unitPrice=" + unitPrice +
                ", stock=" + stock +
                ", status='" + status + '\'' +
                '}';
    }
}
