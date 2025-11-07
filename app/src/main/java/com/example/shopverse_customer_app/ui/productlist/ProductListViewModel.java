package com.example.shopverse_customer_app.ui.productlist;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.shopverse_customer_app.data.model.Brand;
import com.example.shopverse_customer_app.data.model.Category;
import com.example.shopverse_customer_app.data.model.Product;
import com.example.shopverse_customer_app.data.remote.RetrofitClient;
import com.example.shopverse_customer_app.data.remote.SupabaseRestApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListViewModel extends ViewModel {

    private static final String TAG = "ProductListViewModel";

    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private final MutableLiveData<List<Brand>> brands = new MutableLiveData<>();
    private final MutableLiveData<Category> category = new MutableLiveData<>();
    private final MutableLiveData<List<Brand>> selectedBrands = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final SupabaseRestApi restApi;

    // Filter states
    private String sortOrder = null; // null, "unit_price.asc", "unit_price.desc"
    private String searchQuery = null; // Search by product name

    public ProductListViewModel() {
        restApi = RetrofitClient.getInstance().getRestApi();
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public LiveData<List<Brand>> getBrands() {
        return brands;
    }

    public LiveData<Category> getCategory() {
        return category;
    }

    public LiveData<List<Brand>> getSelectedBrands() {
        return selectedBrands;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    /**
     * Initialize with category
     */
    public void initialize(Category category) {
        this.category.setValue(category);
        loadBrandsForCategory(category.getCategoryId());
        loadProducts();
    }

    /**
     * Load brands for the current category
     */
    private void loadBrandsForCategory(String categoryId) {
        String filter = "eq." + categoryId;

        restApi.getBrandsByCategory("brands(*)", filter)
                .enqueue(new Callback<List<SupabaseRestApi.BrandResponse>>() {
                    @Override
                    public void onResponse(Call<List<SupabaseRestApi.BrandResponse>> call,
                                           Response<List<SupabaseRestApi.BrandResponse>> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            List<Brand> brandList = new ArrayList<>();
                            for (SupabaseRestApi.BrandResponse brandResponse : response.body()) {
                                if (brandResponse.brand != null) {
                                    brandList.add(brandResponse.brand);
                                }
                            }
                            brands.setValue(brandList);
                            Log.d(TAG, "Brands loaded: " + brandList.size());
                        } else {
                            Log.e(TAG, "Failed to load brands: " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<List<SupabaseRestApi.BrandResponse>> call, Throwable t) {
                        Log.e(TAG, "Network error loading brands: " + t.getMessage(), t);
                    }
                });
    }

    /**
     * Load products with current filters
     */
    public void loadProducts() {
        Category currentCategory = category.getValue();
        if (currentCategory == null) {
            return;
        }

        loading.setValue(true);
        error.setValue(null);

        String categoryFilter = "eq." + currentCategory.getCategoryId();

        // Build OR condition for multiple brands
        String brandFilter = null;
        List<Brand> currentBrands = selectedBrands.getValue();
        if (currentBrands != null && !currentBrands.isEmpty()) {
            if (currentBrands.size() == 1) {
                brandFilter = "eq." + currentBrands.get(0).getBrandId();
            } else {
                // Multiple brands: use OR condition with "in.(id1,id2,id3)"
                StringBuilder brandIds = new StringBuilder("in.(");
                for (int i = 0; i < currentBrands.size(); i++) {
                    brandIds.append(currentBrands.get(i).getBrandId());
                    if (i < currentBrands.size() - 1) {
                        brandIds.append(",");
                    }
                }
                brandIds.append(")");
                brandFilter = brandIds.toString();
            }
        }

        // Add search filter
        String nameFilter = null;
        if (searchQuery != null && !searchQuery.trim().isEmpty()) {
            nameFilter = "ilike.*" + searchQuery + "*";
        }

        String statusFilter = "eq.active";
        String select = "*,brands(*),categories(*)";

        restApi.getProducts(select, categoryFilter, brandFilter, statusFilter, nameFilter, sortOrder)
                .enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        loading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            products.setValue(response.body());
                            Log.d(TAG, "Products loaded: " + response.body().size());
                        } else {
                            String errorMsg = "Failed to load products: " + response.code();
                            error.setValue(errorMsg);
                            Log.e(TAG, errorMsg);
                        }
                    }

                    @Override
                    public void onFailure(Call<List<Product>> call, Throwable t) {
                        loading.setValue(false);
                        String errorMsg = "Network error: " + t.getMessage();
                        error.setValue(errorMsg);
                        Log.e(TAG, errorMsg, t);
                    }
                });
    }

    /**
     * Toggle brand selection (add/remove from filter)
     */
    public void toggleBrandSelection(Brand brand) {
        List<Brand> currentBrands = selectedBrands.getValue();
        if (currentBrands == null) {
            currentBrands = new ArrayList<>();
        } else {
            currentBrands = new ArrayList<>(currentBrands); // Create mutable copy
        }

        boolean isSelected = false;
        for (Brand b : currentBrands) {
            if (b.getBrandId().equals(brand.getBrandId())) {
                isSelected = true;
                break;
            }
        }

        if (isSelected) {
            // Remove brand
            currentBrands.removeIf(b -> b.getBrandId().equals(brand.getBrandId()));
        } else {
            // Add brand
            currentBrands.add(brand);
        }

        selectedBrands.setValue(currentBrands);
        loadProducts();
    }

    /**
     * Check if brand is selected
     */
    public boolean isBrandSelected(Brand brand) {
        List<Brand> currentBrands = selectedBrands.getValue();
        if (currentBrands == null || currentBrands.isEmpty()) {
            return false;
        }
        for (Brand b : currentBrands) {
            if (b.getBrandId().equals(brand.getBrandId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Clear all brand filters
     */
    public void clearBrandFilter() {
        selectedBrands.setValue(new ArrayList<>());
        loadProducts();
    }

    /**
     * Search products by name
     */
    public void searchProducts(String query) {
        searchQuery = query;
        loadProducts();
    }

    /**
     * Clear search
     */
    public void clearSearch() {
        searchQuery = null;
        loadProducts();
    }

    /**
     * Sort by price ascending
     */
    public void sortByPriceAscending() {
        sortOrder = "unit_price.asc";
        loadProducts();
    }

    /**
     * Sort by price descending
     */
    public void sortByPriceDescending() {
        sortOrder = "unit_price.desc";
        loadProducts();
    }

    /**
     * Clear sort order (popular/default)
     */
    public void clearSortOrder() {
        sortOrder = null;
        loadProducts();
    }

    /**
     * Retry loading products
     */
    public void retry() {
        loadProducts();
    }
}
