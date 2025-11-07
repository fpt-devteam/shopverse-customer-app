# Shopverse Admin App Development Guide

> **Comprehensive documentation for building an admin application based on the Shopverse Customer App codebase**

---

## Table of Contents

1. [Overview](#overview)
2. [Codebase Architecture](#codebase-architecture)
3. [Code Style & Conventions](#code-style--conventions)
4. [Supabase Configuration](#supabase-configuration)
5. [Data Models & Entities](#data-models--entities)
6. [API Integration Patterns](#api-integration-patterns)
7. [Authentication Implementation](#authentication-implementation)
8. [CRUD Operations Guide](#crud-operations-guide)
9. [Project Setup for Admin App](#project-setup-for-admin-app)
10. [Common Patterns & Best Practices](#common-patterns--best-practices)

---

## Overview

This guide provides comprehensive documentation for building an **Android Admin App** for the Shopverse e-commerce platform. The customer app uses **Java**, **MVVM architecture**, **Retrofit** for API communication, and **Supabase** as the backend.

### Technology Stack

- **Language:** Java (Android)
- **Architecture:** MVVM + Clean Architecture
- **UI Framework:** XML Layouts (NOT Jetpack Compose)
- **Backend:** Supabase (PostgreSQL + Auth + Storage)
- **HTTP Client:** Retrofit 2 + OkHttp 3
- **JSON Parsing:** Gson
- **State Management:** LiveData + ViewModel
- **Navigation:** Android Navigation Component
- **Dependency Injection:** Manual (Singleton pattern) - Can upgrade to Hilt
- **Security:** EncryptedSharedPreferences for token storage

### Key Features in Customer App

- User authentication (login, register, password reset)
- Product browsing with filters (category, brand, price)
- Store locator with Google Maps integration
- Category and brand management
- Product detail views

---

## Codebase Architecture

### 1. Project Structure

```
shopverse-customer-app/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/shopverse_customer_app/
│   │   │   ├── config/              # Configuration classes
│   │   │   ├── data/                # Data layer
│   │   │   │   ├── model/           # POJOs/DTOs
│   │   │   │   ├── remote/          # Retrofit API interfaces
│   │   │   │   └── repository/      # Repository pattern
│   │   │   ├── ui/                  # Presentation layer
│   │   │   │   ├── auth/            # Auth screens
│   │   │   │   ├── home/            # Home/Categories
│   │   │   │   ├── dashboard/       # Dashboard
│   │   │   │   ├── productlist/     # Product listing
│   │   │   │   ├── productdetail/   # Product details
│   │   │   │   ├── maps/            # Store locator
│   │   │   │   └── account/         # User account
│   │   │   ├── viewmodel/           # ViewModels
│   │   │   ├── utils/               # Utility classes
│   │   │   └── MainActivity.java
│   │   └── res/                     # Resources (layouts, drawables, etc.)
│   └── build.gradle.kts             # Build configuration
├── ARCHITECTURE.md                  # Architecture documentation
├── AUTH_MODULE_README.md            # Auth module documentation
└── local.properties                 # API keys (gitignored)
```

### 2. MVVM Architecture Layers

```
┌────────────────────────────────────┐
│     UI Layer (Activity/Fragment)   │
│  - Observes LiveData               │
│  - Updates UI                      │
└─────────────┬──────────────────────┘
              │
              ▼
┌────────────────────────────────────┐
│     ViewModel Layer                │
│  - Manages UI state                │
│  - Exposes LiveData                │
│  - Business logic                  │
└─────────────┬──────────────────────┘
              │
              ▼
┌────────────────────────────────────┐
│     Repository Layer               │
│  - Single source of truth          │
│  - Aggregates data sources         │
└─────────────┬──────────────────────┘
              │
              ▼
┌────────────────────────────────────┐
│     Data Source Layer              │
│  - Remote API (Retrofit)           │
│  - Local Storage (TokenManager)    │
└────────────────────────────────────┘
```

### 3. Package Organization

**Base package:** `com.example.shopverse_customer_app`

| Package | Purpose | Example Classes |
|---------|---------|-----------------|
| `config/` | Configuration constants | `SupabaseConfig.java` |
| `data/model/` | Data models (POJOs) | `Product.java`, `Category.java`, `User.java` |
| `data/remote/` | Retrofit API interfaces | `SupabaseAuthApi.java`, `SupabaseRestApi.java`, `RetrofitClient.java` |
| `data/repository/` | Repository implementations | `AuthRepository.java` |
| `ui/{feature}/` | UI components by feature | `HomeFragment.java`, `HomeViewModel.java`, `CategoryAdapter.java` |
| `viewmodel/` | Shared ViewModels | `AuthViewModel.java` |
| `utils/` | Utility classes | `TokenManager.java`, `ErrorParser.java`, `ValidationUtils.java` |

---

## Code Style & Conventions

### 1. Naming Conventions

#### Classes

| Type | Convention | Example |
|------|------------|---------|
| Activity | `{Feature}Activity` | `LoginActivity`, `MainActivity` |
| Fragment | `{Feature}Fragment` | `HomeFragment`, `ProductListFragment` |
| ViewModel | `{Feature}ViewModel` | `HomeViewModel`, `DashboardViewModel` |
| Adapter | `{Type}Adapter` | `CategoryAdapter`, `ProductAdapter` |
| Repository | `{Domain}Repository` | `AuthRepository`, `ProductRepository` |
| API Interface | `Supabase{Domain}Api` | `SupabaseAuthApi`, `SupabaseRestApi` |
| Data Model | `{Entity}` | `Product`, `Category`, `User` |
| Request DTO | `{Action}Request` | `LoginRequest`, `RegisterRequest` |
| Response DTO | `{Action}Response` | `AuthResponse` |

#### Variables & Methods

```java
// Fields - camelCase
private String accessToken;
private final SupabaseRestApi restApi;
private static final String TAG = "ClassName";

// Constants - UPPER_SNAKE_CASE
public static final String SUPABASE_URL = "...";
private static final int REQUEST_CODE = 100;

// Methods - camelCase (verbs)
public void loadCategories() { }
public boolean isLoggedIn() { }
public String getUserId() { }
private void saveAuthSession() { }

// Boolean methods - is/has prefix
public boolean isInStock() { }
public boolean hasValidToken() { }
public boolean isActive() { }

// Getter/Setter - standard Java beans convention
public String getProductName() { }
public void setProductName(String name) { }
```

### 2. File Organization

#### Standard Class Structure

```java
package com.example.shopverse_customer_app.{package};

// Imports organized by:
// 1. Android imports
// 2. Third-party library imports
// 3. Project imports
import android.content.Context;
import androidx.annotation.NonNull;

import com.google.gson.annotations.SerializedName;

import com.example.shopverse_customer_app.data.model.Product;

/**
 * Javadoc comment describing the class
 */
public class ExampleClass {

    // 1. Constants
    private static final String TAG = "ExampleClass";
    public static final int DEFAULT_VALUE = 0;

    // 2. Static fields
    private static ExampleClass instance;

    // 3. Instance fields
    private final Context context;
    private String data;

    // 4. Constructor(s)
    public ExampleClass(Context context) {
        this.context = context;
    }

    // 5. Public methods
    public void publicMethod() {
        // Implementation
    }

    // 6. Private methods
    private void privateHelperMethod() {
        // Implementation
    }

    // 7. Getters and Setters
    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    // 8. Inner classes/interfaces
    public interface Callback {
        void onSuccess();
        void onError(String error);
    }
}
```

### 3. Data Model Pattern

**All models follow this pattern:**

```java
package com.example.shopverse_customer_app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Model representing {table_name} table in Supabase
 */
public class Entity implements Serializable {

    // Fields with @SerializedName annotation (maps to database column names)
    @SerializedName("entity_id")
    private String entityId;

    @SerializedName("entity_name")
    private String entityName;

    // Default constructor
    public Entity() {
    }

    // Parameterized constructor
    public Entity(String entityId, String entityName) {
        this.entityId = entityId;
        this.entityName = entityName;
    }

    // Standard getters and setters
    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    // Helper methods (optional)
    public boolean isValid() {
        return entityName != null && !entityName.isEmpty();
    }

    // toString() for debugging
    @Override
    public String toString() {
        return "Entity{" +
                "entityId='" + entityId + '\'' +
                ", entityName='" + entityName + '\'' +
                '}';
    }
}
```

### 4. ViewModel Pattern

```java
package com.example.shopverse_customer_app.ui.feature;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FeatureViewModel extends ViewModel {

    private static final String TAG = "FeatureViewModel";

    // LiveData for UI state
    private final MutableLiveData<List<Entity>> data = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();

    // Dependencies
    private final Repository repository;

    public FeatureViewModel() {
        repository = RetrofitClient.getInstance().getRestApi();
        loadData();
    }

    // Expose LiveData as read-only
    public LiveData<List<Entity>> getData() {
        return data;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<String> getError() {
        return error;
    }

    // Public methods for UI actions
    public void loadData() {
        loading.setValue(true);
        error.setValue(null);

        repository.getData("*").enqueue(new Callback<List<Entity>>() {
            @Override
            public void onResponse(Call<List<Entity>> call, Response<List<Entity>> response) {
                loading.setValue(false);
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                } else {
                    error.setValue("Failed to load data: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Entity>> call, Throwable t) {
                loading.setValue(false);
                error.setValue("Network error: " + t.getMessage());
            }
        });
    }

    public void retry() {
        loadData();
    }
}
```

### 5. Fragment Pattern

```java
package com.example.shopverse_customer_app.ui.feature;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.shopverse_customer_app.databinding.FragmentFeatureBinding;

public class FeatureFragment extends Fragment {

    private static final String TAG = "FeatureFragment";
    private FragmentFeatureBinding binding;
    private FeatureViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(this).get(FeatureViewModel.class);
        binding = FragmentFeatureBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupUI();
        observeViewModel();

        return root;
    }

    private void setupUI() {
        // Setup RecyclerView, click listeners, etc.
    }

    private void observeViewModel() {
        viewModel.getData().observe(getViewLifecycleOwner(), data -> {
            if (data != null && !data.isEmpty()) {
                // Update UI
            }
        });

        viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
```

### 6. Logging Conventions

```java
// Use Android Log with TAG constant
private static final String TAG = "ClassName";

// Log levels
Log.d(TAG, "Debug message");           // Development debugging
Log.i(TAG, "Info message");            // General information
Log.w(TAG, "Warning message");         // Potential issues
Log.e(TAG, "Error message", exception); // Errors with stack trace
```

### 7. Comments & Documentation

```java
/**
 * Javadoc for public classes and methods
 * Describes what the class/method does
 *
 * @param email User's email address
 * @param password User's password
 * @return Authentication response with tokens
 */
public AuthResponse login(String email, String password) {
    // Inline comments for complex logic
    // TODO: Add retry mechanism
    // FIXME: Handle edge case when email is null
}
```

---

## Supabase Configuration

### 1. Project Configuration

**Supabase Instance Details:**

```
Project URL: https://uehonyhpopuxynbzshyo.supabase.co
Project Ref: uehonyhpopuxynbzshyo
Region: Southeast Asia (Singapore)
```

**API Keys:**

```
SUPABASE_URL=https://uehonyhpopuxynbzshyo.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InVlaG9ueWhwb3B1eHluYnpzaHlvIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjA3MjM2OTAsImV4cCI6MjA3NjI5OTY5MH0.IzFfyl88M6KpyHHqL3qychYdGRR7pRKtu5Sf9j9B5Ag
```

### 2. Configuration Setup

#### Step 1: Add to `local.properties`

```properties
# local.properties (gitignored - do NOT commit to version control)
GOOGLE_MAPS_API_KEY=your-maps-key-here
SUPABASE_URL=https://uehonyhpopuxynbzshyo.supabase.co
SUPABASE_ANON_KEY=eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### Step 2: Update `build.gradle.kts`

```kotlin
// app/build.gradle.kts
android {
    // ... other config

    buildTypes {
        release {
            // Read from local.properties
            val localProperties = Properties()
            val localPropertiesFile = rootProject.file("local.properties")
            if (localPropertiesFile.exists()) {
                localProperties.load(FileInputStream(localPropertiesFile))
            }

            val supabaseUrl: String = localProperties.getProperty("SUPABASE_URL") ?: ""
            val supabaseAnonKey: String = localProperties.getProperty("SUPABASE_ANON_KEY") ?: ""

            // Inject into BuildConfig
            buildConfigField("String", "SUPABASE_URL", "\"$supabaseUrl\"")
            buildConfigField("String", "SUPABASE_ANON_KEY", "\"$supabaseAnonKey\"")
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true  // Enable BuildConfig generation
    }
}
```

#### Step 3: Create `SupabaseConfig.java`

```java
// config/SupabaseConfig.java
package com.example.shopverse_admin_app.config;

import com.example.shopverse_admin_app.BuildConfig;

public class SupabaseConfig {

    // Supabase Project Configuration
    public static final String SUPABASE_URL = BuildConfig.SUPABASE_URL;
    public static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;
    public static final String BASE_URL = SUPABASE_URL.endsWith("/")
        ? SUPABASE_URL
        : SUPABASE_URL + "/";

    // Storage Buckets
    public static final String BUCKET_PRODUCT_IMAGES = "product-images";
    public static final String BUCKET_USER_AVATARS = "user-avatars";
    public static final String BUCKET_CATEGORY_IMAGES = "category-images";
    public static final String BUCKET_BRAND_LOGOS = "brand-logos";

    // HTTP Headers
    public static final String HEADER_API_KEY = "apikey";
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String CONTENT_TYPE_JSON = "application/json";

    // API Endpoints
    public static final String AUTH_ENDPOINT = "auth/v1/";
    public static final String REST_ENDPOINT = "rest/v1/";
    public static final String STORAGE_ENDPOINT = "storage/v1/";
}
```

### 3. Retrofit Client Setup

#### RetrofitClient.java (Singleton Pattern)

```java
// data/remote/RetrofitClient.java
package com.example.shopverse_admin_app.data.remote;

import com.example.shopverse_admin_app.config.SupabaseConfig;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static RetrofitClient instance;
    private final AuthInterceptor authInterceptor;
    private final Retrofit retrofit;
    private SupabaseAuthApi authApi;
    private SupabaseRestApi restApi;

    private RetrofitClient() {
        // Create auth interceptor
        authInterceptor = new AuthInterceptor();

        // Logging interceptor for debugging
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        // Create OkHttpClient with interceptors
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(loggingInterceptor)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();

        // Create Retrofit instance
        retrofit = new Retrofit.Builder()
                .baseUrl(SupabaseConfig.BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public SupabaseAuthApi getAuthApi() {
        if (authApi == null) {
            authApi = retrofit.create(SupabaseAuthApi.class);
        }
        return authApi;
    }

    public SupabaseRestApi getRestApi() {
        if (restApi == null) {
            restApi = retrofit.create(SupabaseRestApi.class);
        }
        return restApi;
    }

    public void setAccessToken(String token) {
        authInterceptor.setAccessToken(token);
    }

    public void clearAccessToken() {
        authInterceptor.clearAccessToken();
    }
}
```

#### AuthInterceptor.java

```java
// data/remote/AuthInterceptor.java
package com.example.shopverse_admin_app.data.remote;

import androidx.annotation.NonNull;

import com.example.shopverse_admin_app.config.SupabaseConfig;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class AuthInterceptor implements Interceptor {

    private volatile String accessToken;

    public void setAccessToken(String token) {
        this.accessToken = token;
    }

    public void clearAccessToken() {
        this.accessToken = null;
    }

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request original = chain.request();
        Request.Builder builder = original.newBuilder()
                .header(SupabaseConfig.HEADER_API_KEY, SupabaseConfig.SUPABASE_ANON_KEY)
                .header(SupabaseConfig.HEADER_CONTENT_TYPE, SupabaseConfig.CONTENT_TYPE_JSON);

        // Add Bearer token if available
        if (accessToken != null && !accessToken.isEmpty()) {
            builder.header(SupabaseConfig.HEADER_AUTHORIZATION, "Bearer " + accessToken);
        }

        return chain.proceed(builder.build());
    }
}
```

### 4. Required Dependencies

Add to `build.gradle.kts`:

```kotlin
dependencies {
    // Core Android
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Lifecycle components
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.7.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0")

    // Navigation
    implementation("androidx.navigation:navigation-fragment:2.7.6")
    implementation("androidx.navigation:navigation-ui:2.7.6")

    // Retrofit & OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Gson
    implementation("com.google.code.gson:gson:2.10.1")

    // Security (Encrypted SharedPreferences)
    implementation("androidx.security:security-crypto:1.1.0-alpha06")

    // Image Loading (Glide)
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")
}
```

---

## Data Models & Entities

### Database Schema Overview

The Supabase database consists of the following main tables:

```
auth.users              # Supabase built-in auth table
├── profiles            # Extended user profile data
├── categories          # Product categories
├── brands              # Product brands
├── products            # Product catalog
└── categories_brands   # Many-to-many relationship
```

### 1. User & Profile

#### User Model (auth.users)

```java
// data/model/User.java
package com.example.shopverse_admin_app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class User implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("email")
    private String email;

    @SerializedName("phone")
    private String phone;

    @SerializedName("created_at")
    private String createdAt;

    @SerializedName("confirmed_at")
    private String confirmedAt;

    @SerializedName("last_sign_in_at")
    private String lastSignInAt;

    // Constructors, getters, setters, toString()
}
```

#### Profile Model (profiles table)

```java
// data/model/Profile.java
package com.example.shopverse_admin_app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Extended user profile data
 * Database table: profiles
 *
 * Columns:
 * - user_id (UUID, PK, FK to auth.users)
 * - email (text)
 * - display_name (text, nullable)
 * - phone (text, nullable)
 * - address (text, nullable)
 * - role (text, default 'customer') - Values: 'customer', 'staff', 'admin'
 * - avatar_url (text, nullable)
 * - created_at (timestamp)
 */
public class Profile implements Serializable {

    @SerializedName("user_id")
    private String userId;

    @SerializedName("email")
    private String email;

    @SerializedName("display_name")
    private String displayName;

    @SerializedName("phone")
    private String phone;

    @SerializedName("address")
    private String address;

    @SerializedName("role")
    private String role; // 'customer', 'staff', 'admin'

    @SerializedName("avatar_url")
    private String avatarUrl;

    @SerializedName("created_at")
    private String createdAt;

    public Profile() {
    }

    public Profile(String userId, String email) {
        this.userId = userId;
        this.email = email;
        this.role = "customer"; // Default role
    }

    // Full getters and setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Helper methods
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(role);
    }

    public boolean isStaff() {
        return "staff".equalsIgnoreCase(role);
    }

    @Override
    public String toString() {
        return "Profile{" +
                "userId='" + userId + '\'' +
                ", email='" + email + '\'' +
                ", displayName='" + displayName + '\'' +
                ", role='" + role + '\'' +
                '}';
    }
}
```

### 2. Category Model

```java
// data/model/Category.java
package com.example.shopverse_admin_app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Product category model
 * Database table: categories
 *
 * Columns:
 * - category_id (UUID, PK)
 * - category_name (text, NOT NULL)
 */
public class Category implements Serializable {

    @SerializedName("category_id")
    private String categoryId;

    @SerializedName("category_name")
    private String categoryName;

    public Category() {
    }

    public Category(String categoryId, String categoryName) {
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    @Override
    public String toString() {
        return "Category{" +
                "categoryId='" + categoryId + '\'' +
                ", categoryName='" + categoryName + '\'' +
                '}';
    }
}
```

### 3. Brand Model

```java
// data/model/Brand.java
package com.example.shopverse_admin_app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

/**
 * Brand model
 * Database table: brands
 *
 * Columns:
 * - brand_id (UUID, PK)
 * - brand_name (text, NOT NULL)
 * - brand_logo_url (text, nullable)
 */
public class Brand implements Serializable {

    @SerializedName("brand_id")
    private String brandId;

    @SerializedName("brand_name")
    private String brandName;

    @SerializedName("brand_logo_url")
    private String brandLogoUrl;

    public Brand() {
    }

    public Brand(String brandId, String brandName) {
        this.brandId = brandId;
        this.brandName = brandName;
    }

    public String getBrandId() {
        return brandId;
    }

    public void setBrandId(String brandId) {
        this.brandId = brandId;
    }

    public String getBrandName() {
        return brandName;
    }

    public void setBrandName(String brandName) {
        this.brandName = brandName;
    }

    public String getBrandLogoUrl() {
        return brandLogoUrl;
    }

    public void setBrandLogoUrl(String brandLogoUrl) {
        this.brandLogoUrl = brandLogoUrl;
    }

    @Override
    public String toString() {
        return "Brand{" +
                "brandId='" + brandId + '\'' +
                ", brandName='" + brandName + '\'' +
                ", brandLogoUrl='" + brandLogoUrl + '\'' +
                '}';
    }
}
```

### 4. Product Model

```java
// data/model/Product.java
package com.example.shopverse_admin_app.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

/**
 * Product model
 * Database table: products
 *
 * Columns:
 * - product_id (UUID, PK)
 * - category_id (UUID, FK to categories)
 * - brand_id (UUID, FK to brands)
 * - product_name (text, NOT NULL)
 * - product_media (text[], nullable) - Array of image URLs
 * - stock (integer, default 0)
 * - unit_price (numeric, NOT NULL)
 * - description (text, nullable)
 * - status (text, default 'active') - Values: 'active', 'inactive'
 */
public class Product implements Serializable {

    @SerializedName("product_id")
    private String productId;

    @SerializedName("category_id")
    private String categoryId;

    @SerializedName("brand_id")
    private String brandId;

    @SerializedName("product_media")
    private List<String> productMedia; // Array of image URLs

    @SerializedName("product_name")
    private String productName;

    @SerializedName("stock")
    private int stock;

    @SerializedName("unit_price")
    private double unitPrice;

    @SerializedName("description")
    private String description;

    @SerializedName("status")
    private String status; // 'active' or 'inactive'

    // Optional: Nested objects when using joins in queries
    @SerializedName("brands")
    private Brand brand;

    @SerializedName("categories")
    private Category category;

    public Product() {
    }

    public Product(String productId, String productName, double unitPrice) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.status = "active";
        this.stock = 0;
    }

    // Full getters and setters
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }

    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }

    public String getBrandId() { return brandId; }
    public void setBrandId(String brandId) { this.brandId = brandId; }

    public List<String> getProductMedia() { return productMedia; }
    public void setProductMedia(List<String> productMedia) { this.productMedia = productMedia; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }

    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Brand getBrand() { return brand; }
    public void setBrand(Brand brand) { this.brand = brand; }

    public Category getCategory() { return category; }
    public void setCategory(Category category) { this.category = category; }

    // Helper methods
    public String getFirstImageUrl() {
        if (productMedia != null && !productMedia.isEmpty()) {
            return productMedia.get(0);
        }
        return null;
    }

    public boolean isInStock() {
        return stock > 0;
    }

    public boolean isActive() {
        return "active".equalsIgnoreCase(status);
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", productName='" + productName + '\'' +
                ", unitPrice=" + unitPrice +
                ", stock=" + stock +
                ", status='" + status + '\'' +
                '}';
    }
}
```

### 5. Authentication Models

#### LoginRequest

```java
// data/model/LoginRequest.java
package com.example.shopverse_admin_app.data.model;

import com.google.gson.annotations.SerializedName;

public class LoginRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public LoginRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

#### RegisterRequest

```java
// data/model/RegisterRequest.java
package com.example.shopverse_admin_app.data.model;

import com.google.gson.annotations.SerializedName;

public class RegisterRequest {

    @SerializedName("email")
    private String email;

    @SerializedName("password")
    private String password;

    public RegisterRequest(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
```

#### AuthResponse

```java
// data/model/AuthResponse.java
package com.example.shopverse_admin_app.data.model;

import com.google.gson.annotations.SerializedName;

public class AuthResponse {

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("expires_in")
    private long expiresIn; // Seconds

    @SerializedName("token_type")
    private String tokenType; // "bearer"

    @SerializedName("user")
    private User user;

    public AuthResponse() {
    }

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }

    public long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(long expiresIn) { this.expiresIn = expiresIn; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    @Override
    public String toString() {
        return "AuthResponse{" +
                "tokenType='" + tokenType + '\'' +
                ", expiresIn=" + expiresIn +
                ", user=" + user +
                '}';
    }
}
```

---

## API Integration Patterns

### 1. Supabase Auth API

```java
// data/remote/SupabaseAuthApi.java
package com.example.shopverse_admin_app.data.remote;

import com.example.shopverse_admin_app.data.model.AuthResponse;
import com.example.shopverse_admin_app.data.model.LoginRequest;
import com.example.shopverse_admin_app.data.model.RegisterRequest;
import com.example.shopverse_admin_app.data.model.User;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Supabase Authentication API endpoints
 * Base URL: {SUPABASE_URL}/auth/v1/
 */
public interface SupabaseAuthApi {

    /**
     * LOGIN
     * POST /auth/v1/token?grant_type=password
     * Headers required: apikey (added by AuthInterceptor)
     *
     * @param request LoginRequest with email and password
     * @return AuthResponse with access_token, refresh_token, and user
     */
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest request);

    /**
     * SIGNUP (Register new user)
     * POST /auth/v1/signup
     * Headers required: apikey (added by AuthInterceptor)
     * Note: User must verify email before login
     *
     * @param request RegisterRequest with email and password
     * @return User object (not authenticated yet - email verification required)
     */
    @POST("auth/v1/signup")
    Call<User> signup(@Body RegisterRequest request);

    /**
     * PASSWORD RESET REQUEST
     * POST /auth/v1/recover
     * Sends password reset email to user
     *
     * @param request JsonObject with "email" field
     * @return Void (success returns 200 OK)
     */
    @POST("auth/v1/recover")
    Call<Void> recover(@Body JsonObject request);

    /**
     * GET CURRENT USER
     * GET /auth/v1/user
     * Headers required: apikey + Authorization (Bearer token)
     *
     * @return Current authenticated user
     */
    @GET("auth/v1/user")
    Call<User> getUser();

    /**
     * UPDATE PASSWORD
     * PUT /auth/v1/user
     * Headers required: apikey + Authorization (Bearer token)
     *
     * @param request JsonObject with "password" field
     * @return Updated user
     */
    @PUT("auth/v1/user")
    Call<User> updatePassword(@Body JsonObject request);
}
```

### 2. Supabase REST API (PostgREST)

```java
// data/remote/SupabaseRestApi.java
package com.example.shopverse_admin_app.data.remote;

import com.example.shopverse_admin_app.data.model.Brand;
import com.example.shopverse_admin_app.data.model.Category;
import com.example.shopverse_admin_app.data.model.Product;
import com.example.shopverse_admin_app.data.model.Profile;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * Supabase PostgREST API endpoints
 * Base URL: {SUPABASE_URL}/rest/v1/
 *
 * PostgREST Query Operators:
 * - eq.value       -> Equals
 * - gt.value       -> Greater than
 * - lt.value       -> Less than
 * - gte.value      -> Greater than or equal
 * - lte.value      -> Less than or equal
 * - neq.value      -> Not equal
 * - like.pattern   -> Pattern matching
 * - ilike.pattern  -> Case-insensitive pattern matching
 * - in.(a,b,c)     -> In list
 */
public interface SupabaseRestApi {

    // ========== CATEGORIES ==========

    /**
     * GET ALL CATEGORIES
     * GET /rest/v1/categories?select=*
     *
     * @param select Fields to select (* for all)
     * @return List of categories
     */
    @GET("rest/v1/categories")
    Call<List<Category>> getCategories(@Query("select") String select);

    /**
     * GET CATEGORY BY ID
     * GET /rest/v1/categories?select=*&category_id=eq.{id}
     *
     * @param select Fields to select
     * @param categoryId Filter: eq.{uuid}
     * @return List with single category
     */
    @GET("rest/v1/categories")
    Call<List<Category>> getCategoryById(
            @Query("select") String select,
            @Query("category_id") String categoryId
    );

    /**
     * CREATE CATEGORY
     * POST /rest/v1/categories
     *
     * @param category Category object
     * @return Created category
     */
    @POST("rest/v1/categories")
    Call<Category> createCategory(@Body Category category);

    /**
     * UPDATE CATEGORY
     * PATCH /rest/v1/categories?category_id=eq.{id}
     *
     * @param categoryId Filter: eq.{uuid}
     * @param category Updated category data
     * @return List of updated categories
     */
    @PATCH("rest/v1/categories")
    Call<List<Category>> updateCategory(
            @Query("category_id") String categoryId,
            @Body Category category
    );

    /**
     * DELETE CATEGORY
     * DELETE /rest/v1/categories?category_id=eq.{id}
     *
     * @param categoryId Filter: eq.{uuid}
     * @return Void
     */
    @DELETE("rest/v1/categories")
    Call<Void> deleteCategory(@Query("category_id") String categoryId);

    // ========== BRANDS ==========

    /**
     * GET ALL BRANDS
     * GET /rest/v1/brands?select=*
     */
    @GET("rest/v1/brands")
    Call<List<Brand>> getBrands(@Query("select") String select);

    /**
     * GET BRAND BY ID
     * GET /rest/v1/brands?select=*&brand_id=eq.{id}
     */
    @GET("rest/v1/brands")
    Call<List<Brand>> getBrandById(
            @Query("select") String select,
            @Query("brand_id") String brandId
    );

    /**
     * CREATE BRAND
     * POST /rest/v1/brands
     */
    @POST("rest/v1/brands")
    Call<Brand> createBrand(@Body Brand brand);

    /**
     * UPDATE BRAND
     * PATCH /rest/v1/brands?brand_id=eq.{id}
     */
    @PATCH("rest/v1/brands")
    Call<List<Brand>> updateBrand(
            @Query("brand_id") String brandId,
            @Body Brand brand
    );

    /**
     * DELETE BRAND
     * DELETE /rest/v1/brands?brand_id=eq.{id}
     */
    @DELETE("rest/v1/brands")
    Call<Void> deleteBrand(@Query("brand_id") String brandId);

    // ========== PRODUCTS ==========

    /**
     * GET ALL PRODUCTS (with optional filters)
     * GET /rest/v1/products?select=*,brands(*),categories(*)
     *
     * Example with filters:
     * - category_id=eq.{uuid}
     * - brand_id=eq.{uuid}
     * - status=eq.active
     * - order=unit_price.asc
     *
     * @param select Fields to select (supports joins: *,brands(*),categories(*))
     * @param categoryId Optional category filter
     * @param brandId Optional brand filter
     * @param statusFilter Optional status filter
     * @param order Optional sort order (e.g., "unit_price.asc", "product_name.desc")
     * @return List of products
     */
    @GET("rest/v1/products")
    Call<List<Product>> getProducts(
            @Query("select") String select,
            @Query("category_id") String categoryId,
            @Query("brand_id") String brandId,
            @Query("status") String statusFilter,
            @Query("order") String order
    );

    /**
     * GET PRODUCT BY ID
     * GET /rest/v1/products?select=*,brands(*),categories(*)&product_id=eq.{id}
     */
    @GET("rest/v1/products")
    Call<List<Product>> getProductById(
            @Query("select") String select,
            @Query("product_id") String productId
    );

    /**
     * CREATE PRODUCT
     * POST /rest/v1/products
     */
    @POST("rest/v1/products")
    Call<Product> createProduct(@Body Product product);

    /**
     * UPDATE PRODUCT
     * PATCH /rest/v1/products?product_id=eq.{id}
     */
    @PATCH("rest/v1/products")
    Call<List<Product>> updateProduct(
            @Query("product_id") String productId,
            @Body Product product
    );

    /**
     * DELETE PRODUCT
     * DELETE /rest/v1/products?product_id=eq.{id}
     */
    @DELETE("rest/v1/products")
    Call<Void> deleteProduct(@Query("product_id") String productId);

    // ========== PROFILES ==========

    /**
     * GET PROFILES (with optional filter)
     * GET /rest/v1/profiles?select=*&user_id=eq.{id}
     */
    @GET("rest/v1/profiles")
    Call<List<Profile>> getProfiles(
            @Query("select") String select,
            @Query("user_id") String userIdFilter
    );

    /**
     * CREATE PROFILE
     * POST /rest/v1/profiles
     */
    @POST("rest/v1/profiles")
    Call<Profile> createProfile(@Body Profile profile);

    /**
     * UPDATE PROFILE
     * PATCH /rest/v1/profiles?user_id=eq.{id}
     */
    @PATCH("rest/v1/profiles")
    Call<List<Profile>> updateProfile(
            @Query("user_id") String userIdFilter,
            @Body Profile profile
    );

    /**
     * DELETE PROFILE
     * DELETE /rest/v1/profiles?user_id=eq.{id}
     */
    @DELETE("rest/v1/profiles")
    Call<Void> deleteProfile(@Query("user_id") String userIdFilter);

    // ========== CATEGORIES_BRANDS (Junction Table) ==========

    /**
     * GET BRANDS FOR CATEGORY (using join query)
     * GET /rest/v1/categories_brands?select=brands(*)&category_id=eq.{id}
     *
     * This endpoint uses PostgREST's relationship syntax to join tables
     */
    @GET("rest/v1/categories_brands")
    Call<List<BrandResponse>> getBrandsByCategory(
            @Query("select") String select,
            @Query("category_id") String categoryIdFilter
    );

    /**
     * Wrapper class for nested brand response from join query
     */
    class BrandResponse {
        @com.google.gson.annotations.SerializedName("brands")
        public Brand brand;
    }
}
```

### 3. PostgREST Query Examples

#### Simple Queries

```java
// Get all categories
restApi.getCategories("*");

// Get category by ID
restApi.getCategoryById("*", "eq.123e4567-e89b-12d3-a456-426614174000");

// Get all active products
restApi.getProducts("*", null, null, "eq.active", null);
```

#### Filtered Queries

```java
// Get products by category
String categoryFilter = "eq." + categoryId;
restApi.getProducts("*", categoryFilter, null, "eq.active", null);

// Get products by category and brand
String categoryFilter = "eq." + categoryId;
String brandFilter = "eq." + brandId;
restApi.getProducts("*", categoryFilter, brandFilter, "eq.active", null);
```

#### Sorted Queries

```java
// Get products sorted by price ascending
restApi.getProducts("*", null, null, "eq.active", "unit_price.asc");

// Get products sorted by name descending
restApi.getProducts("*", null, null, "eq.active", "product_name.desc");
```

#### Joined Queries

```java
// Get products with brand and category data
String select = "*,brands(*),categories(*)";
restApi.getProducts(select, null, null, "eq.active", null);

// Result will include nested objects:
// product.getBrand().getBrandName()
// product.getCategory().getCategoryName()
```

---

## Authentication Implementation

### 1. TokenManager (Secure Token Storage)

```java
// utils/TokenManager.java
package com.example.shopverse_admin_app.utils;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Secure token storage using EncryptedSharedPreferences
 * Encryption: AES256_GCM for values, AES256_SIV for keys
 */
public class TokenManager {

    private static final String PREFS_NAME = "shopverse_auth_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";

    private final SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        try {
            // Create MasterKey for encryption
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            // Initialize EncryptedSharedPreferences
            sharedPreferences = EncryptedSharedPreferences.create(
                    context,
                    PREFS_NAME,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException("Failed to initialize EncryptedSharedPreferences", e);
        }
    }

    /**
     * Save complete auth session
     */
    public void saveAuthSession(String accessToken, String refreshToken,
                                 String userId, String email, long expiresIn) {
        long expiryTimestamp = System.currentTimeMillis() + (expiresIn * 1000);

        sharedPreferences.edit()
                .putString(KEY_ACCESS_TOKEN, accessToken)
                .putString(KEY_REFRESH_TOKEN, refreshToken)
                .putString(KEY_USER_ID, userId)
                .putString(KEY_USER_EMAIL, email)
                .putLong(KEY_TOKEN_EXPIRY, expiryTimestamp)
                .apply();
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return sharedPreferences.getString(KEY_REFRESH_TOKEN, null);
    }

    public String getUserId() {
        return sharedPreferences.getString(KEY_USER_ID, null);
    }

    public String getUserEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, null);
    }

    public long getTokenExpiry() {
        return sharedPreferences.getLong(KEY_TOKEN_EXPIRY, 0);
    }

    public boolean isLoggedIn() {
        String accessToken = getAccessToken();
        return accessToken != null && !accessToken.isEmpty();
    }

    public boolean isTokenExpired() {
        long expiry = getTokenExpiry();
        return expiry > 0 && System.currentTimeMillis() >= expiry;
    }

    public void clearTokens() {
        sharedPreferences.edit()
                .remove(KEY_ACCESS_TOKEN)
                .remove(KEY_REFRESH_TOKEN)
                .remove(KEY_USER_ID)
                .remove(KEY_USER_EMAIL)
                .remove(KEY_TOKEN_EXPIRY)
                .apply();
    }

    public String getAuthorizationHeader() {
        String token = getAccessToken();
        return (token != null && !token.isEmpty()) ? "Bearer " + token : null;
    }
}
```

### 2. AuthRepository

```java
// data/repository/AuthRepository.java
package com.example.shopverse_admin_app.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.shopverse_admin_app.data.model.AuthResponse;
import com.example.shopverse_admin_app.data.model.LoginRequest;
import com.example.shopverse_admin_app.data.model.RegisterRequest;
import com.example.shopverse_admin_app.data.model.User;
import com.example.shopverse_admin_app.data.remote.RetrofitClient;
import com.example.shopverse_admin_app.data.remote.SupabaseAuthApi;
import com.example.shopverse_admin_app.utils.ErrorParser;
import com.example.shopverse_admin_app.utils.TokenManager;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    private static final String TAG = "AuthRepository";

    private final SupabaseAuthApi authApi;
    private final TokenManager tokenManager;
    private final RetrofitClient retrofitClient;

    public AuthRepository(Context context) {
        this.retrofitClient = RetrofitClient.getInstance();
        this.authApi = retrofitClient.getAuthApi();
        this.tokenManager = new TokenManager(context);
        loadSavedToken();
    }

    private void loadSavedToken() {
        String savedToken = tokenManager.getAccessToken();
        if (savedToken != null && !savedToken.isEmpty()) {
            retrofitClient.setAccessToken(savedToken);
            Log.d(TAG, "Loaded saved access token");
        }
    }

    /**
     * Login user
     */
    public void login(String email, String password, AuthCallback callback) {
        LoginRequest request = new LoginRequest(email, password);

        authApi.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(@NonNull Call<AuthResponse> call,
                                   @NonNull Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    AuthResponse authResponse = response.body();
                    saveAuthSession(authResponse);
                    callback.onSuccess(authResponse);
                    Log.d(TAG, "Login successful");
                } else {
                    String errorMsg = ErrorParser.parseError(response);
                    callback.onError(errorMsg);
                    Log.e(TAG, "Login failed: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AuthResponse> call, @NonNull Throwable t) {
                String errorMsg = ErrorParser.parseError(t);
                callback.onError(errorMsg);
                Log.e(TAG, "Login network error", t);
            }
        });
    }

    /**
     * Register new user
     */
    public void register(String email, String password, SimpleCallback callback) {
        RegisterRequest request = new RegisterRequest(email, password);

        authApi.signup(request).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess();
                    Log.d(TAG, "Registration successful - email verification required");
                } else {
                    String errorMsg = ErrorParser.parseError(response);
                    callback.onError(errorMsg);
                    Log.e(TAG, "Registration failed: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                String errorMsg = ErrorParser.parseError(t);
                callback.onError(errorMsg);
                Log.e(TAG, "Registration network error", t);
            }
        });
    }

    /**
     * Logout user
     */
    public void logout(SimpleCallback callback) {
        tokenManager.clearTokens();
        retrofitClient.clearAccessToken();
        callback.onSuccess();
        Log.d(TAG, "Logout successful");
    }

    /**
     * Request password reset
     */
    public void requestPasswordReset(String email, SimpleCallback callback) {
        JsonObject request = new JsonObject();
        request.addProperty("email", email);

        authApi.recover(request).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                    Log.d(TAG, "Password reset email sent");
                } else {
                    String errorMsg = ErrorParser.parseError(response);
                    callback.onError(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onError(ErrorParser.parseError(t));
            }
        });
    }

    public boolean isLoggedIn() {
        return tokenManager.isLoggedIn();
    }

    public String getCurrentUserId() {
        return tokenManager.getUserId();
    }

    private void saveAuthSession(AuthResponse authResponse) {
        tokenManager.saveAuthSession(
                authResponse.getAccessToken(),
                authResponse.getRefreshToken(),
                authResponse.getUser().getId(),
                authResponse.getUser().getEmail(),
                authResponse.getExpiresIn()
        );
        retrofitClient.setAccessToken(authResponse.getAccessToken());
    }

    // Callback interfaces
    public interface AuthCallback {
        void onSuccess(AuthResponse response);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
}
```

### 3. AuthViewModel

```java
// viewmodel/AuthViewModel.java
package com.example.shopverse_admin_app.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.shopverse_admin_app.data.model.AuthResponse;
import com.example.shopverse_admin_app.data.repository.AuthRepository;

public class AuthViewModel extends AndroidViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> registerSuccess = new MutableLiveData<>();
    private final MutableLiveData<Boolean> logoutSuccess = new MutableLiveData<>();
    private final MutableLiveData<AuthResponse> authResponse = new MutableLiveData<>();

    public AuthViewModel(@NonNull Application application) {
        super(application);
        authRepository = new AuthRepository(application);
    }

    public void login(String email, String password) {
        isLoading.setValue(true);
        authRepository.login(email, password, new AuthRepository.AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                isLoading.postValue(false);
                authResponse.postValue(response);
                loginSuccess.postValue(true);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
                loginSuccess.postValue(false);
            }
        });
    }

    public void logout() {
        isLoading.setValue(true);
        authRepository.logout(new AuthRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                isLoading.postValue(false);
                logoutSuccess.postValue(true);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                logoutSuccess.postValue(true); // Logout locally anyway
            }
        });
    }

    public boolean isLoggedIn() {
        return authRepository.isLoggedIn();
    }

    public String getCurrentUserId() {
        return authRepository.getCurrentUserId();
    }

    // LiveData getters
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
    public LiveData<Boolean> getLogoutSuccess() { return logoutSuccess; }
    public LiveData<AuthResponse> getAuthResponse() { return authResponse; }
}
```

---

## CRUD Operations Guide

### 1. Category CRUD Operations

#### CategoryRepository.java

```java
// data/repository/CategoryRepository.java
package com.example.shopverse_admin_app.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.shopverse_admin_app.data.model.Category;
import com.example.shopverse_admin_app.data.remote.RetrofitClient;
import com.example.shopverse_admin_app.data.remote.SupabaseRestApi;
import com.example.shopverse_admin_app.utils.ErrorParser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryRepository {

    private static final String TAG = "CategoryRepository";
    private final SupabaseRestApi restApi;

    public CategoryRepository() {
        this.restApi = RetrofitClient.getInstance().getRestApi();
    }

    /**
     * CREATE - Add new category
     */
    public void createCategory(Category category, CategoryCallback callback) {
        restApi.createCategory(category).enqueue(new Callback<Category>() {
            @Override
            public void onResponse(@NonNull Call<Category> call,
                                   @NonNull Response<Category> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    Log.d(TAG, "Category created: " + response.body().getCategoryName());
                } else {
                    String error = ErrorParser.parseError(response);
                    callback.onError(error);
                    Log.e(TAG, "Create failed: " + error);
                }
            }

            @Override
            public void onFailure(@NonNull Call<Category> call, @NonNull Throwable t) {
                String error = ErrorParser.parseError(t);
                callback.onError(error);
                Log.e(TAG, "Create network error", t);
            }
        });
    }

    /**
     * READ - Get all categories
     */
    public void getAllCategories(CategoriesCallback callback) {
        restApi.getCategories("*").enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call,
                                   @NonNull Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    Log.d(TAG, "Loaded " + response.body().size() + " categories");
                } else {
                    callback.onError(ErrorParser.parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                callback.onError(ErrorParser.parseError(t));
            }
        });
    }

    /**
     * READ - Get category by ID
     */
    public void getCategoryById(String categoryId, CategoryCallback callback) {
        String filter = "eq." + categoryId;
        restApi.getCategoryById("*", filter).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call,
                                   @NonNull Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null
                        && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Category not found");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                callback.onError(ErrorParser.parseError(t));
            }
        });
    }

    /**
     * UPDATE - Update existing category
     */
    public void updateCategory(String categoryId, Category category, CategoryCallback callback) {
        String filter = "eq." + categoryId;
        restApi.updateCategory(filter, category).enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call,
                                   @NonNull Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null
                        && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                    Log.d(TAG, "Category updated successfully");
                } else {
                    callback.onError(ErrorParser.parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                callback.onError(ErrorParser.parseError(t));
            }
        });
    }

    /**
     * DELETE - Delete category
     */
    public void deleteCategory(String categoryId, SimpleCallback callback) {
        String filter = "eq." + categoryId;
        restApi.deleteCategory(filter).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                    Log.d(TAG, "Category deleted successfully");
                } else {
                    callback.onError(ErrorParser.parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onError(ErrorParser.parseError(t));
            }
        });
    }

    // Callback interfaces
    public interface CategoryCallback {
        void onSuccess(Category category);
        void onError(String error);
    }

    public interface CategoriesCallback {
        void onSuccess(List<Category> categories);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
}
```

#### CategoryViewModel.java

```java
// ui/category/CategoryViewModel.java
package com.example.shopverse_admin_app.ui.category;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.shopverse_admin_app.data.model.Category;
import com.example.shopverse_admin_app.data.repository.CategoryRepository;

import java.util.List;

public class CategoryViewModel extends ViewModel {

    private final CategoryRepository repository;

    private final MutableLiveData<List<Category>> categories = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> operationSuccess = new MutableLiveData<>();

    public CategoryViewModel() {
        repository = new CategoryRepository();
        loadCategories();
    }

    public void loadCategories() {
        loading.setValue(true);
        repository.getAllCategories(new CategoryRepository.CategoriesCallback() {
            @Override
            public void onSuccess(List<Category> categoryList) {
                loading.postValue(false);
                categories.postValue(categoryList);
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
            }
        });
    }

    public void createCategory(String categoryName) {
        loading.setValue(true);
        Category newCategory = new Category();
        newCategory.setCategoryName(categoryName);

        repository.createCategory(newCategory, new CategoryRepository.CategoryCallback() {
            @Override
            public void onSuccess(Category category) {
                loading.postValue(false);
                operationSuccess.postValue(true);
                loadCategories(); // Refresh list
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
                operationSuccess.postValue(false);
            }
        });
    }

    public void updateCategory(String categoryId, String categoryName) {
        loading.setValue(true);
        Category updatedCategory = new Category();
        updatedCategory.setCategoryName(categoryName);

        repository.updateCategory(categoryId, updatedCategory,
                new CategoryRepository.CategoryCallback() {
            @Override
            public void onSuccess(Category category) {
                loading.postValue(false);
                operationSuccess.postValue(true);
                loadCategories(); // Refresh list
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
                operationSuccess.postValue(false);
            }
        });
    }

    public void deleteCategory(String categoryId) {
        loading.setValue(true);
        repository.deleteCategory(categoryId, new CategoryRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                loading.postValue(false);
                operationSuccess.postValue(true);
                loadCategories(); // Refresh list
            }

            @Override
            public void onError(String errorMsg) {
                loading.postValue(false);
                error.postValue(errorMsg);
                operationSuccess.postValue(false);
            }
        });
    }

    // LiveData getters
    public LiveData<List<Category>> getCategories() { return categories; }
    public LiveData<Boolean> getLoading() { return loading; }
    public LiveData<String> getError() { return error; }
    public LiveData<Boolean> getOperationSuccess() { return operationSuccess; }
}
```

### 2. Product CRUD Operations

#### ProductRepository.java

```java
// data/repository/ProductRepository.java
package com.example.shopverse_admin_app.data.repository;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.shopverse_admin_app.data.model.Product;
import com.example.shopverse_admin_app.data.remote.RetrofitClient;
import com.example.shopverse_admin_app.data.remote.SupabaseRestApi;
import com.example.shopverse_admin_app.utils.ErrorParser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductRepository {

    private static final String TAG = "ProductRepository";
    private final SupabaseRestApi restApi;

    public ProductRepository() {
        this.restApi = RetrofitClient.getInstance().getRestApi();
    }

    /**
     * CREATE - Add new product
     */
    public void createProduct(Product product, ProductCallback callback) {
        restApi.createProduct(product).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call,
                                   @NonNull Response<Product> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    Log.d(TAG, "Product created: " + response.body().getProductName());
                } else {
                    callback.onError(ErrorParser.parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                callback.onError(ErrorParser.parseError(t));
            }
        });
    }

    /**
     * READ - Get all products (with joins)
     */
    public void getAllProducts(ProductsCallback callback) {
        String select = "*,brands(*),categories(*)";
        restApi.getProducts(select, null, null, null, null)
                .enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call,
                                   @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                    Log.d(TAG, "Loaded " + response.body().size() + " products");
                } else {
                    callback.onError(ErrorParser.parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                callback.onError(ErrorParser.parseError(t));
            }
        });
    }

    /**
     * READ - Get products by category
     */
    public void getProductsByCategory(String categoryId, ProductsCallback callback) {
        String select = "*,brands(*),categories(*)";
        String categoryFilter = "eq." + categoryId;

        restApi.getProducts(select, categoryFilter, null, null, null)
                .enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call,
                                   @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(ErrorParser.parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                callback.onError(ErrorParser.parseError(t));
            }
        });
    }

    /**
     * READ - Get product by ID
     */
    public void getProductById(String productId, ProductCallback callback) {
        String select = "*,brands(*),categories(*)";
        String filter = "eq." + productId;

        restApi.getProductById(select, filter).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call,
                                   @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null
                        && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                } else {
                    callback.onError("Product not found");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                callback.onError(ErrorParser.parseError(t));
            }
        });
    }

    /**
     * UPDATE - Update existing product
     */
    public void updateProduct(String productId, Product product, ProductCallback callback) {
        String filter = "eq." + productId;
        restApi.updateProduct(filter, product).enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call,
                                   @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null
                        && !response.body().isEmpty()) {
                    callback.onSuccess(response.body().get(0));
                    Log.d(TAG, "Product updated successfully");
                } else {
                    callback.onError(ErrorParser.parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                callback.onError(ErrorParser.parseError(t));
            }
        });
    }

    /**
     * DELETE - Delete product
     */
    public void deleteProduct(String productId, SimpleCallback callback) {
        String filter = "eq." + productId;
        restApi.deleteProduct(filter).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess();
                    Log.d(TAG, "Product deleted successfully");
                } else {
                    callback.onError(ErrorParser.parseError(response));
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                callback.onError(ErrorParser.parseError(t));
            }
        });
    }

    // Callback interfaces
    public interface ProductCallback {
        void onSuccess(Product product);
        void onError(String error);
    }

    public interface ProductsCallback {
        void onSuccess(List<Product> products);
        void onError(String error);
    }

    public interface SimpleCallback {
        void onSuccess();
        void onError(String error);
    }
}
```

### 3. Error Handling Utility

```java
// utils/ErrorParser.java
package com.example.shopverse_admin_app.utils;

import android.util.Log;

import com.example.shopverse_admin_app.data.model.ApiError;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;

import retrofit2.Response;

public class ErrorParser {

    private static final String TAG = "ErrorParser";
    private static final Gson gson = new Gson();

    /**
     * Parse HTTP error response
     */
    public static String parseError(Response<?> response) {
        if (response == null) {
            return "Unknown error occurred";
        }

        try {
            if (response.errorBody() != null) {
                String errorBody = response.errorBody().string();

                try {
                    ApiError apiError = gson.fromJson(errorBody, ApiError.class);
                    if (apiError != null && apiError.getMessage() != null) {
                        return apiError.getMessage();
                    }
                } catch (JsonSyntaxException e) {
                    return errorBody;
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading error body", e);
        }

        return getDefaultErrorMessage(response.code());
    }

    /**
     * Parse network/throwable errors
     */
    public static String parseError(Throwable throwable) {
        if (throwable instanceof IOException) {
            return "Network connection error. Please check your internet connection.";
        }
        return throwable.getMessage() != null ? throwable.getMessage() : "Unknown error occurred";
    }

    /**
     * Get default error messages for HTTP status codes
     */
    private static String getDefaultErrorMessage(int statusCode) {
        switch (statusCode) {
            case 400: return "Invalid request";
            case 401: return "Not authenticated. Please log in again";
            case 403: return "You don't have permission to perform this action";
            case 404: return "Resource not found";
            case 409: return "Data already exists";
            case 422: return "Invalid data";
            case 429: return "Too many requests. Please try again later";
            case 500: return "Server error. Please try again later";
            case 502: return "Server connection error";
            case 503: return "Service temporarily unavailable";
            default: return "Error (Code: " + statusCode + ")";
        }
    }
}
```

---

## Project Setup for Admin App

### 1. Create New Android Project

```
App Name: Shopverse Admin
Package Name: com.example.shopverse_admin_app
Language: Java
Minimum SDK: API 24 (Android 7.0)
```

### 2. Copy Shared Code from Customer App

**Files to copy directly:**

```
config/
├── SupabaseConfig.java

data/model/
├── User.java
├── Profile.java
├── AuthResponse.java
├── LoginRequest.java
├── RegisterRequest.java
├── Category.java
├── Brand.java
├── Product.java
└── ApiError.java

data/remote/
├── RetrofitClient.java
├── AuthInterceptor.java
├── SupabaseAuthApi.java
└── SupabaseRestApi.java (extend with admin endpoints)

data/repository/
├── AuthRepository.java

utils/
├── TokenManager.java
├── ErrorParser.java
└── ValidationUtils.java

viewmodel/
└── AuthViewModel.java
```

### 3. Add Admin-Specific Features

**New repositories:**
- `CategoryRepository.java`
- `BrandRepository.java`
- `ProductRepository.java`
- `UserManagementRepository.java`

**New UI screens:**
- Dashboard (stats overview)
- Category management (list, create, edit, delete)
- Brand management (list, create, edit, delete)
- Product management (list, create, edit, delete)
- User management (list, view, update roles)
- Order management
- Analytics/Reports

### 4. Row Level Security (RLS) Policies

**IMPORTANT:** Configure Supabase RLS policies to restrict admin operations.

Example RLS policy for `products` table:

```sql
-- Allow admins to do everything
CREATE POLICY "Admins can do anything on products"
ON products
FOR ALL
TO authenticated
USING (
  EXISTS (
    SELECT 1 FROM profiles
    WHERE profiles.user_id = auth.uid()
    AND profiles.role = 'admin'
  )
);

-- Allow staff to read and update
CREATE POLICY "Staff can read and update products"
ON products
FOR SELECT, UPDATE
TO authenticated
USING (
  EXISTS (
    SELECT 1 FROM profiles
    WHERE profiles.user_id = auth.uid()
    AND profiles.role IN ('admin', 'staff')
  )
);

-- Customers can only read active products
CREATE POLICY "Customers can read active products"
ON products
FOR SELECT
TO authenticated
USING (status = 'active');
```

Apply similar policies to all tables.

### 5. Admin Role Check

Add role checking in your admin app:

```java
// utils/RoleChecker.java
public class RoleChecker {

    public static boolean isAdmin(Profile profile) {
        return profile != null && "admin".equalsIgnoreCase(profile.getRole());
    }

    public static boolean isStaff(Profile profile) {
        return profile != null &&
               ("admin".equalsIgnoreCase(profile.getRole()) ||
                "staff".equalsIgnoreCase(profile.getRole()));
    }
}
```

---

## Common Patterns & Best Practices

### 1. Error Handling Pattern

Always handle errors consistently:

```java
repository.getData(new Repository.Callback() {
    @Override
    public void onSuccess(Data data) {
        loading.postValue(false);
        result.postValue(data);
    }

    @Override
    public void onError(String error) {
        loading.postValue(false);
        errorMessage.postValue(error);
        Log.e(TAG, "Operation failed: " + error);
    }
});
```

### 2. Loading State Pattern

Always show loading indicators:

```java
viewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
    if (isLoading) {
        progressBar.setVisibility(View.VISIBLE);
        contentView.setVisibility(View.GONE);
    } else {
        progressBar.setVisibility(View.GONE);
        contentView.setVisibility(View.VISIBLE);
    }
});
```

### 3. Null Safety Pattern

Always check for null:

```java
if (response.isSuccessful() && response.body() != null) {
    // Process data
} else {
    // Handle error
}
```

### 4. Retrofit Callback Pattern

Use consistent callback structure:

```java
call.enqueue(new Callback<T>() {
    @Override
    public void onResponse(@NonNull Call<T> call, @NonNull Response<T> response) {
        if (response.isSuccessful() && response.body() != null) {
            // Success
        } else {
            // HTTP error
        }
    }

    @Override
    public void onFailure(@NonNull Call<T> call, @NonNull Throwable t) {
        // Network error
    }
});
```

### 5. Logging Best Practices

```java
// Use appropriate log levels
Log.d(TAG, "Debug info: " + data);           // Development
Log.i(TAG, "Info: User logged in");          // General info
Log.w(TAG, "Warning: Token expiring soon");  // Warnings
Log.e(TAG, "Error: API call failed", error); // Errors
```

### 6. Repository Pattern

Keep business logic in repositories:

```java
// ✅ GOOD: Business logic in repository
public void getActiveProducts(ProductsCallback callback) {
    restApi.getProducts("*", null, null, "eq.active", null)
        .enqueue(/* ... */);
}

// ❌ BAD: Business logic in ViewModel
public void loadProducts() {
    restApi.getProducts("*", null, null, "eq.active", null)
        .enqueue(/* ... */);
}
```

### 7. LiveData Observer Pattern

Observe LiveData in lifecycle-aware components:

```java
// ✅ GOOD: Use getViewLifecycleOwner() in Fragments
viewModel.getData().observe(getViewLifecycleOwner(), data -> {
    // Update UI
});

// ❌ BAD: Use 'this' in Fragments (causes memory leaks)
viewModel.getData().observe(this, data -> {
    // Update UI
});
```

---

## Summary & Next Steps

### What You Have Learned

1. **Architecture**: MVVM + Clean Architecture pattern
2. **Supabase Setup**: Configuration, API keys, Retrofit client
3. **Data Models**: All entities with proper serialization
4. **API Integration**: Auth and REST APIs with PostgREST
5. **Authentication**: Secure token management
6. **CRUD Operations**: Complete examples for all entities
7. **Code Style**: Naming conventions and file organization

### Building the Admin App

**Step-by-step process:**

1. Create new Android project
2. Copy shared code from customer app
3. Add admin-specific dependencies
4. Configure Supabase with same credentials
5. Set up RLS policies for admin/staff roles
6. Create admin UI screens (dashboard, CRUD interfaces)
7. Implement role-based access control
8. Test all CRUD operations
9. Add analytics and reporting

### Key Files Reference

| File | Location |
|------|----------|
| Supabase Config | `config/SupabaseConfig.java` |
| Retrofit Client | `data/remote/RetrofitClient.java` |
| Auth API | `data/remote/SupabaseAuthApi.java` |
| REST API | `data/remote/SupabaseRestApi.java` |
| Product Model | `data/model/Product.java` |
| Auth Repository | `data/repository/AuthRepository.java` |
| Token Manager | `utils/TokenManager.java` |
| Error Parser | `utils/ErrorParser.java` |

### Additional Resources

- **Customer App Docs**: `ARCHITECTURE.md`, `AUTH_MODULE_README.md`
- **Supabase Docs**: https://supabase.com/docs
- **PostgREST API Docs**: https://postgrest.org/
- **Retrofit Docs**: https://square.github.io/retrofit/

---

**Good luck building your admin app!** 🚀
