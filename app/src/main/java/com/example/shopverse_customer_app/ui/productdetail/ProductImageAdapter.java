package com.example.shopverse_customer_app.ui.productdetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopverse_customer_app.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying product images in ViewPager2
 */
public class ProductImageAdapter extends RecyclerView.Adapter<ProductImageAdapter.ImageViewHolder> {

    private List<String> imageUrls = new ArrayList<>();

    public void setImageUrls(List<String> urls) {
        this.imageUrls.clear();
        if (urls != null && !urls.isEmpty()) {
            this.imageUrls.addAll(urls);
        } else {
            // Add placeholder if no images
            this.imageUrls.add(null);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_image, parent, false);
        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);
        holder.bind(imageUrl);
    }

    @Override
    public int getItemCount() {
        return imageUrls.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImageView;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            productImageView = itemView.findViewById(R.id.productImageView);
        }

        public void bind(String imageUrl) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_home_black_24dp)
                        .error(R.drawable.ic_home_black_24dp)
                        .into(productImageView);
            } else {
                productImageView.setImageResource(R.drawable.ic_home_black_24dp);
            }
        }
    }
}
