package com.example.OrderService.service;

import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.entity.Order;
import com.example.OrderService.entity.User;
import com.example.OrderService.exception.InsufficientStockException;
import com.example.OrderService.grpc.InventoryClient;
import com.example.OrderService.kafka.OrderProducer;
import com.example.OrderService.message.model.OrderMessage;
import com.example.OrderService.repository.OrderRepository;
import com.example.OrderService.repository.UserRepository;
import com.example.inventory.ProductResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final OrderRepository orderRepository; // Инжектим новый репозиторий

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    public OrderService(InventoryClient inventoryClient, OrderProducer orderProducer, UserRepository userRepository, OrderRepository orderRepository) {
        this.inventoryClient = inventoryClient;
        this.orderProducer = orderProducer;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public String processOrder(OrderRequest request, String username) {
        log.info("Processing order for user: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        BigDecimal total = BigDecimal.ZERO;
        List<OrderMessage.OrderItemMessage> orderItems = new ArrayList<>();

        for (OrderItemDTO item : request.getItems()) {
            log.info("Checking availability for product ID: {}", item.getProductId());

            ProductResponse product = inventoryClient.checkAvailability(item.getProductId());

            if (product.getQuantity() < item.getQuantity()) {
                // Используем кастомное исключение
                throw new InsufficientStockException("Not enough stock for product ID " + item.getProductId() +
                        ". Available: " + product.getQuantity() + ", requested: " + item.getQuantity());
            }

            BigDecimal price = BigDecimal.valueOf(product.getPrice());
            BigDecimal discount = BigDecimal.valueOf(product.getSale());
            BigDecimal itemTotal = price
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .multiply(BigDecimal.ONE.subtract(discount));

            total = total.add(itemTotal);

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

        // Генерируем UUID для заказа
        String orderUuid = UUID.randomUUID().toString();

        // Сохраняем заказ в БД Order Service
        Order order = new Order();
        order.setOrderUuid(orderUuid);
        order.setUserId(user.getId());
        order.setTotalPrice(total);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus("CREATED");

        orderRepository.save(order);
        log.info("Order saved to database with ID: {}", order.getId());

        // Создаем сообщение для Kafka
        OrderMessage message = new OrderMessage(
                orderUuid, // Используем тот же UUID
                user.getId(),
                total,
                LocalDateTime.now().toString(),
                orderItems
        );

        log.info("Order calculated. Total: {}, Items count: {}", total, orderItems.size());

        // Отправляем в Kafka
        orderProducer.sendOrder(message);
        log.info("Order {} sent to Kafka", message.getOrderId());

        // Можно обновить статус заказа на "SENT_TO_KAFKA", если нужно отслеживать
        // order.setStatus("SENT_TO_KAFKA");
        // orderRepository.save(order);

        return orderUuid;
    }
}