package com.floai.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Item belonging to an order")
public record OrderItemDto(

        @Schema(description = "Unique identifier of the order item", example = "7")
        Long id,

        @Schema(description = "ID of the product in this order item", example = "1")
        Long productId,

        @Schema(description = "Name of the product", example = "Kitchen Sink")
        String productName,

        @Schema(description = "Quantity of the product in this order item", example = "2")
        int quantity,

        @Schema(description = "Unit price of the product at the time of order", example = "99.99")
        double price
) {}
