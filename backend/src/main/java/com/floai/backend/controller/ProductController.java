package com.floai.backend.controller;

import com.floai.backend.dto.ProductDto;
import com.floai.backend.mapper.ProductMapper;
import com.floai.backend.model.Product;
import com.floai.backend.repository.ProductRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping(value = "/products", produces = MediaType.APPLICATION_JSON_VALUE)
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    /**
     * GET /products
     * Supports optional filters (brand, category, q search) and pagination (page,size,sort).
     * Example: /products?brand=Nike&category=shoes&q=run&page=0&size=10&sort=name,asc
     */
    @Operation(summary = "List products", description = "Filter by brand/category, text search on name/description, and paginate the results.")
    @GetMapping
    public ResponseEntity<Page<ProductDto>> list(
            @Parameter(description = "Exact brand filter (case-insensitive).")
            @RequestParam(required = false) String brand,
            @Parameter(description = "Exact category filter (case-insensitive).")
            @RequestParam(required = false) String category,
            @Parameter(description = "Free-text search on name and description (case-insensitive).")
            @RequestParam(required = false, name = "q") String query,
            @Parameter(description = "0-based page index (default 0).")
            @RequestParam(required = false, defaultValue = "0") int page,
            @Parameter(description = "Page size (default 20).")
            @RequestParam(required = false, defaultValue = "20") int size,
            @Parameter(description = "Sort in the format 'field,dir' (e.g. 'name,asc'). Default: id,asc.")
            @RequestParam(required = false, defaultValue = "id,asc") String sort
    ) {
        Pageable pageable = toPageable(sort, page, size);
        Specification<Product> spec = buildSpec(brand, category, query);

        Page<ProductDto> result = productRepository
                .findAll(spec, pageable)
                .map(ProductMapper::toDto);

        return ResponseEntity.ok(result);
    }

    /**
     * GET /products/{id}
     * Return a single product by id as DTO.
     */
    @Operation(summary = "Get a product by id")
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getOne(@PathVariable Long id) {
        return productRepository.findById(id)
                .map(ProductMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // --- helpers ---

    private Pageable toPageable(String sortParam, int page, int size) {
        // Expected formats: "field,asc" | "field,desc" | just "field"
        String field = "id";
        Sort.Direction dir = Sort.Direction.ASC;

        if (sortParam != null && !sortParam.isBlank()) {
            String[] parts = sortParam.split(",", 2);
            field = parts[0].trim();
            if (parts.length > 1) {
                String d = parts[1].trim().toLowerCase(Locale.ROOT);
                if ("desc".equals(d)) dir = Sort.Direction.DESC;
            }
        }
        return PageRequest.of(Math.max(0, page), Math.max(1, size), Sort.by(dir, field));
    }

    private Specification<Product> buildSpec(String brand, String category, String query) {
        return (root, q, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (brand != null && !brand.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("brand")), brand.toLowerCase(Locale.ROOT)));
            }
            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), category.toLowerCase(Locale.ROOT)));
            }
            if (query != null && !query.isBlank()) {
                String like = "%" + query.toLowerCase(Locale.ROOT) + "%";
                Predicate nameLike = cb.like(cb.lower(root.get("name")), like);
                Predicate descLike = cb.like(cb.lower(root.get("description")), like);
                predicates.add(cb.or(nameLike, descLike));
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
