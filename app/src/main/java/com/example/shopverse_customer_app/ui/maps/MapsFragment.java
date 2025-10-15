package com.example.shopverse_customer_app.ui.maps;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopverse_customer_app.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.example.shopverse_customer_app.data.Store;
import com.example.shopverse_customer_app.data.StoreRepository;
import com.example.shopverse_customer_app.location.DirectionsHelper;
import com.example.shopverse_customer_app.location.DistanceUtils;
import com.example.shopverse_customer_app.location.GeocodingHelper;
import com.example.shopverse_customer_app.location.LocationHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for displaying stores on Google Maps.
 * Features:
 * - Display store markers loaded from JSON
 * - Find nearest store to user location
 * - Provide directions to selected stores
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MapsFragment";

    // UI Components
    private GoogleMap mMap;
    private View searchBar;
    private AutoCompleteTextView editSearchLocation;
    private ImageView btnClearSearch;
    private TextView txtStoreTitle;
    private TextView txtStoreAddress;
    private TextView txtStoreHours;
    private Button btnNearestStore;
    private Button btnGetDirections;
    private RecyclerView recyclerStores;
    private BottomSheetBehavior<View> bottomSheetBehavior;

    // Data and Controllers
    private StoreRepository storeRepository;
    private LocationHelper locationHelper;
    private GeocodingHelper geocodingHelper;
    private DirectionsHelper directionsHelper;
    private PlacesClient placesClient;
    private PlacesAutocompleteAdapter autocompleteAdapter;
    private MapUiController mapUiController;
    private StoreListAdapter storeListAdapter;
    private List<Store> stores;
    private Store selectedStore;
    private Location userLocation;
    private Polyline currentPolyline;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize repository and helpers
        storeRepository = new StoreRepository(requireContext());
        locationHelper = new LocationHelper(requireContext());
        geocodingHelper = new GeocodingHelper(requireContext());

        // Initialize Google Maps API key (read from BuildConfig)
        String mapsApiKey = com.example.shopverse_customer_app.BuildConfig.GOOGLE_MAPS_API_KEY;
        directionsHelper = new DirectionsHelper(mapsApiKey);

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), mapsApiKey);
        }
        placesClient = Places.createClient(requireContext());

        // Get search bar reference and setup
        searchBar = view.findViewById(R.id.search_bar);
        editSearchLocation = view.findViewById(R.id.edit_search_location);
        btnClearSearch = view.findViewById(R.id.btn_clear_search);

        // Debug: Check if views are found
        if (searchBar == null) {
            Log.e(TAG, "ERROR: searchBar is NULL!");
        } else {
            Log.d(TAG, "searchBar found successfully");
        }

        if (editSearchLocation == null) {
            Log.e(TAG, "ERROR: editSearchLocation is NULL!");
        } else {
            Log.d(TAG, "editSearchLocation found successfully");
        }

        // Ensure search bar is visible and fully opaque
        if (searchBar != null) {
            searchBar.setVisibility(View.VISIBLE);
            searchBar.setAlpha(1f);
            searchBar.bringToFront();
            Log.d(TAG, "Search bar visibility set to VISIBLE, alpha set to 1.0");

            // Force search bar to be visible after a short delay
            // This ensures it's not being hidden by other initialization
            searchBar.postDelayed(() -> {
                if (searchBar != null) {
                    searchBar.setVisibility(View.VISIBLE);
                    searchBar.setAlpha(1f);
                    searchBar.requestLayout();
                    Log.d(TAG, "Search bar forced visible after delay");
                }
            }, 500);
        }

        // Setup autocomplete adapter
        autocompleteAdapter = new PlacesAutocompleteAdapter(requireContext(), placesClient);
        editSearchLocation.setAdapter(autocompleteAdapter);

        setupSearchBar();

        // Initialize bottom sheet
        View bottomSheet = view.findViewById(R.id.bottom_sheet);
        if (bottomSheet == null) {
            Log.e(TAG, "Bottom sheet is null!");
            return;
        }

        // Ensure bottom sheet is visible
        bottomSheet.setVisibility(View.VISIBLE);

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Calculate peek height in pixels (convert dp to px for better compatibility)
        int peekHeightDp = 160;
        float density = getResources().getDisplayMetrics().density;
        int peekHeightPx = (int) (peekHeightDp * density);

        Log.d(TAG, "Setting peek height: " + peekHeightPx + "px (density: " + density + ")");

        bottomSheetBehavior.setPeekHeight(peekHeightPx);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setFitToContents(false);
        bottomSheetBehavior.setHalfExpandedRatio(0.5f);
        bottomSheetBehavior.setSkipCollapsed(false);

        // Force state after layout
        bottomSheet.post(() -> {
            Log.d(TAG, "Forcing bottom sheet to collapsed state");
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        // Add bottom sheet callback to hide search bar when expanded
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                String stateName = getStateName(newState);
                Log.d(TAG, "Bottom sheet state changed to: " + stateName);

                // Hide search bar when expanded, show when collapsed
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    Log.d(TAG, "Hiding search bar (expanded state)");
                    searchBar.animate().alpha(0f).setDuration(200).withEndAction(() ->
                            searchBar.setVisibility(View.GONE));
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED ||
                           newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                    Log.d(TAG, "Showing search bar (collapsed/half-expanded state)");
                    searchBar.setVisibility(View.VISIBLE);
                    searchBar.animate().alpha(1f).setDuration(200);
                }
            }

            private String getStateName(int state) {
                switch (state) {
                    case BottomSheetBehavior.STATE_COLLAPSED: return "COLLAPSED";
                    case BottomSheetBehavior.STATE_EXPANDED: return "EXPANDED";
                    case BottomSheetBehavior.STATE_DRAGGING: return "DRAGGING";
                    case BottomSheetBehavior.STATE_SETTLING: return "SETTLING";
                    case BottomSheetBehavior.STATE_HIDDEN: return "HIDDEN";
                    case BottomSheetBehavior.STATE_HALF_EXPANDED: return "HALF_EXPANDED";
                    default: return "UNKNOWN(" + state + ")";
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Gradually fade search bar as bottom sheet slides up
                // slideOffset: 0 = collapsed, 1 = expanded
                if (slideOffset > 0.5f) {
                    // Start fading when sheet is more than 50% expanded
                    float alpha = 1f - ((slideOffset - 0.5f) * 2);
                    searchBar.setAlpha(Math.max(0, Math.min(1, alpha)));
                } else {
                    // Keep fully visible when less than 50% expanded
                    searchBar.setAlpha(1f);
                }
            }
        });

        // Bind UI components from bottom sheet
        txtStoreTitle = view.findViewById(R.id.txt_selected_store_title);
        txtStoreAddress = view.findViewById(R.id.txt_selected_store_address);
        txtStoreHours = view.findViewById(R.id.txt_selected_store_hours);
        btnNearestStore = view.findViewById(R.id.btn_bottom_nearest_store);
        btnGetDirections = view.findViewById(R.id.btn_bottom_get_directions);
        recyclerStores = view.findViewById(R.id.recycler_stores);

        // Set button click listeners
        btnNearestStore.setOnClickListener(v -> findNearestStore());
        btnGetDirections.setOnClickListener(v -> openDirections());

        // Setup RecyclerView with adapter
        storeListAdapter = new StoreListAdapter(this::onStoreItemClick);
        recyclerStores.setAdapter(storeListAdapter);

        // Initialize map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    /**
     * Handles click on store item in the list.
     */
    private void onStoreItemClick(Store store) {
        selectedStore = store;

        // Update bottom sheet header with selected store
        updateStoreInfo(store);

        // First collapse the bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // After a short delay, move camera to show store location
        // This ensures the bottom sheet is collapsed first
        searchBar.postDelayed(() -> {
            Marker marker = mapUiController.getMarkerForStoreId(store.getId());
            if (marker != null) {
                LatLng position = marker.getPosition();

                // Calculate padding to account for bottom sheet
                int bottomPadding = bottomSheetBehavior.getPeekHeight() + 50; // Extra 50dp for spacing

                // Animate camera with padding to keep marker visible above bottom sheet
                mMap.setPadding(0, 0, 0, bottomPadding);
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f), 300, null);

                // Reset padding after animation
                searchBar.postDelayed(() -> mMap.setPadding(0, 0, 0, bottomPadding), 350);

                marker.showInfoWindow();
            }
        }, 300); // Wait for bottom sheet collapse animation
    }

    /**
     * Updates the selected store info in the bottom sheet header.
     */
    private void updateStoreInfo(Store store) {
        txtStoreTitle.setText(store.getName());
        txtStoreAddress.setText(store.getAddress());
        updateStoreHours(store);
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
            Toast.makeText(requireContext(), "No stores found", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add store markers to map
        mapUiController.addStoreMarkers(stores);

        // Populate store list in RecyclerView
        storeListAdapter.setStores(stores, userLocation);

        // Set marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            Store store = mapUiController.getStoreForMarker(marker);
            if (store != null) {
                selectedStore = store;
                updateStoreInfo(store);

                // Collapse bottom sheet first
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                // After bottom sheet collapses, adjust camera to show marker above sheet
                searchBar.postDelayed(() -> {
                    LatLng position = marker.getPosition();

                    // Calculate padding to account for bottom sheet
                    int bottomPadding = bottomSheetBehavior.getPeekHeight() + 50;

                    // Set padding and animate to marker
                    mMap.setPadding(0, 0, 0, bottomPadding);
                    mMap.animateCamera(CameraUpdateFactory.newLatLng(position), 200, null);

                    marker.showInfoWindow();
                }, 300); // Wait for bottom sheet animation
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
            requestPermissions(
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LocationHelper.LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    /**
     * Enables the MyLocation layer on the map if permission is granted.
     */
    private void enableMyLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            Log.d(TAG, "MyLocation enabled");
        }
    }

    /**
     * Fetches the user's last known location asynchronously and centers map on it.
     */
    private void fetchUserLocation() {
        locationHelper.getLastKnownLocation(
                location -> {
                    userLocation = location;
                    Log.d(TAG, "User location: " + location.getLatitude() + ", " +
                            location.getLongitude());

                    // Update store list with distances
                    if (storeListAdapter != null && stores != null) {
                        storeListAdapter.setStores(stores, userLocation);
                    }

                    // Center map on user's current location
                    centerMapOnUserLocation();
                },
                () -> {
                    Log.w(TAG, "Failed to get user location");
                    // If location fails, show all stores
                    showAllStores();
                }
        );
    }

    /**
     * Centers the map camera on the user's current location with nearby stores.
     */
    private void centerMapOnUserLocation() {
        if (userLocation == null || mMap == null) {
            return;
        }

        LatLng userLatLng = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());

        // Build list including user location and nearby stores
        List<LatLng> positions = new ArrayList<>();
        positions.add(userLatLng);

        // Add stores within reasonable distance (e.g., 10km)
        for (Store store : stores) {
            double distance = DistanceUtils.haversineKm(
                    userLocation.getLatitude(),
                    userLocation.getLongitude(),
                    store.getLatitude(),
                    store.getLongitude()
            );

            if (distance <= 10.0) { // Only show stores within 10km
                positions.add(new LatLng(store.getLatitude(), store.getLongitude()));
            }
        }

        // If user is far from all stores, just center on user with appropriate zoom
        if (positions.size() == 1) {
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13f));
        } else {
            // Show user location and nearby stores
            mapUiController.moveCameraToBounds(positions, 150);
        }
    }

    /**
     * Shows all stores on the map when user location is unavailable.
     */
    private void showAllStores() {
        if (mMap == null || stores == null || stores.isEmpty()) {
            return;
        }

        List<LatLng> storePositions = new ArrayList<>();
        for (Store store : stores) {
            storePositions.add(new LatLng(store.getLatitude(), store.getLongitude()));
        }
        mapUiController.moveCameraToBounds(storePositions, 100);
    }

    /**
     * Finds and highlights the nearest store to the user's current location.
     */
    private void findNearestStore() {
        // Check if location is available
        if (userLocation == null) {
            Toast.makeText(requireContext(), R.string.location_permission_needed,
                    Toast.LENGTH_SHORT).show();

            // Try to fetch location again
            if (locationHelper.hasLocationPermission()) {
                fetchUserLocation();
            } else {
                setupLocation();
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
                updateStoreInfo(nearestStore);

                // Collapse bottom sheet first
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                String message = getString(R.string.nearest_store_found) + ": " +
                        DistanceUtils.formatDistance(minDistance);
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();

                // After bottom sheet collapses, highlight and show the store
                Store finalNearestStore = nearestStore;
                Marker finalNearestMarker = nearestMarker;
                searchBar.postDelayed(() -> {
                    // Calculate padding for bottom sheet
                    int bottomPadding = bottomSheetBehavior.getPeekHeight() + 50;
                    mMap.setPadding(0, 0, 0, bottomPadding);

                    // Highlight and animate to nearest store
                    mapUiController.highlightNearest(finalNearestMarker);
                    finalNearestMarker.showInfoWindow();
                }, 300);
            }
        }
    }

    /**
     * Shows directions on map from user location to selected store.
     * Long press opens external Google Maps navigation.
     */
    private void openDirections() {
        if (selectedStore == null) {
            Toast.makeText(requireContext(), R.string.no_store_selected,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        if (userLocation == null) {
            Toast.makeText(requireContext(), R.string.location_permission_needed,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        // Clear any existing route
        clearRoute();

        Toast.makeText(requireContext(), "Getting directions...", Toast.LENGTH_SHORT).show();

        LatLng origin = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        LatLng destination = new LatLng(selectedStore.getLatitude(), selectedStore.getLongitude());

        // Get and draw route
        directionsHelper.getDirections(origin, destination,
                new DirectionsHelper.DirectionsCallback() {
            @Override
            public void onSuccess(List<LatLng> routePoints, String distance, String duration) {
                drawRoute(routePoints);

                String message = distance + " • " + duration;
                Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

                // Fit camera to show entire route
                fitRouteToView(routePoints);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();

                // Fallback: open external Google Maps
                openExternalNavigation();
            }
        });
    }

    /**
     * Opens Google Maps app for turn-by-turn navigation.
     */
    private void openExternalNavigation() {
        if (selectedStore == null) return;

        double lat = selectedStore.getLatitude();
        double lng = selectedStore.getLongitude();

        Uri navUri = Uri.parse("google.navigation:q=" + lat + "," + lng);
        Intent mapIntent = new Intent(Intent.ACTION_VIEW, navUri);
        mapIntent.setPackage("com.google.android.apps.maps");

        try {
            startActivity(mapIntent);
            Log.d(TAG, "Opened Google Maps for navigation");
        } catch (ActivityNotFoundException e) {
            Uri webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" +
                    lat + "," + lng);
            startActivity(new Intent(Intent.ACTION_VIEW, webUri));
        }
    }

    /**
     * Draws a route polyline on the map.
     */
    private void drawRoute(List<LatLng> routePoints) {
        if (mMap == null || routePoints == null || routePoints.isEmpty()) {
            return;
        }

        // Clear previous route
        clearRoute();

        // Draw polyline
        PolylineOptions polylineOptions = new PolylineOptions()
                .addAll(routePoints)
                .width(12f)
                .color(0xFF4285F4)  // Google Blue
                .geodesic(true);

        currentPolyline = mMap.addPolyline(polylineOptions);

        Log.d(TAG, "Drew route with " + routePoints.size() + " points");
    }

    /**
     * Clears the current route from the map.
     */
    private void clearRoute() {
        if (currentPolyline != null) {
            currentPolyline.remove();
            currentPolyline = null;
        }
    }

    /**
     * Fits the camera view to show the entire route.
     */
    private void fitRouteToView(List<LatLng> routePoints) {
        if (routePoints == null || routePoints.isEmpty()) {
            return;
        }

        com.google.android.gms.maps.model.LatLngBounds.Builder builder =
                new com.google.android.gms.maps.model.LatLngBounds.Builder();

        for (LatLng point : routePoints) {
            builder.include(point);
        }

        com.google.android.gms.maps.model.LatLngBounds bounds = builder.build();

        int padding = bottomSheetBehavior.getPeekHeight() + 100;
        mMap.setPadding(0, 100, 0, padding);
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 150), 500, null);
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

    /**
     * Sets up the search bar with text watchers and search functionality.
     */
    private void setupSearchBar() {
        // Text watcher to show/hide clear button
        editSearchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Clear button click
        btnClearSearch.setOnClickListener(v -> {
            editSearchLocation.setText("");
            hideKeyboard();
        });

        // Handle search action (when user presses search/enter on keyboard)
        editSearchLocation.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                            event.getAction() == KeyEvent.ACTION_DOWN)) {
                performLocationSearch();
                return true;
            }
            return false;
        });
    }

    /**
     * Performs search for stores near the entered location.
     */
    private void performLocationSearch() {
        String searchQuery = editSearchLocation.getText().toString().trim();

        if (searchQuery.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a location", Toast.LENGTH_SHORT).show();
            return;
        }

        hideKeyboard();

        // Show progress
        Toast.makeText(requireContext(), "Searching for: " + searchQuery, Toast.LENGTH_SHORT).show();

        // Geocode the address
        geocodingHelper.getLocationFromAddress(searchQuery, new GeocodingHelper.GeocodingCallback() {
            @Override
            public void onSuccess(LatLng searchLocation, String formattedAddress) {
                Log.d(TAG, "Search location found: " + formattedAddress);

                // Find nearest store to the searched location
                Store nearestStore = findNearestStoreToLocation(
                        searchLocation.latitude,
                        searchLocation.longitude
                );

                if (nearestStore != null) {
                    // Show the nearest store
                    showNearestStoreToSearchLocation(nearestStore, searchLocation, formattedAddress);
                } else {
                    Toast.makeText(requireContext(),
                            "No stores found near " + formattedAddress,
                            Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Finds the nearest store to a given location.
     */
    private Store findNearestStoreToLocation(double latitude, double longitude) {
        if (stores == null || stores.isEmpty()) {
            return null;
        }

        Store nearestStore = null;
        double minDistance = Double.MAX_VALUE;

        for (Store store : stores) {
            double distance = DistanceUtils.haversineKm(
                    latitude, longitude,
                    store.getLatitude(), store.getLongitude()
            );

            if (distance < minDistance) {
                minDistance = distance;
                nearestStore = store;
            }
        }

        return nearestStore;
    }

    /**
     * Shows the nearest store to a searched location on the map.
     */
    private void showNearestStoreToSearchLocation(Store store, LatLng searchLocation, String searchAddress) {
        selectedStore = store;
        updateStoreInfo(store);

        // Collapse bottom sheet
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Calculate distance
        double distance = DistanceUtils.haversineKm(
                searchLocation.latitude, searchLocation.longitude,
                store.getLatitude(), store.getLongitude()
        );

        String message = store.getName() + " is " +
                DistanceUtils.formatDistance(distance) + " from " + searchAddress;
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();

        // After bottom sheet collapses, show the store
        searchBar.postDelayed(() -> {
            // Get marker and highlight it
            Marker marker = mapUiController.getMarkerForStoreId(store.getId());
            if (marker != null) {
                // Highlight as nearest (azure color)
                mapUiController.highlightNearest(marker);

                // Build bounds to show both search location and store
                com.google.android.gms.maps.model.LatLngBounds.Builder builder =
                        new com.google.android.gms.maps.model.LatLngBounds.Builder();
                builder.include(searchLocation);
                builder.include(new LatLng(store.getLatitude(), store.getLongitude()));

                com.google.android.gms.maps.model.LatLngBounds bounds = builder.build();

                // Calculate padding
                int padding = bottomSheetBehavior.getPeekHeight() + 100;
                mMap.setPadding(0, 100, 0, padding);

                // Animate to show both locations
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100), 500, null);

                marker.showInfoWindow();
            }
        }, 300);
    }

    /**
     * Hides the soft keyboard.
     */
    private void hideKeyboard() {
        if (getActivity() != null) {
            InputMethodManager imm = (InputMethodManager)
                    getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null && editSearchLocation != null) {
                imm.hideSoftInputFromWindow(editSearchLocation.getWindowToken(), 0);
            }
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
                Toast.makeText(requireContext(), "Location permission granted",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Permission denied - app still functional but location features disabled
                Toast.makeText(requireContext(),
                        "Location permission denied. Some features will be unavailable.",
                        Toast.LENGTH_LONG).show();
                Log.w(TAG, "Location permission denied");
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up geocoding helper
        if (geocodingHelper != null) {
            geocodingHelper.shutdown();
        }
    }
}
