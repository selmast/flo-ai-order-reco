package com.floai.backend.controller;

import com.floai.backend.dto.OrderDto;
import com.floai.backend.mapper.OrderMapper;
import com.floai.backend.repository.OrderRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;

    public OrderController(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // GET /orders  -> list all orders (with items & products) as DTOs
    @GetMapping
    public List<OrderDto> getAll() {
        return orderRepository.findAllDetailed()
                .stream()
                .map(OrderMapper::toDto)
                .toList();
    }

    // GET /orders/{id} -> single order as DTO
    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getOne(@PathVariable Long id) {
        return orderRepository.findDetailedById(id)
                .map(OrderMapper::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
