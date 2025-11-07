package com.example.shopverse_customer_app.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Payment response from Edge Function
 */
public class PaymentResponse {

    @SerializedName("payment_url")
    private String paymentUrl;

    public PaymentResponse() {
    }

    public String getPaymentUrl() {
        return paymentUrl;
    }

    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }
}
