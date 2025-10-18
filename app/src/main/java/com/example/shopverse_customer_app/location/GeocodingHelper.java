package com.example.shopverse_customer_app.location;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper class for geocoding - converting addresses to coordinates.
 */

public class GeocodingHelper {
    private static final String TAG = "GeocodingHelper";
    private final Geocoder geocoder;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    public interface GeocodingCallback {
        void onSuccess(LatLng location, String formattedAddress);
        void onFailure(String errorMessage);
    }

    public GeocodingHelper(Context context) {
        geocoder = new Geocoder(context);
    }

    public void getLocationFromAddress(String address, GeocodingCallback callback) {
        if (address == null || address.trim().isEmpty()) {
            mainHandler.post(() -> callback.onFailure("Please enter a valid address"));
            return;
        }

        executor.execute(() -> {
            try {
                List<Address> results = geocoder.getFromLocationName(address, 1);
                if (results == null || results.isEmpty()) {
                    postFailure(callback, "Location not found");
                    return;
                }

                Address a = results.get(0);
                LatLng latLng = new LatLng(a.getLatitude(), a.getLongitude());
                String formatted = a.getAddressLine(0);

                Log.d(TAG, "Found: " + formatted);
                mainHandler.post(() -> callback.onSuccess(latLng, formatted));
            } catch (IOException e) {
                postFailure(callback, "Network error. Please try again.");
            }
        });
    }

    private void postFailure(GeocodingCallback cb, String msg) {
        mainHandler.post(() -> cb.onFailure(msg));
    }

    public void shutdown() {
        executor.shutdown();
    }
}
