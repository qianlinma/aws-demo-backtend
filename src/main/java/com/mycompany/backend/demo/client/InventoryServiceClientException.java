package com.mycompany.backend.demo.client;

public class InventoryServiceClientException extends RuntimeException {
    public InventoryServiceClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
