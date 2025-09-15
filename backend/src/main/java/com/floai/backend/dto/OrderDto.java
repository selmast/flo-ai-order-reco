package com.floai.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Order details including items")
public record OrderDto(

        @Schema(description = "Unique identifier of the order", example = "1001")
        Long id,

        @Schema(description = "List of order items included in this order")
        List<OrderItemDto> items
) {}
