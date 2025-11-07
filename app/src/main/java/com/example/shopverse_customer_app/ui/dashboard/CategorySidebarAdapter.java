package com.example.shopverse_customer_app.ui.dashboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.Category;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying categories in a sidebar RecyclerView.
 */
public class CategorySidebarAdapter extends RecyclerView.Adapter<CategorySidebarAdapter.CategoryViewHolder> {

    private List<Category> categories = new ArrayList<>();
    private OnCategoryClickListener listener;
    private int selectedPosition = 0;

    public interface OnCategoryClickListener {
        void onCategoryClick(Category category, int position);
    }

    public CategorySidebarAdapter(OnCategoryClickListener listener) {
        this.listener = listener;
    }

    /**
     * Updates the category list
     */
    public void setCategories(List<Category> newCategories) {
        this.categories.clear();
        if (newCategories != null) {
            this.categories.addAll(newCategories);
        }
        notifyDataSetChanged();
    }

    /**
     * Set selected position
     */
    public void setSelectedPosition(int position) {
        int previousPosition = selectedPosition;
        selectedPosition = position;
        notifyItemChanged(previousPosition);
        notifyItemChanged(selectedPosition);
    }

    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_sidebar, parent, false);
        return new CategoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        Category category = categories.get(position);
        boolean isSelected = position == selectedPosition;
        holder.bind(category, isSelected, listener, position);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class CategoryViewHolder extends RecyclerView.ViewHolder {
        private final ImageView categoryIcon;
        private final TextView categoryName;

        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            categoryIcon = itemView.findViewById(R.id.categoryIcon);
            categoryName = itemView.findViewById(R.id.categoryName);
        }

        public void bind(Category category, boolean isSelected, OnCategoryClickListener listener, int position) {
            categoryName.setText(category.getCategoryName());

            // Set category icon based on category name
            int iconResId = getCategoryIcon(category.getCategoryName());
            categoryIcon.setImageResource(iconResId);

            // Set selected state
            itemView.setSelected(isSelected);

            // Handle click
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category, position);
                }
            });
        }

        private int getCategoryIcon(String categoryName) {
            if (categoryName == null) {
                return R.drawable.ic_dashboard_black_24dp;
            }

            String lowerName = categoryName.toLowerCase().trim();

            // Map category names to icons
            if (lowerName.contains("iphone") || lowerName.contains("điện thoại") ||
                lowerName.contains("phone") || lowerName.contains("smartphone")) {
                return R.drawable.ic_iphone;
            } else if (lowerName.contains("laptop") || lowerName.contains("máy tính xách tay")) {
                return R.drawable.ic_laptop;
            } else if (lowerName.contains("tivi") || lowerName.contains("tv") ||
                       lowerName.contains("television")) {
                return R.drawable.ic_tivi;
            } else if (lowerName.contains("monitor") || lowerName.contains("màn hình")) {
                return R.drawable.ic_monitor;
            } else if (lowerName.contains("ac") || lowerName.contains("điều hòa") ||
                       lowerName.contains("air conditioner") || lowerName.contains("máy lạnh")) {
                return R.drawable.ic_ac;
            } else if (lowerName.contains("airpod") || lowerName.contains("tai nghe") ||
                       lowerName.contains("earphone") || lowerName.contains("headphone")) {
                return R.drawable.ic_airpod;
            } else {
                // Default icon
                return R.drawable.ic_dashboard_black_24dp;
            }
        }
    }
}
