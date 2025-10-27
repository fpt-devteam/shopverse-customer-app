package com.example.shopverse_customer_app.config;

import com.example.shopverse_customer_app.BuildConfig;

/**
 * Supabase configuration constants
 * Contains base URLs and API keys for Supabase backend
 * Values are read from BuildConfig (injected from local.properties)
 */
public class SupabaseConfig {

    // Supabase Project URL (from local.properties)
    public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;

    // Supabase Anonymous Key (from local.properties - safe for client-side use)
    public static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;

    // Base URL for Retrofit (must end with /)
    public static final String BASE_URL = SUPABASE_URL.endsWith("/") ? SUPABASE_URL : SUPABASE_URL + "/";

    // Storage Buckets
    public static final String BUCKET_PRODUCT_IMAGES = "product-images";
    public static final String BUCKET_USER_AVATARS = "user-avatars";

    // Headers
    public static final String HEADER_API_KEY = "apikey";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";

    private SupabaseConfig() {
        // Private constructor to prevent instantiation
    }
}
