package com.floai.backend.service;

import com.floai.backend.model.FeedbackEvent;
import com.floai.backend.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
public class FeedbackService {

    public static class Counters {
        public long viewed;
        public long ignored;
        public long addedToCart;
        public long purchased;

        public double score() {
            return viewed * 0.1
                    + ignored * -0.2
                    + addedToCart * 0.5
                    + purchased * 1.0;
        }
    }

    private final FeedbackRepository repo;

    public FeedbackService(FeedbackRepository repo) {
        this.repo = repo;
    }

    /** Persist a single feedback event (action is case-/underscore-insensitive). */
    @Transactional
    public void record(Long productId, String action) {
        record(productId, action, null);
    }

    /** Persist a single feedback event with optional orderId. */
    @Transactional
    public void record(Long productId, String action, Long orderId) {
        if (productId == null || action == null || action.isBlank()) return;
        String a = normalizeAction(action);
        repo.save(new FeedbackEvent(productId, orderId, a));
    }

    /** Aggregate counters for a product by querying the DB. */
    @Transactional(readOnly = true)
    public Counters get(Long productId) {
        var c = new Counters();
        c.viewed      = repo.countByProductAndAction(productId, "viewed");
        c.ignored     = repo.countByProductAndAction(productId, "ignored");
        c.addedToCart = repo.countByProductAndAction(productId, "added_to_cart");
        c.purchased   = repo.countByProductAndAction(productId, "purchased");
        return c;
    }

    /** Convenience: score for a single product. */
    @Transactional(readOnly = true)
    public double getScore(Long productId) {
        return get(productId).score();
    }

    /**
     * All product scores in one shot (uses GROUP BY in the repository).
     * Falls back to 0 when some counters are null.
     */
    @Transactional(readOnly = true)
    public Map<Long, Double> getAllScores() {
        Map<Long, Double> out = new HashMap<>();
        for (var a : repo.aggregateCountsAll()) {
            out.put(a.getProductId(), scoreOf(a));
        }
        return out;
    }

    /**
     * All product scores limited to the last N days.
     * Uses Instant-based filtering to work on both Postgres & H2.
     */
    @Transactional(readOnly = true)
    public Map<Long, Double> getAllScoresSinceDays(int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        Map<Long, Double> out = new HashMap<>();
        for (var a : repo.aggregateCountsSince(since)) {
            out.put(a.getProductId(), scoreOf(a));
        }
        return out;
    }

    /**
     * Scores for a specific set of product IDs limited to the last N days.
     */
    @Transactional(readOnly = true)
    public Map<Long, Double> getScoresForProductsSinceDays(Collection<Long> productIds, int days) {
        Instant since = Instant.now().minus(days, ChronoUnit.DAYS);
        Map<Long, Double> out = new HashMap<>();
        for (var a : repo.aggregateCountsForProductsSince(productIds, since)) {
            out.put(a.getProductId(), scoreOf(a));
        }
        return out;
    }

    // ---------- helpers ----------

    private static double scoreOf(FeedbackRepository.FeedbackAggregate a) {
        long viewed      = n(a.getViewed());
        long ignored     = n(a.getIgnored());
        long addedToCart = n(a.getAddedToCart());
        long purchased   = n(a.getPurchased());
        return viewed * 0.1
                + ignored * -0.2
                + addedToCart * 0.5
                + purchased * 1.0;
    }

    private static long n(Long v) { return v == null ? 0L : v; }

    private static String normalizeAction(String action) {
        String a = action.trim().toLowerCase(Locale.ROOT).replace(' ', '_');
        return switch (a) {
            case "view", "viewed" -> "viewed";
            case "ignore", "ignored" -> "ignored";
            case "add_to_cart", "added_to_cart", "cart_add" -> "added_to_cart";
            case "purchase", "purchased", "buy" -> "purchased";
            default -> a; // keep as-is to allow future actions
        };
    }
}
