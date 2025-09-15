package com.floai.backend.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Product data transfer object")
public record ProductDto(

        @Schema(description = "Unique identifier of the product", example = "101")
        Long id,

        @NotBlank
        @Size(max = 255)
        @Schema(description = "Name of the product", example = "Running Shoes")
        String name,

        @Size(max = 2000)
        @Schema(description = "Detailed description of the product", example = "Lightweight running shoes with breathable mesh")
        String description,

        @NotBlank
        @Size(max = 100)
        @Schema(description = "Brand of the product", example = "Nike")
        String brand,

        @NotBlank
        @Size(max = 100)
        @Schema(description = "Category of the product", example = "Footwear")
        String category
) {
    public ProductDto {
        // Normalize input: trim whitespace
        if (name != null) name = name.trim();
        if (description != null) description = description.trim();
        if (brand != null) brand = brand.trim();
        if (category != null) category = category.trim();
    }
}
