# Shopverse Android App - Architecture Document

## Overview

Shopverse is an Android e-commerce application built with **Java** following the **MVVM (Model-View-ViewModel)** architecture pattern combined with **Clean Architecture** principles. The app uses **Supabase** as the backend service for authentication, database, and storage.

## Architecture Pattern: MVVM + Clean Architecture

### Why MVVM?

1. **Separation of Concerns** - Clear separation between UI, business logic, and data layers
2. **Testability** - Each layer can be tested independently
3. **Maintainability** - Easy to modify and extend features
4. **Lifecycle Awareness** - ViewModel survives configuration changes
5. **Reactive UI** - LiveData provides automatic UI updates
6. **Android Recommended** - Official Android architecture pattern

---

## 📐 Architecture Layers

```
┌─────────────────────────────────────────────────────────────┐
│                        UI LAYER                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │           Activities / Fragments                     │   │
│  │  - LoginActivity, RegisterActivity                   │   │
│  │  - HomeFragment, CategoryFragment                    │   │
│  │  - CartActivity, ProfileActivity                     │   │
│  │  - Observes ViewModel LiveData                       │   │
│  │  - Updates UI based on state changes                 │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                     VIEWMODEL LAYER                         │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              ViewModels                              │   │
│  │  - AuthViewModel                                     │   │
│  │  - ProductViewModel, CartViewModel                   │   │
│  │  - ProfileViewModel                                  │   │
│  │                                                       │   │
│  │  Responsibilities:                                   │   │
│  │  • Holds UI state (LiveData)                         │   │
│  │  • Handles UI logic                                  │   │
│  │  • Communicates with Repository                      │   │
│  │  • Survives configuration changes                    │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                   REPOSITORY LAYER                          │
│  ┌─────────────────────────────────────────────────────┐   │
│  │             Repositories                             │   │
│  │  - AuthRepository                                    │   │
│  │  - ProductRepository                                 │   │
│  │  - CartRepository                                    │   │
│  │  - ProfileRepository                                 │   │
│  │                                                       │   │
│  │  Responsibilities:                                   │   │
│  │  • Single source of truth                            │   │
│  │  • Mediates between remote and local data            │   │
│  │  • Implements business logic                         │   │
│  │  • Handles data caching strategy                     │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
                            ↕
┌─────────────────────────────────────────────────────────────┐
│                    DATA SOURCE LAYER                        │
│  ┌──────────────────────┐      ┌──────────────────────┐    │
│  │   Remote Data        │      │    Local Data        │    │
│  │   (Supabase)         │      │    (Room DB)         │    │
│  │                      │      │                      │    │
│  │  - SupabaseAuthApi   │      │  - ProductDao        │    │
│  │  - SupabaseRestApi   │      │  - CartDao           │    │
│  │  - RetrofitClient    │      │  - Database          │    │
│  │  - AuthInterceptor   │      │                      │    │
│  └──────────────────────┘      └──────────────────────┘    │
│                                                             │
│  ┌─────────────────────────────────────────────────────┐   │
│  │              Data Models                             │   │
│  │  - User, Profile, Product, CartItem                  │   │
│  │  - Order, OrderItem, Category, Brand                 │   │
│  └─────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🏗️ Detailed Layer Breakdown

### 1. UI Layer (View)

**Components:**
- Activities
- Fragments
- XML Layouts
- Adapters (RecyclerView)
- Custom Views

**Responsibilities:**
- Display data to the user
- Handle user interactions (clicks, input)
- Observe ViewModel LiveData
- Update UI when data changes
- NO business logic

**Example:**
```java
public class LoginActivity extends AppCompatActivity {
    private AuthViewModel authViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get ViewModel instance
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Observe LiveData
        authViewModel.getLoginSuccess().observe(this, success -> {
            if (success) {
                navigateToHome();
            }
        });

