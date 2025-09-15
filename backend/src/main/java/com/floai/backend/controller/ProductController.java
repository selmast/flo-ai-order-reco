package com.floai.backend.controller;

import com.floai.backend.dto.ProductDto;
import com.floai.backend.mapper.ProductMapper;
import com.floai.backend.repository.ProductRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // GET /products  -> List<ProductDto>
    @GetMapping
    public ResponseEntity<List<ProductDto>> getAll() {
        var dtos = productRepository.findAll()
                .stream()
                .map(ProductMapper::toDto)
                .toList();
        return ResponseEntity.ok(dtos);
    }

    // (Optional but useful) GET /products/{id} -> ProductDto
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getOne(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ProductMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
