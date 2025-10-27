package com.example.shopverse_customer_app.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Brand model representing brands table in Supabase
 */
public class Brand implements Serializable {

    @SerializedName("brand_id")
    private String brandId;

    @SerializedName("brand_name")
    private String brandName;

    @SerializedName("brand_logo_url")
    private String brandLogoUrl;

    // Constructors
    public Brand() {
    }

    public Brand(String brandId, String brandName) {
        this.brandId = brandId;
        this.brandName = brandName;
    }

    // Getters and Setters
    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getBrandLogoUrl() {
        return brandLogoUrl;
    }

    public void setBrandLogoUrl(String brandLogoUrl) {
        this.brandLogoUrl = brandLogoUrl;
    }

    @Override
    public String toString() {
        return "Brand{" +
                "brandId='" + brandId + '\'' +
                ", brandName='" + brandName + '\'' +
                ", brandLogoUrl='" + brandLogoUrl + '\'' +
                '}';
    }
}
