package com.floai.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class RecommendationFeedbackRequest {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotBlank(message = "action is required")
    @Pattern(
            regexp = "viewed|ignored|added_to_cart|purchased",
            message = "action must be one of: viewed, ignored, added_to_cart, purchased"
    )
    private String action;

    public Long getProductId() {
        return productId;
    }
    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getAction() {
        return action;
    }
    public void setAction(String action) {
        this.action = action;
    }
}
