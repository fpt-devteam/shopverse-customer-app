package com.example.shopverse_customer_app.ui.maps;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.Store;
import com.example.shopverse_customer_app.data.StoreRepository;
import com.example.shopverse_customer_app.location.DistanceUtils;
import com.example.shopverse_customer_app.location.LocationHelper;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity for displaying stores on Google Maps.
 * Features:
 * - Display store markers loaded from JSON
 * - Find nearest store to user location
 * - Provide directions to selected stores
 */
public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {
    private static final String TAG = "MapsActivity";

    // UI Components
    private GoogleMap mMap;
    private TextView txtStoreTitle;
    private TextView txtStoreAddress;
    private TextView txtStoreHours;
    private Button btnNearestStore;
    private Button btnGetDirections;

    // Data and Controllers
    private StoreRepository storeRepository;
    private LocationHelper locationHelper;
    private MapUiController mapUiController;
    private List<Store> stores;
    private Store selectedStore;
    private Location userLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Initialize repository and helper
        storeRepository = new StoreRepository(this);
        locationHelper = new LocationHelper(this);

        // Bind UI components
        txtStoreTitle = findViewById(R.id.txt_store_title);
        txtStoreAddress = findViewById(R.id.txt_store_address);
        txtStoreHours = findViewById(R.id.txt_store_hours);
        btnNearestStore = findViewById(R.id.btn_nearest_store);
        btnGetDirections = findViewById(R.id.btn_get_directions);

        // Set button click listeners
        btnNearestStore.setOnClickListener(v -> findNearestStore());
        btnGetDirections.setOnClickListener(v -> openDirections());

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Map is ready");

        // Initialize map UI controller
        mapUiController = new MapUiController(mMap, txtStoreTitle, txtStoreAddress);

        // Load stores from repository
        stores = storeRepository.loadStores();
        if (stores.isEmpty()) {
            Toast.makeText(this, "No stores found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add store markers to map
        mapUiController.addStoreMarkers(stores);

        // Build list of all store positions for camera bounds
        List<LatLng> storePositions = new ArrayList<>();
        for (Store store : stores) {
            storePositions.add(new LatLng(store.getLatitude(), store.getLongitude()));
        }

        // Move camera to show all stores with padding
        mapUiController.moveCameraToBounds(storePositions, 100);

        // Set marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            Store store = mapUiController.getStoreForMarker(marker);
            if (store != null) {
                selectedStore = store;
                mapUiController.showStoreInfo(store);
                updateStoreHours(store);
                marker.showInfoWindow();
            }
            return true; // Consume the event
        });

        // Request location permission and enable MyLocation
        setupLocation();
    }

    /**
     * Sets up location services - requests permission and enables MyLocation layer.
     */
    private void setupLocation() {
        if (locationHelper.hasLocationPermission()) {
            enableMyLocation();
            fetchUserLocation();
        } else {
            // Request location permission
            locationHelper.requestLocationPermission(this);
        }
    }

    /**
     * Enables the MyLocation layer on the map if permission is granted.
     */
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            Log.d(TAG, "MyLocation enabled");
        }
    }

    /**
     * Fetches the user's last known location asynchronously.
     */
    private void fetchUserLocation() {
        locationHelper.getLastKnownLocation(
                location -> {
                    userLocation = location;
                    Log.d(TAG, "User location: " + location.getLatitude() + ", " + location.getLongitude());
                },
                () -> Log.w(TAG, "Failed to get user location")
        );
    }

    /**
     * Finds and highlights the nearest store to the user's current location.
     */
    private void findNearestStore() {
        // Check if location is available
        if (userLocation == null) {
            Toast.makeText(this, R.string.location_permission_needed, Toast.LENGTH_SHORT).show();

            // Try to fetch location again
            if (locationHelper.hasLocationPermission()) {
                fetchUserLocation();
            } else {
                locationHelper.requestLocationPermission(this);
            }
            return;
        }

        // Find nearest store using Haversine distance
        Store nearestStore = null;
        double minDistance = Double.MAX_VALUE;
        Marker nearestMarker = null;

        for (Store store : stores) {
            double distance = DistanceUtils.haversineKm(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    store.getLatitude(),
                    store.getLongitude()
            );

            Log.d(TAG, "Distance to " + store.getName() + ": " +
                    DistanceUtils.formatDistance(distance));

            if (distance < minDistance) {
                minDistance = distance;
                nearestStore = store;
            }
        }

        // Highlight the nearest store
        if (nearestStore != null) {
            nearestMarker = mapUiController.getMarkerForStoreId(nearestStore.getId());
            if (nearestMarker != null) {
                selectedStore = nearestStore;
                mapUiController.highlightNearest(nearestMarker);
                updateStoreHours(nearestStore);

                String message = getString(R.string.nearest_store_found) + ": " +
                        DistanceUtils.formatDistance(minDistance);
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Opens Google Maps app or browser to show directions to the selected store.
     */
    private void openDirections() {
        if (selectedStore == null) {
            Toast.makeText(this, R.string.no_store_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        double lat = selectedStore.getLatitude();
        double lng = selectedStore.getLongitude();

        // Try to open Google Maps app with navigation intent
        Uri navUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, navUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapIntent);
            Log.d(TAG, "Opened Google Maps for directions");
        } catch (ActivityNotFoundException e) {
            // Fallback to browser if Google Maps app is not installed
            Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" +
                    lat + "," + lng);
            Intent webIntent = new Intent(Intent.ACTION_VIEW, webUri);
            startActivity(webIntent);
            Log.d(TAG, "Opened browser for directions");
        }
    }

    /**
     * Updates the store hours display in the info panel.
     */
    private void updateStoreHours(Store store) {
        if (store.getHours() != null && !store.getHours().isEmpty()) {
            txtStoreHours.setText(getString(R.string.stores_open_hours, store.getHours()));
            txtStoreHours.setVisibility(View.VISIBLE);
        } else {
            txtStoreHours.setVisibility(View.GONE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LocationHelper.LOCATION_PERMISSION_REQUEST_CODE) {
            if (LocationHelper.isPermissionGranted(grantResults)) {
                // Permission granted - enable location features
                enableMyLocation();
                fetchUserLocation();
                Toast.makeText(this, "Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied - app still functional but location features disabled
                Toast.makeText(this,
                        "Location permission denied. Some features will be unavailable.",
                        Toast.LENGTH_LONG).show();
                Log.w(TAG, "Location permission denied");
            }
        }
    }
}
