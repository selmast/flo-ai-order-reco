// src/main/java/com/floai/backend/dto/OrderSummaryDto.java
package com.floai.backend.dto;

public class OrderSummaryDto {
    private Long id;
    private String status;

    public OrderSummaryDto(Long id, String status) {
        this.id = id;
        this.status = status;
    }

    public Long getId() { return id; }
    public String getStatus() { return status; }
}
