package com.floai.backend.mapper;

import com.floai.backend.dto.ProductDto;
import com.floai.backend.model.Product;

public final class ProductMapper {
    private ProductMapper() {}

    public static ProductDto toDto(Product p) {
        if (p == null) return null;
        return new ProductDto(
                p.getId(),
                p.getName(),
                p.getDescription(),
                p.getBrand(),
                p.getCategory()
        );
    }
}