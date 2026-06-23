package com.mycompany.backend.demo.model;

public record ProductInventory(
        Integer productId,
        Integer quantityAvailable,
        String warehouseRegion,
        Boolean available
) {
}
