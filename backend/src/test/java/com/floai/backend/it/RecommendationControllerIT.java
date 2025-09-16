package com.floai.backend.it;

import com.floai.backend.BackendApplication;
import com.floai.backend.dto.ProductDto;
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
class RecommendationControllerIT {

    @Autowired TestRestTemplate rest;
    @Autowired ProductRepository productRepository;
    @Autowired JdbcTemplate jdbc; // for fast FK-safe cleanup

    private Product p1;
    private Product p2;

    @BeforeEach
    void seed() {
        // Clean state (FK-safe): cascade from products removes dependents (order_items/feedback_events)
        try {
            jdbc.execute("TRUNCATE TABLE products RESTART IDENTITY CASCADE");
        } catch (Exception ignored) {
            // Fallback if TRUNCATE isn't available; order matters if you use repositories
            productRepository.deleteAll();
        }

        // Seed products (Long id, String name, String description, String brand, String category)
        p1 = productRepository.save(new Product(null, "Sink A", "Stainless steel 80x50", "AquaBrand", "Kitchen"));
        p2 = productRepository.save(new Product(null, "Sink B", "Granite composite 78x48", "StoneLine", "Kitchen"));
    }

    @Test
    @DisplayName("GET /api/recommendations/popular ranks products by feedback-based score")
    void popularRanksByScore() {
        // Seed feedback via your endpoint
        postFeedback(p1.getId(), "VIEWED");
        postFeedback(p1.getId(), "VIEWED");
        postFeedback(p1.getId(), "ADDED_TO_CART");
        postFeedback(p1.getId(), "PURCHASED");

        postFeedback(p2.getId(), "VIEWED");
        postFeedback(p2.getId(), "IGNORED");

        ResponseEntity<List<ProductDto>> res = rest.exchange(
                "/api/recommendations/popular?limit=5",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<List<ProductDto>>() {}
        );

        assertThat(res.getStatusCode().is2xxSuccessful()).isTrue();
        List<ProductDto> body = res.getBody();
        assertThat(body).isNotNull();
        assertThat(body).hasSizeGreaterThanOrEqualTo(2);

        // p1 should rank above p2 based on the seeded signals
        assertThat(body.get(0).name()).isEqualTo("Sink A");
        assertThat(body.stream().map(ProductDto::name)).contains("Sink A", "Sink B");
    }

    private void postFeedback(Long productId, String action) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", productId);
        payload.put("action", action);
        // orderId is unused by controller logic; 0 is fine
        rest.postForEntity("/api/recommendations/0/feedback", new HttpEntity<>(payload), Void.class);
    }
}
