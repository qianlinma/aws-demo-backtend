package com.mycompany.backend.demo.service;

import com.mycompany.backend.demo.model.Product;
import com.mycompany.backend.demo.model.ProductDetails;
import com.mycompany.backend.demo.model.ProductInventory;
import com.mycompany.backend.demo.model.ProductUserProfile;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();

    ProductInventory getProductInventory(int productId);

    ProductUserProfile getUserProfile(int userId);

    ProductDetails getProductDetails(int productId);
}
