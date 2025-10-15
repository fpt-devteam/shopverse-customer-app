# Complete Technical Guide: Google Maps Integration for Android
## Shopverse Customer App - Store Locator Implementation

**Document Version:** 1.0
**Last Updated:** October 2025
**Target Audience:** Android Developers
**Prerequisites:** Android Studio, Java knowledge, Google Cloud Console access

---

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites & Setup](#prerequisites-setup)
3. [Google Cloud Console Configuration](#google-cloud-console-configuration)
4. [Project Setup](#project-setup)
5. [Feature 1: Google Maps SDK Integration](#feature-1-google-maps-sdk-integration)
6. [Feature 2: Google Places API (Autocomplete)](#feature-2-google-places-api-autocomplete)
7. [Feature 3: Google Directions API (Navigation)](#feature-3-google-directions-api-navigation)
8. [Testing Guide](#testing-guide)
9. [Troubleshooting](#troubleshooting)
10. [Best Practices & Security](#best-practices-security)

---

## 1. Overview {#overview}

This guide provides step-by-step instructions for implementing a complete Google Maps-based store locator feature in Android using Java.

### Features Implemented:
 Display device's current location on map
 Show nearby store locations with custom markers
 Calculate and highlight nearest store
 Location-based search with autocomplete suggestions
 Draw navigation routes from user to store
 Interactive draggable bottom sheet UI
 Distance sorting and formatting

### Architecture:
- **Pattern:** MVVM-like with Repository pattern
- **Language:** Java
- **Min SDK:** 24 (Android 7.0)
- **Target SDK:** 36

---

## 2. Prerequisites & Setup {#prerequisites-setup}

### 2.1 Development Environment

**Required:**
- Android Studio Hedgehog (2023.1.1) or later
- JDK 11 or higher
- Android SDK with API 24+
- Physical device or emulator with Google Play Services

**Recommended:**
- Physical Android device for location testing
- Google account with billing enabled (for APIs)

### 2.2 Project Dependencies

**File:** `app/build.gradle.kts`

```kotlin
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.shopverse_customer_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.shopverse_customer_app"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true  // Required for API key access
    }
}

dependencies {
    // Google Maps & Location
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.1.0")
    implementation("com.google.android.libraries.places:places:3.3.0")

    // JSON Parsing
    implementation("com.google.code.gson:gson:2.10.1")

    // HTTP Client for Directions API
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // Material Design
    implementation("com.google.android.material:material:1.11.0")

    // AndroidX
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("androidx.coordinatorlayout:coordinatorlayout:1.2.0")
}
```

---

## 3. Google Cloud Console Configuration {#google-cloud-console-configuration}

### 3.1 Create Google Cloud Project

**Step 1:** Go to [Google Cloud Console](https://console.cloud.google.com/)

**Step 2:** Create a new project
- Click "Select a project" � "New Project"
- Name: `shopverse-maps` (or your choice)
- Click "Create"

**Step 3:** Enable Billing
� **CRITICAL:** All three APIs require billing enabled

1. Navigate to "Billing"
2. Link a billing account
3. Set up budget alerts (recommended: $50/month)

### 3.2 Enable Required APIs

Navigate to "APIs & Services" � "Library" and enable:

 **Maps SDK for Android**
- Used for: Displaying maps, markers, polylines

 **Places API**
- Used for: Autocomplete suggestions

 **Directions API**
- Used for: Route calculation and drawing

### 3.3 Create API Key

**Step 1:** Navigate to "APIs & Services" � "Credentials"

**Step 2:** Click "Create Credentials" � "API Key"

**Step 3:** Restrict the API Key (IMPORTANT for security)

**Application Restrictions:**
- Select "Android apps"
- Click "Add an item"
- Package name: `com.example.shopverse_customer_app`
- Get SHA-1 fingerprint:

```bash
# For debug keystore (development)
keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android

# For release keystore (production)
keytool -list -v -keystore /path/to/your/keystore.jks -alias your_alias
```

Copy the SHA-1 certificate fingerprint and add it.

**API Restrictions:**
- Select "Restrict key"
- Check:
  - Maps SDK for Android
  - Places API
  - Directions API

**Step 4:** Copy the API key (format: `AIzaSy...`)

### 3.4 Cost Estimates

**Current Pricing (as of 2025):**

| API | Monthly Free Tier | Cost After Free Tier |
|-----|-------------------|---------------------|
| Maps SDK | $200 credit | $7 per 1,000 loads |
| Places API (Autocomplete) | $200 credit | $2.83 per 1,000 requests |
| Directions API | $200 credit | $5 per 1,000 requests |

**Expected Usage for Small App:**
- ~100 users � 10 searches/month = 1,000 requests
- Estimated monthly cost: **$0-5** (within free tier)

---

## 4. Project Setup {#project-setup}

### 4.1 Configure API Key (Secure Method)

**� NEVER commit API keys to version control!**

**Step 1:** Add to `.gitignore`

```
# File: .gitignore
local.properties
**/secrets.properties
**/*_key.properties
```

**Step 2:** Store in `local.properties`

**File:** `local.properties` (project root)

```properties
sdk.dir=C\:\\Users\\YourName\\AppData\\Local\\Android\\Sdk

# Google Maps API Key (DO NOT COMMIT THIS FILE)
GOOGLE_MAPS_API_KEY=AIzaSyCs4ddDobAcSalwBLz8sQkQeLG622IEdD0
```

**Step 3:** Read API Key in Build Script

**File:** `app/build.gradle.kts` (add at top level)

```kotlin
import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
}

// Read Google Maps API Key from local.properties
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localProperties.load(FileInputStream(localPropertiesFile))
}
val mapsApiKey: String = localProperties.getProperty("GOOGLE_MAPS_API_KEY") ?: "YOUR_API_KEY_HERE"

android {
    namespace = "com.example.shopverse_customer_app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.shopverse_customer_app"
        minSdk = 24
        targetSdk = 36

        // Inject API key as manifest placeholder
        manifestPlaceholders["GOOGLE_MAPS_API_KEY"] = mapsApiKey

        // Also make available via BuildConfig
        buildConfigField("String", "GOOGLE_MAPS_API_KEY", "\"$mapsApiKey\"")
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }
}
```

**Step 4:** Create example template for team

**File:** `local.properties.example`

```properties
# Copy this file to local.properties and replace with your actual values

sdk.dir=/path/to/your/android/sdk

# Google Maps API Key
# Get your key from: https://console.cloud.google.com/google/maps-apis/credentials
GOOGLE_MAPS_API_KEY=YOUR_API_KEY_HERE
```

### 4.2 AndroidManifest.xml Configuration

**File:** `app/src/main/AndroidManifest.xml`

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools">

    <!-- Required Permissions -->
    <uses-permission android:name="android.permission.INTERNET" />
    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
    <uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:theme="@style/Theme.AppTheme">

        <!-- Google Maps API Key (injected from build.gradle) -->
        <meta-data
            android:name="com.google.android.geo.API_KEY"
            android:value="${GOOGLE_MAPS_API_KEY}" />

        <!-- Maps Activity -->
        <activity
            android:name=".ui.maps.MapsActivity"
            android:exported="false"
            android:label="@string/title_maps" />

        <!-- Main Activity -->
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>

    </application>

</manifest>
```

### 4.3 Verify Setup

**Step 1:** Sync Gradle
```bash
./gradlew clean build
```

**Step 2:** Check BuildConfig is generated
```bash
# After sync, check:
# app/build/generated/source/buildConfig/debug/com/example/shopverse_customer_app/BuildConfig.java
# Should contain: public static final String GOOGLE_MAPS_API_KEY = "AIza...";
```

**Step 3:** Run app and check Logcat for errors
```
Filter: "Google Maps"
Look for: "Google Maps Android API: Authorization failure"
```

If you see authorization errors, verify:
- API key is correct
- SHA-1 fingerprint matches
- APIs are enabled in Cloud Console

---

## 5. Feature 1: Google Maps SDK Integration {#feature-1-google-maps-sdk-integration}

### 5.1 Data Model - Store.java

Create a POJO to represent store locations.

**File:** `app/src/main/java/com/example/shopverse_customer_app/data/Store.java`

```java
package com.example.shopverse_customer_app.data;

public class Store {
    private String id;
    private String name;
    private double latitude;
    private double longitude;
    private String address;
    private String hours;

    // Default constructor required for Gson
    public Store() {}

    public Store(String id, String name, double latitude, double longitude,
                 String address, String hours) {
        this.id = id;
        this.name = name;
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.hours = hours;
    }

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    public String getAddress() { return address; }
    public String getHours() { return hours; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setAddress(String address) { this.address = address; }
    public void setHours(String hours) { this.hours = hours; }

    /**
     * Validates that this store has valid coordinates.
     * @return true if latitude and longitude are valid
     */
    public boolean isValid() {
        return latitude >= -90 && latitude <= 90 &&
               longitude >= -180 && longitude <= 180 &&
               name != null && !name.isEmpty();
    }

    @Override
    public String toString() {
        return "Store{id='" + id + "', name='" + name + "', " +
               "lat=" + latitude + ", lng=" + longitude + "}";
    }
}
```

### 5.2 Store Data - stores.json

**File:** `app/src/main/assets/stores.json`

Create the `assets` folder if it doesn't exist:
```
app/src/main/assets/
```

```json
{
  "stores": [
    {
      "id": "store-a",
      "name": "CellphoneS 218-220 Tr�n Quang Kh�i",
      "latitude": 10.776889,
      "longitude": 106.700806,
      "address": "218-220 Tr�n Quang Kh�i, Ph��ng T�n �nh, Qu�n 1, TP. HCM",
      "hours": "8h00 - 22h00 (t�t c� c�c ng�y trong tu�n)"
    },
    {
      "id": "store-b",
      "name": "CellphoneS 567 L� Quang �nh",
      "latitude": 10.78412,
      "longitude": 106.69482,
      "address": "567 L� Quang �nh, Ph��ng 1, Qu�n G� V�p, TP. HCM",
      "hours": "8h00 - 22h00 (t�t c� c�c ng�y trong tu�n)"
    },
    {
      "id": "store-c",
      "name": "CellphoneS 114 Phan ng L�u",
      "latitude": 10.79707,
      "longitude": 106.67773,
      "address": "114 Phan ng L�u, Ph��ng 3, Qu�n Ph� Nhu�n, TP. HCM",
      "hours": "8h00 - 22h00 (t�t c� c�c ng�y trong tu�n)"
    },
    {
      "id": "store-d",
      "name": "CellphoneS 558 Tr�n Quang Kh�i",
      "latitude": 10.77234,
      "longitude": 106.69856,
      "address": "558 Tr�n Quang Kh�i, Ph��ng T�n �nh, Qu�n 1, TP. HCM",
      "hours": "8h00 - 22h00 (t�t c� c�c ng�y trong tu�n)"
    },
    {
      "id": "store-e",
      "name": "CellphoneS 377-379 i�n Bi�n Ph�",
      "latitude": 10.78045,
      "longitude": 106.69123,
      "address": "377-379 i�n Bi�n Ph�, Ph��ng 25, Qu�n B�nh Th�nh, TP. HCM",
      "hours": "8h00 - 22h00 (t�t c� c�c ng�y trong tu�n)"
    },
    {
      "id": "store-f",
      "name": "CellphoneS 336 X� Vi�t Ngh� T)nh",
      "latitude": 10.79856,
      "longitude": 106.68234,
      "address": "336 X� Vi�t Ngh� T)nh, Ph��ng 25, Qu�n B�nh Th�nh, TP. HCM",
      "hours": "8h00 - 22h00 (t�t c� c�c ng�y trong tu�n)"
    }
  ]
}
```

### 5.3 JSON Utility

**File:** `app/src/main/java/com/example/shopverse_customer_app/utils/JsonUtils.java`

```java
package com.example.shopverse_customer_app.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Utility class for reading JSON files from the assets folder.
 */
public class JsonUtils {
    private static final String TAG = "JsonUtils";

    /**
     * Reads a JSON file from the assets folder and returns it as a String.
     *
     * @param context Application context
     * @param fileName Name of the file in the assets folder (e.g., "stores.json")
     * @return JSON content as String, or null if an error occurs
     */
    public static String readJsonFromAssets(Context context, String fileName) {
        StringBuilder jsonBuilder = new StringBuilder();
        InputStream inputStream = null;
        BufferedReader reader = null;

        try {
            // Open the file from assets
            inputStream = context.getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));

            // Read line by line
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuilder.append(line);
            }

            return jsonBuilder.toString();

        } catch (IOException e) {
            Log.e(TAG, "Error reading JSON from assets: " + fileName, e);
            return null;
        } finally {
            // Close resources
            try {
                if (reader != null) reader.close();
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing stream", e);
            }
        }
    }
}
```

### 5.4 Store Repository

**File:** `app/src/main/java/com/example/shopverse_customer_app/data/StoreRepository.java`

```java
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

        List<Store> stores = new ArrayList<>();

        try {
            // Read JSON from assets
            String json = JsonUtils.readJsonFromAssets(context, STORES_FILE);
            if (json == null || json.isEmpty()) {
                Log.e(TAG, "Failed to read stores.json or file is empty");
                return stores;
            }

            // Parse JSON
            Gson gson = new Gson();
            Type mapType = new TypeToken<Map<String, List<Store>>>() {}.getType();
            Map<String, List<Store>> storeMap = gson.fromJson(json, mapType);

            if (storeMap != null && storeMap.containsKey("stores")) {
                List<Store> rawStores = storeMap.get("stores");

                // Filter out invalid stores
                if (rawStores != null) {
                    for (Store store : rawStores) {
                        if (store.isValid()) {
                            stores.add(store);
                        } else {
                            Log.w(TAG, "Skipping invalid store: " + store);
                        }
                    }
                }
            }

            Log.d(TAG, "Loaded " + stores.size() + " valid stores");

            // Cache the stores
            cachedStores = stores;

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "JSON parsing error", e);
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error loading stores", e);
        }

        return stores;
    }

    /**
     * Clears the cached stores, forcing a reload on next call.
     */
    public void clearCache() {
        cachedStores = null;
    }
}
```

### 5.5 Distance Utilities

**File:** `app/src/main/java/com/example/shopverse_customer_app/utils/DistanceUtils.java`

```java
package com.example.shopverse_customer_app.utils;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.util.Locale;

/**
 * Utility class for distance calculations between geographic coordinates.
 */
public class DistanceUtils {
    // Earth's radius in kilometers
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calculates the distance between two coordinates using the Haversine formula.
     * This is accurate for most practical purposes and accounts for Earth's curvature.
     *
     * @param lat1 Latitude of first point (degrees)
     * @param lon1 Longitude of first point (degrees)
     * @param lat2 Latitude of second point (degrees)
     * @param lon2 Longitude of second point (degrees)
     * @return Distance in kilometers
     */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        // Convert degrees to radians
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        // Haversine formula
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(radLat1) * Math.cos(radLat2) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS_KM * c;
    }

    /**
     * Overloaded method that accepts LatLng objects.
     */
    public static double haversineKm(LatLng point1, LatLng point2) {
        return haversineKm(point1.latitude, point1.longitude,
                          point2.latitude, point2.longitude);
    }

    /**
     * Overloaded method that accepts Location objects.
     */
    public static double haversineKm(Location location1, Location location2) {
        return haversineKm(location1.getLatitude(), location1.getLongitude(),
                          location2.getLatitude(), location2.getLongitude());
    }

    /**
     * Formats distance in a human-readable format.
     * - Less than 1 km: shows meters (e.g., "850 m")
     * - 1 km or more: shows kilometers with 1 decimal (e.g., "2.5 km")
     *
     * @param distanceKm Distance in kilometers
     * @return Formatted distance string
     */
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            // Convert to meters and round
            int meters = (int) Math.round(distanceKm * 1000);
            return String.format(Locale.US, "%d m", meters);
        } else {
            // Show kilometers with 1 decimal place
            return String.format(Locale.US, "%.1f km", distanceKm);
        }
    }
}
```

**Key Points:**
- **Haversine Formula**: Accurately calculates great-circle distance between two points on a sphere
- **Overloading**: Provides multiple method signatures for convenience
- **Formatting**: Smart formatting based on distance (meters vs kilometers)

### 5.6 Location Helper

**File:** `app/src/main/java/com/example/shopverse_customer_app/utils/LocationHelper.java`

```java
package com.example.shopverse_customer_app.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

/**
 * Helper class for location-related operations using FusedLocationProviderClient.
 */
public class LocationHelper {
    private static final String TAG = "LocationHelper";

    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;

    public LocationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.context);
    }

    /**
     * Checks if location permissions are granted.
     *
     * @return true if either FINE or COARSE location permission is granted
     */
    public boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context,
                   Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
               ActivityCompat.checkSelfPermission(context,
                   Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Gets the last known location asynchronously.
     * Requires location permissions to be granted.
     *
     * @param onSuccess Callback when location is successfully retrieved
     * @param onFailure Callback when location retrieval fails
     */
    public void getLastKnownLocation(final OnSuccessListener<Location> onSuccess,
                                     final Runnable onFailure) {
        if (!hasLocationPermission()) {
            Log.e(TAG, "Location permission not granted");
            onFailure.run();
            return;
        }

        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Log.d(TAG, "Location obtained: " + location.getLatitude() +
                                      ", " + location.getLongitude());
                            onSuccess.onSuccess(location);
                        } else {
                            Log.w(TAG, "Location is null");
                            onFailure.run();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get location", e);
                        onFailure.run();
                    });
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException when accessing location", e);
            onFailure.run();
        }
    }
}
```

**Key Points:**
- **FusedLocationProviderClient**: Modern Google Play Services API for location
- **Permission Handling**: Checks permissions before accessing location
- **Callbacks**: Provides success/failure callbacks for async operations
- **Error Handling**: Comprehensive error logging

### 5.7 MapsFragment Implementation

Now let's create the main fragment that ties everything together.

**File:** `app/src/main/java/com/example/shopverse_customer_app/ui/maps/MapsFragment.java`

This is a large file (~700 lines), so I'll break it into logical sections:

```java
package com.example.shopverse_customer_app.ui.maps;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shopverse_customer_app.BuildConfig;
import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.Store;
import com.example.shopverse_customer_app.data.StoreRepository;
import com.example.shopverse_customer_app.utils.DistanceUtils;
import com.example.shopverse_customer_app.utils.LocationHelper;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.card.MaterialCardView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment for displaying stores on Google Maps with search and navigation features.
 */
