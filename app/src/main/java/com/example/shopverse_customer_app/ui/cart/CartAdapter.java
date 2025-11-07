package com.example.shopverse_customer_app.ui.cart;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.CartItem;
import com.example.shopverse_customer_app.data.model.Product;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for displaying cart items in RecyclerView
 */
public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems = new ArrayList<>();
    private OnCartItemListener listener;

    public interface OnCartItemListener {
        void onIncreaseQuantity(CartItem cartItem);
        void onDecreaseQuantity(CartItem cartItem);
        void onDeleteItem(CartItem cartItem);
        void onItemSelectionChanged(CartItem cartItem);
    }

    public CartAdapter(OnCartItemListener listener) {
        this.listener = listener;
    }

    public void setCartItems(List<CartItem> cartItems) {
        this.cartItems = cartItems != null ? cartItems : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_cart_item, parent, false);
        return new CartViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem cartItem = cartItems.get(position);
        holder.bind(cartItem);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    class CartViewHolder extends RecyclerView.ViewHolder {

        private final CheckBox checkboxSelect;
        private final ImageView imageProduct;
        private final TextView textProductName;
        private final TextView textProductPrice;
        private final TextView textOriginalPrice;
        private final TextView textStockStatus;
        private final ImageButton buttonDecrease;
        private final TextView textQuantity;
        private final ImageButton buttonIncrease;
        private final ImageButton buttonDelete;

        public CartViewHolder(@NonNull View itemView) {
            super(itemView);

            checkboxSelect = itemView.findViewById(R.id.checkboxSelect);
            imageProduct = itemView.findViewById(R.id.imageProduct);
            textProductName = itemView.findViewById(R.id.textProductName);
            textProductPrice = itemView.findViewById(R.id.textProductPrice);
            textOriginalPrice = itemView.findViewById(R.id.textOriginalPrice);
            textStockStatus = itemView.findViewById(R.id.textStockStatus);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }

        public void bind(CartItem cartItem) {
            Product product = cartItem.getProduct();

            if (product == null) {
                textProductName.setText("Product not found");
                return;
            }

            // Set selection state
            checkboxSelect.setOnCheckedChangeListener(null);
            checkboxSelect.setChecked(cartItem.isSelected());
            checkboxSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (listener != null) {
                    listener.onItemSelectionChanged(cartItem);
                }
            });

            // Load product image
            String imageUrl = product.getFirstImageUrl();
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_launcher_background)
                        .error(R.drawable.ic_launcher_background)
                        .into(imageProduct);
            } else {
                imageProduct.setImageResource(R.drawable.ic_launcher_background);
            }

            // Set product name
            textProductName.setText(product.getProductName());

            // Set product price
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            String priceFormatted = formatPrice(product.getUnitPrice());
            textProductPrice.setText(priceFormatted);

            // Hide original price for now (you can add discount logic here)
            textOriginalPrice.setVisibility(View.GONE);

            // Set stock status
            if (product.isInStock() && product.isActive()) {
                textStockStatus.setText("Còn hàng");
                textStockStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_green_dark));
                textStockStatus.setVisibility(View.VISIBLE);
            } else {
                textStockStatus.setText("Hết hàng");
                textStockStatus.setTextColor(itemView.getContext().getColor(android.R.color.holo_red_dark));
                textStockStatus.setVisibility(View.VISIBLE);
            }

            // Set quantity
            textQuantity.setText(String.valueOf(cartItem.getQuantity()));

            // Decrease button
            buttonDecrease.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDecreaseQuantity(cartItem);
                }
            });

            // Increase button
            buttonIncrease.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIncreaseQuantity(cartItem);
                }
            });

            // Delete button
            buttonDelete.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteItem(cartItem);
                }
            });

            // Disable increase button if out of stock
            buttonIncrease.setEnabled(cartItem.getQuantity() < product.getStock());
        }

        /**
         * Format price to Vietnamese currency format
         */
        private String formatPrice(double price) {
            NumberFormat format = NumberFormat.getInstance(new Locale("vi", "VN"));
            return format.format(price) + "đ";
        }
    }
}
