package com.example.OrderService.service;

import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.entity.User;
import com.example.OrderService.exception.ProductsUnavailableException;
import com.example.OrderService.exception.UserNotFoundException;
import com.example.OrderService.grpc.InventoryClient;
import com.example.OrderService.kafka.OrderProducer;
import com.example.OrderService.message.model.OrderMessage;
import com.example.OrderService.repository.UserRepository;
import com.example.inventory.BulkProductResponse;
import com.example.inventory.ProductResponseItem;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Основной сервис для обработки заказов покупателей.
 * Отвечает за проверку наличия товаров, расчет стоимости и отправку заказов в Kafka.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final InventoryClient inventoryClient;
    private final OrderProducer orderProducer;
    private final UserRepository userRepository;

    /**
     * Обрабатывает новый заказ от пользователя.
     * Генерирует UUID, проверяет наличие товаров, рассчитывает стоимость и отправляет в Kafka.
     *
     * @param request данные заказа
     * @param username имя пользователя, оформляющего заказ
     * @return UUID созданного заказа
     * @throws UserNotFoundException если пользователь не найден
     * @throws ProductsUnavailableException если товары недоступны
     */
    public String processOrder(OrderRequest request, String username) {
        String orderUuid = UUID.randomUUID().toString();
        log.info("[Заказ: RqUuid {}] Начало обработки заказа | Пользователь: {} | Товаров: {}",
                orderUuid, username, request.getItems().size());
        try {
            User user = findUser(username, orderUuid);

            // Проверяем доступность ВСЕХ товаров одним gRPC вызовом
            BulkProductResponse bulkResponse = inventoryClient.checkBulkAvailability(request.getItems(), orderUuid);

            // Если есть недоступные товары - бросаем исключение
            if (bulkResponse.getUnavailableItemsCount() > 0) {
                List<String> unavailableProducts = new ArrayList<>();
                for (ProductResponseItem item : bulkResponse.getUnavailableItemsList()) {
                    unavailableProducts.add(String.format("Товар ID:%d '%s' (запрошено: %d, доступно: %d)",
                            item.getProductId(), item.getName(), item.getRequestedQuantity(), item.getAvailableQuantity()));
                }

                log.warn("[Заказ: {}] Найдены недоступные товары: {}", orderUuid, unavailableProducts);
                throw new ProductsUnavailableException("Некоторые товары недоступны", unavailableProducts);
            }

            // Создаем OrderItems из доступных товаров
            List<OrderItemProcessingResult> processingResults = createOrderItemsFromResponse(
                    bulkResponse.getAvailableItemsList(), orderUuid);

            // Рассчитываем общую сумму
            BigDecimal total = calculateTotal(processingResults);
            List<OrderMessage.OrderItemMessage> orderItems = createOrderItems(processingResults);

            log.info("[Заказ: {}] Все товары проверены успешно. Итого: {}, Количество позиций: {}",
                    orderUuid, total, orderItems.size());

            // Создаем и отправляем сообщение в Kafka
            OrderMessage message = createOrderMessage(orderUuid, user, total, orderItems);
            orderProducer.sendOrder(message);

            log.info("[Заказ: {}] Заказ отправлен в Kafka. Notification Service сохранит его в базу данных", orderUuid);
            log.info("[Заказ: {}] === ОБРАБОТКА ЗАКАЗА ЗАВЕРШЕНА ===", orderUuid);

            return orderUuid;

        } catch (Exception e) {
            log.error("[Заказ: {}] === ОШИБКА ПРИ ОБРАБОТКЕ ЗАКАЗА === Ошибка: {}", orderUuid, e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Создает OrderItemProcessingResult из gRPC ответа
     */
    private List<OrderItemProcessingResult> createOrderItemsFromResponse(List<ProductResponseItem> availableItems, String orderUuid) {
        List<OrderItemProcessingResult> results = new ArrayList<>();

        for (ProductResponseItem item : availableItems) {
            BigDecimal price = BigDecimal.valueOf(item.getPrice());
            BigDecimal discount = BigDecimal.valueOf(item.getSale());
            BigDecimal itemTotal = calculateItemTotal(price, discount, item.getRequestedQuantity());

            log.debug("[Заказ: {}] Товар {} обработан. Цена: {}, Скидка: {}, Итого: {}",
                    orderUuid, item.getProductId(), price, discount, itemTotal);

            results.add(new OrderItemProcessingResult(
                    item.getProductId(),
                    item.getRequestedQuantity(),
                    price,
                    discount,
                    itemTotal,
                    true,
                    null
            ));
        }

        return results;
    }

    /**
     * Рассчитывает общую сумму заказа.
     */
    private BigDecimal calculateTotal(List<OrderItemProcessingResult> results) {
        return results.stream()
                .map(OrderItemProcessingResult::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Создает элементы заказа для Kafka сообщения.
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
     * Находит пользователя по имени.
     */
    private User findUser(String username, String orderUuid) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("[Заказ: {}] Пользователь не найден: {}", orderUuid, username);
                    return new UserNotFoundException("Пользователь не найден: " + username);
                });
    }

    /**
     * Рассчитывает стоимость одного товара с учетом скидки.
     */
    private BigDecimal calculateItemTotal(BigDecimal price, BigDecimal discount, Integer quantity) {
        return price
                .multiply(BigDecimal.valueOf(quantity))
                .multiply(BigDecimal.ONE.subtract(discount));
    }

    /**
     * Создает сообщение для Kafka.
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
     * Вспомогательный класс для хранения результатов обработки товара.
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