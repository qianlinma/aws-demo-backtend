package com.mycompany.backend.demo.integration;

import com.mycompany.backend.demo.model.Product;
import com.mycompany.backend.demo.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest 会启动 Spring 测试环境。
// 这里不是手动 new Controller，而是让 Spring 创建 Controller、Service、Repository 这些 bean。
@SpringBootTest
class ProductApiIntegrationTest {
    private MockMvc mockMvc;
    private final WebApplicationContext webApplicationContext;

    // @MockitoBean 会把 Spring 容器里的 ProductRepository 替换成 Mockito mock。
    // 这样 ProductController 和 ProductService 是 Spring 创建的真实 bean。
    // 但最底层 repository 不会真的连接 DynamoDB。
    @MockitoBean
    private ProductRepository productRepository;

    ProductApiIntegrationTest(WebApplicationContext webApplicationContext) {
        this.webApplicationContext = webApplicationContext;
    }

    @BeforeEach
    void setUp() {
        // WebApplicationContext 是 Spring 启动后的 Web 应用上下文。
        // MockMvcBuilders.webAppContextSetup(...) 会基于这个上下文创建 MockMvc。
        // 这样 MockMvc 发送 request 时，会走 Spring 的真实路由和 Controller。
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
    }

    @Test
    void getAllProductsReturnsProductsFromSpringHttpFlow() throws Exception {
        // 这里定义 mock repository 的行为。
        // 当 ProductService 调用 productRepository.findAll() 时，返回我们准备好的测试数据。
        when(productRepository.findAll()).thenReturn(List.of(
                new Product(1, "/images/laptop.svg", "Laptop"),
                new Product(2, "/images/keyboard.svg", "Keyboard")
        ));

        // 这个 test 发送一个假的 GET request 到真实的 Spring MVC 路由。
        // 请求会经过 ProductController -> ProductService -> mock ProductRepository。
        mockMvc.perform(get("/api/getAllProducts"))
                // 先检查 HTTP status 是 200。
                .andExpect(status().isOk())
                // 再检查返回 JSON 里有 products array。
                .andExpect(jsonPath("$.products").isArray())
                // 检查第一个 product 的字段是否来自测试版 repository。
                .andExpect(jsonPath("$.products[0].id").value(1))
                .andExpect(jsonPath("$.products[0].path").value("/images/laptop.svg"))
                .andExpect(jsonPath("$.products[0].title").value("Laptop"))
                // 检查第二个 product，确认不是只返回了一个固定字段。
                .andExpect(jsonPath("$.products[1].id").value(2))
                .andExpect(jsonPath("$.products[1].title").value("Keyboard"));
    }
}
