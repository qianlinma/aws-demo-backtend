package com.mycompany.backend.demo.controller;

import com.mycompany.backend.demo.model.Product;
import com.mycompany.backend.demo.model.ProductInventory;
import com.mycompany.backend.demo.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/api/getAllProducts")
    public Map<String, List<Product>> getAllProducts() {
        return Map.of("products", productService.getAllProducts());
    }

    @GetMapping("/api/products/{productId}/inventory")
    public ProductInventory getProductInventory(@PathVariable int productId) {
        return productService.getProductInventory(productId);
    }
}