        // Handle user action
        btnLogin.setOnClickListener(v -> {
            authViewModel.login(email, password);
        });
    }
}
```

---

### 2. ViewModel Layer

**Components:**
- ViewModels (extends AndroidViewModel or ViewModel)
- LiveData / MutableLiveData
- UI State classes

**Responsibilities:**
- Hold UI-related data that survives configuration changes
- Expose data to UI via LiveData
- Handle UI logic (validation, formatting)
- Call Repository methods
- Transform data for UI consumption
- NO context references (except AndroidViewModel)

**Example:**
```java
public class AuthViewModel extends AndroidViewModel {
    private final AuthRepository authRepository;

    // LiveData for UI states
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>(false);
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loginSuccess = new MutableLiveData<>();

    public void login(String email, String password) {
        // Validate input
        if (email.isEmpty()) {
            errorMessage.setValue("Email is required");
            return;
        }

        // Set loading state
        isLoading.setValue(true);

        // Call repository
        authRepository.login(email, password, new AuthCallback() {
            @Override
            public void onSuccess(AuthResponse response) {
                isLoading.postValue(false);
                loginSuccess.postValue(true);
            }

            @Override
            public void onError(String error) {
                isLoading.postValue(false);
                errorMessage.postValue(error);
            }
        });
    }

    // Getters for LiveData
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }
    public LiveData<Boolean> getLoginSuccess() { return loginSuccess; }
}
```

---

### 3. Repository Layer

**Components:**
- Repository classes
- Data source coordinators
- Business logic

**Responsibilities:**
- Single source of truth for data
- Decide where to fetch data (remote vs local)
- Cache data when appropriate
- Handle data synchronization
- Implement business rules
- Abstract data sources from ViewModel

**Example:**
```java
public class AuthRepository {
    private final SupabaseAuthApi authApi;
    private final TokenManager tokenManager;
    private final RetrofitClient retrofitClient;

    public void login(String email, String password, AuthCallback callback) {
        LoginRequest request = new LoginRequest(email, password);

        authApi.login(request).enqueue(new Callback<AuthResponse>() {
            @Override
            public void onResponse(Call<AuthResponse> call, Response<AuthResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Save tokens
                    saveAuthSession(response.body());
                    callback.onSuccess(response.body());
                } else {
                    callback.onError(parseErrorMessage(response));
                }
            }

            @Override
            public void onFailure(Call<AuthResponse> call, Throwable t) {
                callback.onError("Network error: " + t.getMessage());
            }
        });
    }

    private void saveAuthSession(AuthResponse authResponse) {
        // Save to encrypted storage
        tokenManager.saveAuthSession(...);
        // Set token in interceptor for API calls
        retrofitClient.setAccessToken(authResponse.getAccessToken());
    }
}
```

---

### 4. Data Source Layer

#### 4.1 Remote Data Source (Supabase)

**Components:**
- Retrofit API interfaces
- RetrofitClient
- AuthInterceptor
- Data models (DTOs)

**Responsibilities:**
- Define API endpoints
- Handle HTTP requests/responses
- Manage authentication headers
- Parse JSON responses

**Example:**
```java
public interface SupabaseAuthApi {
    @POST("auth/v1/token?grant_type=password")
    Call<AuthResponse> login(@Body LoginRequest request);

    @POST("auth/v1/signup")
    Call<User> signup(@Body RegisterRequest request);

    @GET("auth/v1/user")
    Call<User> getUser();
}

public class RetrofitClient {
    private final AuthInterceptor authInterceptor;
    private final Retrofit retrofit;

    public void setAccessToken(String token) {
        authInterceptor.setAccessToken(token);
    }
}

public class AuthInterceptor implements Interceptor {
    private volatile String accessToken;

    @Override
    public Response intercept(Chain chain) {
        Request.Builder builder = chain.request().newBuilder()
            .header("apikey", SUPABASE_ANON_KEY)
            .header("Content-Type", "application/json");

        if (accessToken != null) {
            builder.header("Authorization", "Bearer " + accessToken);
        }

        return chain.proceed(builder.build());
    }
}
```

#### 4.2 Local Data Source (Room Database)

**Components:**
- Room Database
- DAO (Data Access Objects)
- Entity classes
- Database migrations

**Responsibilities:**
- Persist data locally
- Cache network data
- Enable offline functionality
- Provide fast data access

**Example:**
```java
@Database(entities = {Product.class, CartItem.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase {
    public abstract ProductDao productDao();
    public abstract CartDao cartDao();

    private static AppDatabase instance;

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                context.getApplicationContext(),
                AppDatabase.class,
                "shopverse_db"
            ).build();
        }
        return instance;
    }
}

