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
    int orderId;
    int quantity;
    double price;
    String transactionType;
    @Id
    private Long id;

    public OrderDetail(int orderId, int quantity, double price, String transactionType) {
        this.orderId = orderId;
        this.quantity = quantity;
        this.price = price;
        this.transactionType = transactionType;
    }
    public OrderDetail(Order order) {
        this.orderId = Integer.parseInt(order.orderId);
        this.quantity = Integer.parseInt(order.quantity);
        this.price = Double.parseDouble(order.price);
        this.transactionType = order.transactionType;
    }
}
