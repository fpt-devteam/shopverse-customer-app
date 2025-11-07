package com.example.shopverse_customer_app.data.remote;

import com.example.shopverse_customer_app.config.SupabaseConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Retrofit client with AuthInterceptor pattern
 * Single Retrofit instance for all Supabase endpoints
 */
public class RetrofitClient {

    private static RetrofitClient instance;
    private final AuthInterceptor authInterceptor;
    private final Retrofit retrofit;
    private SupabaseAuthApi authApi;
    private SupabaseRestApi restApi;
    private PaymentService paymentService;

    /**
     * Private constructor - Singleton pattern
     */
    private RetrofitClient() {
        // Create auth interceptor
        authInterceptor = new AuthInterceptor();

        // Logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create OkHttpClient with interceptors
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Create single Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(SupabaseConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    /**
     * Get singleton instance
     */
    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    /**
     * Get Auth API service
     */
    public SupabaseAuthApi getAuthApi() {
        if (authApi == null) {
            authApi = retrofit.create(SupabaseAuthApi.class);
        }
        return authApi;
    }

    /**
     * Get REST API service
     */
    public SupabaseRestApi getRestApi() {
        if (restApi == null) {
            restApi = retrofit.create(SupabaseRestApi.class);
        }
        return restApi;
    }

    /**
     * Get AuthInterceptor for token management
     */
    public AuthInterceptor getAuthInterceptor() {
        return authInterceptor;
    }

    /**
     * Set access token (for authenticated requests)
     */
    public void setAccessToken(String token) {
        authInterceptor.setAccessToken(token);
    }

    /**
     * Clear access token (logout)
     */
    public void clearAccessToken() {
        authInterceptor.clearAccessToken();
    }

    /**
     * Get PaymentService instance
     */
    public PaymentService getPaymentService() {
        if (paymentService == null) {
            paymentService = retrofit.create(PaymentService.class);
        }
        return paymentService;
    }

    /**
     * Reset instance (for testing)
     */
    public static void resetInstance() {
        instance = null;
    }
}

