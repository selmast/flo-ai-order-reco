package com.floai.backend.dto;

public class RecommendationItemDto {
    private Long productId;
    private String name;
    private String brand;
    private String category;
    private double score;

    public RecommendationItemDto(Long productId, String name, String brand, String category, double score) {
        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.score = score;
    }

    public Long getProductId() { return productId; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getCategory() { return category; }
    public double getScore() { return score; }
}
