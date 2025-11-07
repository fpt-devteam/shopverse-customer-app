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
     */
    class PaymentRequest {
        private Long order_id;

        public PaymentRequest(Long orderId) {
            this.order_id = orderId;
        }

        public Long getOrder_id() {
            return order_id;
        }

        public void setOrder_id(Long order_id) {
            this.order_id = order_id;
        }
    }
}
