package com.example.shopverse_customer_app.ui.home;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.shopverse_customer_app.data.model.Category;
import com.example.shopverse_customer_app.databinding.FragmentHomeBinding;

public class HomeFragment extends Fragment implements CategoryAdapter.OnCategoryClickListener {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private CategoryAdapter categoryAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        observeViewModel();
        setupSearchBar();

        return root;
    }

    private void setupRecyclerView() {
        // Set up adapter
        categoryAdapter = new CategoryAdapter(this);

        // Set up RecyclerView with GridLayoutManager (3 columns)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        binding.categoriesRecyclerView.setLayoutManager(gridLayoutManager);
        binding.categoriesRecyclerView.setAdapter(categoryAdapter);
        binding.categoriesRecyclerView.setHasFixedSize(true);
    }

    private void observeViewModel() {
        // Observe categories
        homeViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                categoryAdapter.setCategories(categories);
                Log.d(TAG, "Categories updated: " + categories.size());
            }
        });

        // Observe loading state
        homeViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.categoriesRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
            }
        });

        // Observe errors
        homeViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    private void setupSearchBar() {
        // Set up search bar click listener
        binding.searchBar.setOnClickListener(v -> {
            // TODO: Implement search functionality
            Toast.makeText(getContext(), "Search coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onCategoryClick(Category category) {
        // TODO: Navigate to category detail or products page
        Toast.makeText(getContext(),
                "Category: " + category.getCategoryName(),
                Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Category clicked: " + category.getCategoryName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}