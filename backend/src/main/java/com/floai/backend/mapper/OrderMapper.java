package com.floai.backend.mapper;

import com.floai.backend.dto.OrderDto;
import com.floai.backend.dto.OrderItemDto;
import com.floai.backend.dto.ProductDto;
import com.floai.backend.model.Order;
import com.floai.backend.model.OrderItem;
import com.floai.backend.model.Product;

import java.util.List;

public class OrderMapper {

    public static OrderDto toDto(Order order) {
        List<OrderItemDto> items = order.getItems().stream()
                .map(OrderMapper::toDto)
                .toList();

        return new OrderDto(order.getId(), items);
    }

    public static OrderItemDto toDto(OrderItem item) {
        Product product = item.getProduct();
        ProductDto productDto = new ProductDto(
                product.getId(),
                product.getName(),
                product.getDescription(),
                product.getBrand(),
                product.getCategory()
        );

        return new OrderItemDto(
                item.getId(),
                item.getQuantity(),
                productDto
        );
    }
}
