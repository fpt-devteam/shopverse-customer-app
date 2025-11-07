package com.example.shopverse_customer_app.ui.orderhistory;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.Order;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for displaying orders in RecyclerView
 */
public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private List<Order> orders = new ArrayList<>();
    private OnOrderClickListener listener;

    public interface OnOrderClickListener {
        void onOrderClick(Order order);
    }

    public OrderAdapter(OnOrderClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orders.get(position);
        holder.bind(order, listener);
    }

    @Override
    public int getItemCount() {
        return orders.size();
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders != null ? orders : new ArrayList<>();
        notifyDataSetChanged();
    }

    static class OrderViewHolder extends RecyclerView.ViewHolder {

        private final TextView orderIdText;
        private final TextView orderStatusText;
        private final TextView orderDateText;
        private final TextView orderTotalText;
        private final TextView orderAddressText;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            orderStatusText = itemView.findViewById(R.id.orderStatusText);
            orderDateText = itemView.findViewById(R.id.orderDateText);
            orderTotalText = itemView.findViewById(R.id.orderTotalText);
            orderAddressText = itemView.findViewById(R.id.orderAddressText);
        }

        public void bind(Order order, OnOrderClickListener listener) {
            // Order ID (show first 8 characters)
            String orderId = order.getOrderId();
            if (orderId != null && orderId.length() > 8) {
                orderIdText.setText("Đơn hàng #" + orderId.substring(0, 8));
            } else {
                orderIdText.setText("Đơn hàng #" + orderId);
            }

            // Status
            orderStatusText.setText(getStatusText(order.getStatus()));
            orderStatusText.setBackgroundColor(getStatusBackgroundColor(order.getStatus()));
            orderStatusText.setTextColor(getStatusTextColor(order.getStatus()));

            // Date
            if (order.getOrderDate() != null) {
                orderDateText.setText(order.getOrderDate());
            } else {
                orderDateText.setText("");
            }

            // Total price
            orderTotalText.setText(formatPrice(order.getTotalPrice()));

            // Address
            if (order.getAddress() != null && !order.getAddress().isEmpty()) {
                orderAddressText.setText("Địa chỉ: " + order.getAddress());
                orderAddressText.setVisibility(View.VISIBLE);
            } else {
                orderAddressText.setVisibility(View.GONE);
            }

            // Click listener
            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onOrderClick(order);
                }
            });
        }

        private String getStatusText(String status) {
            if (status == null) return "Không rõ";

            switch (status.toLowerCase()) {
                case "pending":
                    return "Chờ xác nhận";
                case "paid":
                    return "Đã thanh toán";
                case "shipped":
                    return "Đang vận chuyển";
                case "completed":
                    return "Hoàn thành";
                case "cancelled":
                    return "Đã hủy";
                default:
                    return status;
            }
        }

        private int getStatusBackgroundColor(String status) {
            if (status == null) return 0xFFE0E0E0; // Gray

            switch (status.toLowerCase()) {
                case "pending":
                    return 0xFFFFF3E0; // Light orange
                case "paid":
                    return 0xFFE3F2FD; // Light blue
                case "shipped":
                    return 0xFFFFF9C4; // Light yellow
                case "completed":
                    return 0xFFE8F5E9; // Light green
                case "cancelled":
                    return 0xFFFFEBEE; // Light red
                default:
                    return 0xFFE0E0E0; // Gray
            }
        }

        private int getStatusTextColor(String status) {
            if (status == null) return 0xFF757575; // Gray

            switch (status.toLowerCase()) {
                case "pending":
                    return 0xFFFF6F00; // Orange
                case "paid":
                    return 0xFF1976D2; // Blue
                case "shipped":
                    return 0xFFF57F17; // Yellow
                case "completed":
                    return 0xFF388E3C; // Green
                case "cancelled":
                    return 0xFFD32F2F; // Red
                default:
                    return 0xFF757575; // Gray
            }
        }

        private String formatPrice(double price) {
            long priceInt = (long) price;
            String formatted = String.format("%,d", priceInt).replace(",", ".");
            return formatted + "₫";
        }
    }
}
