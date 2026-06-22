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
        this.dynamoDbClient = DynamoDbClient.builder()
                .region(Region.of(System.getenv().getOrDefault("AWS_REGION",
                        System.getenv().getOrDefault("AWS_DEFAULT_REGION", "us-west-2"))))
                .build();
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
