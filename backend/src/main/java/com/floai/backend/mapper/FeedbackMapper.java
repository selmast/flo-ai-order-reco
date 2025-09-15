package com.floai.backend.mapper;

import com.floai.backend.dto.FeedbackCreateRequest;
import com.floai.backend.dto.FeedbackDto;
import com.floai.backend.model.FeedbackEvent;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Component
public class FeedbackMapper {

    /**
     * Build a FeedbackEvent entity from the create request.
     */
    public FeedbackEvent fromCreateRequest(FeedbackCreateRequest req) {
        if (req == null) return null;

        FeedbackEvent event = new FeedbackEvent(
                req.productId(),
                req.orderId(),
                req.action()
        );
        event.setCreatedAt(OffsetDateTime.now());
        return event;
    }

    /**
     * Convert entity to DTO.
     */
    public FeedbackDto toDto(FeedbackEvent e) {
        if (e == null) return null;
        return new FeedbackDto(
                e.getId(),
                e.getProductId(),
                e.getOrderId(),
                e.getAction(),
                e.getCreatedAt()
        );
    }
}
