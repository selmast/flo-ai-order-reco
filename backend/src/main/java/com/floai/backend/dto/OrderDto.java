package com.floai.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;

@Schema(description = "Order summary and details")
public record OrderDto(

        @Schema(description = "Unique identifier of the order", example = "1001")
        Long id,

        @Schema(description = "UTC timestamp when the order was created",
                example = "2025-09-15T12:00:00Z")
        Instant createdAt,

        @Schema(description = "Total price of the order in the store's default currency",
                example = "199.99")
        double totalPrice,

        @Schema(description = "List of order items included in this order")
        List<OrderItemDto> items
) {}
