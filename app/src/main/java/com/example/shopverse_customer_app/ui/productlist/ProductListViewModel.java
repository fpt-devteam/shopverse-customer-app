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
    private final MutableLiveData<Brand> selectedBrand = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final SupabaseRestApi restApi;

    // Filter states
    private String sortOrder = null; // null, "unit_price.asc", "unit_price.desc"

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

    public LiveData<Brand> getSelectedBrand() {
        return selectedBrand;
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
        String brandFilter = null;
        Brand currentBrand = selectedBrand.getValue();
        if (currentBrand != null) {
            brandFilter = "eq." + currentBrand.getBrandId();
        }

        String statusFilter = "eq.active";
        String select = "*,brands(*),categories(*)";

        restApi.getProducts(select, categoryFilter, brandFilter, statusFilter, sortOrder)
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
     * Filter by brand
     */
    public void filterByBrand(Brand brand) {
        selectedBrand.setValue(brand);
        loadProducts();
    }

    /**
     * Clear brand filter
     */
    public void clearBrandFilter() {
        selectedBrand.setValue(null);
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
