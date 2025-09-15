package com.floai.backend.model;

import jakarta.persistence.*;
import lombok.Builder;

import java.time.OffsetDateTime;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(hidden = true)
@Entity
@Table(
        name = "feedback_events",
        indexes = {
                @Index(name = "idx_feedback_events_product_action", columnList = "product_id, action"),
                @Index(name = "idx_feedback_events_created_at", columnList = "created_at")
        }
)


@Builder
public class FeedbackEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    // Optional link to order; nullable
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "action", nullable = false, length = 32)
    private String action;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt = OffsetDateTime.now();

    protected FeedbackEvent() {
        // JPA only
    }

    public FeedbackEvent(Long productId, Long orderId, String action) {
        this.productId = productId;
        this.orderId = orderId;
        this.action = action != null ? action.toLowerCase() : null;
    }

    // Getters & Setters
    public Long getId() { return id; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getAction() { return action; }
    public void setAction(String action) { this.action = action != null ? action.toLowerCase() : null; }

    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
}
