package com.floai.backend.repository;

import com.floai.backend.model.FeedbackEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface FeedbackRepository extends JpaRepository<FeedbackEvent, Long> {

    @Query("""
        SELECT COUNT(f) FROM FeedbackEvent f
        WHERE f.productId = :productId AND LOWER(f.action) = LOWER(:action)
    """)
    long countByProductAndAction(Long productId, String action);

    interface FeedbackAggregate {
        Long getProductId();
        Long getViewed();
        Long getIgnored();
        Long getAddedToCart();
        Long getPurchased();
    }

    @Query("""
        SELECT f.productId AS productId,
               SUM(CASE WHEN LOWER(f.action)='viewed'        THEN 1 ELSE 0 END) AS viewed,
               SUM(CASE WHEN LOWER(f.action)='ignored'       THEN 1 ELSE 0 END) AS ignored,
               SUM(CASE WHEN LOWER(f.action)='added_to_cart' THEN 1 ELSE 0 END) AS addedToCart,
               SUM(CASE WHEN LOWER(f.action)='purchased'     THEN 1 ELSE 0 END) AS purchased
        FROM FeedbackEvent f
        GROUP BY f.productId
    """)
    List<FeedbackAggregate> aggregateCounts();
}
