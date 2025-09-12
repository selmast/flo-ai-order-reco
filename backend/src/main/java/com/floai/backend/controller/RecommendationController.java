// src/main/java/com/floai/backend/controller/RecommendationController.java
package com.floai.backend.controller;

import com.floai.backend.model.Product;
import com.floai.backend.service.RecommendationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/recommendations")
public class RecommendationController {

    private final RecommendationService service;

    public RecommendationController(RecommendationService service) {
        this.service = service;
    }

    @GetMapping("/{orderId}")
    public List<Product> get(@PathVariable Long orderId) {
        return service.recommendForOrder(orderId);
    }
}
