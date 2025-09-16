package com.floai.backend.controller;

import com.floai.backend.dto.PopularItemDto;
import com.floai.backend.dto.ProductDto;
import com.floai.backend.dto.RecommendationFeedbackRequest;
import com.floai.backend.dto.RecommendationItemDto;
import com.floai.backend.model.Order;
import com.floai.backend.model.Product;
import com.floai.backend.repository.OrderRepository;
import com.floai.backend.repository.ProductRepository;
import com.floai.backend.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping(
        value = {"/api/recommendations", "/recommendations"},
        produces = MediaType.APPLICATION_JSON_VALUE
)
@Validated
@Tag(name = "Recommendations")
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
    @Operation(summary = "Order-aware recommendations",
            description = "Returns products recommended for a given order, ranked by score.")
    @GetMapping("/{orderId}")
    public ResponseEntity<List<RecommendationItemDto>> forOrder(
            @PathVariable Long orderId,
            @RequestParam(defaultValue = "5")
            @Min(value = 1, message = "limit must be at least 1")
            @Max(value = 50, message = "limit must be at most 50")
            int limit
    ) {
        var opt = orderRepository.findDetailedById(orderId);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Order order = opt.get();

        // products already in the order
        Set<Long> inOrderIds = order.getItems().stream()
                .map(i -> i.getProduct())
                .filter(Objects::nonNull)
                .map(Product::getId)
                .collect(Collectors.toSet());

        // signals from the order to bias scoring
        Set<String> categoriesInOrder = order.getItems().stream()
                .map(i -> i.getProduct())
                .filter(Objects::nonNull)
                .map(Product::getCategory)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<String> brandsInOrder = order.getItems().stream()
                .map(i -> i.getProduct())
                .filter(Objects::nonNull)
                .map(Product::getBrand)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<RecommendationItemDto> recs = new ArrayList<>();
        for (Product p : productRepository.findAll()) {
            if (p == null || p.getId() == null) continue;
            if (inOrderIds.contains(p.getId())) continue;

            double score = 0.10; // base
            if (p.getCategory() != null && categoriesInOrder.contains(p.getCategory())) score += 0.50;
            if (p.getBrand() != null && brandsInOrder.contains(p.getBrand())) score += 0.20;

            // feedback-based boost (CTR-like)
            var f = feedbackService.get(p.getId());
            long pos = f.addedToCart + f.purchased;
            long neg = f.ignored;
            if (pos + neg > 0) {
                double ctr = (double) pos / (pos + neg); // 0..1
                score += 0.20 * ctr;
            }

            double rounded = BigDecimal.valueOf(score)
                    .setScale(3, RoundingMode.HALF_UP)
                    .doubleValue();

            recs.add(new RecommendationItemDto(
                    p.getId(),
                    p.getName(),
                    p.getBrand(),
                    p.getCategory(),
                    rounded
            ));
        }

        // sort by score desc, tie-break by name
        recs.sort(
                Comparator.comparingDouble(RecommendationItemDto::getScore).reversed()
                        .thenComparing(RecommendationItemDto::getName,
                                Comparator.nullsLast(String::compareToIgnoreCase))
        );

        if (limit > 0 && limit < recs.size()) {
            recs = recs.subList(0, limit);
        }
        return ResponseEntity.ok(recs);
    }

    // GET /api/recommendations/popular?limit=10  -> return ProductDto (id, name, description, brand, category)
    @Operation(summary = "Popular products (simple)",
            description = "Returns a ranked list of popular products as ProductDto.")
    @GetMapping("/popular")
    public ResponseEntity<List<ProductDto>> popularSimple(
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "limit must be at least 1")
            @Max(value = 50, message = "limit must be at most 50")
            int limit
    ) {
        // rank products by the same popularity score as detailed stats
        List<Map.Entry<Product, Double>> ranked = new ArrayList<>();

        for (Product p : productRepository.findAll()) {
            if (p == null || p.getId() == null) continue;

            var f = feedbackService.get(p.getId());
            long pos = f.addedToCart + f.purchased;
            long neg = f.ignored;

            double score;
            if (pos + neg == 0) {
                score = 0.0;
            } else {
                double ctr = (double) pos / (pos + neg); // 0..1
                double volumeWeight = Math.min(1.0, Math.log10(Math.max(1, pos + neg)) / 2.0);
                score = ctr * (0.5 + 0.5 * volumeWeight);
            }
            double rounded = BigDecimal.valueOf(score).setScale(3, RoundingMode.HALF_UP).doubleValue();
            ranked.add(Map.entry(p, rounded));
        }

        ranked.sort((a, b) -> {
            int cmp = Double.compare(b.getValue(), a.getValue()); // desc by score
            if (cmp != 0) return cmp;
            String n1 = a.getKey().getName();
            String n2 = b.getKey().getName();
            if (n1 == null && n2 == null) return 0;
            if (n1 == null) return 1;
            if (n2 == null) return -1;
            return n1.compareToIgnoreCase(n2);
        });

        if (limit > 0 && limit < ranked.size()) {
            ranked = ranked.subList(0, limit);
        }

        List<ProductDto> out = ranked.stream()
                .map(e -> new ProductDto(
                        e.getKey().getId(),
                        e.getKey().getName(),
                        e.getKey().getDescription(),
                        e.getKey().getBrand(),
                        e.getKey().getCategory()
                ))
                .toList();

        return ResponseEntity.ok(out);
    }

    // GET /api/recommendations/popular/stats?limit=10  -> detailed metrics (PopularItemDto)
    @Operation(summary = "Popular products (detailed)",
            description = "Returns popularity score and counters per product.")
    @GetMapping("/popular/stats")
    public ResponseEntity<List<PopularItemDto>> popularDetailed(
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "limit must be at least 1")
            @Max(value = 50, message = "limit must be at most 50")
            int limit
    ) {
        List<PopularItemDto> items = new ArrayList<>();

        for (Product p : productRepository.findAll()) {
            if (p == null || p.getId() == null) continue;

            var f = feedbackService.get(p.getId());
            long viewed = f.viewed;
            long ignored = f.ignored;
            long addedToCart = f.addedToCart;
            long purchased = f.purchased;

            long pos = addedToCart + purchased;
            long neg = ignored;
            double score;
            if (pos + neg == 0) {
                score = 0.0;
            } else {
                double ctr = (double) pos / (pos + neg); // 0..1
                double volumeWeight = Math.min(1.0, Math.log10(Math.max(1, pos + neg)) / 2.0);
                score = ctr * (0.5 + 0.5 * volumeWeight);
            }
            double rounded = BigDecimal.valueOf(score).setScale(3, RoundingMode.HALF_UP).doubleValue();

            items.add(new PopularItemDto(
                    p.getId(),
                    p.getName(),
                    p.getBrand(),
                    p.getCategory(),
                    rounded,
                    viewed,
                    ignored,
                    addedToCart,
                    purchased
            ));
        }

        items.sort(
                Comparator.comparingDouble(PopularItemDto::score).reversed()
                        .thenComparing(PopularItemDto::name, Comparator.nullsLast(String::compareToIgnoreCase))
        );

        if (limit > 0 && limit < items.size()) {
            items = items.subList(0, limit);
        }
        return ResponseEntity.ok(items);
    }

    // POST /api/recommendations/{orderId}/feedback
    @Operation(summary = "Record recommendation feedback",
            description = "Adds a feedback signal (view, add-to-cart, purchase, ignore) for a product.")
    @PostMapping(value = "/{orderId}/feedback", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Void> recordFeedback(@PathVariable Long orderId,
                                               @Valid @RequestBody RecommendationFeedbackRequest req) {
        // Note: we keep orderId for future personalization; aggregation is per product
        feedbackService.record(req.getProductId(), req.getAction());
        return ResponseEntity.accepted().build();
    }
}
