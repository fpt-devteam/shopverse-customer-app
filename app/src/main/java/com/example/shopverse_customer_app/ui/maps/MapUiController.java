package com.example.shopverse_customer_app.ui.maps;

import android.widget.TextView;

import com.example.shopverse_customer_app.data.Store;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller class responsible for managing map UI elements including
 * markers, camera movements, and store information display.
 */
public class MapUiController {
    private final GoogleMap googleMap;
    private final TextView storeTitleView;
    private final TextView storeAddressView;

    // Map to keep track of markers and their associated stores
    private final Map<String, Marker> markerMap = new HashMap<>();
    private final Map<Marker, Store> storeMap = new HashMap<>();

    private Marker highlightedMarker = null;

    public MapUiController(GoogleMap googleMap, TextView storeTitleView, TextView storeAddressView) {
        this.googleMap = googleMap;
        this.storeTitleView = storeTitleView;
        this.storeAddressView = storeAddressView;
    }

    /**
     * Adds markers for all stores on the map.
     *
     * @param stores List of stores to display
     * @return Map of store IDs to their corresponding markers
     */
    public Map<String, Marker> addStoreMarkers(List<Store> stores) {
        markerMap.clear();
        storeMap.clear();

        for (Store store : stores) {
            LatLng position = new LatLng(store.getLatitude(), store.getLongitude());

            // Create marker with red color (default)
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(position)
                    .title(store.getName())
                    .snippet(store.getAddress())
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));

            Marker marker = googleMap.addMarker(markerOptions);
            if (marker != null) {
                markerMap.put(store.getId(), marker);
                storeMap.put(marker, store);
            }
        }

        return markerMap;
    }

    /**
     * Moves camera to show all store locations with padding.
     *
     * @param points List of LatLng points to include in bounds
     * @param paddingPx Padding in pixels around the bounds
     */
    public void moveCameraToBounds(List<LatLng> points, int paddingPx) {
        if (points == null || points.isEmpty()) {
            return;
        }

        // Build bounds to include all points
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : points) {
            builder.include(point);
        }

        LatLngBounds bounds = builder.build();

        // Animate camera to fit bounds with padding
        try {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx));
        } catch (IllegalStateException e) {
            // Map may not be ready yet, use moveCamera instead
            googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, paddingPx));
        }
    }

    /**
     * Highlights the nearest store marker with azure color and animates camera to it.
     *
     * @param marker The marker to highlight
     */
    public void highlightNearest(Marker marker) {
        if (marker == null) {
            return;
        }

        // Clear previous highlight
        clearHighlight();

        // Set azure color for nearest marker
        marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        highlightedMarker = marker;

        // Animate camera to the nearest store
        LatLng position = marker.getPosition();
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15f));

        // Show marker info window
        marker.showInfoWindow();

        // Update store info panel
        Store store = storeMap.get(marker);
        if (store != null) {
            showStoreInfo(store);
        }
    }

    /**
     * Clears the highlight from the previously highlighted marker.
     */
    public void clearHighlight() {
        if (highlightedMarker != null) {
            // Restore default red color
            highlightedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            highlightedMarker = null;
        }
    }

    /**
     * Updates the store information panel with the selected store's details.
     *
     * @param store The store to display information for
     */
    public void showStoreInfo(Store store) {
        if (store != null) {
            storeTitleView.setText(store.getName());
            storeAddressView.setText(store.getAddress());
        }
    }

    /**
     * Gets the store associated with a marker.
     *
     * @param marker The marker to look up
     * @return Store object or null if not found
     */
    public Store getStoreForMarker(Marker marker) {
        return storeMap.get(marker);
    }

    /**
     * Gets a marker by store ID.
     *
     * @param storeId The ID of the store
     * @return Marker object or null if not found
     */
    public Marker getMarkerForStoreId(String storeId) {
        return markerMap.get(storeId);
    }

    /**
     * Gets the currently highlighted marker.
     *
     * @return The highlighted marker or null if none is highlighted
     */
    public Marker getHighlightedMarker() {
        return highlightedMarker;
    }
}
