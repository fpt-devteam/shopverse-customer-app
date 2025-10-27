package com.example.shopverse_customer_app.ui.dashboard;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.Brand;
import com.example.shopverse_customer_app.data.model.Category;
import com.example.shopverse_customer_app.databinding.FragmentDashboardBinding;
import com.google.android.flexbox.FlexDirection;
import com.google.android.flexbox.FlexWrap;
import com.google.android.flexbox.FlexboxLayoutManager;

public class DashboardFragment extends Fragment
        implements CategorySidebarAdapter.OnCategoryClickListener,
        BrandAdapter.OnBrandClickListener {

    private static final String TAG = "DashboardFragment";
    private FragmentDashboardBinding binding;
    private DashboardViewModel dashboardViewModel;
    private CategorySidebarAdapter categoryAdapter;
    private BrandAdapter brandAdapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupCategorySidebar();
        setupBrandsRecyclerView();
        observeViewModel();
        setupSearchBar();

        return root;
    }

    private void setupCategorySidebar() {
        // Set up adapter
        categoryAdapter = new CategorySidebarAdapter(this);

        // Set up RecyclerView with LinearLayoutManager (vertical)
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        binding.categoriesSidebarRecyclerView.setLayoutManager(layoutManager);
        binding.categoriesSidebarRecyclerView.setAdapter(categoryAdapter);
        binding.categoriesSidebarRecyclerView.setHasFixedSize(true);
    }

    private void setupBrandsRecyclerView() {
        // Set up adapter
        brandAdapter = new BrandAdapter(this);

        // Set up FlexboxLayoutManager for chip-style layout
        FlexboxLayoutManager flexboxLayoutManager = new FlexboxLayoutManager(getContext());
        flexboxLayoutManager.setFlexDirection(FlexDirection.ROW);
        flexboxLayoutManager.setFlexWrap(FlexWrap.WRAP);

        binding.brandsRecyclerView.setLayoutManager(flexboxLayoutManager);
        binding.brandsRecyclerView.setAdapter(brandAdapter);
    }

    private void observeViewModel() {
        // Observe categories
        dashboardViewModel.getCategories().observe(getViewLifecycleOwner(), categories -> {
            if (categories != null && !categories.isEmpty()) {
                categoryAdapter.setCategories(categories);
                Log.d(TAG, "Categories updated: " + categories.size());
            }
        });

        // Observe selected category
        dashboardViewModel.getSelectedCategory().observe(getViewLifecycleOwner(), category -> {
            if (category != null) {
                binding.categoryTitle.setText(category.getCategoryName());
                // Update brands title based on category
                String brandsTitle = "Hãng " + category.getCategoryName().toLowerCase();
                binding.brandsTitle.setText(brandsTitle);
                Log.d(TAG, "Selected category: " + category.getCategoryName());
            }
        });

        // Observe brands
        dashboardViewModel.getBrands().observe(getViewLifecycleOwner(), brands -> {
            if (brands != null) {
                brandAdapter.setBrands(brands);

                // Show/hide empty state
                if (brands.isEmpty()) {
                    binding.emptyStateTextView.setVisibility(View.VISIBLE);
                    binding.brandsRecyclerView.setVisibility(View.GONE);
                } else {
                    binding.emptyStateTextView.setVisibility(View.GONE);
                    binding.brandsRecyclerView.setVisibility(View.VISIBLE);
                }

                Log.d(TAG, "Brands updated: " + brands.size());
            }
        });

        // Observe loading state
        dashboardViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                binding.loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe errors
        dashboardViewModel.getError().observe(getViewLifecycleOwner(), error -> {
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
            Toast.makeText(getContext(), "Tìm kiếm đang được phát triển", Toast.LENGTH_SHORT).show();
        });

        // Set up "See All" click listener
        binding.seeAllTextView.setOnClickListener(v -> {
            Category selected = dashboardViewModel.getSelectedCategory().getValue();
            if (selected != null) {
                navigateToProductList(selected);
            }
        });
    }

    @Override
    public void onCategoryClick(Category category, int position) {
        // Update selected category in adapter
        categoryAdapter.setSelectedPosition(position);

        // Update ViewModel
        dashboardViewModel.selectCategory(category);

        Log.d(TAG, "Category clicked: " + category.getCategoryName());
    }

    @Override
    public void onBrandClick(Brand brand) {
        // Navigate to products filtered by this brand
        Category selected = dashboardViewModel.getSelectedCategory().getValue();
        if (selected != null) {
            navigateToProductList(selected);
        }
    }

    /**
     * Navigate to ProductListFragment with the selected category
     */
    private void navigateToProductList(Category category) {
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.nav_host_fragment_activity_main,
                            com.example.shopverse_customer_app.ui.productlist.ProductListFragment.newInstance(category))
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
