package com.mycompany.backend.demo.service;

import com.mycompany.backend.demo.client.InventoryServiceClient;
import com.mycompany.backend.demo.model.Product;
import com.mycompany.backend.demo.model.ProductInventory;
import com.mycompany.backend.demo.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;
    private final InventoryServiceClient inventoryServiceClient;

    public ProductServiceImpl(ProductRepository productRepository, InventoryServiceClient inventoryServiceClient) {
        this.productRepository = productRepository;
        this.inventoryServiceClient = inventoryServiceClient;
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public ProductInventory getProductInventory(int productId) {
        return inventoryServiceClient.getInventory(productId);
    }
}
