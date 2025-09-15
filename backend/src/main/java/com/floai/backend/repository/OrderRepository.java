package com.floai.backend.repository;

import com.floai.backend.model.Order;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // Used already by RecommendationController; keep it
    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("select o from Order o where o.id = :id")
    Optional<Order> findDetailedById(Long id);

    // New: list all orders with items & products eager-fetched
    @EntityGraph(attributePaths = {"items", "items.product"})
    @Query("select distinct o from Order o")
    List<Order> findAllDetailed();
}
