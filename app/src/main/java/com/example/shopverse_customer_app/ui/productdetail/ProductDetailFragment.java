package com.example.shopverse_customer_app.ui.productdetail;

import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.example.shopverse_customer_app.R;
import com.example.shopverse_customer_app.data.model.Product;
import com.example.shopverse_customer_app.ui.cart.CartViewModel;
import com.example.shopverse_customer_app.utils.TokenManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Fragment for displaying product details
 */
public class ProductDetailFragment extends Fragment {

    private static final String TAG = "ProductDetailFragment";
    private static final String ARG_PRODUCT = "product";

    private ProductDetailViewModel viewModel;
    private CartViewModel cartViewModel;
    private TokenManager tokenManager;
    private Product product;

    // Views
    private ViewPager2 productImagesViewPager;
    private RecyclerView specificationsRecyclerView;
    private TextView productName, ratingValue, favoriteButton;
    private TextView currentPrice, originalPrice;
    private TextView productDescription;
    private ImageView backButton;
    private ProgressBar loadingProgressBar;

    private SpecificationAdapter specificationAdapter;
    private ProductImageAdapter imageAdapter;

    public static ProductDetailFragment newInstance(Product product) {
        ProductDetailFragment fragment = new ProductDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PRODUCT, product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            product = (Product) getArguments().getSerializable(ARG_PRODUCT);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_product_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialize ViewModel
        viewModel = new ViewModelProvider(this).get(ProductDetailViewModel.class);
        cartViewModel = new ViewModelProvider(this).get(CartViewModel.class);
        tokenManager = new TokenManager(requireContext());

        // Initialize views
        initializeViews(view);

        // Setup components
        setupHeader(view);
        setupImageViewPager();
        setupSpecificationsRecyclerView();
        setupActionButtons(view);

        // Set product data
        if (product != null) {
            viewModel.setProduct(product);
            displayProductData();
        }

        // Observe ViewModel
        observeViewModel();
    }

    private void initializeViews(View view) {
        productImagesViewPager = view.findViewById(R.id.productImagesViewPager);
        specificationsRecyclerView = view.findViewById(R.id.specificationsRecyclerView);
        productName = view.findViewById(R.id.productName);
        ratingValue = view.findViewById(R.id.ratingValue);
        favoriteButton = view.findViewById(R.id.favoriteButton);
        currentPrice = view.findViewById(R.id.currentPrice);
        originalPrice = view.findViewById(R.id.originalPrice);
        productDescription = view.findViewById(R.id.productDescription);
        backButton = view.findViewById(R.id.backButton);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
    }

