// src/main/java/com/floai/backend/dto/OrderDto.java
package com.floai.backend.dto;

import java.util.List;

public record OrderDto(Long id, String status, List<OrderItemDto> items) {}

