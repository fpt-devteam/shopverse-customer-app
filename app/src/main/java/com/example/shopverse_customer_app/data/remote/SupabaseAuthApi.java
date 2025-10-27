package com.example.shopverse_customer_app.data.remote;

import com.example.shopverse_customer_app.data.model.AuthResponse;
import com.example.shopverse_customer_app.data.model.LoginRequest;
import com.example.shopverse_customer_app.data.model.RegisterRequest;
import com.example.shopverse_customer_app.data.model.User;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Retrofit interface for Supabase Auth API endpoints
 * Base URL: https://shopverse.supabase.co/
 * All paths are relative to base URL
 */
public interface SupabaseAuthApi {

    /**
     * LOGIN: POST /auth/v1/token?grant_type=password
     * Only requires apikey (no bearer), returns access & refresh tokens
     */
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest request);

    /**
     * SIGNUP: POST /auth/v1/signup
     * Only requires apikey, returns user object
     */
    @POST("auth/v1/signup")
    Call<User> signup(@Body RegisterRequest request);

    /**
     * RECOVER PASSWORD: POST /auth/v1/recover
     * Send password reset email (requires apikey only)
     */
    @POST("auth/v1/recover")
    Call<Void> recover(@Body JsonObject request);

    /**
     * GET USER: GET /auth/v1/user
     * Get current user info (requires apikey + bearer token)
     */
    @GET("auth/v1/user")
    Call<User> getUser();

    /**
     * UPDATE PASSWORD: PUT /auth/v1/user
     * Update user password (requires apikey + bearer token)
     */
    @PUT("auth/v1/user")
    Call<User> updatePassword(@Body JsonObject request);
}
