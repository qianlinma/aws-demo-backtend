package com.mycompany.backend.demo.service;

import com.mycompany.backend.demo.client.InventoryServiceClient;
import com.mycompany.backend.demo.client.UserServiceGrpcClient;
import com.mycompany.backend.demo.model.Product;
import com.mycompany.backend.demo.model.ProductDetails;
import com.mycompany.backend.demo.model.ProductInventory;
import com.mycompany.backend.demo.model.ProductUserProfile;
import com.mycompany.backend.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final UserServiceGrpcClient userServiceGrpcClient;

    public ProductServiceImpl(
            ProductRepository productRepository,
            InventoryServiceClient inventoryServiceClient,
            UserServiceGrpcClient userServiceGrpcClient
    ) {
        this.productRepository = productRepository;
        this.inventoryServiceClient = inventoryServiceClient;
        this.userServiceGrpcClient = userServiceGrpcClient;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public ProductInventory getProductInventory(int productId) {
        return inventoryServiceClient.getInventory(productId);
    }

    @Override
    public ProductUserProfile getUserProfile(int userId) {
        return userServiceGrpcClient.getUserProfile(userId);
    }

    @Override
    public ProductDetails getProductDetails(int productId) {
        Product product = productRepository.findAll().stream()
                .filter(item -> item.id() != null && item.id() == productId)
                .findFirst()
                .orElseGet(() -> new Product(productId, "", "Unknown product"));

        ProductInventory inventory = inventoryServiceClient.getInventory(productId);
        ProductUserProfile userProfile = userServiceGrpcClient.getUserProfile(productId);

        return new ProductDetails(product, inventory, userProfile);
    }
}
