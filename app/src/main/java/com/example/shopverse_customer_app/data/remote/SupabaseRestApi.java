package com.example.shopverse_customer_app.data.remote;

import com.example.shopverse_customer_app.data.model.Brand;
import com.example.shopverse_customer_app.data.model.CartItem;
import com.example.shopverse_customer_app.data.model.Category;
import com.example.shopverse_customer_app.data.model.Order;
import com.example.shopverse_customer_app.data.model.OrderItem;
import com.example.shopverse_customer_app.data.model.Product;
import com.example.shopverse_customer_app.data.model.Profile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
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
     * Get products by category with optional brand filter and search
     * GET /rest/v1/products?select=*,brands(*),categories(*)&category_id=eq.{id}&status=eq.active
     * Optional: &brand_id=eq.{brandId} or &brand_id=in.(id1,id2,id3)
     * Optional: &product_name=ilike.*search*
     */
    @GET("rest/v1/products")
    Call<List<Product>> getProducts(
            @Query("select") String select,
            @Query("category_id") String categoryIdFilter,
            @Query("brand_id") String brandIdFilter,
            @Query("status") String statusFilter,
            @Query("product_name") String productNameFilter,
            @Query("order") String order
    );

    // ========== CART ITEMS ==========

    /**
     * Get cart items for a user (with product join)
     * GET /rest/v1/cart_items?select=*,products(*)&user_id=eq.{id}&order={order}
     *
     * @param select Fields to select with joins (e.g., "*,products(*)")
     * @param userIdFilter User ID filter (e.g., "eq.123")
     * @param order Sort order (e.g., "updated_at.desc" for newest first)
     * @return List of cart items with product details
     */
    @GET("rest/v1/cart_items")
    Call<List<CartItem>> getCartItems(
            @Query("select") String select,
            @Query("user_id") String userIdFilter,
            @Query("order") String order
    );

    /**
     * Add item to cart (or update if already exists due to UNIQUE constraint)
     * POST /rest/v1/cart_items
     *
     * Note: If item already exists (same user_id + product_id), this will fail.
     * Use PATCH with upsert header or check existence first.
     *
     * @param cartItem Cart item to add
     * @return Created cart item (as array with single element)
     */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/cart_items")
    Call<List<CartItem>> addToCart(@Body CartItem cartItem);

    /**
     * Update cart item quantity using composite primary key
     * PATCH /rest/v1/cart_items?user_id=eq.{userId}&product_id=eq.{productId}
     *
     * @param userIdFilter User ID filter (e.g., "eq.123")
     * @param productIdFilter Product ID filter (e.g., "eq.456")
     * @param cartItem Updated cart item data (typically just quantity)
     * @return List of updated cart items (should be single item)
     */
    @Headers("Prefer: return=representation")
    @PATCH("rest/v1/cart_items")
    Call<List<CartItem>> updateCartItem(
            @Query("user_id") String userIdFilter,
            @Query("product_id") String productIdFilter,
            @Body CartItem cartItem
    );

    /**
     * Delete cart item using composite primary key
     * DELETE /rest/v1/cart_items?user_id=eq.{userId}&product_id=eq.{productId}
     *
     * @param userIdFilter User ID filter (e.g., "eq.123")
     * @param productIdFilter Product ID filter (e.g., "eq.456")
     * @return Void
     */
    @DELETE("rest/v1/cart_items")
    Call<Void> deleteCartItem(
            @Query("user_id") String userIdFilter,
            @Query("product_id") String productIdFilter
    );

    // ========== ORDERS ==========

    /**
     * Create a new order
     * POST /rest/v1/orders
     *
     * @param order Order object to create
     * @return Created order with order_id
     */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/orders")
    Call<List<Order>> createOrder(@Body Order order);

    /**
     * Get orders for a user
     * GET /rest/v1/orders?select=*&user_id=eq.{id}&status=eq.{status}&order=order_date.desc
     *
     * @param select Fields to select
     * @param userIdFilter User ID filter (e.g., "eq.{userId}")
     * @param statusFilter Status filter (e.g., "eq.pending") or null for all
     * @param order Sort order (e.g., "order_date.desc")
     * @return List of orders
     */
    @GET("rest/v1/orders")
    Call<List<Order>> getOrders(
            @Query("select") String select,
            @Query("user_id") String userIdFilter,
            @Query("status") String statusFilter,
            @Query("order") String order
    );

    /**
     * Create order items
     * POST /rest/v1/order_items
     *
     * @param orderItems List of order items to insert
     * @return Created order items
     */
    @Headers("Prefer: return=representation")
    @POST("rest/v1/order_items")
    Call<List<OrderItem>> createOrderItems(@Body List<OrderItem> orderItems);

    /**
     * Inner class to handle nested brand response from join query
     */
    class BrandResponse {
        @com.google.gson.annotations.SerializedName("brands")
        public Brand brand;
    }
}
