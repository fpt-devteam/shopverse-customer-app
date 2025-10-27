package com.example.shopverse_customer_app.data.remote;

import androidx.annotation.NonNull;

import com.example.shopverse_customer_app.config.SupabaseConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Auth Interceptor that adds Supabase apikey header to all requests
 * and Bearer token when available
 */
public class AuthInterceptor implements Interceptor {

    private volatile String accessToken; // nullable

    /**
     * Set access token for authenticated requests
     */
    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    /**
     * Clear access token (logout)
     */
    public void clearAccessToken() {
        this.accessToken = null;
    }

    /**
     * Get current access token
     */
    public String getAccessToken() {
        return accessToken;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header(SupabaseConfig.HEADER_API_KEY, SupabaseConfig.SUPABASE_ANON_KEY)
                .header(SupabaseConfig.HEADER_CONTENT_TYPE, SupabaseConfig.CONTENT_TYPE_JSON);

        // Add Bearer token if available
        if (accessToken != null && !accessToken.isEmpty()) {
            builder.header(SupabaseConfig.HEADER_AUTHORIZATION, "Bearer " + accessToken);
        }

        return chain.proceed(builder.build());
    }
}
