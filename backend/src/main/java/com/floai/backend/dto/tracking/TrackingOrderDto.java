package com.floai.backend.dto.tracking;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "TrackingOrder")
public record TrackingOrderDto(
        Long id,
        String status,
        List<TrackingItemDto> items
) {}
