package com.floai.backend.repository;

import com.floai.backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findTop5ByIdNotIn(Collection<Long> ids);
    List<Product> findTop5ByOrderByIdAsc();

    @Query(value = "SELECT * FROM products ORDER BY id ASC LIMIT ?1", nativeQuery = true)
    List<Product> findTopNOrderByIdAsc(int limit);
}
