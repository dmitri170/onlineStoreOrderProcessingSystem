package com.example.NotificationService.entity;


import com.example.OrderService.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "order_items")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Связь с заказом
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    // Связь с пользователем
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Информация о позиции заказа
    private Long productId;
    private int quantity;
    private double price;
    private double sale;
    private double totalPrice;
}
