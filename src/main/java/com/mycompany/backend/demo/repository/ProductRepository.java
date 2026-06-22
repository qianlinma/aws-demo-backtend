package com.mycompany.backend.demo.repository;

import com.mycompany.backend.demo.model.Product;
import org.springframework.stereotype.Repository;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.ScanRequest;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Repository
public class ProductRepository {
    private final DynamoDbClient dynamoDbClient;
    private final String productsTableName;

    public ProductRepository() {
        // Integration test 会在 CodeBuild 里访问真实的 dev DynamoDB。
        // 不是严格意义上的“dev 环境”，CodeBuild 是一个临时启动的构建容器。
        // 但容器本身是每次 build 临时创建的，不会自动知道“我是 dev，所以表名是哪个、region 是哪个”。
        // 所以我们要通过环境变量明确告诉它：AWS_REGION=us-west-2，PRODUCTS_TABLE_NAME=demo-products-tf。
        // CodeBuild 运行时不一定能像本机一样自动推断 AWS region，
        // 所以这里优先读取 AWS_REGION，其次读取 AWS_DEFAULT_REGION，最后才默认 us-west-2。
        // 这样同一份 backend code 在本地、CodeBuild、ECS 里都能明确知道要访问哪个 region。
        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv().getOrDefault("AWS_REGION",
                        System.getenv().getOrDefault("AWS_DEFAULT_REGION", "us-west-2"))))
                .build();
        // table name 也从环境变量读取，避免把 dev/prod 的 DynamoDB table 写死在代码里。
        // 现在 dev 默认是 demo-products-tf；以后 prod 可以通过 PRODUCTS_TABLE_NAME 指向 prod table。
        this.productsTableName = System.getenv().getOrDefault("PRODUCTS_TABLE_NAME", "demo-products-tf");
    }

    public List<Product> findAll() {
        return dynamoDbClient.scan(ScanRequest.builder()
                        .tableName(productsTableName)
                        .build())
                .items()
                .stream()
                .map(this::toProduct)
                .sorted(Comparator.comparing(Product::id))
                .toList();
    }

    private Product toProduct(Map<String, AttributeValue> item) {
        return new Product(
                Integer.parseInt(item.get("id").n()),
                item.get("path").s(),
                item.get("title").s()
        );
    }
}
