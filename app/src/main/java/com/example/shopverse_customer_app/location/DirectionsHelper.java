package com.example.shopverse_customer_app.location;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Helper class for getting directions using Google Directions API.
 */
public class DirectionsHelper {
    private static final String TAG = "DirectionsHelper";
    private static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json";

    private final OkHttpClient client;
    private final Handler mainHandler;
    private final String apiKey;

    public interface DirectionsCallback {
        void onSuccess(List<LatLng> routePoints, String distance, String duration);
        void onFailure(String errorMessage);
    }

    public DirectionsHelper(String apiKey) {
        this.client = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.apiKey = apiKey;
    }

    /**
     * Gets directions from origin to destination.
     *
     * @param origin      Starting point
     * @param destination End point
     * @param callback    Callback with route points
     */
    public void getDirections(LatLng origin, LatLng destination, DirectionsCallback callback) {
        String url = buildDirectionsUrl(origin, destination);

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Directions API call failed", e);
                mainHandler.post(() ->
                        callback.onFailure("Failed to get directions. Check your connection."));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mainHandler.post(() ->
                            callback.onFailure("Directions service unavailable."));
                    return;
                }

                try {
                    String jsonData = response.body().string();
                    parseDirectionsResponse(jsonData, callback);
                } catch (Exception e) {
                    Log.e(TAG, "Error parsing directions response", e);
                    mainHandler.post(() ->
                            callback.onFailure("Error processing directions."));
                }
            }
        });
    }

    /**
     * Builds the Directions API URL.
     */
    private String buildDirectionsUrl(LatLng origin, LatLng destination) {
        return DIRECTIONS_API_URL +
                "?origin=" + origin.latitude + "," + origin.longitude +
                "&destination=" + destination.latitude + "," + destination.longitude +
                "&mode=driving" +
                "&key=" + apiKey;
    }

    /**
     * Parses the JSON response from Directions API.
     */
    private void parseDirectionsResponse(String jsonData, DirectionsCallback callback) {
        try {
            JsonObject jsonObject = JsonParser.parseString(jsonData).getAsJsonObject();
            String status = jsonObject.get("status").getAsString();

            if (!status.equals("OK")) {
                mainHandler.post(() ->
                        callback.onFailure("No route found between locations."));
                return;
            }

            JsonArray routes = jsonObject.getAsJsonArray("routes");
            if (routes.size() == 0) {
                mainHandler.post(() ->
                        callback.onFailure("No route available."));
                return;
            }

            JsonObject route = routes.get(0).getAsJsonObject();
            JsonObject overviewPolyline = route.getAsJsonObject("overview_polyline");
            String encodedPolyline = overviewPolyline.get("points").getAsString();

            // Get distance and duration from first leg
            JsonArray legs = route.getAsJsonArray("legs");
            JsonObject leg = legs.get(0).getAsJsonObject();
            String distance = leg.getAsJsonObject("distance").get("text").getAsString();
            String duration = leg.getAsJsonObject("duration").get("text").getAsString();

            // Decode polyline
            List<LatLng> routePoints = decodePolyline(encodedPolyline);

            Log.d(TAG, "Route found: " + distance + ", " + duration +
                    ", " + routePoints.size() + " points");

            mainHandler.post(() ->
                    callback.onSuccess(routePoints, distance, duration));

        } catch (Exception e) {
            Log.e(TAG, "Error parsing directions", e);
            mainHandler.post(() ->
                    callback.onFailure("Error parsing route data."));
        }
    }

    /**
     * Decodes an encoded polyline string into a list of LatLng points.
     * Based on Google's polyline encoding algorithm.
     */
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng point = new LatLng((lat / 1E5), (lng / 1E5));
            poly.add(point);
        }

        return poly;
    }
}
