package com.example.shopverse_customer_app.ui.maps;

import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.Store;
import com.example.shopverse_customer_app.location.DistanceUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Adapter for displaying a list of stores in a RecyclerView.
 */
public class StoreListAdapter extends RecyclerView.Adapter<StoreListAdapter.StoreViewHolder> {

    private List<Store> stores = new ArrayList<>();
    private Location userLocation;
    private OnStoreClickListener listener;

    public interface OnStoreClickListener {
        void onStoreClick(Store store);
    }

    public StoreListAdapter(OnStoreClickListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the store list and optionally sorts by distance.
     *
     * @param newStores    List of stores to display
     * @param userLocation User's current location for distance calculation (can be null)
     */
    public void setStores(List<Store> newStores, Location userLocation) {
        this.stores.clear();
        if (newStores != null) {
            this.stores.addAll(newStores);
        }
        this.userLocation = userLocation;

        // Sort by distance if user location is available
        if (userLocation != null && !stores.isEmpty()) {
            Collections.sort(stores, new Comparator<Store>() {
                @Override
                public int compare(Store s1, Store s2) {
                    double dist1 = DistanceUtils.haversineKm(
                            userLocation.getLatitude(),
                            userLocation.getLongitude(),
                            s1.getLatitude(),
                            s1.getLongitude()
                    );
                    double dist2 = DistanceUtils.haversineKm(
                            userLocation.getLatitude(),
                            userLocation.getLongitude(),
                            s2.getLatitude(),
                            s2.getLongitude()
                    );
                    return Double.compare(dist1, dist2);
                }
            });
        }

        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store_list, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = stores.get(position);
        holder.bind(store, userLocation, listener);
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtStoreName;
        private final TextView txtStoreAddress;
        private final TextView txtStoreDistance;

        public StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            txtStoreName = itemView.findViewById(R.id.txt_item_store_name);
            txtStoreAddress = itemView.findViewById(R.id.txt_item_store_address);
            txtStoreDistance = itemView.findViewById(R.id.txt_item_store_distance);
        }

        public void bind(Store store, Location userLocation, OnStoreClickListener listener) {
            txtStoreName.setText(store.getName());
            txtStoreAddress.setText(store.getAddress());

            // Show distance if user location is available
            if (userLocation != null) {
                double distance = DistanceUtils.haversineKm(
                        userLocation.getLatitude(),
                        userLocation.getLongitude(),
                        store.getLatitude(),
                        store.getLongitude()
                );
                txtStoreDistance.setText(DistanceUtils.formatDistance(distance));
                txtStoreDistance.setVisibility(View.VISIBLE);
            } else {
                txtStoreDistance.setVisibility(View.GONE);
            }

            // Handle click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onStoreClick(store);
                }
            });
        }
    }
}
