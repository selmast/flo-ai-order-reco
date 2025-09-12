// src/main/java/com/floai/backend/service/RecommendationService.java
package com.floai.backend.service;

import com.floai.backend.model.Order;
import com.floai.backend.model.OrderItem;
import com.floai.backend.model.Product;
import com.floai.backend.repository.OrderRepository;
import com.floai.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {

    private final OrderRepository orderRepo;
    private final ProductRepository productRepo;

    public RecommendationService(OrderRepository orderRepo, ProductRepository productRepo) {
        this.orderRepo = orderRepo;
        this.productRepo = productRepo;
    }

    public List<Product> recommendForOrder(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new NoSuchElementException("Order %d not found".formatted(orderId)));

        Set<Long> inOrder = order.getItems().stream()
                .map(OrderItem::getProduct)
                .filter(Objects::nonNull)
                .map(Product::getId)
                .collect(Collectors.toSet());

        List<Product> recs;
        if (!inOrder.isEmpty()) {
            recs = productRepo.findTop5ByIdNotIn(inOrder);
        } else {
            recs = productRepo.findTop5ByOrderByIdAsc();
        }

        // Fallback fill if DB has very few rows
        if (recs.size() < 5) {
            List<Product> all = productRepo.findAll();
            for (Product p : all) {
                if (recs.size() >= 5) break;
                if (!inOrder.contains(p.getId()) && !recs.contains(p)) recs.add(p);
            }
        }
        return recs;
    }
}
