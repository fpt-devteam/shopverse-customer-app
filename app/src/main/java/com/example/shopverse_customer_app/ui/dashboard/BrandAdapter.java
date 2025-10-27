package com.example.shopverse_customer_app.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.Brand;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying brands in a flexbox/chip-style RecyclerView.
 */
public class BrandAdapter extends RecyclerView.Adapter<BrandAdapter.BrandViewHolder> {

    private List<Brand> brands = new ArrayList<>();
    private OnBrandClickListener listener;

    public interface OnBrandClickListener {
        void onBrandClick(Brand brand);
    }

    public BrandAdapter(OnBrandClickListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the brand list
     */
    public void setBrands(List<Brand> newBrands) {
        this.brands.clear();
        if (newBrands != null) {
            this.brands.addAll(newBrands);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BrandViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_brand_chip, parent, false);
        return new BrandViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BrandViewHolder holder, int position) {
        Brand brand = brands.get(position);
        holder.bind(brand, listener);
    }

    @Override
    public int getItemCount() {
        return brands.size();
    }

    static class BrandViewHolder extends RecyclerView.ViewHolder {
        private final TextView brandName;

        public BrandViewHolder(@NonNull View itemView) {
            super(itemView);
            brandName = itemView.findViewById(R.id.brandName);
        }

        public void bind(Brand brand, OnBrandClickListener listener) {
            brandName.setText(brand.getBrandName());

            // Handle click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onBrandClick(brand);
                }
            });
        }
    }
}
