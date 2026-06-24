package com.mycompany.backend.demo.service;

import com.mycompany.backend.demo.client.InventoryServiceClient;
import com.mycompany.backend.demo.client.UserServiceGrpcClient;
import com.mycompany.backend.demo.model.Product;
import com.mycompany.backend.demo.model.ProductDetails;
import com.mycompany.backend.demo.model.ProductInventory;
import com.mycompany.backend.demo.model.ProductUserProfile;
import com.mycompany.backend.demo.repository.ProductRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    private static final Logger logger = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository productRepository;
    private final InventoryServiceClient inventoryServiceClient;
    private final UserServiceGrpcClient userServiceGrpcClient;
    private final MeterRegistry meterRegistry;
    private final Counter productDetailsRequests;
    private final Counter inventoryFallbacks;
    private final Counter userProfileFallbacks;

    public ProductServiceImpl(
            ProductRepository productRepository,
            InventoryServiceClient inventoryServiceClient,
            UserServiceGrpcClient userServiceGrpcClient,
            MeterRegistry meterRegistry
    ) {
        this.productRepository = productRepository;
        this.inventoryServiceClient = inventoryServiceClient;
        this.userServiceGrpcClient = userServiceGrpcClient;
        this.meterRegistry = meterRegistry;
        this.productDetailsRequests = Counter.builder("product.details.requests")
                .description("Number of product detail requests handled by product service")
                .register(meterRegistry);
        this.inventoryFallbacks = Counter.builder("product.dependency.fallbacks")
                .description("Number of fallback responses returned for product dependencies")
                .tag("dependency", "inventory")
                .register(meterRegistry);
        this.userProfileFallbacks = Counter.builder("product.dependency.fallbacks")
                .description("Number of fallback responses returned for product dependencies")
                .tag("dependency", "user-profile")
                .register(meterRegistry);
    }

    @Override
    public List<Product> getAllProducts() {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            List<Product> products = productRepository.findAll();
            logger.info("Loaded product catalog productCount={}", products.size());
            sample.stop(timer("product.catalog.load", "success"));
            return products;
        } catch (RuntimeException e) {
            sample.stop(timer("product.catalog.load", "error"));
            throw e;
        }
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
        productDetailsRequests.increment();
        Timer.Sample sample = Timer.start(meterRegistry);
        String outcome = "success";
        try {
            Product product = productRepository.findAll().stream()
                    .filter(item -> item.id() != null && item.id() == productId)
                    .findFirst()
                    .orElseGet(() -> {
                        logger.info("Product not found, returning placeholder productId={}", productId);
                        return new Product(productId, "", "Unknown product");
                    });

            ProductInventory inventory = getProductInventoryOrDefault(productId);
            ProductUserProfile userProfile = getUserProfileOrDefault(productId);

            logger.info(
                    "Built product details productId={} inventoryAvailable={} userRegion={}",
                    productId,
                    inventory.available(),
                    userProfile.region()
            );
            return new ProductDetails(product, inventory, userProfile);
        } catch (RuntimeException e) {
            outcome = "error";
            throw e;
        } finally {
            sample.stop(timer("product.details.duration", outcome));
        }
    }

    private ProductInventory getProductInventoryOrDefault(int productId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            ProductInventory inventory = inventoryServiceClient.getInventory(productId);
            if (inventory == null) {
                inventoryFallbacks.increment();
                sample.stop(timer("product.dependency.duration", "inventory", "fallback"));
                logger.warn("Inventory service returned empty response for productId={}", productId);
                return new ProductInventory(productId, 0, "Inventory service unavailable", false);
            }
            sample.stop(timer("product.dependency.duration", "inventory", "success"));
            return inventory;
        } catch (RuntimeException e) {
            inventoryFallbacks.increment();
            sample.stop(timer("product.dependency.duration", "inventory", "fallback"));
            logger.warn("Inventory service unavailable for productId={}", productId, e);
            return new ProductInventory(productId, 0, "Inventory service unavailable", false);
        }
    }

    private ProductUserProfile getUserProfileOrDefault(int userId) {
        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            ProductUserProfile userProfile = userServiceGrpcClient.getUserProfile(userId);
            if (userProfile == null) {
                userProfileFallbacks.increment();
                sample.stop(timer("product.dependency.duration", "user-profile", "fallback"));
                logger.warn("User service returned empty response for userId={}", userId);
                return new ProductUserProfile(userId, "User service unavailable", "UNKNOWN", "unknown");
            }
            sample.stop(timer("product.dependency.duration", "user-profile", "success"));
            return userProfile;
        } catch (RuntimeException e) {
            userProfileFallbacks.increment();
            sample.stop(timer("product.dependency.duration", "user-profile", "fallback"));
            logger.warn("User service unavailable for userId={}", userId, e);
            return new ProductUserProfile(userId, "User service unavailable", "UNKNOWN", "unknown");
        }
    }

    private Timer timer(String name, String outcome) {
        return Timer.builder(name)
                .tag("outcome", outcome)
                .register(meterRegistry);
    }

    private Timer timer(String name, String dependency, String outcome) {
        return Timer.builder(name)
                .tag("dependency", dependency)
                .tag("outcome", outcome)
                .register(meterRegistry);
    }
}
