package com.mycompany.backend.demo.model;

public record ProductDetails(
        Product product,
        ProductInventory inventory,
        ProductUserProfile userProfile
) {
}
