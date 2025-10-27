package com.example.shopverse_customer_app.ui.productlist;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.Brand;
import com.example.shopverse_customer_app.ui.dashboard.BrandAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.List;

/**
 * Bottom sheet dialog for filtering products by brand
 */
public class BrandFilterBottomSheet extends BottomSheetDialogFragment
        implements BrandAdapter.OnBrandClickListener {

    private static final String ARG_BRANDS = "brands";

    private List<Brand> brands;
    private BrandAdapter brandAdapter;
    private OnBrandSelectedListener listener;

    public interface OnBrandSelectedListener {
        void onBrandSelected(Brand brand);
    }

    public static BrandFilterBottomSheet newInstance(List<Brand> brands) {
        BrandFilterBottomSheet fragment = new BrandFilterBottomSheet();
        Bundle args = new Bundle();
        args.putSerializable(ARG_BRANDS, new java.util.ArrayList<>(brands));
        fragment.setArguments(args);
        return fragment;
    }

    public void setOnBrandSelectedListener(OnBrandSelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            brands = (List<Brand>) getArguments().getSerializable(ARG_BRANDS);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.bottom_sheet_brand_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up close button
        ImageView closeButton = view.findViewById(R.id.closeButton);
        closeButton.setOnClickListener(v -> dismiss());

        // Set up brands RecyclerView with GridLayoutManager (4 columns)
        RecyclerView brandsRecyclerView = view.findViewById(R.id.brandsRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 4);
        brandsRecyclerView.setLayoutManager(gridLayoutManager);

        // Set up adapter
        brandAdapter = new BrandAdapter(this);
        brandsRecyclerView.setAdapter(brandAdapter);

        // Set brands
        if (brands != null) {
            brandAdapter.setBrands(brands);
        }

        // Set up done button
        Button doneButton = view.findViewById(R.id.doneButton);
        doneButton.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onBrandClick(Brand brand) {
        if (listener != null) {
            listener.onBrandSelected(brand);
        }
        dismiss();
    }
}
