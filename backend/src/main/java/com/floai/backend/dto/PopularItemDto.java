package com.floai.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Popular product item with feedback statistics and score")
public record PopularItemDto(
        @Schema(description = "Product ID", example = "1")
        Long productId,

        @Schema(description = "Product name", example = "Running Shoes")
        String name,

        @Schema(description = "Brand of the product", example = "Nike")
        String brand,

        @Schema(description = "Category of the product", example = "Sportswear")
        String category,

        @Schema(description = "Aggregated score based on feedback signals", example = "4.75")
        double score,

        @Schema(description = "Number of times viewed", example = "120")
        long viewed,

        @Schema(description = "Number of times ignored", example = "30")
        long ignored,

        @Schema(description = "Number of times added to cart", example = "25")
        long addedToCart,

        @Schema(description = "Number of times purchased", example = "10")
        long purchased
) {}
