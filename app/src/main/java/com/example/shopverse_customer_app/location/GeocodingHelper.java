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
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface GeocodingCallback {
        void onSuccess(LatLng location, String formattedAddress);
        void onFailure(String errorMessage);
    }

    public GeocodingHelper(Context context) {
        this.geocoder = new Geocoder(context);
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Converts an address string to geographic coordinates.
     * Runs on background thread and returns result on main thread.
     *
     * @param addressString The address to search for (e.g., "District 1, Ho Chi Minh")
     * @param callback      Callback with results
     */
    public void getLocationFromAddress(String addressString, GeocodingCallback callback) {
        if (addressString == null || addressString.trim().isEmpty()) {
            mainHandler.post(() -> callback.onFailure("Please enter a location"));
            return;
        }

        executorService.execute(() -> {
            try {
                // Get up to 5 results
                List<Address> addresses = geocoder.getFromLocationName(addressString, 5);

                if (addresses != null && !addresses.isEmpty()) {
                    Address bestMatch = addresses.get(0);
                    LatLng location = new LatLng(bestMatch.getLatitude(), bestMatch.getLongitude());
                    String formattedAddress = getFormattedAddress(bestMatch);

                    Log.d(TAG, "Found location: " + formattedAddress + " at " +
                            location.latitude + ", " + location.longitude);

                    // Return result on main thread
                    mainHandler.post(() -> callback.onSuccess(location, formattedAddress));
                } else {
                    Log.w(TAG, "No results found for: " + addressString);
                    mainHandler.post(() ->
                            callback.onFailure("Location not found. Try a different address."));
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding error", e);
                mainHandler.post(() ->
                        callback.onFailure("Network error. Please check your connection."));
            } catch (Exception e) {
                Log.e(TAG, "Unexpected error", e);
                mainHandler.post(() ->
                        callback.onFailure("An error occurred. Please try again."));
            }
        });
    }

    /**
     * Formats an Address object into a readable string.
     */
    private String getFormattedAddress(Address address) {
        StringBuilder sb = new StringBuilder();

        // Add street address
        if (address.getMaxAddressLineIndex() >= 0) {
            sb.append(address.getAddressLine(0));
        } else {
            // Build from components
            if (address.getSubThoroughfare() != null) {
                sb.append(address.getSubThoroughfare()).append(" ");
            }
            if (address.getThoroughfare() != null) {
                sb.append(address.getThoroughfare());
            }
            if (address.getLocality() != null) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(address.getLocality());
            }
        }

        return sb.toString();
    }

    /**
     * Cleans up resources.
     */
    public void shutdown() {
        executorService.shutdown();
    }
}
