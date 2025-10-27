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

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
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
    private static final long ANIM_DURATION_MS = 200L;
    private static final long SEARCHBAR_FORCE_SHOW_DELAY_MS = 500L;
    private static final int PEEK_HEIGHT_DP = 160;
    private static final float HALF_EXPANDED_RATIO = 0.5f;
    private static final float FADE_START_OFFSET = 0.5f;

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

    /* ---------- Step 1: Services ---------- */
    private void initServices() {
        Context ctx = requireContext();

        storeRepository  = new StoreRepository(ctx);
        locationHelper   = new LocationHelper(ctx);
        geocodingHelper  = new GeocodingHelper(ctx);

        String mapsApiKey = com.example.shopverse_customer_app.BuildConfig.GOOGLE_MAPS_API_KEY;
        directionsHelper  = new DirectionsHelper(mapsApiKey);

        if (!Places.isInitialized()) {
            Places.initialize(ctx, mapsApiKey);
        }
        placesClient = Places.createClient(ctx);
    }

    /* ---------- Step 2: Bind Views ---------- */
    private void bindViews(@NonNull View root) {
        searchBar          = root.findViewById(R.id.search_bar);
        editSearchLocation = root.findViewById(R.id.edit_search_location);
        btnClearSearch     = root.findViewById(R.id.btn_clear_search);

        View bottomSheet   = root.findViewById(R.id.bottom_sheet);
        if (bottomSheet == null) {
            Log.e(TAG, "Bottom sheet is null!");
            return;
        }
        bottomSheet.setVisibility(View.VISIBLE);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        txtStoreTitle      = root.findViewById(R.id.txt_selected_store_title);
        txtStoreAddress    = root.findViewById(R.id.txt_selected_store_address);
        txtStoreHours      = root.findViewById(R.id.txt_selected_store_hours);
        btnNearestStore    = root.findViewById(R.id.btn_bottom_nearest_store);
        btnGetDirections   = root.findViewById(R.id.btn_bottom_get_directions);
        recyclerStores     = root.findViewById(R.id.recycler_stores);
    }

    /* ---------- Step 3: Search Bar ---------- */
    private void configureSearchBar() {
        if (searchBar == null || editSearchLocation == null) {
            Log.e(TAG, "Search views are null");
            return;
        }

        // Ensure visible & on top
        searchBar.setVisibility(View.VISIBLE);
        searchBar.setAlpha(1f);
        searchBar.bringToFront();

        // Force show after short delay to avoid being overlapped
        searchBar.postDelayed(() -> {
            searchBar.setVisibility(View.VISIBLE);
            searchBar.setAlpha(1f);
            searchBar.requestLayout();
            Log.d(TAG, "Search bar forced visible after delay");
        }, SEARCHBAR_FORCE_SHOW_DELAY_MS);

        // Autocomplete (Places)
        autocompleteAdapter = new PlacesAutocompleteAdapter(requireContext(), placesClient);
        editSearchLocation.setAdapter(autocompleteAdapter);

        // Any extra behaviors you had in setupSearchBar() — keep them here
        setupSearchBarBehaviors();
    }

    private void setupSearchBarBehaviors() {
        setupClearButtonBehavior();
        setupSearchActionBehavior();
    }
    private abstract static class SimpleTextWatcher implements TextWatcher {
        @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
        @Override public void afterTextChanged(Editable s) {}
    }
    /* ----- 1️⃣ Clear Button Visibility ----- */
    private void setupClearButtonBehavior() {
        // Hiện/Giấu nút clear khi người dùng nhập text
        editSearchLocation.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });

        // Khi nhấn nút clear → xoá text + ẩn bàn phím
        btnClearSearch.setOnClickListener(v -> {
            editSearchLocation.setText("");
            hideKeyboard();
        });
    }

    /* ----- 2️⃣ Search Action (nhấn enter hoặc search) ----- */
    private void setupSearchActionBehavior() {
        editSearchLocation.setOnEditorActionListener((v, actionId, event) -> {
            boolean isSearchAction = actionId == EditorInfo.IME_ACTION_SEARCH;
            boolean isEnterPressed = event != null &&
                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
                    event.getAction() == KeyEvent.ACTION_DOWN;

            if (isSearchAction || isEnterPressed) {
                performLocationSearch();
                return true;
            }
            return false;
        });
    }


    /* ---------- Step 4: Bottom Sheet ---------- */
    private void configureBottomSheet(@NonNull View root) {
        View bottomSheet   = root.findViewById(R.id.bottom_sheet);
        if (bottomSheet == null) {
            Log.e(TAG, "Bottom sheet is null!");
            return;
        }
        if (bottomSheetBehavior == null || bottomSheet == null) return;

        int peekHeightPx = (int) (PEEK_HEIGHT_DP * getResources().getDisplayMetrics().density);
        bottomSheetBehavior.setPeekHeight(peekHeightPx);
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setFitToContents(false);
        bottomSheetBehavior.setHalfExpandedRatio(HALF_EXPANDED_RATIO);
        bottomSheetBehavior.setSkipCollapsed(false);

        // Start collapsed after layout — dùng chính bottomSheet.post(...)
        bottomSheet.post(() -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bs, int newState) {
                if (searchBar == null) return;
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    searchBar.animate().alpha(0f)
                            .setDuration(ANIM_DURATION_MS)
                            .withEndAction(() -> searchBar.setVisibility(View.GONE));
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED
                        || newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                    searchBar.setVisibility(View.VISIBLE);
                    searchBar.animate().alpha(1f).setDuration(ANIM_DURATION_MS);
                }
            }

            @Override
            public void onSlide(@NonNull View bs, float slideOffset) {
                if (searchBar == null) return;
                if (slideOffset > FADE_START_OFFSET) {
                    float alpha = 1f - ((slideOffset - FADE_START_OFFSET) * 2f);
                    searchBar.setAlpha(Math.max(0f, Math.min(1f, alpha)));
                } else {
                    searchBar.setAlpha(1f);
                }
            }
        });
    }


    // Helper to get underlying bottom sheet view from behavior (API shim)
    private static @Nullable View getBottomSheet(@NonNull BottomSheetBehavior<View> behavior) {
        try {
            Field f = BottomSheetBehavior.class.getDeclaredField("viewRef");
            f.setAccessible(true);
            WeakReference<View> ref = (WeakReference<View>) f.get(behavior);
            return ref != null ? ref.get() : null;
        } catch (Exception ignore) { return null; }
    }

    /* ---------- Step 5: List & Actions ---------- */
    private void configureStoreList() {
        storeListAdapter = new StoreListAdapter(this::onStoreItemClick);
        recyclerStores.setAdapter(storeListAdapter);

        btnNearestStore.setOnClickListener(v -> findNearestStore());
        btnGetDirections.setOnClickListener(v -> openDirections());
    }

    /* ---------- Step 6: Map ---------- */
    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        } else {
            Log.e(TAG, "Map fragment is null");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1) Init services (repo, location, geocoding, directions, places)
        initServices();

        // 2) Bind views (search bar, inputs, bottomsheet, recycler, buttons...)
        bindViews(view);

        // 3) Configure search bar (visibility + autocomplete + behaviors)
        configureSearchBar();

        // 4) Configure bottom sheet (peek height, state, callbacks)
        configureBottomSheet(view);

        // 5) Configure list + actions (adapter + click listeners)
        configureStoreList();

        // 6) Initialize Google Map
        initMap();
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
//    private void setupSearchBar() {
//        // Text watcher to show/hide clear button
//        editSearchLocation.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {}
//        });
//
//        // Clear button click
//        btnClearSearch.setOnClickListener(v -> {
//            editSearchLocation.setText("");
//            hideKeyboard();
//        });
//
//        // Handle search action (when user presses search/enter on keyboard)
//        editSearchLocation.setOnEditorActionListener((v, actionId, event) -> {
//            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
//                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER &&
//                            event.getAction() == KeyEvent.ACTION_DOWN)) {
//                performLocationSearch();
//                return true;
//            }
//            return false;
//        });
//    }

    /**
     * Performs search for stores near the entered location.
     */
