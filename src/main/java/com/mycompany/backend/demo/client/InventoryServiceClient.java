package com.mycompany.backend.demo.client;

import com.mycompany.backend.demo.inventoryclient.ApiException;
import com.mycompany.backend.demo.inventoryclient.api.InventoryApi;
import com.mycompany.backend.demo.model.ProductInventory;
import org.springframework.stereotype.Component;

// 这是 Product Service 自己写的 client wrapper。
// 它负责把 OpenAPI Generator 自动生成的 InventoryApi 包起来，
// 让业务层不用直接依赖 generated code 的细节。
@Component
// @Component 是告诉 Spring：这个 class 交给 Spring 容器管理。是 Spring 帮你做：new InventoryServiceClient(inventoryApi)
// 所以你不用自己手动 new。
public class InventoryServiceClient {
    // InventoryApi 是根据 inventory-api.yml 自动生成的 REST client。
    // 它里面真正封装了 HTTP request：GET /api/inventory/{productId}。
    private final InventoryApi inventoryApi;

    public InventoryServiceClient(InventoryApi inventoryApi) {
        this.inventoryApi = inventoryApi; // InventoryApi 是 OpenAPI Generator 自动生成的 client class。
    }

    // ProductServiceImpl 调用这个 method，就能拿到某个 product 的库存信息。
    // 表面上像普通 Java method，底层其实会发 HTTP request 到 Inventory Service。
    public ProductInventory getInventory(int productId) {
        try {
            // 调用 generated client。
            // 这里会根据 OpenAPI contract 去请求 Inventory Service 的 REST API。
            var inventoryItem = inventoryApi.getInventory(productId);

            // generated model 属于 inventoryclient package。
            // 这里转换成 Product Service 自己的 model，避免业务层依赖 generated model。
            return new ProductInventory(
                    inventoryItem.getProductId(),
                    inventoryItem.getQuantityAvailable(),
                    inventoryItem.getWarehouseRegion(),
                    inventoryItem.getAvailable()
            );
        } catch (ApiException e) {
            // OpenAPI generated client 抛的是 checked exception。
            // 这里转换成 runtime exception，让 ProductServiceImpl 不需要 throws ApiException。
            throw new InventoryServiceClientException("Failed to call inventory service", e);
        }
    }
}
