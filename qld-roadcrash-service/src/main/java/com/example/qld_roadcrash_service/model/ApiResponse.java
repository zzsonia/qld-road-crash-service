package com.example.qld_roadcrash_service.model;

public record ApiResponse<T>(
        boolean status,
        String message,
        T data
) {}