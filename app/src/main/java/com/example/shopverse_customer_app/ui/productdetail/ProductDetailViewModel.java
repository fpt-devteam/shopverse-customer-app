package com.example.shopverse_customer_app.ui.productdetail;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.shopverse_customer_app.data.model.Product;

/**
 * ViewModel for Product Detail screen
 */
public class ProductDetailViewModel extends ViewModel {

    private final MutableLiveData<Product> product = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isFavorite = new MutableLiveData<>(false);

    public void setProduct(Product product) {
        this.product.setValue(product);
    }

    public LiveData<Product> getProduct() {
        return product;
    }

    public LiveData<Boolean> getIsFavorite() {
        return isFavorite;
    }

    public void toggleFavorite() {
        Boolean current = isFavorite.getValue();
        isFavorite.setValue(current == null || !current);
    }
}
