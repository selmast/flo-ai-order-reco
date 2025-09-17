package com.floai.backend.service;

import com.floai.backend.dto.ProductDto;
import com.floai.backend.dto.RecommendationItemDto;
import com.floai.backend.model.Order;
import com.floai.backend.model.Product;
import com.floai.backend.repository.OrderRepository;
import com.floai.backend.repository.ProductRepository;
import io.micrometer.core.annotation.Timed;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationEngine {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final FeedbackService feedbackService;

    public RecommendationEngine(OrderRepository orderRepository,
                                ProductRepository productRepository,
                                FeedbackService feedbackService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.feedbackService = feedbackService;
    }

    /**
     * Order-aware recommendations (re-using your heuristic + feedback).
     * Cached for a short TTL (configured in CacheConfig) per orderId.
     */
    @Timed(value = "reco_for_order_timer", description = "Time to compute order-aware recommendations")
    @Cacheable(cacheNames = "recoByOrder", key = "#orderId", unless = "#result == null || #result.isEmpty()")
    public List<RecommendationItemDto> forOrder(Long orderId, int limit) {
        Order order = orderRepository.findDetailedById(orderId).orElse(null);
        if (order == null) return List.of();

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

            var f = feedbackService.get(p.getId());
            long pos = f.addedToCart + f.purchased;
            long neg = f.ignored;
            if (pos + neg > 0) {
                double ctr = (double) pos / (pos + neg);
                score += 0.20 * ctr;
            }

            double rounded = BigDecimal.valueOf(score).setScale(3, RoundingMode.HALF_UP).doubleValue();

            recs.add(new RecommendationItemDto(
                    p.getId(), p.getName(), p.getBrand(), p.getCategory(), rounded
            ));
        }

        recs.sort(Comparator
                .comparingDouble(RecommendationItemDto::getScore).reversed()
                .thenComparing(RecommendationItemDto::getName, Comparator.nullsLast(String::compareToIgnoreCase)));

        if (limit > 0 && limit < recs.size()) recs = recs.subList(0, limit);
        return recs;
    }

    /** Simple popularity list used for tracking page fallback. */
    @Timed(value = "reco_popular_timer", description = "Time to compute popular items")
    public List<ProductDto> popular(int limit) {
        List<Map.Entry<Product, Double>> ranked = new ArrayList<>();
        for (Product p : productRepository.findAll()) {
            if (p == null || p.getId() == null) continue;

            var f = feedbackService.get(p.getId());
            long pos = f.addedToCart + f.purchased;
            long neg = f.ignored;

            double score = (pos + neg == 0) ? 0.0 :
                    ((double) pos / (pos + neg)) * (0.5 + 0.5 * Math.min(1.0, Math.log10(Math.max(1, pos + neg)) / 2.0));
            double rounded = BigDecimal.valueOf(score).setScale(3, RoundingMode.HALF_UP).doubleValue();
            ranked.add(Map.entry(p, rounded));
        }

        ranked.sort((a, b) -> {
            int cmp = Double.compare(b.getValue(), a.getValue());
            if (cmp != 0) return cmp;
            String n1 = a.getKey().getName(), n2 = b.getKey().getName();
            if (n1 == null && n2 == null) return 0;
            if (n1 == null) return 1;
            if (n2 == null) return -1;
            return n1.compareToIgnoreCase(n2);
        });
        if (limit > 0 && limit < ranked.size()) ranked = ranked.subList(0, limit);

        return ranked.stream()
                .map(e -> new ProductDto(
                        e.getKey().getId(),
                        e.getKey().getName(),
                        e.getKey().getDescription(),
                        e.getKey().getBrand(),
                        e.getKey().getCategory()
                ))
                .toList();
    }

    /** Evict recommendations cache for an order when we receive feedback tied to that order. */
    @CacheEvict(cacheNames = "recoByOrder", key = "#orderId", condition = "#orderId != null")
    public void evictOrderCache(Long orderId) {
        // no-op; Spring handles eviction by annotation
    }
}
