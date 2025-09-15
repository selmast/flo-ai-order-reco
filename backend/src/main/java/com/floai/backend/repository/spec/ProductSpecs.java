package com.floai.backend.repository.spec;

import com.floai.backend.model.Product;
import org.springframework.data.jpa.domain.Specification;

public final class ProductSpecs {
    private ProductSpecs() {}

    public static Specification<Product> nameOrDescContains(String q) {
        if (q == null || q.isBlank()) return null;
        String like = "%" + q.trim().toLowerCase() + "%";
        return (root, cq, cb) -> cb.or(
                cb.like(cb.lower(root.get("name")), like),
                cb.like(cb.lower(root.get("description")), like)
        );
    }

    public static Specification<Product> brandEquals(String brand) {
        if (brand == null || brand.isBlank()) return null;
        return (root, cq, cb) -> cb.equal(cb.lower(root.get("brand")), brand.trim().toLowerCase());
    }

    public static Specification<Product> categoryEquals(String category) {
        if (category == null || category.isBlank()) return null;
        return (root, cq, cb) -> cb.equal(cb.lower(root.get("category")), category.trim().toLowerCase());
    }
}
