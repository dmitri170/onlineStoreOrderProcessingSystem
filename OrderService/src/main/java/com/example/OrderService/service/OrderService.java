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

/**
 * Основной сервис для обработки заказов покупателей.
 * Отвечает за проверку наличия товаров, расчет стоимости и отправку заказов в Kafka.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderService {

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
     * @throws IllegalArgumentException если пользователь не найден
     * @throws InsufficientStockException если товаров недостаточно на складе
     */
    public String processOrder(OrderRequest request, String username) {
        String orderUuid = UUID.randomUUID().toString();
        log.info("[Заказ: {}] === НАЧАЛО ОБРАБОТКИ ЗАКАЗА ===", orderUuid);
        log.info("[Заказ: {}] Пользователь: {}, Количество товаров: {}", orderUuid, username, request.getItems().size());

        try {
            User user = findUser(username, orderUuid);
            log.info("[Заказ: {}] Пользователь найден: ID={}", orderUuid, user.getId());

            // Обрабатываем все товары через Stream API
            List<OrderItemProcessingResult> processingResults = request.getItems().stream()
                    .map(item -> processOrderItem(item, orderUuid))
                    .collect(Collectors.toList());

            // Проверяем наличие всех товаров
            validateStockAvailability(processingResults, orderUuid);

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
     * Обрабатывает один товар в заказе: проверяет наличие и рассчитывает стоимость.
     *
     * @param item данные товара
     * @param orderUuid UUID заказа для логирования
     * @return результат обработки товара
     */
    private OrderItemProcessingResult processOrderItem(OrderItemDTO item, String orderUuid) {
        log.info("[Заказ: {}] Проверка наличия товара ID: {}", orderUuid, item.getProductId());

        ProductResponse product = inventoryClient.checkAvailability(item.getProductId());

        if (product.getQuantity() < item.getQuantity()) {
            String errorMessage = String.format(
                    "Недостаточно товара ID %d. В наличии: %d, запрошено: %d",
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

        log.debug("[Заказ: {}] Товар {} обработан. Цена: {}, Скидка: {}, Итого: {}",
                orderUuid, item.getProductId(), price, discount, itemTotal);

        return new OrderItemProcessingResult(
                item.getProductId(), item.getQuantity(),
                price, discount, itemTotal,
                true, null
        );
    }

    /**
     * Проверяет доступность всех товаров в заказе.
     *
     * @param results результаты обработки товаров
     * @param orderUuid UUID заказа для логирования
     * @throws InsufficientStockException если какой-либо товар недоступен
     */
    private void validateStockAvailability(List<OrderItemProcessingResult> results, String orderUuid) {
        results.stream()
                .filter(result -> !result.isAvailable())
                .findFirst()
                .ifPresent(result -> {
                    log.error("[Заказ: {}] {}", orderUuid, result.getErrorMessage());
                    throw new InsufficientStockException(result.getErrorMessage());
                });
    }

    /**
     * Рассчитывает общую сумму заказа.
     *
     * @param results результаты обработки товаров
     * @return общая сумма заказа
     */
    private BigDecimal calculateTotal(List<OrderItemProcessingResult> results) {
        return results.stream()
                .map(OrderItemProcessingResult::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Создает элементы заказа для Kafka сообщения.
     *
     * @param results результаты обработки товаров
     * @return список элементов заказа
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
     *
     * @param username имя пользователя
     * @param orderUuid UUID заказа для логирования
     * @return найденный пользователь
     * @throws IllegalArgumentException если пользователь не найден
     */
    private User findUser(String username, String orderUuid) {
        log.debug("[Заказ: {}] Поиск пользователя: {}", orderUuid, username);
        return userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("[Заказ: {}] Пользователь не найден: {}", orderUuid, username);
                    return new IllegalArgumentException("Пользователь не найден: " + username);
                });
    }

    /**
     * Рассчитывает стоимость одного товара с учетом скидки.
     *
     * @param price цена товара
     * @param discount скидка (от 0 до 1)
     * @param quantity количество
     * @return общая стоимость позиции
     */
    private BigDecimal calculateItemTotal(BigDecimal price, BigDecimal discount, Integer quantity) {
        return price
                .multiply(BigDecimal.valueOf(quantity))
                .multiply(BigDecimal.ONE.subtract(discount));
    }

    /**
     * Создает сообщение для Kafka.
     *
     * @param orderUuid UUID заказа
     * @param user пользователь
     * @param total общая сумма
     * @param orderItems элементы заказа
     * @return сообщение для отправки в Kafka
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
     * Содержит информацию о товаре, его доступности и расчетах стоимости.
     */
    @Data
    @AllArgsConstructor
    private static class OrderItemProcessingResult {
        /** ID товара */
        private Long productId;

        /** Количество товара */
        private Integer quantity;

        /** Цена за единицу */
        private BigDecimal price;

        /** Скидка (от 0 до 1) */
        private BigDecimal discount;

        /** Общая стоимость позиции */
        private BigDecimal itemTotal;

        /** Доступен ли товар в нужном количестве */
        private boolean available;

        /** Сообщение об ошибке, если товар недоступен */
        private String errorMessage;
    }
}