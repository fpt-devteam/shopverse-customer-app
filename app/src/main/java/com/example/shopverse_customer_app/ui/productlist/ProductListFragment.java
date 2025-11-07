package com.example.shopverse_customer_app.ui.productlist;

import android.content.Context;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
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

import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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
            showSearchDialog();
        });
    }

    private void showSearchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Tìm kiếm sản phẩm");

        // Set up the input
        final EditText input = new EditText(requireContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Nhập tên sản phẩm...");
        input.setPadding(50, 30, 50, 30);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Tìm kiếm", (dialog, which) -> {
            String query = input.getText().toString().trim();
            if (!query.isEmpty()) {
                viewModel.searchProducts(query);
                Toast.makeText(getContext(), "Đang tìm: " + query, Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", (dialog, which) -> dialog.cancel());

        builder.setNeutralButton("Xóa tìm kiếm", (dialog, which) -> {
            viewModel.clearSearch();
            Toast.makeText(getContext(), "Đã xóa tìm kiếm", Toast.LENGTH_SHORT).show();
        });

        AlertDialog dialog = builder.create();
        dialog.show();

        // Auto-focus and show keyboard
        input.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
        }
    }

    private void setupProductsRecyclerView() {
        // Set up adapter
        productAdapter = new ProductAdapter(this);

        // Set up RecyclerView with GridLayoutManager (2 columns)
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        productsRecyclerView.setLayoutManager(gridLayoutManager);
        productsRecyclerView.setAdapter(productAdapter);
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

        // Observe selected brands (to refresh chip states)
        viewModel.getSelectedBrands().observe(getViewLifecycleOwner(), selectedBrands -> {
            // Refresh chips when selection changes
            List<Brand> allBrands = viewModel.getBrands().getValue();
            if (allBrands != null) {
                updateBrandChips(allBrands);
            }
        });

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

        // Check if no brands are selected (show "All" as selected)
        List<Brand> selectedBrands = viewModel.getSelectedBrands().getValue();
        boolean noSelection = (selectedBrands == null || selectedBrands.isEmpty());

        // Add "All" chip first
        Chip allChip = createBrandChip("Tất cả", null, noSelection);
        brandChipsContainer.addView(allChip);

        // Add brand chips with dynamic selection state
        for (Brand brand : brands) {
            boolean isSelected = viewModel.isBrandSelected(brand);
            Chip chip = createBrandChip(brand.getBrandName(), brand, isSelected);
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
                viewModel.toggleBrandSelection(brand);
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
            viewModel.toggleBrandSelection(brand);
            Toast.makeText(getContext(), "Lọc theo: " + brand.getBrandName(), Toast.LENGTH_SHORT).show();
        });
        bottomSheet.show(getParentFragmentManager(), "BrandFilterBottomSheet");
    }

    @Override
    public void onProductClick(Product product) {
        // Navigate to product detail
        navigateToProductDetail(product);
        Log.d(TAG, "Product clicked: " + product.getProductName());
    }

    /**
     * Navigate to ProductDetailFragment with the selected product
     */
    private void navigateToProductDetail(Product product) {
        Bundle bundle = new Bundle();
        bundle.putSerializable("product", product);
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
        navController.navigate(R.id.action_productListFragment_to_productDetailFragment, bundle);
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
