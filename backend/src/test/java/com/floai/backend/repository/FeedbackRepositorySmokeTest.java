// src/test/java/com/floai/backend/repository/FeedbackRepositorySmokeTest.java
package com.floai.backend.repository;

import com.floai.backend.model.FeedbackEvent;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import jakarta.annotation.Resource;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class FeedbackRepositorySmokeTest {

    @Resource
    FeedbackRepository repo;

    @Test
    void savesAndAggregates() {
        repo.save(new FeedbackEvent(1L, null, "viewed"));
        repo.save(new FeedbackEvent(1L, null, "added_to_cart"));
        repo.save(new FeedbackEvent(2L, null, "ignored"));

        var all = repo.aggregateCountsAll();
        var p1 = all.stream().filter(a -> a.getProductId() == 1L).findFirst().orElseThrow();
        assertThat(p1.getViewed()).isGreaterThanOrEqualTo(1L);
        assertThat(p1.getAddedToCart()).isGreaterThanOrEqualTo(1L);
    }
}
