package com.floai.backend.controller;

import com.floai.backend.dto.PopularItemDto;
import com.floai.backend.model.Product;
import com.floai.backend.repository.ProductRepository;
import com.floai.backend.service.FeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping(value = "/popular", produces = MediaType.APPLICATION_JSON_VALUE)
public class PopularController {

    private final ProductRepository productRepository;
    private final FeedbackService feedbackService;

    public PopularController(ProductRepository productRepository,
                             FeedbackService feedbackService) {
        this.productRepository = productRepository;
        this.feedbackService = feedbackService;
    }

    @Operation(
            summary = "Popular products (CTR-like)",
            description = "Ranks products using an approximate CTR ( (addedToCart + purchased) / (addedToCart + purchased + ignored) ), "
                    + "lightly weighted by total volume. Returns a pageable list of PopularItemDto."
    )
    @GetMapping
    public ResponseEntity<Page<PopularItemDto>> list(
            @Parameter(description = "0-based page index") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size (1..100)") @RequestParam(defaultValue = "10") int size
    ) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(100, size));
        Pageable pageable = PageRequest.of(safePage, safeSize);

        // Build all popular items (small catalog assumption). If catalog is large, switch to DB-side aggregation.
        List<PopularItemDto> all = new ArrayList<>();
        for (Product p : productRepository.findAll()) {
            if (p == null || p.getId() == null) continue;

            var stats = feedbackService.get(p.getId());
            long viewed = stats.viewed;
            long ignored = stats.ignored;
            long added = stats.addedToCart;
            long purchased = stats.purchased;

            long pos = added + purchased;
            long neg = ignored;
            double score;
            if (pos + neg == 0) {
                score = 0.0;
            } else {
                double ctr = (double) pos / (pos + neg);            // 0..1
                double volume = Math.max(1, pos + neg);
                // Lightly down-weight very tiny samples:
                double volumeWeight = Math.min(1.0, Math.log10(volume) / 2.0); // 0..1 roughly
                score = ctr * (0.5 + 0.5 * volumeWeight);
            }

            double rounded = BigDecimal.valueOf(score)
                    .setScale(3, RoundingMode.HALF_UP)
                    .doubleValue();

            all.add(new PopularItemDto(
                    p.getId(),
                    p.getName(),
                    p.getBrand(),
                    p.getCategory(),
                    rounded,
                    viewed,
                    ignored,
                    added,
                    purchased
            ));
        }

        // Sort by score desc, tie-break by name asc
        all.sort(
                Comparator.comparingDouble(PopularItemDto::score).reversed()
                        .thenComparing(PopularItemDto::name, Comparator.nullsLast(String::compareToIgnoreCase))
        );

        // Page in-memory
        int from = Math.min(safePage * safeSize, all.size());
        int to = Math.min(from + safeSize, all.size());
        List<PopularItemDto> slice = all.subList(from, to);

        Page<PopularItemDto> pageResult = new PageImpl<>(slice, pageable, all.size());
        return ResponseEntity.ok(pageResult);
    }
}
