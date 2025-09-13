package com.floai.backend.dto;

public record OrderItemDto(Long id, ProductDto product, int quantity) {}