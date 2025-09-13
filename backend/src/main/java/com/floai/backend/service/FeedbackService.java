package com.floai.backend.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FeedbackService {

    public static class Counters {
        public long viewed;
        public long ignored;
        public long addedToCart;
        public long purchased;
    }

    // key: productId
    private final Map<Long, Counters> productCounters = new ConcurrentHashMap<>();

    public void record(Long productId, String action) {
        var c = productCounters.computeIfAbsent(productId, id -> new Counters());
        switch (action.toLowerCase()) {
            case "viewed" -> c.viewed++;
            case "ignored" -> c.ignored++;
            case "added_to_cart" -> c.addedToCart++;
            case "purchased" -> c.purchased++;
            default -> { /* ignore unknown */ }
        }
    }

    public Counters get(Long productId) {
        return productCounters.getOrDefault(productId, new Counters());
    }
}
