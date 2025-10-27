package com.example.shopverse_customer_app.ui.productlist;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.Brand;
import com.example.shopverse_customer_app.data.model.Category;
import com.example.shopverse_customer_app.data.model.Product;
import com.google.android.material.chip.Chip;

import java.util.List;

/**
 * Fragment for displaying list of products with filtering options
 */
public class ProductListFragment extends Fragment
        implements ProductAdapter.OnProductClickListener {

    private static final String TAG = "ProductListFragment";
    private static final String ARG_CATEGORY = "category";

    private ProductListViewModel viewModel;
    private ProductAdapter productAdapter;
    private RecyclerView productsRecyclerView;
    private LinearLayout brandChipsContainer;
    private TextView filterPopular, filterPromotion, filterPrice, filterMore;
    private View loadingProgressBar, emptyStateTextView;

    public static ProductListFragment newInstance(Category category) {
        ProductListFragment fragment = new ProductListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CATEGORY, category);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProductListViewModel.class);

        // Get category from arguments
        Category category = null;
        if (getArguments() != null) {
            category = (Category) getArguments().getSerializable(ARG_CATEGORY);
        }

        // Initialize views
        initializeViews(view);

        // Setup components
        setupBackButton(view);
        setupSearchBar(view);
        setupProductsRecyclerView();
        setupFilterButtons();

        // Initialize ViewModel with category
        if (category != null) {
            viewModel.initialize(category);
        }

        // Observe ViewModel
        observeViewModel();
    }

    private void initializeViews(View view) {
        productsRecyclerView = view.findViewById(R.id.productsRecyclerView);
        brandChipsContainer = view.findViewById(R.id.brandChipsContainer);
        filterPopular = view.findViewById(R.id.filterPopular);
        filterPromotion = view.findViewById(R.id.filterPromotion);
        filterPrice = view.findViewById(R.id.filterPrice);
        filterMore = view.findViewById(R.id.filterMore);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        emptyStateTextView = view.findViewById(R.id.emptyStateTextView);
    }

    private void setupBackButton(View view) {
        view.findViewById(R.id.backButton).setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupSearchBar(View view) {
        view.findViewById(R.id.searchBar).setOnClickListener(v -> {
            // TODO: Implement search functionality
            Toast.makeText(getContext(), "Tìm kiếm đang được phát triển", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupProductsRecyclerView() {
        // Set up adapter
        productAdapter = new ProductAdapter(this);

        // Set up RecyclerView with GridLayoutManager (2 columns)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        productsRecyclerView.setLayoutManager(gridLayoutManager);
        productsRecyclerView.setAdapter(productAdapter);
        productsRecyclerView.setHasFixedSize(true);
    }

    private void setupFilterButtons() {
        // Popular filter (default selected)
        filterPopular.setOnClickListener(v -> {
            selectFilter(filterPopular);
            viewModel.clearSortOrder();
        });

        // Promotion filter
        filterPromotion.setOnClickListener(v -> {
            selectFilter(filterPromotion);
            // TODO: Implement promotion filter
            Toast.makeText(getContext(), "Lọc khuyến mãi", Toast.LENGTH_SHORT).show();
        });

        // Price filter
        filterPrice.setOnClickListener(v -> {
            selectFilter(filterPrice);
            togglePriceSort();
        });

        // More filters
        filterMore.setOnClickListener(v -> {
            // TODO: Show more filter options
            Toast.makeText(getContext(), "Bộ lọc khác", Toast.LENGTH_SHORT).show();
        });
    }

    private void selectFilter(TextView selectedFilter) {
        // Reset all filters
        filterPopular.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        filterPromotion.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        filterPrice.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));

        // Set selected filter
        selectedFilter.setTextColor(ContextCompat.getColor(requireContext(), R.color.red_500));
    }

    private boolean isPriceAscending = true;

    private void togglePriceSort() {
        if (isPriceAscending) {
            filterPrice.setText("Giá ▲");
            viewModel.sortByPriceAscending();
        } else {
            filterPrice.setText("Giá ▼");
            viewModel.sortByPriceDescending();
        }
        isPriceAscending = !isPriceAscending;
    }

    private void observeViewModel() {
        // Observe category
        viewModel.getCategory().observe(getViewLifecycleOwner(), category -> {
            if (category != null) {
                Log.d(TAG, "Category: " + category.getCategoryName());
            }
        });

        // Observe brands
        viewModel.getBrands().observe(getViewLifecycleOwner(), this::updateBrandChips);

        // Observe products
        viewModel.getProducts().observe(getViewLifecycleOwner(), products -> {
            if (products != null) {
                productAdapter.setProducts(products);

                // Show/hide empty state
                if (products.isEmpty()) {
                    emptyStateTextView.setVisibility(View.VISIBLE);
                    productsRecyclerView.setVisibility(View.GONE);
                } else {
                    emptyStateTextView.setVisibility(View.GONE);
                    productsRecyclerView.setVisibility(View.VISIBLE);
                }

                Log.d(TAG, "Products updated: " + products.size());
            }
        });

        // Observe loading state
        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null) {
                loadingProgressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            }
        });

        // Observe errors
        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error: " + error);
            }
        });
    }

    private void updateBrandChips(List<Brand> brands) {
        brandChipsContainer.removeAllViews();

        if (brands == null || brands.isEmpty()) {
            return;
        }

        // Add "All" chip first
        Chip allChip = createBrandChip("Tất cả", null, true);
        brandChipsContainer.addView(allChip);

        // Add brand chips
        for (Brand brand : brands) {
            Chip chip = createBrandChip(brand.getBrandName(), brand, false);
            brandChipsContainer.addView(chip);
        }

        // Add "See all brands" chip
        Chip seeAllChip = createSeeAllChip(brands);
        brandChipsContainer.addView(seeAllChip);
    }

    private Chip createBrandChip(String text, Brand brand, boolean isSelected) {
        Chip chip = new Chip(requireContext());
        chip.setText(text);
        chip.setCheckable(true);
        chip.setChecked(isSelected);

        chip.setOnClickListener(v -> {
            if (brand == null) {
                viewModel.clearBrandFilter();
            } else {
                viewModel.filterByBrand(brand);
            }
        });

        return chip;
    }

    private Chip createSeeAllChip(List<Brand> brands) {
        Chip chip = new Chip(requireContext());
        chip.setText("Xem tất cả");
        chip.setChipIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_notifications_black_24dp));

        chip.setOnClickListener(v -> showBrandFilterDialog(brands));

        return chip;
    }

    private void showBrandFilterDialog(List<Brand> brands) {
        BrandFilterBottomSheet bottomSheet = BrandFilterBottomSheet.newInstance(brands);
        bottomSheet.setOnBrandSelectedListener(brand -> {
            viewModel.filterByBrand(brand);
            Toast.makeText(getContext(), "Lọc theo: " + brand.getBrandName(), Toast.LENGTH_SHORT).show();
        });
        bottomSheet.show(getParentFragmentManager(), "BrandFilterBottomSheet");
    }

    @Override
    public void onProductClick(Product product) {
        // TODO: Navigate to product detail
        Toast.makeText(getContext(),
                "Sản phẩm: " + product.getProductName(),
                Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Product clicked: " + product.getProductName());
    }

    @Override
    public void onFavoriteClick(Product product) {
        // TODO: Add to favorites
        Toast.makeText(getContext(),
                "Đã thêm vào yêu thích",
                Toast.LENGTH_SHORT).show();
        Log.d(TAG, "Favorite clicked: " + product.getProductName());
    }
}
