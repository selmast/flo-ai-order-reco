package com.floai.backend.dto;

public class RecommendationResponse {
    private Long productId;
    private double score;

    public RecommendationResponse() {}
    public RecommendationResponse(Long productId, double score) {
        this.productId = productId;
        this.score = score;
    }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public double getScore() { return score; }
    public void setScore(double score) { this.score = score; }
}
