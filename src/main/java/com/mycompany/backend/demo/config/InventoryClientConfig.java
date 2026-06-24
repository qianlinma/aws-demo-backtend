package com.mycompany.backend.demo.config;

// OpenAPI Generator 自动生成的底层 HTTP client。
import com.mycompany.backend.demo.inventoryclient.ApiClient;
// OpenAPI Generator 根据 inventory-api.yml 自动生成的 Inventory API client。
import com.mycompany.backend.demo.inventoryclient.api.InventoryApi;
// @Value 用来从 Spring 配置里读取值，比如 application.properties 或 ECS environment variable。
import org.springframework.beans.factory.annotation.Value;
// @Bean 用来告诉 Spring：这个 method 返回的 object 要放进 Spring 容器。
import org.springframework.context.annotation.Bean;
// @Configuration 表示这个 class 是 Spring 配置类，里面会声明一些 Spring-managed objects。
import org.springframework.context.annotation.Configuration;

// 告诉 Spring：这个 class 不是普通业务类，而是配置类。
// Spring 启动时会读取这里的 @Bean method，并创建对应 object。
@Configuration
public class InventoryClientConfig {
    // 告诉 Spring：把这个 method 返回的 ApiClient object 注册成一个 bean。
    // 之后别的 class 需要 ApiClient 时，Spring 可以自动注入这个 object。
    @Bean
    public ApiClient inventoryApiClient(
            // 从配置中读取 inventory.service.base-url。
            // 如果配置里没有这个值，就使用默认值 http://inventory.demo.internal。
            // 本地运行时可以改成 http://localhost:8080。
            // AWS ECS 运行时会通过环境变量 INVENTORY_SERVICE_BASE_URL 传入。
            @Value("${inventory.service.base-url:http://inventory.demo.internal}") String inventoryServiceBaseUrl
    ) {
        // 创建 OpenAPI generated ApiClient。
        // 这个 object 负责保存 base URL、headers、JSON 解析等 HTTP client 配置。
        ApiClient apiClient = new ApiClient();
        // 设置 Inventory Service 的 base URL。
        // 以后 InventoryApi 发请求时，会以这个地址作为服务根地址。
        apiClient.setBasePath(inventoryServiceBaseUrl);
        // 把配置好的 ApiClient 返回给 Spring 容器管理。
        return apiClient;
    }

    // 告诉 Spring：把这个 method 返回的 InventoryApi object 注册成一个 bean。
    // 之后 InventoryServiceClient 可以通过 constructor 自动拿到它。
    @Bean
    // Spring 会自动把上面创建的 ApiClient bean 传进来。
    public InventoryApi inventoryApi(ApiClient inventoryApiClient) {
        // 用配置好的 ApiClient 创建 generated InventoryApi。
        // InventoryApi 里面有 getInventory(productId) 这种 generated method。
        return new InventoryApi(inventoryApiClient);
    }
}
