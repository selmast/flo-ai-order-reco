package com.floai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Request body for creating a feedback event.
 * Example:
 * {
 *   "productId": 1,
 *   "orderId": null,
 *   "action": "VIEWED"
 * }
 */
public record FeedbackCreateRequest(
        @NotNull Long productId,
        Long orderId,                 // optional
        @NotBlank String action       // viewed | ignored | added_to_cart | purchased
) {}