@Dao
public interface ProductDao {
    @Query("SELECT * FROM products WHERE status = 'active'")
    List<Product> getAllActiveProducts();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertProducts(List<Product> products);

    @Query("DELETE FROM products")
    void clearAll();
}
```

---

## 🔄 Data Flow

### Example: User Login Flow

```
1. USER ACTION
   ↓
   LoginActivity: btnLogin.onClick()

2. UI → VIEWMODEL
   ↓
   authViewModel.login(email, password)

3. VIEWMODEL → REPOSITORY
   ↓
   authRepository.login(email, password, callback)

4. REPOSITORY → REMOTE DATA SOURCE
   ↓
   authApi.login(request) [Retrofit call]

5. SUPABASE SERVER
   ↓
   Returns AuthResponse with tokens

6. REMOTE → REPOSITORY
   ↓
   Parse response, save tokens to TokenManager
   Set token in AuthInterceptor

7. REPOSITORY → VIEWMODEL
   ↓
   callback.onSuccess(authResponse)

8. VIEWMODEL → UI
   ↓
   loginSuccess.postValue(true)

9. UI UPDATES
   ↓
   Observer receives update → navigate to home
```

---

## 🛠️ Key Technologies

### Core Architecture
- **MVVM Pattern** - Separation of concerns
- **LiveData** - Lifecycle-aware observable data
- **ViewModel** - Survives configuration changes
- **Repository Pattern** - Single source of truth

### Networking
- **Retrofit 2** - HTTP client
- **OkHttp 3** - HTTP/HTTPS operations
- **Gson** - JSON serialization/deserialization

### Database
- **Room** - SQLite object mapping
- **EncryptedSharedPreferences** - Secure token storage

### Dependency Injection (Optional - Future)
- **Hilt** - Recommended DI framework
- **Manual DI** - Current approach (simpler, fewer dependencies)

### UI
- **Material Components** - Material Design UI
- **ViewBinding** - Type-safe view access
- **RecyclerView** - Efficient list display
- **Navigation Component** - Fragment navigation

### Image Loading
- **Glide** - Image loading and caching

### Location
- **Google Maps SDK** - Map display
- **Google Places API** - Location search

---

## 📦 Package Structure

```
com.example.shopverse_customer_app/
├── config/
│   └── SupabaseConfig.java              # API keys and constants
│
├── data/
│   ├── model/                            # Data models (POJOs)
│   │   ├── User.java
│   │   ├── Profile.java
│   │   ├── Product.java
│   │   ├── CartItem.java
│   │   ├── Order.java
│   │   └── AuthResponse.java
│   │
│   ├── remote/                           # Remote data source
│   │   ├── RetrofitClient.java
│   │   ├── AuthInterceptor.java
│   │   ├── SupabaseAuthApi.java
│   │   └── SupabaseRestApi.java
│   │
│   ├── local/                            # Local data source (Room)
│   │   ├── AppDatabase.java
│   │   ├── ProductDao.java
│   │   └── CartDao.java
│   │
│   └── repository/                       # Repository layer
│       ├── AuthRepository.java
│       ├── ProductRepository.java
│       ├── CartRepository.java
│       └── ProfileRepository.java
│
├── ui/                                   # UI layer
│   ├── auth/
│   │   ├── LoginActivity.java
│   │   ├── RegisterActivity.java
│   │   └── ForgotPasswordActivity.java
│   │
│   ├── home/
│   │   ├── HomeFragment.java
│   │   └── ProductDetailActivity.java
│   │
│   ├── category/
│   │   └── CategoryFragment.java
│   │
│   ├── cart/
│   │   ├── CartActivity.java
│   │   └── CheckoutActivity.java
│   │
│   ├── profile/
│   │   ├── ProfileFragment.java
│   │   └── OrderHistoryActivity.java
│   │
│   └── maps/
│       └── MapsActivity.java
│
├── viewmodel/                            # ViewModel layer
│   ├── AuthViewModel.java
│   ├── ProductViewModel.java
│   ├── CartViewModel.java
│   └── ProfileViewModel.java
│
├── utils/                                # Utility classes
│   ├── TokenManager.java
│   ├── ValidationUtils.java
│   └── Constants.java
│
└── MainActivity.java                     # Main entry point
```

---

## 🎯 Architecture Principles

### 1. Separation of Concerns
Each layer has a single, well-defined responsibility:
- **UI** - Display data and handle user input
- **ViewModel** - Prepare data for UI and handle UI logic
- **Repository** - Manage data operations and business logic
- **Data Source** - Provide data from network or database

### 2. Single Source of Truth
- Repository is the single source of truth for data
- UI never directly accesses data sources
- All data flows through Repository → ViewModel → UI

### 3. Unidirectional Data Flow
```
User Action → UI → ViewModel → Repository → Data Source
                ←      ←           ←            ←
             Observe  LiveData   Callback    Response
