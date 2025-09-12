package com.floai.backend.repository;

import com.floai.backend.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
           select distinct o
           from Order o
           left join fetch o.items i
           left join fetch i.product p
           where o.id = :id
           """)
    Optional<Order> findDetailedById(@Param("id") Long id);
}