    private void setupHeader(View view) {
        backButton.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });
    }

    private void setupImageViewPager() {
        imageAdapter = new ProductImageAdapter();
        productImagesViewPager.setAdapter(imageAdapter);

        // Load product images if available
        if (product != null && product.getProductMedia() != null) {
            imageAdapter.setImageUrls(product.getProductMedia());
        }
    }

    private void setupSpecificationsRecyclerView() {
        specificationAdapter = new SpecificationAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        specificationsRecyclerView.setLayoutManager(layoutManager);
        specificationsRecyclerView.setAdapter(specificationAdapter);
        specificationsRecyclerView.setNestedScrollingEnabled(false);
    }

    private void setupActionButtons(View view) {
        favoriteButton.setOnClickListener(v -> {
            viewModel.toggleFavorite();
        });

        view.findViewById(R.id.addToCartButton).setOnClickListener(v -> {
            addToCart();
        });

        view.findViewById(R.id.buyNowButton).setOnClickListener(v -> {
            // TODO: Proceed to checkout
            Toast.makeText(getContext(), "Mua ngay", Toast.LENGTH_SHORT).show();
        });
    }

    /**
     * Add product to cart
     */
    private void addToCart() {
        if (product == null) {
            Toast.makeText(getContext(), "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        // Check if user is logged in
        String userIdStr = tokenManager.getUserId();
        if (userIdStr == null || userIdStr.isEmpty()) {
            Toast.makeText(getContext(), "Vui lòng đăng nhập để thêm vào giỏ hàng", Toast.LENGTH_LONG).show();
            return;
        }

        // Check if product is available
        if (!product.isActive() || !product.isInStock()) {
            Toast.makeText(getContext(), "Sản phẩm hiện không có sẵn", Toast.LENGTH_SHORT).show();
            return;
        }

        // User ID is UUID string, no need to parse
        String userId = userIdStr;
        String productId = product.getProductId();
        String accessToken = tokenManager.getAccessToken();

        Log.e("ERROR: RLS", "userId from storage: " + userId);
        Log.e("ERROR: RLS", "accessToken: " + (accessToken != null ? accessToken.substring(0, Math.min(50, accessToken.length())) + "..." : "null"));

        int quantity = 1; // Default quantity

        // Add to cart via ViewModel
        cartViewModel.addOrUpdateCartItem(userId, productId, quantity);
        Log.d(TAG, "Requesting to add product to cart: " + product.getProductName());
    }

    private void displayProductData() {
        if (product == null) return;

        // Set product name
        productName.setText(product.getProductName());

        // Set price
        String priceText = formatPrice(product.getUnitPrice());
        currentPrice.setText(priceText);

        // Strike through original price
        originalPrice.setPaintFlags(originalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        // Set description if available
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            productDescription.setText(product.getDescription());
        }

        // Set rating (placeholder for now)
        ratingValue.setText("4.9");

        // Load specifications
        loadSpecifications();

        Log.d(TAG, "Product displayed: " + product.getProductName());
    }

    private void loadSpecifications() {
        // Create sample specifications
        // In production, these should come from the database
        Map<String, String> specs = new LinkedHashMap<>();

        // Add basic specs that are available
        if (product.getBrand() != null) {
            specs.put("Thương hiệu", product.getBrand().getBrandName());
        }

        if (product.getCategory() != null) {
            specs.put("Danh mục", product.getCategory().getCategoryName());
        }

        specs.put("Tình trạng", product.getStock() > 0 ? "Còn hàng (" + product.getStock() + ")" : "Hết hàng");

        // Add placeholder specs - these should come from a specifications table in production
        specs.put("Kích thước màn hình", "6.9 inches");
        specs.put("Công nghệ màn hình", "Super Retina XDR OLED");
        specs.put("Camera sau", "48MP, f/1.78, 24mm");
        specs.put("Camera trước", "12MP, f/1.9");
        specs.put("Chipset", "Apple A18 Pro");
        specs.put("Bộ nhớ trong", "256 GB");
        specs.put("Hệ điều hành", "iOS 18");

        specificationAdapter.setSpecifications(specs);
    }

    private void observeViewModel() {
        viewModel.getIsFavorite().observe(getViewLifecycleOwner(), isFavorite -> {
            if (isFavorite) {
                favoriteButton.setText("♥ Đã thích");
            } else {
                favoriteButton.setText("♡ Yêu thích");
            }
        });

        // Observe cart operations
        cartViewModel.getLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                // Show loading indicator if needed
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                }
            } else {
                if (loadingProgressBar != null) {
                    loadingProgressBar.setVisibility(View.GONE);
                }
            }
        });

        cartViewModel.getSuccess().observe(getViewLifecycleOwner(), successMsg -> {
            if (successMsg != null && !successMsg.isEmpty()) {
                Toast.makeText(getContext(), successMsg, Toast.LENGTH_SHORT).show();
                Log.d(TAG, "Cart success: " + successMsg);
            }
        });

        cartViewModel.getError().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(getContext(), "Lỗi: " + error, Toast.LENGTH_LONG).show();
                Log.e(TAG, "Cart error: " + error);
            }
        });
    }

    private String formatPrice(double price) {
        long priceInt = (long) price;
        String formatted = String.format("%,d", priceInt).replace(",", ".");
        return formatted + "₫";
    }
}
