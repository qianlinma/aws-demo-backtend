package com.mycompany.backend.demo.controller;

import com.mycompany.backend.demo.model.Product;
import com.mycompany.backend.demo.service.ProductService;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductControllerMockitoTest {

    @Test
    void getAllProductsReturnsProductsKeyWithMockService() {
        // mock(...) 让 Mockito 自动生成一个假的 ProductService。如果用 subclass mock maker，它通常会生成一个临时子类
        // 这和我们手写 FakeProductService 的目的相同：不调用真的 service/repository/DynamoDB。

        // 它只是一个“空的假 ProductService”。里面的方法默认没有业务逻辑。
        ProductService productService = mock(ProductService.class);
        ProductController productController = new ProductController(productService);

        Map<String, List<Product>> response = productController.getAllProducts();

        assertTrue(response.containsKey("products"));
    }

    @Test
    void getAllProductsReturnsProductsFromMockService() {
        ProductService productService = mock(ProductService.class);
        List<Product> products = List.of(new Product(1, "/images/product-1.svg", "Demo Product"));
        
        // when(...).thenReturn(...) 告诉 mock：当 getAllProducts() 被调用时，返回 products。
        // 刚 mock(ProductService.class) 时，它只是一个“空的假 ProductService”。里面的方法默认没有业务逻辑。
        // 所以我们要写：when(productService.getAllProducts()).thenReturn(products);
        // 通过when thenreturn来模拟proudctservice中某一些function的返回值

        when(productService.getAllProducts()).thenReturn(products);
        ProductController productController = new ProductController(productService);

        Map<String, List<Product>> response = productController.getAllProducts();

        assertEquals(products, response.get("products"));
    }

    @Test
    void getAllProductsReturnsEmptyListFromMockService() {
        ProductService productService = mock(ProductService.class);
        // 这里让 mock service 返回空 list，用来测试没有产品时 controller 的返回结果。
        when(productService.getAllProducts()).thenReturn(List.of());
        ProductController productController = new ProductController(productService);

        Map<String, List<Product>> response = productController.getAllProducts();

        assertEquals(List.of(), response.get("products"));
    }
}
