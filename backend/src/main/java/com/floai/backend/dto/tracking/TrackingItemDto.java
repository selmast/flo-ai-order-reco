package com.floai.backend.dto.tracking;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "TrackingItem")
public record TrackingItemDto(
        Long productId,
        String name,
        String brand,
        String category,
        int qty
) {}
