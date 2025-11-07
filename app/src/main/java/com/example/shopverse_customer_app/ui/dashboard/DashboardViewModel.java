package com.example.shopverse_customer_app.ui.dashboard;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.shopverse_customer_app.data.model.Brand;
import com.example.shopverse_customer_app.data.model.Category;
import com.example.shopverse_customer_app.data.remote.RetrofitClient;
import com.example.shopverse_customer_app.data.remote.SupabaseRestApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardViewModel extends ViewModel {

    private static final String TAG = "DashboardViewModel";

    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<Brand>> brands = new MutableLiveData<>();
    private final MutableLiveData<List<Brand>> filteredBrands = new MutableLiveData<>();
    private final MutableLiveData<Category> selectedCategory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final SupabaseRestApi restApi;

    // Keep reference to all brands for filtering
    private List<Brand> allBrands = new ArrayList<>();

    public DashboardViewModel() {
        restApi = RetrofitClient.getInstance().getRestApi();
        loadCategories();
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<List<Brand>> getBrands() {
        return brands;
    }

    public LiveData<List<Brand>> getFilteredBrands() {
        return filteredBrands;
    }

    public LiveData<Category> getSelectedCategory() {
        return selectedCategory;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<String> getSearchQuery() {
        return searchQuery;
    }

    /**
     * Fetch categories from Supabase
     */
    public void loadCategories() {
        loading.setValue(true);
        error.setValue(null);

        restApi.getCategories("category_id,category_name").enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Category> categoryList = response.body();
                    categories.setValue(categoryList);
                    Log.d(TAG, "Categories loaded: " + categoryList.size());

                    // Auto-select first category
                    if (!categoryList.isEmpty()) {
                        selectCategory(categoryList.get(0));
                    }
                } else {
                    String errorMsg = "Failed to load categories: " + response.code();
                    error.setValue(errorMsg);
                    Log.e(TAG, errorMsg);
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {
                loading.setValue(false);
                String errorMsg = "Network error: " + t.getMessage();
                error.setValue(errorMsg);
                Log.e(TAG, errorMsg, t);
            }
        });
    }

    /**
     * Select a category and fetch its brands
     */
    public void selectCategory(Category category) {
        if (category == null)
            return;

        selectedCategory.setValue(category);
        loadBrandsForCategory(category.getCategoryId());
    }

    /**
     * Fetch brands for a specific category
     */
    private void loadBrandsForCategory(String categoryId) {
        loading.setValue(true);
        error.setValue(null);

        // Query format: select=brands(*)&category_id=eq.{categoryId}
        String filter = "eq." + categoryId;

        restApi.getBrandsByCategory("brands(*)", filter)
                .enqueue(new Callback<List<SupabaseRestApi.BrandResponse>>() {
                    @Override
                    public void onResponse(Call<List<SupabaseRestApi.BrandResponse>> call,
                            Response<List<SupabaseRestApi.BrandResponse>> response) {
                        loading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            // Extract brands from nested response
                            List<Brand> brandList = new ArrayList<>();
                            for (SupabaseRestApi.BrandResponse brandResponse : response.body()) {
                                if (brandResponse.brand != null) {
                                    brandList.add(brandResponse.brand);
                                }
                            }
                            allBrands = brandList;
                            brands.setValue(brandList);
                            // Apply current search filter
                            filterBrands(searchQuery.getValue());
                            Log.d(TAG, "Brands loaded for category " + categoryId + ": " + brandList.size());
                        } else {
                            String errorMsg = "Failed to load brands: " + response.code();
                            error.setValue(errorMsg);
                            Log.e(TAG, errorMsg);
                            brands.setValue(new ArrayList<>()); // Empty list on error
                        }
                    }

                    @Override
                    public void onFailure(Call<List<SupabaseRestApi.BrandResponse>> call, Throwable t) {
                        loading.setValue(false);
                        String errorMsg = "Network error: " + t.getMessage();
                        error.setValue(errorMsg);
                        Log.e(TAG, errorMsg, t);
                        brands.setValue(new ArrayList<>()); // Empty list on error
                    }
                });
    }

    /**
     * Search brands by query
     * Filters brands by name (case-insensitive)
     */
    public void searchBrands(String query) {
        searchQuery.setValue(query);
        filterBrands(query);
    }

    /**
     * Filter brands based on search query
     */
    private void filterBrands(String query) {
        if (query == null || query.trim().isEmpty()) {
            // No filter, show all brands
            filteredBrands.setValue(allBrands);
            return;
        }

        String lowerQuery = query.toLowerCase().trim();
        List<Brand> filtered = new ArrayList<>();

        for (Brand brand : allBrands) {
            if (brand.getBrandName() != null &&
                    brand.getBrandName().toLowerCase().contains(lowerQuery)) {
                filtered.add(brand);
            }
        }

        filteredBrands.setValue(filtered);
        Log.d(TAG, "Filtered brands: " + filtered.size() + " out of " + allBrands.size());
    }

    /**
     * Clear search query
     */
    public void clearSearch() {
        searchQuery.setValue("");
        filteredBrands.setValue(allBrands);
    }

    /**
     * Retry loading data
     */
    public void retry() {
        loadCategories();
    }
}
