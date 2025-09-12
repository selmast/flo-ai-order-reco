package com.floai.backend.controller;

import com.floai.backend.dto.OrderSummaryDto;
import com.floai.backend.model.Order;
import com.floai.backend.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public List<OrderSummaryDto> all() {
        return orderRepository.findAll()
                .stream()
                .map(o -> new OrderSummaryDto(o.getId(), o.getStatus()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return orderRepository.findDetailedById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

   


}

