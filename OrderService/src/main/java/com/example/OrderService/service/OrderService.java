package com.example.OrderService.service;

import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.entity.User;
import com.example.OrderService.exception.InsufficientStockException;
import com.example.OrderService.grpc.InventoryClient;
import com.example.OrderService.kafka.OrderProducer;
import com.example.OrderService.message.model.OrderMessage;
import com.example.OrderService.repository.UserRepository;
import com.example.inventory.ProductResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final InventoryClient inventoryClient;
    private final OrderProducer orderProducer;
    private final UserRepository userRepository;

    public String processOrder(OrderRequest request, String username) {
        String orderUuid = UUID.randomUUID().toString();
        log.info("[Order: {}] === START ORDER PROCESSING ===", orderUuid);
        log.info("[Order: {}] User: {}, Items count: {}", orderUuid, username, request.getItems().size());

        try {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> {
                        log.error("[Order: {}] User not found: {}", orderUuid, username);
                        return new IllegalArgumentException("User not found: " + username);
                    });
            log.info("[Order: {}] User found: ID={}", orderUuid, user.getId());

            // Обрабатываем все items через Stream API
            List<OrderItemProcessingResult> processingResults = request.getItems().stream()
                    .map(item -> processOrderItem(item, orderUuid))
                    .collect(Collectors.toList());

            // Проверяем наличие всех товаров
            validateStockAvailability(processingResults, orderUuid);

            // Рассчитываем общую сумму
            BigDecimal total = calculateTotal(processingResults);
            List<OrderMessage.OrderItemMessage> orderItems = createOrderItems(processingResults);

            log.info("[Order: {}] All products checked successfully. Total: {}, Items count: {}",
                    orderUuid, total, orderItems.size());

            // Создаем и отправляем сообщение в Kafka
            OrderMessage message = createOrderMessage(orderUuid, user, total, orderItems);
            orderProducer.sendOrder(message);

            log.info("[Order: {}] Order sent to Kafka. Notification Service will save it to database", orderUuid);
            log.info("[Order: {}] === ORDER PROCESSING COMPLETED ===", orderUuid);

            return orderUuid;

        } catch (Exception e) {
            log.error("[Order: {}] === ORDER PROCESSING FAILED === Error: {}", orderUuid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Обработка одного элемента заказа
     */
    private OrderItemProcessingResult processOrderItem(OrderItemDTO item, String orderUuid) {
        log.info("[Order: {}] Checking availability for product ID: {}", orderUuid, item.getProductId());

        ProductResponse product = inventoryClient.checkAvailability(item.getProductId());

        if (product.getQuantity() < item.getQuantity()) {
            String errorMessage = String.format(
                    "Not enough stock for product ID %d. Available: %d, requested: %d",
                    item.getProductId(), product.getQuantity(), item.getQuantity()
            );
            return new OrderItemProcessingResult(
                    item.getProductId(), item.getQuantity(),
                    BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                    false, errorMessage
            );
        }

        BigDecimal price = BigDecimal.valueOf(product.getPrice());
        BigDecimal discount = BigDecimal.valueOf(product.getSale());
        BigDecimal itemTotal = calculateItemTotal(price, discount, item.getQuantity());

        log.debug("[Order: {}] Product {} processed. Price: {}, Discount: {}, ItemTotal: {}",
                orderUuid, item.getProductId(), price, discount, itemTotal);

        return new OrderItemProcessingResult(
                item.getProductId(), item.getQuantity(),
                price, discount, itemTotal,
                true, null
        );
    }

    /**
     * Проверка доступности всех товаров
     */
    private void validateStockAvailability(List<OrderItemProcessingResult> results, String orderUuid) {
        results.stream()
                .filter(result -> !result.isAvailable())
                .findFirst()
                .ifPresent(result -> {
                    log.error("[Order: {}] {}", orderUuid, result.getErrorMessage());
                    throw new InsufficientStockException(result.getErrorMessage());
                });
    }

    /**
     * Расчет общей суммы заказа
     */
    private BigDecimal calculateTotal(List<OrderItemProcessingResult> results) {
        return results.stream()
                .map(OrderItemProcessingResult::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Создание элементов заказа для Kafka сообщения
     */
    private List<OrderMessage.OrderItemMessage> createOrderItems(List<OrderItemProcessingResult> results) {
        return results.stream()
                .map(result -> new OrderMessage.OrderItemMessage(
                        result.getProductId(),
                        result.getQuantity(),
                        result.getPrice(),
                        result.getDiscount(),
                        result.getItemTotal()
                ))
                .collect(Collectors.toList());
    }

    /**
     * Расчет стоимости одного товара с учетом скидки
     */
    private BigDecimal calculateItemTotal(BigDecimal price, BigDecimal discount, Integer quantity) {
        return price
                .multiply(BigDecimal.valueOf(quantity))
                .multiply(BigDecimal.ONE.subtract(discount));
    }

    /**
     * Создание сообщения для Kafka
     */
    private OrderMessage createOrderMessage(String orderUuid, User user, BigDecimal total,
                                            List<OrderMessage.OrderItemMessage> orderItems) {
        return new OrderMessage(
                orderUuid,
                user.getId(),
                user.getUsername(),
                total,
                LocalDateTime.now().toString(),
                orderItems
        );
    }

    /**
     * Вспомогательный класс для обработки результатов
     */
    @Data
    @AllArgsConstructor
    private static class OrderItemProcessingResult {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
        private BigDecimal discount;
        private BigDecimal itemTotal;
        private boolean available;
        private String errorMessage;
    }
}