package com.example.shopverse_customer_app.ui.productdetail;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopverse_customer_app.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Adapter for displaying product specifications
 */
public class SpecificationAdapter extends RecyclerView.Adapter<SpecificationAdapter.SpecViewHolder> {

    private List<Map.Entry<String, String>> specifications = new ArrayList<>();

    public void setSpecifications(Map<String, String> specs) {
        this.specifications.clear();
        if (specs != null) {
            this.specifications.addAll(specs.entrySet());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SpecViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_specification, parent, false);
        return new SpecViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SpecViewHolder holder, int position) {
        Map.Entry<String, String> spec = specifications.get(position);
        holder.bind(spec.getKey(), spec.getValue());
    }

    @Override
    public int getItemCount() {
        return specifications.size();
    }

    static class SpecViewHolder extends RecyclerView.ViewHolder {
        private final TextView specLabel;
        private final TextView specValue;

        public SpecViewHolder(@NonNull View itemView) {
            super(itemView);
            specLabel = itemView.findViewById(R.id.specLabel);
            specValue = itemView.findViewById(R.id.specValue);
        }

        public void bind(String label, String value) {
            specLabel.setText(label);
            specValue.setText(value != null ? value : "N/A");
        }
    }
}
