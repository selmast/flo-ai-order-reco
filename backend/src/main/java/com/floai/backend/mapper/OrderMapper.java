package com.floai.backend.mapper;

import com.floai.backend.dto.OrderDto;
import com.floai.backend.dto.OrderItemDto;
import com.floai.backend.model.Order;
import com.floai.backend.model.OrderItem;
import com.floai.backend.model.Product;

import java.time.Instant;
import java.util.List;

public final class OrderMapper {

    private OrderMapper() {}

    public static OrderDto toDto(Order order) {
        // map items
        List<OrderItemDto> items = order.getItems().stream()
                .map(OrderMapper::toDto)
                .toList();

        // total = sum(quantity * unit price)
        double totalPrice = items.stream()
                .mapToDouble(i -> i.price() * i.quantity())
                .sum();

        // entity doesn't expose createdAt yet
        Instant createdAt = null;

        return new OrderDto(
                order.getId(),
                createdAt,
                totalPrice,
                items
        );
    }

    public static OrderItemDto toDto(OrderItem item) {
        Product p = item.getProduct();
        Long productId   = (p == null) ? null : p.getId();
        String prodName  = (p == null) ? null : p.getName();

        // no pricing on entity yet -> keep unit price 0.0 for now
        double unitPrice = 0.0;

        return new OrderItemDto(
                item.getId(),
                productId,
                prodName,
                item.getQuantity(),
                unitPrice
        );
    }
}
