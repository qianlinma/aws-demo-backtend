package com.mycompany.backend.demo.service;

import com.mycompany.backend.demo.client.InventoryServiceClient;
import com.mycompany.backend.demo.client.UserServiceGrpcClient;
import com.mycompany.backend.demo.model.Product;
import com.mycompany.backend.demo.model.ProductDetails;
import com.mycompany.backend.demo.model.ProductInventory;
import com.mycompany.backend.demo.repository.ProductRepository;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductServiceImplTest {

    @Test
    void getProductDetailsReturnsDefaultUserWhenUserServiceFails() {
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryServiceClient inventoryServiceClient = mock(InventoryServiceClient.class);
        UserServiceGrpcClient userServiceGrpcClient = mock(UserServiceGrpcClient.class);
        ProductServiceImpl productService = new ProductServiceImpl(
                productRepository,
                inventoryServiceClient,
                userServiceGrpcClient,
                new SimpleMeterRegistry()
        );

        Product product = new Product(1, "/images/product-1.svg", "First product");
        ProductInventory inventory = new ProductInventory(1, 12, "us-west-2", true);

        when(productRepository.findAll()).thenReturn(List.of(product));
        when(inventoryServiceClient.getInventory(1)).thenReturn(inventory);
        when(userServiceGrpcClient.getUserProfile(1))
                .thenThrow(new RuntimeException("User gRPC call failed"));

        ProductDetails details = productService.getProductDetails(1);

        assertEquals(product, details.product());
        assertEquals(inventory, details.inventory());
        assertEquals(1, details.userProfile().id());
        assertEquals("User service unavailable", details.userProfile().name());
        assertEquals("UNKNOWN", details.userProfile().membershipLevel());
        assertEquals("unknown", details.userProfile().region());
    }

    @Test
    void getProductDetailsReturnsDefaultInventoryWhenInventoryServiceFails() {
        ProductRepository productRepository = mock(ProductRepository.class);
        InventoryServiceClient inventoryServiceClient = mock(InventoryServiceClient.class);
        UserServiceGrpcClient userServiceGrpcClient = mock(UserServiceGrpcClient.class);
        ProductServiceImpl productService = new ProductServiceImpl(
                productRepository,
                inventoryServiceClient,
                userServiceGrpcClient,
                new SimpleMeterRegistry()
        );

        Product product = new Product(1, "/images/product-1.svg", "First product");

        when(productRepository.findAll()).thenReturn(List.of(product));
        when(inventoryServiceClient.getInventory(1))
                .thenThrow(new RuntimeException("Inventory REST call failed"));

        ProductDetails details = productService.getProductDetails(1);

        assertEquals(product, details.product());
        assertEquals(1, details.inventory().productId());
        assertEquals(0, details.inventory().quantityAvailable());
        assertEquals("Inventory service unavailable", details.inventory().warehouseRegion());
        assertFalse(details.inventory().available());
    }
}