public class MapsFragment extends Fragment implements OnMapReadyCallback {
    private static final String TAG = "MapsFragment";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;
    private static final LatLng DEFAULT_LOCATION = new LatLng(10.7769, 106.7009); // Ho Chi Minh City
    private static final float DEFAULT_ZOOM = 13f;

    // Map and location
    private GoogleMap mMap;
    private LocationHelper locationHelper;
    private Location userLocation;
    private DirectionsHelper directionsHelper;
    private GeocodingHelper geocodingHelper;

    // Data
    private StoreRepository storeRepository;
    private List<Store> stores;
    private Store selectedStore;
    private Map<String, Marker> markerMap = new HashMap<>();
    private Marker highlightedMarker;
    private Polyline currentPolyline;

    // UI Components
    private MaterialCardView searchBar;
    private AutoCompleteTextView searchEditText;
    private ImageView clearSearchButton;
    private View bottomSheet;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private TextView selectedStoreName;
    private TextView selectedStoreDistance;
    private RecyclerView storesRecyclerView;
    private StoreListAdapter storeListAdapter;
    private PlacesClient placesClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                            @Nullable ViewGroup container,
                            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize components
        initializeComponents(view);
        initializeMap();
        setupSearchBar();
        setupBottomSheet();
        loadStoresData();
    }

    private void initializeComponents(View view) {
        // Initialize helpers
        locationHelper = new LocationHelper(requireContext());
        storeRepository = new StoreRepository(requireContext());

        // Get API key from BuildConfig
        String mapsApiKey = BuildConfig.GOOGLE_MAPS_API_KEY;
        directionsHelper = new DirectionsHelper(mapsApiKey);
        geocodingHelper = new GeocodingHelper(requireContext());

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), mapsApiKey);
        }
        placesClient = Places.createClient(requireContext());

        // Find views
        searchBar = view.findViewById(R.id.search_bar);
        searchEditText = view.findViewById(R.id.edit_search_location);
        clearSearchButton = view.findViewById(R.id.btn_clear_search);
        bottomSheet = view.findViewById(R.id.bottom_sheet);
        selectedStoreName = view.findViewById(R.id.tv_selected_store_name);
        selectedStoreDistance = view.findViewById(R.id.tv_selected_store_distance);
        storesRecyclerView = view.findViewById(R.id.rv_stores);
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
            getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        Log.d(TAG, "Map is ready");

        // Configure map UI
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);

        // Set marker click listener
        mMap.setOnMarkerClickListener(marker -> {
            onMarkerClick(marker);
            return true;
        });

        // Request location permission and enable location layer
        if (locationHelper.hasLocationPermission()) {
            enableMyLocation();
        } else {
            requestLocationPermission();
        }

        // Display stores on map
        displayStoresOnMap();
    }

    private void enableMyLocation() {
        if (!locationHelper.hasLocationPermission()) {
            return;
        }

        try {
            mMap.setMyLocationEnabled(true);

            // Get user's location and move camera
            locationHelper.getLastKnownLocation(
                location -> {
                    userLocation = location;
                    LatLng userLatLng = new LatLng(location.getLatitude(),
                                                   location.getLongitude());
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng,
                                                                         DEFAULT_ZOOM));

                    // Update store distances
                    if (storeListAdapter != null) {
                        storeListAdapter.setUserLocation(userLocation);
                        storeListAdapter.notifyDataSetChanged();
                    }

                    // Find and highlight nearest store
                    findAndHighlightNearestStore();
                },
                () -> {
                    Log.w(TAG, "Could not get location, using default");
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION,
                                                                       DEFAULT_ZOOM));
                }
            );
        } catch (SecurityException e) {
            Log.e(TAG, "SecurityException in enableMyLocation", e);
        }
    }

    private void requestLocationPermission() {
        requestPermissions(
            new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            },
            LOCATION_PERMISSION_REQUEST_CODE
        );
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                          @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 &&
                grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableMyLocation();
            } else {
                Toast.makeText(requireContext(),
                    "Location permission denied. Using default location.",
                    Toast.LENGTH_SHORT).show();
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(DEFAULT_LOCATION,
                                                                   DEFAULT_ZOOM));
            }
        }
    }

    private void loadStoresData() {
        stores = storeRepository.loadStores();
        Log.d(TAG, "Loaded " + stores.size() + " stores");

        if (mMap != null) {
            displayStoresOnMap();
        }
    }

    private void displayStoresOnMap() {
        if (mMap == null || stores == null || stores.isEmpty()) {
            return;
        }

        markerMap.clear();
        mMap.clear();

        for (Store store : stores) {
            LatLng position = new LatLng(store.getLatitude(), store.getLongitude());

            Marker marker = mMap.addMarker(new MarkerOptions()
                .position(position)
                .title(store.getName())
                .snippet(store.getAddress())
                .icon(BitmapDescriptorFactory.defaultMarker(
                    BitmapDescriptorFactory.HUE_RED))
            );

            if (marker != null) {
                marker.setTag(store);
                markerMap.put(store.getId(), marker);
            }
        }

        Log.d(TAG, "Displayed " + markerMap.size() + " markers on map");
    }

    private void findAndHighlightNearestStore() {
        if (userLocation == null || stores == null || stores.isEmpty()) {
            return;
        }

        Store nearest = null;
        double minDistance = Double.MAX_VALUE;

        for (Store store : stores) {
            double distance = DistanceUtils.haversineKm(
                userLocation.getLatitude(), userLocation.getLongitude(),
                store.getLatitude(), store.getLongitude()
            );

            if (distance < minDistance) {
                minDistance = distance;
                nearest = store;
            }
        }

        if (nearest != null) {
            selectStore(nearest);

            // Collapse bottom sheet to show marker
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            // Move camera to show nearest store
            LatLng storeLocation = new LatLng(nearest.getLatitude(),
                                              nearest.getLongitude());
            int bottomSheetPeekHeight = bottomSheetBehavior.getPeekHeight();
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(storeLocation, 15f));
        }
    }

    private void onMarkerClick(Marker marker) {
        Store store = (Store) marker.getTag();
        if (store != null) {
            selectStore(store);

            // Collapse bottom sheet to show the store location
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            // Animate camera to the store location with padding for bottom sheet
            int bottomSheetPeekHeight = bottomSheetBehavior.getPeekHeight();
            mMap.setPadding(0, 0, 0, bottomSheetPeekHeight);
            mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        }
    }

    private void selectStore(Store store) {
        selectedStore = store;

        // Clear previous highlight
        if (highlightedMarker != null) {
            highlightedMarker.setIcon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_RED));
        }

        // Highlight new marker
        Marker marker = markerMap.get(store.getId());
        if (marker != null) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(
                BitmapDescriptorFactory.HUE_AZURE));
            highlightedMarker = marker;
        }

        // Update bottom sheet header
        updateBottomSheetHeader(store);
    }

    private void updateBottomSheetHeader(Store store) {
        selectedStoreName.setText(store.getName());

        if (userLocation != null) {
            double distance = DistanceUtils.haversineKm(
                userLocation.getLatitude(), userLocation.getLongitude(),
                store.getLatitude(), store.getLongitude()
            );
            selectedStoreDistance.setText(DistanceUtils.formatDistance(distance));
            selectedStoreDistance.setVisibility(View.VISIBLE);
        } else {
            selectedStoreDistance.setVisibility(View.GONE);
        }
    }

    private void setupBottomSheet() {
        // Initialize BottomSheetBehavior
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        // Set peek height programmatically
        int peekHeightDp = 160;
        float density = getResources().getDisplayMetrics().density;
        int peekHeightPx = (int) (peekHeightDp * density);
        bottomSheetBehavior.setPeekHeight(peekHeightPx);

        bottomSheet.setVisibility(View.VISIBLE);
        bottomSheet.post(() -> bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED));

        // Auto-hide search bar when bottom sheet expands
        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    searchBar.animate()
                            .alpha(0f)
                            .setDuration(200)
                            .withEndAction(() -> searchBar.setVisibility(View.GONE));
                } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    searchBar.setVisibility(View.VISIBLE);
                    searchBar.animate().alpha(1f).setDuration(200);
                }
            }

            @Override
            public void onSlide(@NonNull View view, float slideOffset) {
                // Optional: implement smooth fade during slide
            }
        });

        // Set up RecyclerView
        storesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        storeListAdapter = new StoreListAdapter(stores, userLocation, this::onStoreItemClick);
        storesRecyclerView.setAdapter(storeListAdapter);

        // Setup action buttons
        setupActionButtons();
    }

    private void setupActionButtons() {
        View btnNearestStore = getView().findViewById(R.id.btn_nearest_store);
        View btnGetDirections = getView().findViewById(R.id.btn_get_directions);

        btnNearestStore.setOnClickListener(v -> findAndHighlightNearestStore());

        btnGetDirections.setOnClickListener(v -> {
            if (selectedStore != null) {
                openDirections(selectedStore);
            } else {
                Toast.makeText(requireContext(), "Please select a store first",
                    Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void onStoreItemClick(Store store) {
        selectStore(store);

        // Collapse bottom sheet to show store location
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        // Move camera to store with padding
        LatLng storeLocation = new LatLng(store.getLatitude(), store.getLongitude());
        int bottomSheetPeekHeight = bottomSheetBehavior.getPeekHeight();
        mMap.setPadding(0, 0, 0, bottomSheetPeekHeight);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(storeLocation, 15f));
    }

    private void openDirections(Store store) {
        if (userLocation == null) {
            Toast.makeText(requireContext(), "Unable to get your location",
                Toast.LENGTH_SHORT).show();
            return;
        }

        LatLng origin = new LatLng(userLocation.getLatitude(), userLocation.getLongitude());
        LatLng destination = new LatLng(store.getLatitude(), store.getLongitude());

        // Try to draw route on map
        directionsHelper.getDirections(origin, destination, new DirectionsHelper.DirectionsCallback() {
            @Override
            public void onSuccess(List<LatLng> routePoints, String distance, String duration) {
                drawRoute(routePoints);
                Toast.makeText(requireContext(),
                    "Distance: " + distance + ", Duration: " + duration,
                    Toast.LENGTH_LONG).show();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Directions API failed: " + error);
                // Fallback to external Google Maps app
                openExternalMaps(destination);
            }
        });
    }

    private void drawRoute(List<LatLng> routePoints) {
        if (routePoints == null || routePoints.isEmpty()) {
            return;
        }

        // Clear previous route
        clearRoute();

        // Draw new polyline
        PolylineOptions polylineOptions = new PolylineOptions()
            .addAll(routePoints)
            .width(12f)
            .color(0xFF4285F4)  // Google Blue
            .geodesic(true);

        currentPolyline = mMap.addPolyline(polylineOptions);

        // Fit route to view
        fitRouteToView(routePoints);
    }

    private void clearRoute() {
        if (currentPolyline != null) {
            currentPolyline.remove();
            currentPolyline = null;
        }
    }

    private void fitRouteToView(List<LatLng> routePoints) {
        if (routePoints.isEmpty()) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (LatLng point : routePoints) {
            builder.include(point);
        }

        LatLngBounds bounds = builder.build();
        int padding = 150; // pixels
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }

    private void openExternalMaps(LatLng destination) {
        String uri = "https://www.google.com/maps/dir/?api=1&destination=" +
            destination.latitude + "," + destination.longitude;

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");

        if (intent.resolveActivity(requireContext().getPackageManager()) != null) {
            startActivity(intent);
        } else {
            // Google Maps app not installed, open in browser
            intent.setPackage(null);
            startActivity(intent);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        clearRoute();
    }
}
```

**Note**: This fragment also requires helper classes `DirectionsHelper` and `GeocodingHelper` which will be covered in Features 2 and 3.

---

## 6. Feature 2: Google Places API (Autocomplete) {#feature-2-google-places-api-autocomplete}

### 6.1 Overview

The Places API provides location autocomplete functionality, allowing users to search for locations with intelligent suggestions.

### 6.2 PlacesAutocompleteAdapter

**File:** `app/src/main/java/com/example/shopverse_customer_app/ui/maps/PlacesAutocompleteAdapter.java`

```java
package com.example.shopverse_customer_app.ui.maps;

import android.content.Context;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.model.TypeFilter;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for AutoCompleteTextView that provides place suggestions from Google Places API.
 */
public class PlacesAutocompleteAdapter extends ArrayAdapter<String> implements Filterable {
    private static final String TAG = "PlacesAutocomplete";

    private final PlacesClient placesClient;
    private final List<AutocompletePrediction> predictions = new ArrayList<>();
    private final AutocompleteSessionToken token;

    public PlacesAutocompleteAdapter(Context context, PlacesClient placesClient) {
        super(context, android.R.layout.simple_dropdown_item_1line, new ArrayList<>());
        this.placesClient = placesClient;
        this.token = AutocompleteSessionToken.newInstance();
    }

    @Override
    public int getCount() {
        return predictions.size();
    }

    @Override
    public String getItem(int position) {
        if (position >= 0 && position < predictions.size()) {
            AutocompletePrediction prediction = predictions.get(position);
            return prediction.getFullText(null).toString();
        }
        return null;
    }

    /**
     * Gets the place ID for a prediction at the given position.
     */
    public String getPlaceId(int position) {
        if (position >= 0 && position < predictions.size()) {
            return predictions.get(position).getPlaceId();
        }
        return null;
    }

    @NonNull
    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();

                if (constraint == null || constraint.length() < 3) {
                    return results;
                }

                try {
                    // Bias results to Vietnam (Ho Chi Minh City area)
                    RectangularBounds bounds = RectangularBounds.newInstance(
                            new LatLng(10.0, 105.0),  // Southwest corner of Vietnam region
                            new LatLng(12.0, 108.0)   // Northeast corner
                    );

                    FindAutocompletePredictionsRequest request =
                            FindAutocompletePredictionsRequest.builder()
                                    .setSessionToken(token)
                                    .setQuery(constraint.toString())
                                    .setLocationBias(bounds)
                                    .setCountries("VN")  // Restrict to Vietnam
                                    .setTypeFilter(TypeFilter.REGIONS)  // Cities, districts, etc.
                                    .build();

                    placesClient.findAutocompletePredictions(request)
                            .addOnSuccessListener(response -> {
                                predictions.clear();
                                predictions.addAll(response.getAutocompletePredictions());

                                Log.d(TAG, "Found " + predictions.size() + " suggestions");

                                results.values = predictions;
                                results.count = predictions.size();

                                // Notify on main thread
                                notifyDataSetChanged();
                            })
                            .addOnFailureListener(exception -> {
                                Log.e(TAG, "Error getting predictions", exception);
                                predictions.clear();
                                notifyDataSetInvalidated();
                            });

                } catch (Exception e) {
                    Log.e(TAG, "Exception in autocomplete", e);
                }

                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        };
    }
}
```

**Key Features:**
- **Session Tokens**: Reduce API costs by grouping requests
- **Geographic Biasing**: Prioritize results in Vietnam/Ho Chi Minh City
- **Type Filtering**: Restrict to REGIONS (cities, districts) vs addresses
- **Minimum 3 characters**: Prevent excessive API calls

### 6.3 GeocodingHelper

**File:** `app/src/main/java/com/example/shopverse_customer_app/ui/maps/GeocodingHelper.java`

```java
package com.example.shopverse_customer_app.ui.maps;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Helper class for converting addresses to coordinates (Geocoding).
 */
public class GeocodingHelper {
    private static final String TAG = "GeocodingHelper";

    private final Geocoder geocoder;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public GeocodingHelper(Context context) {
        this.geocoder = new Geocoder(context, Locale.getDefault());
        this.executorService = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Converts an address string to LatLng coordinates.
     * Executes on background thread, callbacks on main thread.
     */
    public void getLocationFromAddress(String addressString, GeocodingCallback callback) {
        executorService.execute(() -> {
            try {
                List<Address> addresses = geocoder.getFromLocationName(addressString, 5);

                if (addresses != null && !addresses.isEmpty()) {
                    Address bestMatch = addresses.get(0);
                    LatLng location = new LatLng(bestMatch.getLatitude(),
                                                 bestMatch.getLongitude());

                    String formattedAddress = bestMatch.getAddressLine(0);

                    Log.d(TAG, "Geocoded '" + addressString + "' to " + location);

                    // Callback on main thread
                    mainHandler.post(() -> callback.onSuccess(location, formattedAddress));
                } else {
                    Log.w(TAG, "No results for: " + addressString);
                    mainHandler.post(() -> callback.onFailure("No results found"));
                }

            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed", e);
                mainHandler.post(() -> callback.onFailure("Geocoding error: " +
                    e.getMessage()));
            }
        });
    }

    /**
     * Callback interface for geocoding results.
     */
    public interface GeocodingCallback {
        void onSuccess(LatLng location, String formattedAddress);
        void onFailure(String error);
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
```

**Key Points:**
- **Background Threading**: Geocoding is synchronous and blocks, so run on executor
- **Main Thread Callbacks**: UI updates must happen on main thread
- **Error Handling**: Graceful fallback when geocoding fails

### 6.4 Search Bar Integration in MapsFragment

Add this method to MapsFragment (already shown in section 5.7):

```java
private void setupSearchBar() {
    // Set up autocomplete adapter
    PlacesAutocompleteAdapter adapter = new PlacesAutocompleteAdapter(
        requireContext(), placesClient);
    searchEditText.setAdapter(adapter);
    searchEditText.setThreshold(3);  // Minimum 3 characters

    // Handle search query submission
    searchEditText.setOnEditorActionListener((v, actionId, event) -> {
        if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            performLocationSearch(searchEditText.getText().toString());
            hideKeyboard();
            return true;
        }
        return false;
    });

    // Handle autocomplete item selection
    searchEditText.setOnItemClickListener((parent, view, position, id) -> {
        String selectedPlace = adapter.getItem(position);
        performLocationSearch(selectedPlace);
        hideKeyboard();
    });

    // Show/hide clear button
    searchEditText.addTextChangedListener(new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            clearSearchButton.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
        }

        @Override
        public void afterTextChanged(Editable s) {}
    });

    clearSearchButton.setOnClickListener(v -> {
        searchEditText.setText("");
        clearRoute();
    });
}

private void performLocationSearch(String query) {
    if (query == null || query.trim().isEmpty()) {
        return;
    }

    geocodingHelper.getLocationFromAddress(query,
        new GeocodingHelper.GeocodingCallback() {
            @Override
            public void onSuccess(LatLng location, String formattedAddress) {
                // Move camera to searched location
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, 14f));

                // Find nearest store to this location
                findNearestStoreToLocation(location);

                Toast.makeText(requireContext(),
                    "Found: " + formattedAddress,
                    Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Toast.makeText(requireContext(),
                    "Location not found: " + error,
                    Toast.LENGTH_SHORT).show();
            }
        });
}

private void findNearestStoreToLocation(LatLng searchLocation) {
    if (stores == null || stores.isEmpty()) {
        return;
    }

    Store nearest = null;
    double minDistance = Double.MAX_VALUE;

    for (Store store : stores) {
        LatLng storeLocation = new LatLng(store.getLatitude(), store.getLongitude());
        double distance = DistanceUtils.haversineKm(searchLocation, storeLocation);

        if (distance < minDistance) {
            minDistance = distance;
            nearest = store;
        }
    }

    if (nearest != null) {
        selectStore(nearest);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
    }
}

private void hideKeyboard() {
    InputMethodManager imm = (InputMethodManager)
        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    if (imm != null && getView() != null) {
        imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
    }
}
```

### 6.5 Testing Places API

**Step 1:** Run the app and tap the search bar

**Step 2:** Type at least 3 characters (e.g., "District 1")

**Step 3:** Verify autocomplete suggestions appear

**Step 4:** Select a suggestion or press search

**Expected Results:**
- Suggestions appear after 3 characters
- Results are biased to Vietnam
- Selecting a suggestion finds the nearest store
- Camera moves to the searched location

**Troubleshooting:**
- **No suggestions**: Check Logcat for "PlacesAutocomplete" errors
- **API_KEY_INVALID**: Verify Places API is enabled in Cloud Console
- **OVER_QUERY_LIMIT**: Check billing is enabled

---

## 7. Feature 3: Google Directions API (Navigation) {#feature-3-google-directions-api-navigation}

### 7.1 Overview

The Directions API calculates routes between two points and returns encoded polylines for drawing on the map.

### 7.2 DirectionsHelper

**File:** `app/src/main/java/com/example/shopverse_customer_app/ui/maps/DirectionsHelper.java`

```java
package com.example.shopverse_customer_app.ui.maps;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Helper class for getting directions from Google Directions API.
 */
public class DirectionsHelper {
    private static final String TAG = "DirectionsHelper";
    private static final String DIRECTIONS_API_URL =
        "https://maps.googleapis.com/maps/api/directions/json";

    private final String apiKey;
    private final OkHttpClient httpClient;
    private final Handler mainHandler;

    public DirectionsHelper(String apiKey) {
        this.apiKey = apiKey;
        this.httpClient = new OkHttpClient();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Gets directions from origin to destination.
     * Executes asynchronously and calls callback on main thread.
     */
    public void getDirections(LatLng origin, LatLng destination, DirectionsCallback callback) {
        String url = DIRECTIONS_API_URL +
            "?origin=" + origin.latitude + "," + origin.longitude +
            "&destination=" + destination.latitude + "," + destination.longitude +
            "&mode=driving" +
            "&key=" + apiKey;

        Request request = new Request.Builder()
            .url(url)
            .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.e(TAG, "Network request failed", e);
                mainHandler.post(() -> callback.onFailure("Network error: " + e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!response.isSuccessful()) {
                    mainHandler.post(() -> callback.onFailure("HTTP error: " +
                        response.code()));
                    return;
                }

                String responseBody = response.body().string();
                parseDirectionsResponse(responseBody, callback);
            }
        });
    }

    private void parseDirectionsResponse(String json, DirectionsCallback callback) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            String status = root.get("status").getAsString();

            if (!"OK".equals(status)) {
                mainHandler.post(() -> callback.onFailure("Directions API status: " + status));
                return;
            }

            JsonArray routes = root.getAsJsonArray("routes");
            if (routes.size() == 0) {
                mainHandler.post(() -> callback.onFailure("No routes found"));
                return;
            }

            JsonObject route = routes.get(0).getAsJsonObject();
            JsonObject overviewPolyline = route.getAsJsonObject("overview_polyline");
            String encodedPolyline = overviewPolyline.get("points").getAsString();

            // Decode polyline
            List<LatLng> routePoints = decodePolyline(encodedPolyline);

            // Extract distance and duration
            JsonArray legs = route.getAsJsonArray("legs");
            JsonObject leg = legs.get(0).getAsJsonObject();
            String distance = leg.getAsJsonObject("distance").get("text").getAsString();
            String duration = leg.getAsJsonObject("duration").get("text").getAsString();

            Log.d(TAG, "Route found: " + distance + ", " + duration +
                ", " + routePoints.size() + " points");

            mainHandler.post(() -> callback.onSuccess(routePoints, distance, duration));

        } catch (Exception e) {
            Log.e(TAG, "Failed to parse directions response", e);
            mainHandler.post(() -> callback.onFailure("Parse error: " + e.getMessage()));
        }
    }

    /**
     * Decodes Google's encoded polyline format into a list of LatLng points.
     * Algorithm from: https://developers.google.com/maps/documentation/utilities/polylinealgorithm
     */
    private List<LatLng> decodePolyline(String encoded) {
        List<LatLng> points = new ArrayList<>();
        int index = 0;
        int len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;

            // Decode latitude
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;

            // Decode longitude
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng point = new LatLng(lat / 1E5, lng / 1E5);
            points.add(point);
        }

        return points;
    }

    /**
     * Callback interface for directions results.
     */
    public interface DirectionsCallback {
        void onSuccess(List<LatLng> routePoints, String distance, String duration);
        void onFailure(String error);
    }
}
```

**Key Points:**
- **OkHttp**: Async HTTP client for network requests
- **Polyline Decoding**: Google's encoded polyline algorithm implemented
- **Main Thread Callbacks**: Ensures UI updates are safe
- **Error Handling**: Network, HTTP, and parsing errors handled gracefully

### 7.3 Testing Directions API

**Step 1:** Select a store from the list or map

**Step 2:** Tap "Get Directions" button

**Expected Results:**
- Blue polyline route drawn on map
- Toast shows distance and duration (e.g., "2.5 km, 8 mins")
- Camera zooms to fit entire route

**Fallback:** If Directions API fails, app opens external Google Maps app

**Troubleshooting:**
- **No route drawn**: Check Logcat for "DirectionsHelper" errors
- **Status: REQUEST_DENIED**: Verify Directions API is enabled and API key is correct
- **Status: OVER_QUERY_LIMIT**: Check billing is enabled

---


## 8. Troubleshooting {#troubleshooting}

### 8.1 Common Issues

**Issue: "Google Maps Android API: Authorization failure"**

**Causes:**
- API key not configured correctly
- SHA-1 fingerprint mismatch
- Maps SDK for Android not enabled

**Solutions:**
1. Verify `local.properties` contains correct API key
2. Check AndroidManifest.xml has `<meta-data>` tag
3. Re-generate SHA-1 fingerprint:
   ```bash
   keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey
   ```
4. Add fingerprint to API key restrictions in Cloud Console
5. Enable "Maps SDK for Android" in APIs & Services

**Issue: Map shows but no markers appear**

**Causes:**
- `stores.json` not in assets folder
- JSON parsing error
- Invalid coordinates in data

**Solutions:**
1. Check Logcat for "StoreRepository" errors
2. Verify `app/src/main/assets/stores.json` exists
3. Validate JSON syntax with online validator
4. Check coordinates are valid (lat: -90 to 90, lng: -180 to 180)

**Issue: No autocomplete suggestions**

**Causes:**
- Places API not enabled
- Billing not enabled
- API key missing Places API restriction

**Solutions:**
1. Enable "Places API" in Cloud Console
2. Link billing account
3. Add Places API to key restrictions
4. Check Logcat for "PlacesAutocomplete" errors
5. Test with > 3 characters

**Issue: Directions not drawing**

**Causes:**
- Directions API not enabled
- No billing account
- Network connectivity issues

**Solutions:**
1. Enable "Directions API" in Cloud Console
2. Enable billing (required for Directions API)
3. Check device internet connection
4. Look for "DirectionsHelper" errors in Logcat
5. Test fallback to Google Maps app works

**Issue: App crashes on startup**

**Causes:**
- Missing BuildConfig.GOOGLE_MAPS_API_KEY
- Fragment initialization error

**Solutions:**
1. Clean and rebuild project:
   ```bash
   ./gradlew clean build
   ```
2. Verify `buildConfig = true` in build.gradle.kts
3. Check `BuildConfig.java` was generated:
   ```
   app/build/generated/source/buildConfig/debug/.../BuildConfig.java
   ```
4. Ensure Google Play Services is installed on device/emulator
