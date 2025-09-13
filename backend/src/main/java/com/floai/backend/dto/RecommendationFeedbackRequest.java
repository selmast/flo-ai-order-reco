package com.floai.backend.dto;

import jakarta.validation.constraints.NotNull;

public class RecommendationFeedbackRequest {
    @NotNull
    private Long productId;
    @NotNull
    private String action; // "viewed" | "ignored" | "added_to_cart" | "purchased"

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
}
