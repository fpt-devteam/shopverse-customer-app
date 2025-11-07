package com.example.shopverse_customer_app.ui.checkout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.CartItem;

import java.util.ArrayList;
import java.util.List;

public class CheckoutProductAdapter extends RecyclerView.Adapter<CheckoutProductAdapter.ViewHolder> {

    private List<CartItem> cartItems = new ArrayList<>();

    public void setCartItems(List<CartItem> items) {
        this.cartItems = items != null ? items : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_checkout_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView productImage;
        private final TextView productName;
        private final TextView productQuantityPrice;
        private final TextView productTotal;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.productImage);
            productName = itemView.findViewById(R.id.productName);
            productQuantityPrice = itemView.findViewById(R.id.productQuantityPrice);
            productTotal = itemView.findViewById(R.id.productTotal);
        }

        public void bind(CartItem cartItem) {
            if (cartItem.getProduct() != null) {
                // Product name
                productName.setText(cartItem.getProduct().getProductName());

                // Quantity × Unit Price
                String quantityPrice = cartItem.getQuantity() + " × " +
                        formatPrice(cartItem.getProduct().getUnitPrice());
                productQuantityPrice.setText(quantityPrice);

                // Total for this item
                double total = cartItem.getProduct().getUnitPrice() * cartItem.getQuantity();
                productTotal.setText(formatPrice(total));

                // Load product image
                String imageUrl = cartItem.getProduct().getFirstImageUrl();
                if (imageUrl != null && !imageUrl.isEmpty()) {
                    Glide.with(itemView.getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_home_black_24dp)
                            .error(R.drawable.ic_home_black_24dp)
                            .into(productImage);
                } else {
                    productImage.setImageResource(R.drawable.ic_home_black_24dp);
                }
            }
        }

        private String formatPrice(double price) {
            long priceInt = (long) price;
            String formatted = String.format("%,d", priceInt).replace(",", ".");
            return formatted + "₫";
        }
    }
}
