package com.floai.backend.controller;

import com.floai.backend.dto.RecommendationItemDto;
import com.floai.backend.model.Order;
import com.floai.backend.repository.OrderRepository;
import com.floai.backend.service.RecommendationEngine;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/tracking", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Tracking")
public class TrackingController {

    private final OrderRepository orderRepository;
    private final RecommendationEngine recommendationEngine;

    public TrackingController(OrderRepository orderRepository,
                              RecommendationEngine recommendationEngine) {
        this.orderRepository = orderRepository;
        this.recommendationEngine = recommendationEngine;
    }

    /** Minimal order DTO that matches your model (id + status). */
    public record TrackingOrderDto(Long id, String status) {}

    /** Payload returned to the tracking page: order info + recs. */
    public record TrackingPageDto(TrackingOrderDto order,
                                  List<RecommendationItemDto> recommendations) {}

    @Operation(
            summary = "Tracking page data (order + recommendations)",
            operationId = "trackingPage"
    )
    @Timed(value = "tracking_page_timer", description = "Time to build tracking page payload")
    @GetMapping("/{orderId}")
    public ResponseEntity<TrackingPageDto> page(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "6")
            @Min(1) @Max(50) int limit
    ) {
        Order order = orderRepository.findDetailedById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        TrackingOrderDto orderDto = new TrackingOrderDto(order.getId(), order.getStatus());
        List<RecommendationItemDto> recs = recommendationEngine.forOrder(orderId, limit);

        return ResponseEntity.ok(new TrackingPageDto(orderDto, recs));
    }
}
