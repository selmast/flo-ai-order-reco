package com.floai.backend.dto;

import java.time.OffsetDateTime;

public record FeedbackDto(
        Long id,
        Long productId,
        Long orderId,
        String action,
        OffsetDateTime createdAt
) {}