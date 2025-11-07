package com.example.shopverse_customer_app.ui.home;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.Category;
import com.example.shopverse_customer_app.data.model.Product;
import com.example.shopverse_customer_app.databinding.FragmentHomeBinding;
import com.example.shopverse_customer_app.ui.productdetail.ProductDetailFragment;
import com.example.shopverse_customer_app.ui.productlist.ProductAdapter;
import com.google.android.material.chip.Chip;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

public class HomeFragment extends Fragment implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private HomeViewModel homeViewModel;
    private ProductAdapter productAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
            ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupRecyclerView();
        observeViewModel();
        setupSearchBar();
        setupPriceFilter();

        return root;
    }

    private void setupRecyclerView() {
        // Set up adapter
        productAdapter = new ProductAdapter(this);

        // Set up RecyclerView with GridLayoutManager (2 columns for products)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.productsRecyclerView.setLayoutManager(gridLayoutManager);
        binding.productsRecyclerView.setAdapter(productAdapter);
        binding.productsRecyclerView.setHasFixedSize(true);
    }

    private void observeViewModel() {
        // Observe products (for debugging)
        homeViewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                Log.d(TAG, "Total products loaded: " + products.size());
            }
        });

        // Observe filtered products (this is what we display)
        homeViewModel.getFilteredProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                productAdapter.setProducts(products);

                // Show/hide empty state
                if (products.isEmpty()) {
                    binding.emptyStateTextView.setVisibility(View.VISIBLE);
                    binding.productsRecyclerView.setVisibility(View.GONE);

                    // Update empty message based on search state
                    String query = homeViewModel.getSearchQuery().getValue();
                    if (query != null && !query.isEmpty()) {
                        binding.emptyStateTextView.setText("Không tìm thấy sản phẩm \"" + query + "\"");
                    } else {
                        binding.emptyStateTextView.setText("Không có sản phẩm nào");
                    }
                } else {
                    binding.emptyStateTextView.setVisibility(View.GONE);
                    binding.productsRecyclerView.setVisibility(View.VISIBLE);
                }

                Log.d(TAG, "Filtered products updated: " + products.size());
            }
        });

        // Observe loading state
        homeViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                binding.productsRecyclerView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
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
        // Set up search TextWatcher for real-time filtering
        binding.searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Not needed
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Trigger search on each text change
                homeViewModel.searchProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Not needed
            }
        });
    }

    private void setupPriceFilter() {
        // Set up chip click listeners for price filtering
        binding.chipAllPrices.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                homeViewModel.filterByPriceRange("all");
            }
        });

        binding.chipUnder5M.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                homeViewModel.filterByPriceRange("under5m");
            }
        });

        binding.chipFrom5To10M.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                homeViewModel.filterByPriceRange("5to10m");
            }
        });

        binding.chipFrom10To20M.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                homeViewModel.filterByPriceRange("10to20m");
            }
        });

        binding.chipFrom20To30M.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                homeViewModel.filterByPriceRange("20to30m");
            }
        });

        binding.chipAbove30M.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                homeViewModel.filterByPriceRange("above30m");
            }
        });
    }

    @Override
    public void onProductClick(Product product) {
        // Navigate to product detail
        Bundle bundle = new Bundle();
        bundle.putSerializable("product", product);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.action_navigation_home_to_productDetailFragment, bundle);
        Log.d(TAG, "Product clicked: " + product.getProductName());
    }

    @Override
    public void onFavoriteClick(Product product) {
        // TODO: Implement favorite functionality
        Toast.makeText(getContext(), "Added to favorites: " + product.getProductName(), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Favorite clicked: " + product.getProductName());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}