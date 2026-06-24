package com.mycompany.backend.demo.controller;

import com.mycompany.backend.demo.model.Product;
import com.mycompany.backend.demo.model.ProductDetails;
import com.mycompany.backend.demo.model.ProductInventory;
import com.mycompany.backend.demo.model.ProductUserProfile;
import com.mycompany.backend.demo.service.ProductService;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductControllerTest {

    @Test
    void getAllProductsReturnsProductsKey() {
        // ProductController 的构造函数需要一个 ProductService。
        // 这里传入 FakeProductService，是为了让 controller test 不依赖真的 service/repository/DynamoDB。
        // List.of() 没有放任何 Product，所以它是一个空 list。
        ProductController productController = new ProductController(new FakeProductService(List.of()));

        Map<String, List<Product>> response = productController.getAllProducts();

        assertTrue(response.containsKey("products"));
    }

    @Test
    void getAllProductsReturnsProductsFromService() {
        List<Product> products = List.of(new Product(1, "/images/product-1.svg", "Demo Product"));
        ProductController productController = new ProductController(new FakeProductService(products));

        Map<String, List<Product>> response = productController.getAllProducts();

        assertEquals(products, response.get("products"));
    }

    @Test
    void getAllProductsReturnsEmptyListWhenServiceHasNoProducts() {
        // 这个 test 特意给 fake service 一个空 list，用来测试没有产品时 controller 怎么返回。
        ProductController productController = new ProductController(new FakeProductService(List.of()));

        Map<String, List<Product>> response = productController.getAllProducts();

        assertEquals(Collections.emptyList(), response.get("products"));
    }

    // 这是一个只给 ProductControllerTest 使用的假 service。
    // 它的作用是控制 ProductService.getAllProducts() 返回什么数据。
    // 我们 fake service，而不是 fake repository，是因为这个 unit test 的目标是 ProductController。
    // ProductController 的直接依赖是 ProductService，所以 fake 掉 service 就够了。
    // 如果我们测试 ProductService，才会 fake 它的下一层依赖 ProductRepository。
    // private 表示这个 fake class 只在当前 test class 内部使用，外面代码不能用它。
    // static 表示它不需要依赖 ProductControllerTest 的实例，可以像普通 helper class 一样被创建。
    // implements ProductService 是因为 ProductController 构造函数要求传入 ProductService 类型。
    // FakeProductService 实现 ProductService 后，就可以被当成 ProductService 传给 ProductController。
    private static class FakeProductService implements ProductService {
        private final List<Product> products;

        FakeProductService(List<Product> products) {
            // 保存 test 想让 fake service 返回的产品列表。
            this.products = products;
        }

        @Override
        public List<Product> getAllProducts() {
            // 当 controller 调用 productService.getAllProducts() 时，直接返回 test 提前准备好的 products。
            return products;
        }

        @Override
        public ProductInventory getProductInventory(int productId) {
            return new ProductInventory(productId, 0, "unknown", false);
        }

        @Override
        public ProductUserProfile getUserProfile(int userId) {
            return new ProductUserProfile(userId, "Test User", "BASIC", "unknown");
        }

        @Override
        public ProductDetails getProductDetails(int productId) {
            Product product = products.stream()
                    .filter(item -> item.id() != null && item.id() == productId)
                    .findFirst()
                    .orElseGet(() -> new Product(productId, "", "Unknown product"));

            return new ProductDetails(
                    product,
                    new ProductInventory(productId, 0, "unknown", false),
                    new ProductUserProfile(productId, "Test User", "BASIC", "unknown")
            );
        }
    }
}