```

### 4. Dependency Rule
- **Inner layers don't know about outer layers**
- Data Source doesn't know about Repository
- Repository doesn't know about ViewModel
- ViewModel doesn't know about specific UI implementation

### 5. Testability
Each layer can be tested independently:
- **UI Tests** - Espresso, UI Automator
- **ViewModel Tests** - Unit tests with mocked Repository
- **Repository Tests** - Unit tests with mocked API and DAO
- **Integration Tests** - Test multiple layers together

---

## 🔐 Security Considerations

### 1. API Key Storage
- Stored in `local.properties` (gitignored)
- Injected via BuildConfig at compile time
- Never hardcoded in source code

### 2. Token Management
- Access tokens stored in EncryptedSharedPreferences (AES-256)
- Tokens automatically added via AuthInterceptor
- Token refresh mechanism (if needed)
- Automatic logout on token expiry

### 3. Network Security
- All requests use HTTPS
- Certificate pinning (optional, for production)
- Timeout configuration (30 seconds)

### 4. Data Validation
- Client-side validation before API calls
- Server-side validation via Supabase RLS
- Input sanitization

---

## 📊 State Management

### ViewModel State Pattern

```java
public class ProductViewModel extends ViewModel {
    // UI State
    private final MutableLiveData<UiState<List<Product>>> productsState = new MutableLiveData<>();

    public LiveData<UiState<List<Product>>> getProductsState() {
        return productsState;
    }

    public void loadProducts() {
        // Set loading state
        productsState.setValue(UiState.loading());

        productRepository.getProducts(new Callback() {
            @Override
            public void onSuccess(List<Product> products) {
                productsState.postValue(UiState.success(products));
            }

            @Override
            public void onError(String error) {
                productsState.postValue(UiState.error(error));
            }
        });
    }
}

// UI State wrapper
public class UiState<T> {
    private final Status status;
    private final T data;
    private final String error;

    public static <T> UiState<T> loading() {
        return new UiState<>(Status.LOADING, null, null);
    }

    public static <T> UiState<T> success(T data) {
        return new UiState<>(Status.SUCCESS, data, null);
    }

    public static <T> UiState<T> error(String error) {
        return new UiState<>(Status.ERROR, null, error);
    }

    public enum Status { LOADING, SUCCESS, ERROR }
}
```

---

## 🔄 Offline-First Strategy

### Cache Strategy

1. **Network First** - Always try network, fallback to cache
   - Use for: Product listings, user profile

2. **Cache First** - Check cache, then network if stale
   - Use for: Static data (categories, brands)

3. **Network Only** - Always fetch from network
   - Use for: Cart, orders, authentication

### Implementation Example

```java
public class ProductRepository {

