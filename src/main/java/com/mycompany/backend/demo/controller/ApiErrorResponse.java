package com.mycompany.backend.demo.controller;

public record ApiErrorResponse(
        String error,
        String message
) {
}
