package com.mycompany.backend.demo.controller;

import com.mycompany.backend.demo.client.InventoryServiceClientException;
import io.grpc.StatusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import software.amazon.awssdk.core.exception.SdkException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({InventoryServiceClientException.class, StatusRuntimeException.class, SdkException.class})
    public ResponseEntity<ApiErrorResponse> handleDependencyFailure(RuntimeException e) {
        logger.warn("Dependency failure while handling request", e);
        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(new ApiErrorResponse("SERVICE_UNAVAILABLE", "A downstream service is unavailable"));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedFailure(RuntimeException e) {
        logger.error("Unexpected failure while handling request", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiErrorResponse("INTERNAL_SERVER_ERROR", "Unexpected server error"));
    }
}