    public void getProducts(Callback<List<Product>> callback) {
        // Try network first
        productApi.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onSuccess(List<Product> products) {
                // Save to cache
                executor.execute(() -> {
                    productDao.clearAll();
                    productDao.insertProducts(products);
                });
                callback.onSuccess(products);
            }

            @Override
            public void onFailure(Throwable t) {
                // Fallback to cache
                executor.execute(() -> {
                    List<Product> cachedProducts = productDao.getAllActiveProducts();
                    if (cachedProducts != null && !cachedProducts.isEmpty()) {
                        callback.onSuccess(cachedProducts);
                    } else {
                        callback.onError("No internet and no cached data");
                    }
                });
            }
        });
    }
}
```

---

## 🧪 Testing Strategy

### Unit Tests
```java
// ViewModel Test
@Test
public void login_withValidCredentials_shouldSucceed() {
    // Arrange
    AuthRepository mockRepo = mock(AuthRepository.class);
    AuthViewModel viewModel = new AuthViewModel(mockRepo);

    // Act
    viewModel.login("test@example.com", "password123");

    // Assert
    assertTrue(viewModel.getLoginSuccess().getValue());
}

// Repository Test
@Test
public void login_withValidCredentials_callsApiAndSavesToken() {
    // Arrange
    SupabaseAuthApi mockApi = mock(SupabaseAuthApi.class);
    TokenManager mockTokenManager = mock(TokenManager.class);
    AuthRepository repo = new AuthRepository(mockApi, mockTokenManager);

    // Act
    repo.login("test@example.com", "password123", callback);

    // Assert
    verify(mockApi).login(any(LoginRequest.class));
    verify(mockTokenManager).saveAccessToken(anyString());
}
```

### Integration Tests
```java
@Test
public void loginFlow_endToEnd() {
    // Launch LoginActivity
    ActivityScenario.launch(LoginActivity.class);

    // Enter credentials
    onView(withId(R.id.etEmail)).perform(typeText("test@example.com"));
    onView(withId(R.id.etPassword)).perform(typeText("password123"));

    // Click login
    onView(withId(R.id.btnLogin)).perform(click());

    // Verify navigation to MainActivity
    intended(hasComponent(MainActivity.class.getName()));
}
```

---

## 📈 Future Improvements

### 1. Dependency Injection (Hilt)
```java
@HiltAndroidApp
public class App extends Application {}

@Module
@InstallIn(SingletonComponent.class)
public class NetworkModule {
    @Provides
    @Singleton
    public RetrofitClient provideRetrofitClient() {
        return RetrofitClient.getInstance();
    }
}

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {
    @Inject
    AuthRepository authRepository;
}
```

### 2. Kotlin Coroutines (if migrating to Kotlin)
```kotlin
suspend fun login(email: String, password: String): Result<AuthResponse> {
    return withContext(Dispatchers.IO) {
        try {
            val response = authApi.login(LoginRequest(email, password))
            Result.success(response)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### 3. Flow for Reactive Streams
```kotlin
fun getProducts(): Flow<List<Product>> = flow {
    emit(productDao.getAllProducts()) // Emit cached data first

    val freshData = productApi.getProducts()
    productDao.insertProducts(freshData)
    emit(freshData) // Emit fresh data
}
```

---

## 📚 Resources

### Official Documentation
- [Android Architecture Guide](https://developer.android.com/topic/architecture)
- [MVVM Pattern](https://developer.android.com/topic/libraries/architecture/viewmodel)
- [Room Database](https://developer.android.com/training/data-storage/room)
- [LiveData](https://developer.android.com/topic/libraries/architecture/livedata)

### Supabase
- [Supabase Docs](https://supabase.com/docs)
- [Supabase Auth](https://supabase.com/docs/guides/auth)
- [PostgREST API](https://postgrest.org/)

### Best Practices
- [Android App Architecture Best Practices](https://developer.android.com/topic/architecture/recommendations)
- [Guide to App Architecture](https://developer.android.com/topic/architecture)

---

## ✅ Summary

**Shopverse follows MVVM + Clean Architecture:**

✅ **Clear separation of concerns** - Each layer has one responsibility
✅ **Testable** - Easy to write unit and integration tests
✅ **Maintainable** - Easy to add features and fix bugs
✅ **Scalable** - Can grow with additional features
✅ **Lifecycle-aware** - Survives configuration changes
✅ **Reactive** - UI automatically updates with LiveData
✅ **Offline-capable** - Room database for local caching
✅ **Secure** - Encrypted storage and secure API communication

This architecture provides a solid foundation for building a production-grade Android application that is maintainable, testable, and scalable.
