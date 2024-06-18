package com.nextjedi.trading.tipbasedtrading.models;

import com.zerodhatech.models.Order;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
public class OrderDetail {
    String orderId;
    int quantity;
    double price;
    String transactionType;
    String product;
    @Id
    private Long id;
    public OrderDetail(Order order) {
        this.orderId = order.orderId;
    }
}
