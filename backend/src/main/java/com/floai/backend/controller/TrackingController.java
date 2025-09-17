package com.floai.backend.controller;

import com.floai.backend.dto.RecommendationItemDto;
import com.floai.backend.dto.tracking.TrackingItemDto;
import com.floai.backend.dto.tracking.TrackingOrderDto;
import com.floai.backend.dto.tracking.TrackingPageDto;
import com.floai.backend.model.Order;
import com.floai.backend.model.OrderItem;
import com.floai.backend.model.Product;
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
import java.util.Objects;

@RestController
@CrossOrigin(
        origins = {
                "http://localhost:5173", "http://127.0.0.1:5173",
                "http://localhost:3000", "http://127.0.0.1:3000"
        },
        methods = { RequestMethod.GET, RequestMethod.OPTIONS },
        allowedHeaders = "*",
        maxAge = 3600,
        allowCredentials = "false"
)
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

    @Operation(summary = "Tracking page data (order + recommendations)", operationId = "trackingPage")
    @Timed(value = "tracking_page_timer", description = "Time to build tracking page payload")
    @GetMapping("/{orderId}")
    public ResponseEntity<TrackingPageDto> page(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "6") @Min(1) @Max(50) int limit
    ) {
        Order order = orderRepository.findDetailedById(orderId).orElse(null);
        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        // map order items -> lightweight DTOs
        List<TrackingItemDto> items = order.getItems().stream()
                .filter(Objects::nonNull)
                .map(TrackingController::toDto)
                .toList();

        TrackingOrderDto orderDto = new TrackingOrderDto(order.getId(), order.getStatus(), items);
        List<RecommendationItemDto> recs = recommendationEngine.forOrder(orderId, limit);

        return ResponseEntity.ok(new TrackingPageDto(orderDto, recs));
    }

    private static TrackingItemDto toDto(OrderItem it) {
        Product p = it.getProduct();
        Long   productId = (p != null ? p.getId()       : null);
        String name      = (p != null ? p.getName()     : null);
        String brand     = (p != null ? p.getBrand()    : null);
        String category  = (p != null ? p.getCategory() : null);

        // getQuantity() is primitive int; clamp to at least 1 so UI always shows a number
        int qty = Math.max(1, it.getQuantity());

        return new TrackingItemDto(productId, name, brand, category, qty);
    }
}
