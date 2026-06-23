package com.mycompany.backend.demo.service;

import com.mycompany.backend.demo.model.Product;
import com.mycompany.backend.demo.model.ProductInventory;

import java.util.List;

public interface ProductService {
    List<Product> getAllProducts();

    ProductInventory getProductInventory(int productId);
}
