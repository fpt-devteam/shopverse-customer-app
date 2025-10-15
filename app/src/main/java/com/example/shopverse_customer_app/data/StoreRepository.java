package com.example.shopverse_customer_app.data;

import android.content.Context;
import android.util.Log;

import com.example.shopverse_customer_app.utils.JsonUtils;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Repository class responsible for loading and managing store data.
 * Reads stores from a JSON file in assets and validates entries.
 */
public class StoreRepository {
    private static final String TAG = "StoreRepository";
    private static final String STORES_FILE = "stores.json";

    private final Context context;
    private List<Store> cachedStores;

    public StoreRepository(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Loads stores from the JSON file in assets.
     * Invalid stores are filtered out gracefully.
     *
     * @return List of valid Store objects
     */
    public List<Store> loadStores() {
        // Return cached stores if already loaded
        if (cachedStores != null) {
            return cachedStores;
        }

        cachedStores = new ArrayList<>();

        try {
            // Read JSON from assets
            String jsonString = JsonUtils.readJsonFromAssets(context, STORES_FILE);
            if (jsonString == null || jsonString.isEmpty()) {
                Log.e(TAG, "Failed to read stores.json from assets");
                return cachedStores;
            }

            // Parse JSON using Gson
            Gson gson = new Gson();
            Type type = new TypeToken<Map<String, List<Store>>>() {}.getType();
            Map<String, List<Store>> storesMap = gson.fromJson(jsonString, type);

            if (storesMap != null && storesMap.containsKey("stores")) {
                List<Store> allStores = storesMap.get("stores");

                // Validate and filter stores
                if (allStores != null) {
                    for (Store store : allStores) {
                        if (store != null && store.isValid()) {
                            cachedStores.add(store);
                            Log.d(TAG, "Loaded store: " + store.getName());
                        } else {
                            Log.w(TAG, "Skipping invalid store: " + store);
                        }
                    }
                }
            }

            Log.i(TAG, "Successfully loaded " + cachedStores.size() + " valid stores");

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "JSON parsing error", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error loading stores", e);
        }

        return cachedStores;
    }

    /**
     * Gets a store by its ID.
     *
     * @param storeId The ID of the store
     * @return Store object or null if not found
     */
    public Store getStoreById(String storeId) {
        if (cachedStores == null) {
            loadStores();
        }

        for (Store store : cachedStores) {
            if (store.getId().equals(storeId)) {
                return store;
            }
        }

        return null;
    }

    /**
     * Clears the cached stores, forcing a reload on next access.
     */
    public void clearCache() {
        cachedStores = null;
    }
}
