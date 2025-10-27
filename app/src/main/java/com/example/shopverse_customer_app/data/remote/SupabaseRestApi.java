package com.example.shopverse_customer_app.data.remote;

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
}
