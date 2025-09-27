package com.example.OrderService.service;

import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.entity.User;
import com.example.OrderService.grpc.InventoryClient;
import com.example.OrderService.kafka.OrderProducer;
import com.example.OrderService.message.model.OrderMessage;
import com.example.OrderService.repository.UserRepository;
import com.example.inventory.ProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service

public class OrderService {

    private final InventoryClient inventoryClient;
    private final OrderProducer orderProducer;
    private final UserRepository userRepository;

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderService(InventoryClient inventoryClient, OrderProducer orderProducer, UserRepository userRepository) {
        this.inventoryClient = inventoryClient;
        this.orderProducer = orderProducer;
        this.userRepository = userRepository;
    }

    public String processOrder(OrderRequest request, String username) {
        log.info("Processing order for user: {}", username);

        // Находим пользователя
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        BigDecimal total = BigDecimal.ZERO;
        List<OrderMessage.OrderItemMessage> orderItems = new ArrayList<>();

        // Проверяем наличие и рассчитываем стоимость для каждого товара
        for (OrderItemDTO item : request.getItems()) {
            log.info("Checking availability for product ID: {}", item.getProductId());

            ProductResponse product = inventoryClient.checkAvailability(item.getProductId());

            if (product.getQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product ID " + item.getProductId() +
                        ". Available: " + product.getQuantity() + ", requested: " + item.getQuantity());
            }

            BigDecimal price = BigDecimal.valueOf(product.getPrice());
            BigDecimal discount = BigDecimal.valueOf(product.getSale());
            BigDecimal itemTotal = price
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .multiply(BigDecimal.ONE.subtract(discount));

            total = total.add(itemTotal);

            // Создаем элемент заказа для Kafka сообщения
            OrderMessage.OrderItemMessage orderItem = new OrderMessage.OrderItemMessage(
                    item.getProductId(),
                    item.getQuantity(),
                    price,
                    discount,
                    itemTotal
            );
            orderItems.add(orderItem);

            log.info("Product {} added to order. Price: {}, Discount: {}, ItemTotal: {}",
                    item.getProductId(), price, discount, itemTotal);
        }

        // Создаем сообщение для Kafka
        OrderMessage message = new OrderMessage(
                UUID.randomUUID().toString(),
                user.getId(),
                total,
                LocalDateTime.now().toString(),
                orderItems
        );

        log.info("Order calculated. Total: {}, Items count: {}", total, orderItems.size());

        // Отправляем в Kafka
        orderProducer.sendOrder(message);

        log.info("Order {} sent to Kafka", message.getOrderId());

        return message.getOrderId();
    }
}