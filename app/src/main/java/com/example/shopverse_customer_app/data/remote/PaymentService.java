package com.example.shopverse_customer_app.data.remote;

import com.example.shopverse_customer_app.data.model.PaymentResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * API interface for Supabase Edge Functions
 */
public interface PaymentService {

    /**
     * Create payment link via Edge Function
     * POST https://uehonyhpopuxynbzshyo.supabase.co/functions/v1/create-payment
     *
     * @param request Request body with order_id
     * @return Payment response with payment_url
     */
    @POST("functions/v1/create-payment")
    Call<PaymentResponse> createPaymentLink(@Body PaymentRequest request);

    /**
     * Payment request body
     * Note: Edge Function expects "orderId" (camelCase), not "order_id" (snake_case)
     */
    class PaymentRequest {
        private String orderId; // UUID from orders table

        public PaymentRequest(String orderId) {
            this.orderId = orderId;
        }

        public String getOrderId() {
            return orderId;
        }

        public void setOrderId(String orderId) {
            this.orderId = orderId;
        }
    }
}
