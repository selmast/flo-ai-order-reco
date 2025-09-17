package com.floai.backend.dto.tracking;

import com.floai.backend.dto.RecommendationItemDto;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "TrackingPage")
public record TrackingPageDto(
        TrackingOrderDto order,
        List<RecommendationItemDto> recommendations
) {}
