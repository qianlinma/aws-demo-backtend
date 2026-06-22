package com.mycompany.backend.demo.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest 会启动 Spring 测试环境。
// 这里不是手动 new Controller，而是让 Spring 创建 Controller、Service、Repository 这些 bean。
@SpringBootTest
class ProductApiIntegrationTest {
    private MockMvc mockMvc;
    // 保存 Spring 启动后的 Web 应用上下文。
    // 后面会用它创建 MockMvc，让测试 request 走真实的 Spring 路由和 Controller。
    private final WebApplicationContext webApplicationContext;

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
        // 这个 test 发送一个假的 GET request 到真实的 Spring MVC 路由。
        // 请求会经过 ProductController -> ProductService -> real ProductRepository -> AWS dev DynamoDB。
        mockMvc.perform(get("/api/getAllProducts"))
                // 先检查 HTTP status 是 200。
                .andExpect(status().isOk())
                // 再检查返回 JSON 里有 products array。
                .andExpect(jsonPath("$.products").isArray())
                // 检查第一个 product 的字段是否来自 dev DynamoDB 里的 Terraform seed data。
                .andExpect(jsonPath("$.products[0].id").value(1))
                .andExpect(jsonPath("$.products[0].title").value("First product"))
                // 检查第二个 product，确认 repository 真的读到了多条 DynamoDB item。
                .andExpect(jsonPath("$.products[1].id").value(2))
                .andExpect(jsonPath("$.products[1].title").value("Second product"))
                // Terraform 目前 seed 了 6 个 product。
                .andExpect(jsonPath("$.products.length()").value(6));
    }
}
