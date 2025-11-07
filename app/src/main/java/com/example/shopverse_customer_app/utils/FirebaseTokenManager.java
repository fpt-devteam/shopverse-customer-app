package com.example.shopverse_customer_app.utils;

import android.content.Context;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import okhttp3.*;

import com.example.shopverse_customer_app.config.SupabaseConfig;

import java.io.IOException;

public class FirebaseTokenManager {

    private static final String SUPABASE_URL = SupabaseConfig.SUPABASE_URL;
    private static final String SUPABASE_KEY = SupabaseConfig.SUPABASE_ANON_KEY;

    public static void updateToken(Context context, String userId) {
        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.w("FCM", "Fetching FCM token failed", task.getException());
                        return;
                    }

                    String token = task.getResult();
                    Log.d("FCM", "Device token: " + token);

                    sendTokenToSupabase(context, userId, token);
                });
    }

    private static void sendTokenToSupabase(Context context, String userId, String token) {
        Log.d("Supabase", "userId " + userId);
        Log.d("Supabase", "device token: " + token);

        // Get user's JWT access token from TokenManager
        TokenManager tokenManager = new TokenManager(context);
        String accessToken = tokenManager.getAccessToken();

        if (accessToken == null || accessToken.isEmpty()) {
            Log.e("Supabase", "No access token found! User must be logged in to update device token.");
            return;
        }

        OkHttpClient client = new OkHttpClient();

        String json = "{\"device_token\":\"" + token + "\"}";
        RequestBody body = RequestBody.create(
                json, MediaType.parse("application/json; charset=utf-8")
        );

        // Build proper Supabase REST API URL
        String url = SUPABASE_URL + "/rest/v1/users?user_id=eq." + userId;
        Log.d("Supabase", "PATCH URL: " + url);

        Request request = new Request.Builder()
                .url(url)
                .patch(body)
                .addHeader("apikey", SUPABASE_KEY)
                .addHeader("Authorization", "Bearer " + accessToken)  // Use user's JWT token
                .addHeader("Content-Type", "application/json")
                .addHeader("Prefer", "return=representation")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("Supabase", "Failed to send token: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String responseBody = response.body() != null ? response.body().string() : "";
                String contentRange = response.header("Content-Range");

                Log.d("Supabase", "Response code: " + response.code());
                Log.d("Supabase", "Response body: " + responseBody);
                Log.d("Supabase", "Content-Range: " + contentRange);

                if (response.isSuccessful()) {
                    if (responseBody.isEmpty() || responseBody.equals("[]")) {
                        Log.e("Supabase", "Token update returned success but NO ROWS were updated! Check if user_id exists in users table.");
                    } else {
                        Log.d("Supabase", "Token updated successfully! Updated: " + responseBody);
                    }
                } else {
                    Log.e("Supabase", "Failed: " + response.code() + " " + response.message() + " Body: " + responseBody);
                }
            }
        });
    }
}
