package com.example.shopverse_customer_app.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.shopverse_customer_app.data.model.Category;
import com.example.shopverse_customer_app.data.model.Product;
import com.example.shopverse_customer_app.data.remote.RetrofitClient;
import com.example.shopverse_customer_app.data.remote.SupabaseRestApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel";

    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<List<Product>> products = new MutableLiveData<>();
    private final MutableLiveData<List<Product>> filteredProducts = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<String> priceRange = new MutableLiveData<>("all");
    private final SupabaseRestApi restApi;

    // Keep reference to all products for filtering
    private List<Product> allProducts = new ArrayList<>();

    public HomeViewModel() {
        restApi = RetrofitClient.getInstance().getRestApi();
        loadCategories();
        loadProducts();
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<List<Product>> getProducts() {
        return products;
    }

    public LiveData<List<Product>> getFilteredProducts() {
        return filteredProducts;
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

    public LiveData<String> getPriceRange() {
        return priceRange;
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
                    categories.setValue(response.body());
                    Log.d(TAG, "Categories loaded: " + response.body().size());
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
     * Retry loading categories
     */
    public void retryLoadCategories() {
        loadCategories();
    }

    /**
     * Load all products
     */
    public void loadProducts() {
        loading.setValue(true);
        error.setValue(null);

        // Load all products with brand and category info
        // Parameters: select, categoryId, brandId, status, productName, order
        restApi.getProducts("*,brands(*),categories(*)", null, null, "eq.active", null, null)
                .enqueue(new Callback<List<Product>>() {
                    @Override
                    public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                        loading.setValue(false);
                        if (response.isSuccessful() && response.body() != null) {
                            allProducts = response.body();
                            products.setValue(allProducts);
                            // Apply current search filter
                            applyFilters();
                            Log.d(TAG, "Products loaded: " + allProducts.size());
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
     * Search products by query
     * Filters products by name (case-insensitive)
     */
    public void searchProducts(String query) {
        searchQuery.setValue(query);
        applyFilters();
    }

    /**
     * Filter products by price range
     * 
     * @param range: "all", "under5m", "5to10m", "10to20m", "20to30m", "above30m"
     */
    public void filterByPriceRange(String range) {
        priceRange.setValue(range);
        applyFilters();
    }

    /**
     * Filter products based on search query and price range
     */
    private void applyFilters() {
        String query = searchQuery.getValue();
        String range = priceRange.getValue();

        if ((query == null || query.trim().isEmpty()) &&
                (range == null || range.equals("all"))) {
            // No filters, show all products
            filteredProducts.setValue(allProducts);
            return;
        }

        List<Product> filtered = new ArrayList<>();

        for (Product product : allProducts) {
            // Apply search filter
            boolean matchesSearch = true;
            if (query != null && !query.trim().isEmpty()) {
                String lowerQuery = query.toLowerCase().trim();
                matchesSearch = product.getProductName() != null &&
                        product.getProductName().toLowerCase().contains(lowerQuery);
            }

            // Apply price range filter
            boolean matchesPrice = true;
            if (range != null && !range.equals("all") && product.getUnitPrice() != 0) {
                double price = product.getUnitPrice();
                switch (range) {
                    case "under5m":
                        matchesPrice = price < 5000000;
                        break;
                    case "5to10m":
                        matchesPrice = price >= 5000000 && price < 10000000;
                        break;
                    case "10to20m":
                        matchesPrice = price >= 10000000 && price < 20000000;
                        break;
                    case "20to30m":
                        matchesPrice = price >= 20000000 && price < 30000000;
                        break;
                    case "above30m":
                        matchesPrice = price >= 30000000;
                        break;
                }
            }

            // Include product if it matches both filters
            if (matchesSearch && matchesPrice) {
                filtered.add(product);
            }
        }

        filteredProducts.setValue(filtered);
        Log.d(TAG, "Filtered products: " + filtered.size() + " out of " + allProducts.size() +
                " (query: " + query + ", price: " + range + ")");
    }

    /**
     * Clear search query
     */
    public void clearSearch() {
        searchQuery.setValue("");
        priceRange.setValue("all");
        filteredProducts.setValue(allProducts);
    }

    /**
     * Retry loading all data
     */
    public void retry() {
        loadCategories();
        loadProducts();
    }
}