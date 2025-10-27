package com.example.shopverse_customer_app.data.remote;

import com.example.shopverse_customer_app.data.model.Brand;
import com.example.shopverse_customer_app.data.model.Category;
import com.example.shopverse_customer_app.data.model.Product;
import com.example.shopverse_customer_app.data.model.Profile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Retrofit interface for Supabase REST API (Database/PostgREST endpoints)
 * Base URL: https://shopverse.supabase.co/
 * All paths are relative to base URL
 * Requires apikey + bearer token (added by AuthInterceptor)
 */
public interface SupabaseRestApi {

    /**
     * Get profiles from profiles table
     * GET /rest/v1/profiles?select=*&user_id=eq.{id}
     */
    @GET("rest/v1/profiles")
    Call<List<Profile>> getProfiles(
            @Query("select") String select,
            @Query("user_id") String userIdFilter
    );

    /**
     * Create new profile in profiles table
     * POST /rest/v1/profiles
     */
    @POST("rest/v1/profiles")
    Call<Profile> createProfile(@Body Profile profile);

    /**
     * Update profile in profiles table
     * PATCH /rest/v1/profiles?user_id=eq.{id}
     */
    @PATCH("rest/v1/profiles")
    Call<List<Profile>> updateProfile(
            @Query("user_id") String userIdFilter,
            @Body Profile profile
    );

    /**
     * Get all categories from categories table
     * GET /rest/v1/categories?select=*
     */
    @GET("rest/v1/categories")
    Call<List<Category>> getCategories(
            @Query("select") String select
    );

    /**
     * Get brands for a specific category from categories_brands table with join
     * GET /rest/v1/categories_brands?select=brands(*)&category_id=eq.{id}
     */
    @GET("rest/v1/categories_brands")
    Call<List<BrandResponse>> getBrandsByCategory(
            @Query("select") String select,
            @Query("category_id") String categoryIdFilter
    );

    /**
     * Get products by category with optional brand filter
     * GET /rest/v1/products?select=*,brands(*),categories(*)&category_id=eq.{id}&status=eq.active
     * Optional: &brand_id=eq.{brandId}
     */
    @GET("rest/v1/products")
    Call<List<Product>> getProducts(
            @Query("select") String select,
            @Query("category_id") String categoryIdFilter,
            @Query("brand_id") String brandIdFilter,
            @Query("status") String statusFilter,
            @Query("order") String order
    );

    /**
     * Inner class to handle nested brand response from join query
     */
    class BrandResponse {
        @com.google.gson.annotations.SerializedName("brands")
        public Brand brand;
    }
}
