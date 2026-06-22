package com.mycompany.backend.demo.integration; // 这个 test class 属于 integration test 这个 package。

import org.junit.jupiter.api.BeforeEach; // JUnit 注解：每个 test method 运行前，都会先运行一次 @BeforeEach method。
import org.junit.jupiter.api.Test; // JUnit 注解：标记下面某个 method 是一个 test case。
import org.springframework.boot.test.context.SpringBootTest; // Spring Boot 注解：启动 Spring 测试环境。
import org.springframework.test.web.servlet.MockMvc; // Spring 测试工具：不用真的开 HTTP server，也能模拟发送 HTTP request。
import org.springframework.test.web.servlet.setup.MockMvcBuilders; // Spring 测试工具：用来创建 MockMvc object。
import org.springframework.web.context.WebApplicationContext; // Spring 的 Web 应用上下文：里面保存了 Controller、Service、Repository 等 bean。

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get; // static import：让代码可以直接写 get(...) 来创建 GET request。
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath; // static import：让代码可以直接写 jsonPath(...) 来检查 JSON response。
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status; // static import：让代码可以直接写 status() 来检查 HTTP status。

// @SpringBootTest 会启动一个接近真实运行状态的 Spring Boot 应用上下文，用来做集成测试。

// Spring Boot 应用上下文可以理解成：Spring 帮你搭好的“应用运行容器”。
// 你的 Controller、Service、Repository、配置类、数据库连接这些对象，都会被 Spring 创建出来、管理起来，并按需要互相注入。
// 比如 Controller 需要 ProductRepository，Spring 就从这个上下文里找到 Repository 实例塞进去。

// 这让 test 更接近真实 app，而不是只测试一个单独 class。
// SpringBootTest把“ApplicationContext”这个容器启动起来是吗
@SpringBootTest // 告诉 Spring Boot：运行这个 test 前，请启动 application context。
class ProductApiIntegrationTest { // 定义一个 test class，用来测试 Product API 的整条 Spring 链路。
    private MockMvc mockMvc; // MockMvc 是测试用的 HTTP request 工具，后面会用它请求 /api/getAllProducts。

    // 保存 Spring 启动后的 Web 应用上下文。
    // 后面会用它创建 MockMvc，让测试 request 走真实的 Spring 路由和 Controller。
    private final WebApplicationContext webApplicationContext; // final 表示 constructor 赋值后，这个 reference 不会再被换掉。

    ProductApiIntegrationTest(WebApplicationContext webApplicationContext) { // Spring 会把启动好的 WebApplicationContext 传进来。
        this.webApplicationContext = webApplicationContext; // 保存 Spring 给我们的 context，后面 setUp() 里会用。
    }

    @BeforeEach // 每个 @Test method 运行前，JUnit 都会先运行这个 method。
    void setUp() { // 准备测试需要的工具。
        // WebApplicationContext 是 Spring 启动后的 Web 应用上下文。
        // MockMvcBuilders.webAppContextSetup(...) 会基于这个上下文创建 MockMvc。
        // 这样 MockMvc 发送 request 时，会走 Spring 的真实路由和 Controller。
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build(); 
        // MockMvcBuilders：Spring 提供的工厂类，用来创建 MockMvc。
        // .webAppContextSetup(webApplicationContext)：告诉它基于已经启动好的 Spring Web 容器来搭建测试请求环境。
        // .build()：真正生成一个 MockMvc 对象并赋值给 mockMvc。
    }
    // 保证每个 @Test 前都有一个干净、确定的 MockMvc 实例。
    // MockMvc 本身很轻量，重新 build 成本低。
    // 如果你的测试不会修改 MockMvc 配置，也可以用 @BeforeAll 或字段初始化一次。



    @Test // 标记下面这个 method 是一个 test case。
    void getAllProductsReturnsProductsFromSpringHttpFlow() throws Exception { // 测试 GET /api/getAllProducts 是否能通过 Spring 链路拿到产品数据。
        // 这个 test 发送一个假的 GET request 到真实的 Spring MVC 路由。
        // 请求会经过 ProductController -> ProductService -> real ProductRepository -> AWS dev DynamoDB。
        mockMvc.perform(get("/api/getAllProducts")) // 用 MockMvc 模拟发送 GET request。
                // 先检查 HTTP status 是 200。
                .andExpect(status().isOk()) // 断言 response status 是 200 OK。
                // 再检查返回 JSON 里有 products array。
                .andExpect(jsonPath("$.products").isArray()) // 断言 JSON 里的 products 字段是 array。
                // 检查第一个 product 的字段是否来自 dev DynamoDB 里的 Terraform seed data。
                .andExpect(jsonPath("$.products[0].id").value(1)) // 断言第一个 product 的 id 是 1。
                .andExpect(jsonPath("$.products[0].title").value("First product")) // 断言第一个 product 的 title 是 First product。
                // 检查第二个 product，确认 repository 真的读到了多条 DynamoDB item。
                .andExpect(jsonPath("$.products[1].id").value(2)) // 断言第二个 product 的 id 是 2。
                .andExpect(jsonPath("$.products[1].title").value("Second product")) // 断言第二个 product 的 title 是 Second product。
                // Terraform 目前 seed 了 6 个 product。
                .andExpect(jsonPath("$.products.length()").value(6)); // 断言 products array 一共有 6 个元素。
    }
} // test class 结束。
