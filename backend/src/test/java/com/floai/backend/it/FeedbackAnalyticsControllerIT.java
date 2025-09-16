package com.floai.backend.it;

import com.floai.backend.BackendApplication;
import com.floai.backend.dto.PopularItemDto;
import com.floai.backend.model.Product;
import com.floai.backend.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = BackendApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FeedbackAnalyticsControllerIT {

    @Autowired TestRestTemplate rest;
    @Autowired ProductRepository productRepository;
    @Autowired JdbcTemplate jdbc;

    private Product p1;
    private Product p2;

    @BeforeEach
    void seed() {
        // FK-safe cleanup; works with Postgres in jdbc:tc profile
        try {
            jdbc.execute("TRUNCATE TABLE products RESTART IDENTITY CASCADE");
        } catch (Exception ignored) {
            // Fallback for other DBs
            productRepository.deleteAll();
        }

        // Seed products (Long id, String name, String description, String brand, String category)
        p1 = productRepository.save(new Product(null, "Sink A", "Stainless steel 80x50", "AquaBrand", "Kitchen"));
        p2 = productRepository.save(new Product(null, "Sink B", "Granite composite 78x48", "StoneLine", "Kitchen"));

        // Seed feedback via your existing endpoint (expects lowercase actions)
        postFeedback(p1.getId(), "viewed");
        postFeedback(p1.getId(), "viewed");
        postFeedback(p1.getId(), "added_to_cart");
        postFeedback(p1.getId(), "purchased");

        postFeedback(p2.getId(), "viewed");
        postFeedback(p2.getId(), "ignored");
    }

    @Test
    @DisplayName("GET /api/feedback/stats aggregates counters and ranks by score")
    void feedbackStats_aggregatesAndRanks() {
        ResponseEntity<List<PopularItemDto>> res = rest.exchange(
                "/api/feedback/stats?limit=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<PopularItemDto>>() {}
        );

        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        List<PopularItemDto> body = res.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSizeGreaterThanOrEqualTo(2);

        // Top item should be Sink A (more positive signals)
        assertThat(body.get(0).name()).isEqualTo("Sink A");

        // Check counters per product
        PopularItemDto a = body.stream().filter(x -> "Sink A".equals(x.name())).findFirst().orElseThrow();
        PopularItemDto b = body.stream().filter(x -> "Sink B".equals(x.name())).findFirst().orElseThrow();

        assertThat(a.viewed()).isEqualTo(2);
        assertThat(a.ignored()).isEqualTo(0);
        assertThat(a.addedToCart()).isEqualTo(1);
        assertThat(a.purchased()).isEqualTo(1);

        assertThat(b.viewed()).isEqualTo(1);
        assertThat(b.ignored()).isEqualTo(1);
        assertThat(b.addedToCart()).isEqualTo(0);
        assertThat(b.purchased()).isEqualTo(0);

        // Score should be higher for A than B
        assertThat(a.score()).isGreaterThan(b.score());
    }

    private void postFeedback(Long productId, String action) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", productId);
        payload.put("action", action);
        rest.postForEntity("/api/recommendations/0/feedback", new HttpEntity<>(payload), Void.class);
    }
}
