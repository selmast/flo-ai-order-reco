package com.floai.backend.controller;

import com.floai.backend.dto.PopularItemDto;
import com.floai.backend.model.Product;
import com.floai.backend.repository.ProductRepository;
import com.floai.backend.service.FeedbackService;
import io.micrometer.core.annotation.Timed;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@RestController
@RequestMapping(value =  "/feedback", produces = MediaType.APPLICATION_JSON_VALUE)
@Validated
@Tag(name = "Feedback Analytics")
public class FeedbackAnalyticsController {

    private final ProductRepository productRepository;
    private final FeedbackService feedbackService;

    public FeedbackAnalyticsController(ProductRepository productRepository,
                                       FeedbackService feedbackService) {
        this.productRepository = productRepository;
        this.feedbackService = feedbackService;
    }

    @Operation(summary = "Feedback stats per product",
            description = "Aggregates feedback counters (viewed, ignored, added_to_cart, purchased) "
                    + "and returns a popularity score per product.")
    @Timed(value = "feedback_stats_timer", description = "Time to compute feedback stats")
    @GetMapping("/stats")
    public ResponseEntity<List<PopularItemDto>> stats(
            @RequestParam(defaultValue = "10")
            @Min(value = 1, message = "limit must be at least 1")
            @Max(value = 50, message = "limit must be at most 50")
            int limit
    ) {
        List<PopularItemDto> items = new ArrayList<>();

        for (Product p : productRepository.findAll()) {
            if (p == null || p.getId() == null) continue;

            var f = feedbackService.get(p.getId());
            long viewed      = f.viewed;
            long ignored     = f.ignored;
            long addedToCart = f.addedToCart;
            long purchased   = f.purchased;

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
            double rounded = BigDecimal.valueOf(score)
                    .setScale(3, RoundingMode.HALF_UP)
                    .doubleValue();

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
}
