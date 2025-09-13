package com.floai.backend.controller;

import com.floai.backend.dto.RecommendationItemDto;
import com.floai.backend.dto.RecommendationFeedbackRequest;
import com.floai.backend.model.Order;
import com.floai.backend.model.Product;
import com.floai.backend.repository.OrderRepository;
import com.floai.backend.repository.ProductRepository;
import com.floai.backend.service.FeedbackService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final FeedbackService feedbackService;

    public RecommendationController(OrderRepository orderRepository,
                                    ProductRepository productRepository,
                                    FeedbackService feedbackService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.feedbackService = feedbackService;
    }

    // GET /api/recommendations/{orderId}?limit=5
    @GetMapping("/{orderId}")
    public ResponseEntity<List<RecommendationItemDto>> forOrder(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "5") int limit
    ) {
        var opt = orderRepository.findDetailedById(orderId);
        if (opt.isEmpty()) return ResponseEntity.notFound().build();

        Order order = opt.get();

        // products already in the order
        Set<Long> inOrderIds = order.getItems().stream()
                .map(i -> i.getProduct().getId())
                .collect(Collectors.toSet());

        // simple signals from the order to bias scoring
        Set<String> categoriesInOrder = order.getItems().stream()
                .map(i -> i.getProduct().getCategory())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> brandsInOrder = order.getItems().stream()
                .map(i -> i.getProduct().getBrand())
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // score candidates: all products not already in the order
        List<RecommendationItemDto> recs = new ArrayList<>();
        for (Product p : productRepository.findAll()) {
            if (inOrderIds.contains(p.getId())) continue;

            double score = 0.10; // base score
            if (p.getCategory() != null && categoriesInOrder.contains(p.getCategory())) score += 0.50;
            if (p.getBrand() != null && brandsInOrder.contains(p.getBrand())) score += 0.20;

            // tiny feedback-based boost
            var f = feedbackService.get(p.getId());
            long pos = f.addedToCart + f.purchased;
            long neg = f.ignored;
            if (pos + neg > 0) {
                double ctr = (double) pos / (pos + neg); // 0..1
                score += 0.20 * ctr; // cap feedback influence
            }

            recs.add(new RecommendationItemDto(
                    p.getId(),
                    p.getName(),
                    p.getBrand(),
                    p.getCategory(),
                    score
            ));
        }

        // sort by score desc, tie-break by name, then limit
        recs.sort(Comparator
                .comparingDouble(RecommendationItemDto::getScore).reversed()
                .thenComparing(RecommendationItemDto::getName, Comparator.nullsLast(String::compareToIgnoreCase)));

        if (limit > 0 && limit < recs.size()) {
            recs = recs.subList(0, limit);
        }

        return ResponseEntity.ok(recs);
    }

    // POST /api/recommendations/{orderId}/feedback
    @PostMapping("/{orderId}/feedback")
    public ResponseEntity<Void> recordFeedback(@PathVariable Long orderId,
                                               @Valid @RequestBody RecommendationFeedbackRequest req) {
        // orderId is contextual for now; we aggregate per product in-memory
        feedbackService.record(req.getProductId(), req.getAction());
        return ResponseEntity.accepted().build();
    }
}
