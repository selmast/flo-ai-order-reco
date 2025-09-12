package com.floai.backend.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders") // 'order' is reserved in many DBs
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String status;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference("order-items") // <-- prevents back recursion
    private List<OrderItem> items = new ArrayList<>();

    public Order() {}

    public Order(Long id, String status, List<OrderItem> items) {
        this.id = id;
        this.status = status;
        if (items != null) {
            this.items = items;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) {
        this.items.clear();
        if (items != null) {
            for (OrderItem it : items) addItem(it);
        }
    }

    /** convenience helpers so back-references stay in sync */
    public void addItem(OrderItem item) {
        if (item == null) return;
        items.add(item);
        item.setOrder(this);
    }

    public void removeItem(OrderItem item) {
        if (item == null) return;
        items.remove(item);
        item.setOrder(null);
    }
}

