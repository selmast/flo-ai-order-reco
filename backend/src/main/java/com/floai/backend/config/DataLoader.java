package com.floai.backend.config;

import com.floai.backend.model.*;
import com.floai.backend.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class DataLoader {
    @Bean
    CommandLineRunner loadData(ProductRepository productRepo, OrderRepository orderRepo) {
        return args -> {
            // Ensure there are at least two sample products
            if (productRepo.count() == 0) {
                Product p1 = new Product();
                p1.setName("Running Shoes");
                p1.setDescription("Lightweight running shoes");
                p1.setBrand("Nike");
                p1.setCategory("Shoes");
                productRepo.save(p1);

                Product p2 = new Product();
                p2.setName("Backpack");
                p2.setDescription("Durable travel backpack");
                p2.setBrand("Adidas");
                p2.setCategory("Accessories");
                productRepo.save(p2);
            }

            // Always create one sample order if none exists yet
            if (orderRepo.count() == 0) {
                List<Product> products = productRepo.findAll();
                if (products.size() >= 2) {
                    Product p1 = products.get(0);
                    Product p2 = products.get(1);

                    Order order = new Order();
                    order.setStatus("CREATED");
                    order.setItems(new ArrayList<>());

                    OrderItem item1 = new OrderItem();
                    item1.setOrder(order);
                    item1.setProduct(p1);
                    item1.setQuantity(1);

                    OrderItem item2 = new OrderItem();
                    item2.setOrder(order);
                    item2.setProduct(p2);
                    item2.setQuantity(2);

                    order.getItems().add(item1);
                    order.getItems().add(item2);

                    // relies on cascade = CascadeType.ALL on Order.items
                    orderRepo.save(order);
                }
            }
        };
    }
}
