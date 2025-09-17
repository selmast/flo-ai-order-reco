package com.floai.backend.controller;

import com.floai.backend.dto.OrderDto;
import com.floai.backend.mapper.OrderMapper;
import com.floai.backend.model.OrderItem;
import com.floai.backend.model.Product;
import com.floai.backend.repository.OrderRepository;
import com.floai.backend.service.FeedbackService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final FeedbackService feedbackService;

    public OrderController(OrderRepository orderRepository,
                           FeedbackService feedbackService) {
        this.orderRepository = orderRepository;
        this.feedbackService = feedbackService;
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

    // PATCH /orders/{id}/complete -> mark as COMPLETED and record purchases
    @PatchMapping("/{id}/complete")
    @Transactional
    public ResponseEntity<OrderDto> complete(@PathVariable Long id) {
        var opt = orderRepository.findDetailedById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var order = opt.get();
        boolean alreadyCompleted = "COMPLETED".equalsIgnoreCase(order.getStatus());

        if (!alreadyCompleted) {
            order.setStatus("COMPLETED");
            // record a purchase signal for each product in the order
            for (OrderItem it : order.getItems()) {
                Product p = it.getProduct();
                if (p != null && p.getId() != null) {
                    feedbackService.record(p.getId(), "purchased");
                }
            }
            orderRepository.save(order);
        }

        return ResponseEntity.ok(OrderMapper.toDto(order));
    }
}
