package com.example.shopverse_customer_app.ui.productlist;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying products in a grid RecyclerView.
 */
public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private List<Product> products = new ArrayList<>();
    private OnProductClickListener listener;
    private final NumberFormat currencyFormat;

    public interface OnProductClickListener {
        void onProductClick(Product product);
        void onFavoriteClick(Product product);
    }

    public ProductAdapter(OnProductClickListener listener) {
        this.listener = listener;
        this.currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
    }

    /**
     * Updates the product list
     */
    public void setProducts(List<Product> newProducts) {
        this.products.clear();
        if (newProducts != null) {
            this.products.addAll(newProducts);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_product_card, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        Product product = products.get(position);
        holder.bind(product, listener, currencyFormat);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView discountBadge;
        private final TextView installmentBadge;
        private final ImageView favoriteIcon;
        private final TextView productName;
        private final TextView brandName;
        private final TextView currentPrice;
        private final TextView originalPrice;
        private final TextView studentPrice;
        private final TextView productDescription;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            discountBadge = itemView.findViewById(R.id.discountBadge);
            installmentBadge = itemView.findViewById(R.id.installmentBadge);
            favoriteIcon = itemView.findViewById(R.id.favoriteIcon);
            productName = itemView.findViewById(R.id.productName);
            brandName = itemView.findViewById(R.id.brandName);
            currentPrice = itemView.findViewById(R.id.currentPrice);
            originalPrice = itemView.findViewById(R.id.originalPrice);
            studentPrice = itemView.findViewById(R.id.studentPrice);
            productDescription = itemView.findViewById(R.id.productDescription);
        }

        public void bind(Product product, OnProductClickListener listener, NumberFormat currencyFormat) {
            // Set product name
            productName.setText(product.getProductName());

            // Set brand name
            if (product.getBrand() != null && product.getBrand().getBrandName() != null) {
                brandName.setText(product.getBrand().getBrandName());
                brandName.setVisibility(View.VISIBLE);
            } else {
                brandName.setVisibility(View.GONE);
            }

            // Set current price
            String priceText = formatPrice(product.getUnitPrice());
            currentPrice.setText(priceText);

            // Load product image
            String imageUrl = product.getFirstImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_home_black_24dp)
                        .error(R.drawable.ic_home_black_24dp)
                        .into(productImage);
            } else {
                productImage.setImageResource(R.drawable.ic_home_black_24dp);
            }

            // Set description if available
            if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                productDescription.setText(product.getDescription());
                productDescription.setVisibility(View.VISIBLE);
            } else {
                productDescription.setVisibility(View.GONE);
            }

            // Original price with strikethrough
            // For now, hide it unless we have discount logic
            originalPrice.setVisibility(View.GONE);
            originalPrice.setPaintFlags(originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            // Hide optional badges for now
            discountBadge.setVisibility(View.GONE);
            installmentBadge.setVisibility(View.GONE);
            studentPrice.setVisibility(View.GONE);

            // Handle product click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onProductClick(product);
                }
            });

            // Handle favorite click
            favoriteIcon.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onFavoriteClick(product);
                }
            });
        }

        private String formatPrice(double price) {
            // Format: 21.490.000₫
            long priceInt = (long) price;
            String formatted = String.format(Locale.US, "%,d", priceInt).replace(",", ".");
            return formatted + "₫";
        }
    }
}
