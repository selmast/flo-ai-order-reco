package com.floai.backend.service;

import com.floai.backend.model.Product;
import com.floai.backend.repository.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PopularService {
    private final ProductRepository productRepo;

    public PopularService(ProductRepository productRepo) {
        this.productRepo = productRepo;
    }

    public List<Product> topPopular(int limit) {
        // naive popularity = lowest id first for now (or replace with sales count later)
        return productRepo.findTopNOrderByIdAsc(limit);
    }
}
