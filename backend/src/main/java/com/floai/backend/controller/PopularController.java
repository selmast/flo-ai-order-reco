package com.floai.backend.controller;

import com.floai.backend.model.Product;
import com.floai.backend.service.PopularService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/popular")
public class PopularController {

    private final PopularService popularService;

    public PopularController(PopularService popularService) {
        this.popularService = popularService;
    }

    // GET /popular?limit=10
    @GetMapping
    public List<Product> topPopular(@RequestParam(defaultValue = "10") int limit) {
        return popularService.topPopular(limit);
    }
}
