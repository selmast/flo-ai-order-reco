package com.floai.backend.controller;

import com.floai.backend.model.Product;
import com.floai.backend.repository.ProductRepository;
import com.floai.backend.dto.ProductDto;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<ProductDto> getAll() {
        return productRepository.findAll().stream()
                .map(p -> new ProductDto(
                        p.getId(),
                        p.getName(),
                        p.getBrand(),
                        p.getCategory(),
                        p.getDescription()
                ))
                .toList();
    }
}
