package com.floai.backend.repository;

import com.floai.backend.model.FeedbackEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

public interface FeedbackRepository extends JpaRepository<FeedbackEvent, Long> {


    List<FeedbackEvent> findByProductId(Long productId);

    @Query("""
        SELECT COUNT(f) FROM FeedbackEvent f
        WHERE f.productId = :productId AND LOWER(f.action) = LOWER(:action)
    """)
    long countByProductAndAction(@Param("productId") Long productId,
                                 @Param("action") String action);

    interface FeedbackAggregate {
        Long getProductId();
        Long getViewed();
        Long getIgnored();
        Long getAddedToCart();
        Long getPurchased();
    }

    // all products, all time
    @Query("""
        SELECT f.productId AS productId,
               SUM(CASE WHEN LOWER(f.action)='viewed'        THEN 1 ELSE 0 END) AS viewed,
               SUM(CASE WHEN LOWER(f.action)='ignored'       THEN 1 ELSE 0 END) AS ignored,
               SUM(CASE WHEN LOWER(f.action)='added_to_cart' THEN 1 ELSE 0 END) AS addedToCart,
               SUM(CASE WHEN LOWER(f.action)='purchased'     THEN 1 ELSE 0 END) AS purchased
        FROM FeedbackEvent f
        GROUP BY f.productId
    """)
    List<FeedbackAggregate> aggregateCountsAll();

    // only given product ids, all time
    @Query("""
        SELECT f.productId AS productId,
               SUM(CASE WHEN LOWER(f.action)='viewed'        THEN 1 ELSE 0 END) AS viewed,
               SUM(CASE WHEN LOWER(f.action)='ignored'       THEN 1 ELSE 0 END) AS ignored,
               SUM(CASE WHEN LOWER(f.action)='added_to_cart' THEN 1 ELSE 0 END) AS addedToCart,
               SUM(CASE WHEN LOWER(f.action)='purchased'     THEN 1 ELSE 0 END) AS purchased
        FROM FeedbackEvent f
        WHERE f.productId IN :productIds
        GROUP BY f.productId
    """)
    List<FeedbackAggregate> aggregateCountsForProducts(@Param("productIds") Collection<Long> productIds);

    // all products, since a point in time
    @Query("""
        SELECT f.productId AS productId,
               SUM(CASE WHEN LOWER(f.action)='viewed'        THEN 1 ELSE 0 END) AS viewed,
               SUM(CASE WHEN LOWER(f.action)='ignored'       THEN 1 ELSE 0 END) AS ignored,
               SUM(CASE WHEN LOWER(f.action)='added_to_cart' THEN 1 ELSE 0 END) AS addedToCart,
               SUM(CASE WHEN LOWER(f.action)='purchased'     THEN 1 ELSE 0 END) AS purchased
        FROM FeedbackEvent f
        WHERE f.createdAt >= :since
        GROUP BY f.productId
    """)
    List<FeedbackAggregate> aggregateCountsSince(@Param("since") Instant since);

    // given product ids, since a point in time
    @Query("""
        SELECT f.productId AS productId,
               SUM(CASE WHEN LOWER(f.action)='viewed'        THEN 1 ELSE 0 END) AS viewed,
               SUM(CASE WHEN LOWER(f.action)='ignored'       THEN 1 ELSE 0 END) AS ignored,
               SUM(CASE WHEN LOWER(f.action)='added_to_cart' THEN 1 ELSE 0 END) AS addedToCart,
               SUM(CASE WHEN LOWER(f.action)='purchased'     THEN 1 ELSE 0 END) AS purchased
        FROM FeedbackEvent f
        WHERE f.productId IN :productIds
          AND f.createdAt >= :since
        GROUP BY f.productId
    """)
    List<FeedbackAggregate> aggregateCountsForProductsSince(@Param("productIds") Collection<Long> productIds,
                                                            @Param("since") Instant since);
}
