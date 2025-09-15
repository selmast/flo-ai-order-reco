package com.floai.backend.dto;

public class PopularItemDto {
    private Long productId;
    private String name;
    private String brand;
    private String category;
    private double score;

    private long viewed;
    private long ignored;
    private long addedToCart;
    private long purchased;

    public PopularItemDto(Long productId, String name, String brand, String category,
                          double score, long viewed, long ignored, long addedToCart, long purchased) {
        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.category = category;
        this.score = score;
        this.viewed = viewed;
        this.ignored = ignored;
        this.addedToCart = addedToCart;
        this.purchased = purchased;
    }

    public Long getProductId() { return productId; }
    public String getName() { return name; }
    public String getBrand() { return brand; }
    public String getCategory() { return category; }
    public double getScore() { return score; }
    public long getViewed() { return viewed; }
    public long getIgnored() { return ignored; }
    public long getAddedToCart() { return addedToCart; }
    public long getPurchased() { return purchased; }

    public void setProductId(Long productId) { this.productId = productId; }
    public void setName(String name) { this.name = name; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setCategory(String category) { this.category = category; }
    public void setScore(double score) { this.score = score; }
    public void setViewed(long viewed) { this.viewed = viewed; }
    public void setIgnored(long ignored) { this.ignored = ignored; }
    public void setAddedToCart(long addedToCart) { this.addedToCart = addedToCart; }
    public void setPurchased(long purchased) { this.purchased = purchased; }
}
