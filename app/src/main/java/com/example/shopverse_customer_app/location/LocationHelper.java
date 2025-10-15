package com.example.shopverse_customer_app.location;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Helper class to manage location permissions and retrieve user location
 * using FusedLocationProviderClient.
 */
public class LocationHelper {
    private static final String TAG = "LocationHelper";
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    public LocationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    /**
     * Checks if location permission is granted.
     *
     * @return true if ACCESS_FINE_LOCATION permission is granted
     */
    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Requests location permission from the user.
     * Should be called from an Activity.
     *
     * @param activity The activity requesting permission
     */
    public void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    /**
     * Gets the last known location of the device.
     * Requires location permission to be granted.
     *
     * @param onSuccess Callback invoked with the location when successful
     * @param onFailure Callback invoked when location retrieval fails
     */
    public void getLastKnownLocation(final OnSuccessListener<Location> onSuccess,
                                     final Runnable onFailure) {
        // Check if we have permission
        if (!hasLocationPermission()) {
            Log.w(TAG, "Location permission not granted");
            if (onFailure != null) {
                onFailure.run();
            }
            return;
        }

        // Get last known location
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Log.d(TAG, "Location retrieved: " + location.getLatitude() +
                                    ", " + location.getLongitude());
                            if (onSuccess != null) {
                                onSuccess.onSuccess(location);
                            }
                        } else {
                            Log.w(TAG, "Last known location is null");
                            if (onFailure != null) {
                                onFailure.run();
                            }
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get location", e);
                        if (onFailure != null) {
                            onFailure.run();
                        }
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when getting location", e);
            if (onFailure != null) {
                onFailure.run();
            }
        }
    }

    /**
     * Checks if the result of a permission request was granted.
     *
     * @param grantResults The grant results from onRequestPermissionsResult
     * @return true if permission was granted
     */
    public static boolean isPermissionGranted(int[] grantResults) {
        return grantResults.length > 0 &&
               grantResults[0] == PackageManager.PERMISSION_GRANTED;
    }
}
