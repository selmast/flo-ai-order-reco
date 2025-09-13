package com.floai.backend.dto;

public class CreateOrderResponse {
    private Long id;
    private String status;

    public CreateOrderResponse(Long id, String status) {
        this.id = id; this.status = status;
    }
    public Long getId() { return id; }
    public String getStatus() { return status; }
}
