package com.example.shopverse_customer_app.ui.home;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.shopverse_customer_app.data.model.Category;
import com.example.shopverse_customer_app.data.remote.RetrofitClient;
import com.example.shopverse_customer_app.data.remote.SupabaseRestApi;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel";

    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final SupabaseRestApi restApi;

    public HomeViewModel() {
        restApi = RetrofitClient.getInstance().getRestApi();
        loadCategories();
    }

    public LiveData<List<Category>> getCategories() {
        return categories;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    /**
     * Fetch categories from Supabase
     */
    public void loadCategories() {
        loading.setValue(true);
        error.setValue(null);

        restApi.getCategories("*").enqueue(new Callback<List<Category>>() {
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
}