//    private void performLocationSearch() {
//        String searchQuery = editSearchLocation.getText().toString().trim();
//
//        if (searchQuery.isEmpty()) {
//            Toast.makeText(requireContext(), "Please enter a location", Toast.LENGTH_SHORT).show();
//            return;
//        }
//
//        hideKeyboard();
//
//        // Show progress
//        Toast.makeText(requireContext(), "Searching for: " + searchQuery, Toast.LENGTH_SHORT).show();
//
//        // Geocode the address
//        geocodingHelper.getLocationFromAddress(searchQuery, new GeocodingHelper.GeocodingCallback() {
//            @Override
//            public void onSuccess(LatLng searchLocation, String formattedAddress) {
//                Log.d(TAG, "Search location found: " + formattedAddress);
//
//                // Find nearest store to the searched location
//                Store nearestStore = findNearestStoreToLocation(
//                        searchLocation.latitude,
//                        searchLocation.longitude
//                );
//
//                if (nearestStore != null) {
//                    // Show the nearest store
//                    showNearestStoreToSearchLocation(nearestStore, searchLocation, formattedAddress);
//                } else {
//                    Toast.makeText(requireContext(),
//                            "No stores found near " + formattedAddress,
//                            Toast.LENGTH_LONG).show();
//                }
//            }
//
//            @Override
//            public void onFailure(String errorMessage) {
//                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show();
//            }
//        });
//    }

    private void performLocationSearch() {
        performLocationSearch(editSearchLocation != null ? editSearchLocation.getText().toString() : null);
    }

    private void performLocationSearch(@Nullable String rawQuery) {
        final String query = (rawQuery == null) ? "" : rawQuery.trim();

        if (query.isEmpty()) {
            showToast("Please enter a location");
            return;
        }

        hideKeyboard();
        showToast("Searching for: " + query);

        if (geocodingHelper == null) {
            Log.w(TAG, "GeocodingHelper is null");
            showToast("Search service is not available. Please try again.");
            return;
        }

        geocodingHelper.getLocationFromAddress(query, new GeocodingHelper.GeocodingCallback() {
            @Override
            public void onSuccess(@NonNull LatLng searchLocation, @NonNull String formattedAddress) {
                Log.d(TAG, "Search location found: " + formattedAddress + " (" +
                        searchLocation.latitude + ", " + searchLocation.longitude + ")");

                Store nearestStore = findNearestStoreToLocation(
                        searchLocation.latitude,
                        searchLocation.longitude
                );

                if (nearestStore != null) {
                    showNearestStoreToSearchLocation(nearestStore, searchLocation, formattedAddress);
                    return;
                }

                showToast("No stores found near " + formattedAddress);
            }

            @Override
            public void onFailure(@NonNull String errorMessage) {
                Log.w(TAG, "Geocoding failed: " + errorMessage);
                showToast(errorMessage);
            }
        });
    }

    private void showToast(@NonNull String msg) {
        Context ctx = getContext();
        if (ctx != null) {
            Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
        }
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
