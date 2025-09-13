package com.floai.backend.controller;

import com.floai.backend.dto.*;
import com.floai.backend.model.Order;
import com.floai.backend.model.OrderItem;
import com.floai.backend.model.Product;
import com.floai.backend.repository.OrderRepository;
import com.floai.backend.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderController(OrderRepository orderRepository,
                           ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @GetMapping
    public List<OrderSummaryDto> all() {
        return orderRepository.findAll()
                .stream()
                .map(o -> new OrderSummaryDto(o.getId(), o.getStatus()))
                .toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderDto> getById(@PathVariable Long id) {
        return orderRepository.findDetailedById(id)
                .map(o -> {
                    var items = o.getItems().stream().map(oi ->
                            new OrderItemDto(
                                    oi.getId(),
                                    new ProductDto(
                                            oi.getProduct().getId(),
                                            oi.getProduct().getName(),
                                            oi.getProduct().getDescription(),
                                            oi.getProduct().getBrand(),
                                            oi.getProduct().getCategory()
                                    ),
                                    oi.getQuantity()
                            )
                    ).toList();
                    return ResponseEntity.ok(new OrderDto(o.getId(), o.getStatus(), items));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CreateOrderResponse> create(@Valid @RequestBody CreateOrderRequest req) {
        Order order = new Order();
        order.setStatus("CREATED");

        for (CreateOrderRequest.Item it : req.getItems()) {
            Product p = productRepository.findById(it.getProductId())
                    .orElseThrow(() ->
                            new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                    "Unknown productId: " + it.getProductId()));
            OrderItem oi = new OrderItem();
            oi.setProduct(p);
            oi.setQuantity(it.getQuantity());
            order.addItem(oi); // keeps back-reference in sync
        }

        orderRepository.save(order);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new CreateOrderResponse(order.getId(), order.getStatus()));
    }
}
