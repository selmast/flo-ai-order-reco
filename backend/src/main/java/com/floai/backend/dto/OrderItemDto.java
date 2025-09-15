package com.floai.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Order item details")
public record OrderItemDto(

        @Schema(description = "Unique identifier of the order item", example = "7")
        Long id,

        @Schema(description = "Quantity of the product in this order item", example = "2")
        int quantity,

        @Schema(description = "Product details for the item")
        ProductDto product
) {}
