package com.example.OrderService.service;

import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.entity.User;
import com.example.OrderService.exception.ProductsUnavailableException;
import com.example.OrderService.exception.UserNotFoundException;
import com.example.OrderService.grpc.InventoryClient;
import com.example.OrderService.kafka.OrderProducer;
import com.example.OrderService.repository.UserRepository;
import com.example.inventory.BulkProductResponse;
import com.example.inventory.ProductResponseItem;
import com.example.inventory.ReserveProductsResponse;
import dto.OrderMessage;
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
 * Реализует полный цикл обработки заказа от валидации до отправки уведомлений.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final InventoryClient inventoryClient;
    private final OrderProducer orderProducer;
    private final UserRepository userRepository;

    /**
     * Обрабатывает новый заказ от пользователя.
     * Генерирует UUID, проверяет наличие товаров, рассчитывает стоимость и отправляет в Kafka.
     * Процесс включает следующие шаги:
     * 1. Валидация входных данных
     * 2. Поиск пользователя в базе данных
     * 3. Проверка доступности товаров через gRPC
     * 4. Резервирование товаров
     * 5. Расчет стоимости заказа
     * 6. Отправка уведомления в Kafka
     *
     * @param request данные заказа, включая список товаров
     * @param username имя пользователя, оформляющего заказ
     * @return UUID созданного заказа
     * @throws UserNotFoundException если пользователь не найден в базе данных
     * @throws ProductsUnavailableException если некоторые товары недоступны для заказа
     * @throws IllegalArgumentException если входные данные не прошли валидацию
     * @throws RuntimeException при ошибках связи с inventory-service или Kafka
     */
    @Override
    public String processOrder(OrderRequest request, String username) {
        String orderUuid = UUID.randomUUID().toString();
        log.info("[Заказ: {}] === НАЧАЛО ОБРАБОТКИ ЗАКАЗА ===", orderUuid);
        log.info("[Заказ: {}] Пользователь: {}, Товаров: {}", orderUuid, username, request.getItems().size());

        try {
            // 0. Валидация входных данных
            log.info("[Заказ: {}] Шаг 0: Валидация входных данных", orderUuid);
            validateOrderRequest(request, orderUuid);
            log.info("[Заказ: {}] Валидация пройдена успешно", orderUuid);

            // 1. Поиск пользователя
            log.info("[Заказ: {}] Шаг 1: Поиск пользователя в БД", orderUuid);
            User user = findUser(username, orderUuid);
            log.info("[Заказ: {}] Пользователь найден: ID {}", orderUuid, user.getId());

            // 2. Проверка доступности товаров через gRPC
            log.info("[Заказ: {}] Шаг 2: Проверка доступности товаров через gRPC", orderUuid);
            BulkProductResponse bulkResponse = inventoryClient.checkBulkAvailability(request.getItems(), orderUuid);
            log.info("[Заказ: {}] gRPC ответ получен: доступно={}, недоступно={}",
                    orderUuid, bulkResponse.getAvailableItemsCount(), bulkResponse.getUnavailableItemsCount());

            // Если есть недоступные товары - бросаем исключение
            if (bulkResponse.getUnavailableItemsCount() > 0) {
                List<String> unavailableProducts = new ArrayList<>();
                for (ProductResponseItem item : bulkResponse.getUnavailableItemsList()) {
                    String productInfo = String.format("Товар ID:%d '%s' (запрошено: %d, доступно: %d)",
                            item.getProductId(), item.getName(), item.getRequestedQuantity(), item.getAvailableQuantity());
                    unavailableProducts.add(productInfo);
                    log.warn("[Заказ: {}] Недоступный товар: {}", orderUuid, productInfo);
                }
                log.error("[Заказ: {}] Найдены недоступные товары. Количество: {}", orderUuid, unavailableProducts.size());
                throw new ProductsUnavailableException("Некоторые товары недоступны", unavailableProducts);
            }
            log.info("[Заказ: {}] Все товары доступны для заказа", orderUuid);

            // 3. Резервирование товаров
            log.info("[Заказ: {}] Шаг 3: Резервирование товаров через gRPC", orderUuid);
            ReserveProductsResponse reserveResponse = inventoryClient.reserveProducts(orderUuid, request.getItems());
            log.info("[Заказ: {}] Ответ резервирования: успех={}, сообщение='{}', зарезервировано={}",
                    orderUuid, reserveResponse.getSuccess(), reserveResponse.getMessage(), reserveResponse.getReservedItemsCount());

            if (!reserveResponse.getSuccess()) {
                log.error("[Заказ: {}] Ошибка резервирования товаров: {}", orderUuid, reserveResponse.getMessage());
                throw new RuntimeException("Не удалось зарезервировать товары: " + reserveResponse.getMessage());
            }
            log.info("[Заказ: {}] Товары успешно зарезервированы", orderUuid);

            // 4. Создание OrderItems из доступных товаров
            log.info("[Заказ: {}] Шаг 4: Создание элементов заказа", orderUuid);
            List<OrderItemProcessingResult> processingResults = createOrderItemsFromResponse(
                    bulkResponse.getAvailableItemsList(), orderUuid);
            log.info("[Заказ: {}] Создано элементов заказа: {}", orderUuid, processingResults.size());

            // 5. Расчет общей суммы
            log.info("[Заказ: {}] Шаг 5: Расчет общей суммы заказа", orderUuid);
            BigDecimal total = calculateTotal(processingResults);
            List<OrderMessage.OrderItemMessage> orderItems = createOrderItems(processingResults);
            log.info("[Заказ: {}] Сумма заказа рассчитана: {}", orderUuid, total);

            // 6. Создание и отправка сообщения в Kafka
            log.info("[Заказ: {}] Шаг 6: Отправка заказа в Kafka", orderUuid);
            OrderMessage message = createOrderMessage(orderUuid, user, total, orderItems);
            log.info("[Заказ: {}] Kafka сообщение создано: orderId={}, userId={}, total={}, items={}",
                    orderUuid, message.getOrderId(), message.getUserId(), message.getTotalPrice(), message.getItems().size());

            orderProducer.sendOrder(message);
            log.info("[Заказ: {}] Сообщение успешно отправлено в Kafka", orderUuid);

            log.info("[Заказ: {}] === ОБРАБОТКА ЗАКАЗА УСПЕШНО ЗАВЕРШЕНА ===", orderUuid);
            log.info("[Заказ: {}] Итог: orderId={}, user={}, total={}, itemsCount={}",
                    orderUuid, orderUuid, username, total, orderItems.size());

            return orderUuid;

        } catch (ProductsUnavailableException | UserNotFoundException e) {
            // Эти исключения пробрасываем как есть (бизнес-логика)
            log.error("[Заказ: {}] Бизнес-ошибка: {}", orderUuid, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[Заказ: {}] === КРИТИЧЕСКАЯ ОШИБКА ПРИ ОБРАБОТКЕ ЗАКАЗА ===", orderUuid);
            log.error("[Заказ: {}] Тип ошибки: {}", orderUuid, e.getClass().getName());
            log.error("[Заказ: {}] Сообщение ошибки: {}", orderUuid, e.getMessage());
            log.error("[Заказ: {}] Stack trace: ", orderUuid, e);
            throw new RuntimeException("Внутренняя ошибка при обработке заказа: " + e.getMessage(), e);
        }
    }

    /**
     * Валидирует запрос на создание заказа.
     * Проверяет корректность входных данных, включая:
     * - Наличие запроса
     * - Наличие и корректность списка товаров
     * - Корректность ID товаров (положительные числа)
     * - Корректность количества товаров (больше 0)
     *
     * @param request запрос на создание заказа
     * @param orderUuid UUID заказа для логирования
     * @throws IllegalArgumentException если запрос не прошел валидацию
     */
    private void validateOrderRequest(OrderRequest request, String orderUuid) {
        log.debug("[Заказ: {}] Начало валидации запроса", orderUuid);

        if (request == null) {
            log.error("[Заказ: {}] Запрос не может быть null", orderUuid);
            throw new IllegalArgumentException("Запрос не может быть пустым");
        }

        if (request.getItems() == null || request.getItems().isEmpty()) {
            log.error("[Заказ: {}] Список товаров не может быть пустым", orderUuid);
            throw new IllegalArgumentException("Список товаров не может быть пустым");
        }

        for (int i = 0; i < request.getItems().size(); i++) {
            var item = request.getItems().get(i);

            if (item.getProductId() == null) {
                log.error("[Заказ: {}] Не указан ID товара в позиции {}", orderUuid, i);
                throw new IllegalArgumentException("ID товара не может быть пустым в позиции " + i);
            }

            if (item.getProductId() <= 0) {
                log.error("[Заказ: {}] Неверный ID товара {} в позиции {}", orderUuid, item.getProductId(), i);
                throw new IllegalArgumentException("ID товара должен быть положительным числом в позиции " + i);
            }

            if (item.getQuantity() == null) {
                log.error("[Заказ: {}] Не указано количество товара ID {} в позиции {}", orderUuid, item.getProductId(), i);
                throw new IllegalArgumentException("Количество товара не может быть пустым в позиции " + i);
            }

            if (item.getQuantity() <= 0) {
                log.error("[Заказ: {}] Неверное количество товара ID {}: {}", orderUuid, item.getProductId(), item.getQuantity());
                throw new IllegalArgumentException("Количество товара должно быть больше 0 в позиции " + i);
            }

            log.debug("[Заказ: {}] Позиция {} валидна: productId={}, quantity={}",
                    orderUuid, i, item.getProductId(), item.getQuantity());
        }

        log.info("[Заказ: {}] Валидация запроса завершена успешно. Проверено позиций: {}",
                orderUuid, request.getItems().size());
    }

    /**
     * Создает список результатов обработки товаров из gRPC ответа.
     * Преобразует данные от inventory-service в внутренний формат для дальнейшей обработки.
     * Для каждого товара рассчитывает итоговую стоимость с учетом скидки.
     *
     * @param availableItems список доступных товаров из gRPC ответа
     * @param orderUuid UUID заказа для логирования
     * @return список результатов обработки товаров
     */
    private List<OrderItemProcessingResult> createOrderItemsFromResponse(List<ProductResponseItem> availableItems, String orderUuid) {
        log.debug("[Заказ: {}] Создание элементов заказа из {} доступных товаров", orderUuid, availableItems.size());

        List<OrderItemProcessingResult> results = new ArrayList<>();

        for (ProductResponseItem item : availableItems) {
            try {
                BigDecimal price = BigDecimal.valueOf(item.getPrice());
                BigDecimal discount = BigDecimal.valueOf(item.getSale());
                BigDecimal itemTotal = calculateItemTotal(price, discount, item.getRequestedQuantity());

                log.debug("[Заказ: {}] Товар ID:{} обработан. Цена: {}, Скидка: {}, Количество: {}, Итого: {}",
                        orderUuid, item.getProductId(), price, discount, item.getRequestedQuantity(), itemTotal);

                results.add(new OrderItemProcessingResult(
                        item.getProductId(),
                        item.getRequestedQuantity(),
                        price,
                        discount,
                        itemTotal,
                        true,
                        null
                ));
            } catch (Exception e) {
                log.error("[Заказ: {}] Ошибка обработки товара ID {}: {}", orderUuid, item.getProductId(), e.getMessage());
                // Добавляем товар с ошибкой
                results.add(new OrderItemProcessingResult(
                        item.getProductId(),
                        item.getRequestedQuantity(),
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        BigDecimal.ZERO,
                        false,
                        "Ошибка расчета стоимости: " + e.getMessage()
                ));
            }
        }

        log.info("[Заказ: {}] Создано результатов обработки: {} (успешно: {}, с ошибками: {})",
                orderUuid, results.size(),
                results.stream().filter(OrderItemProcessingResult::isAvailable).count(),
                results.stream().filter(r -> !r.isAvailable()).count());

        return results;
    }

    /**
     * Рассчитывает общую сумму заказа на основе результатов обработки товаров.
     * Учитывает только успешно обработанные товары.
     *
     * @param results список результатов обработки товаров
     * @return общая сумма заказа
     */
    private BigDecimal calculateTotal(List<OrderItemProcessingResult> results) {
        BigDecimal total = results.stream()
                .filter(OrderItemProcessingResult::isAvailable) // Только успешно обработанные товары
                .map(OrderItemProcessingResult::getItemTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        log.debug("Рассчитана общая сумма заказа: {}", total);
        return total;
    }

    /**
     * Создает список элементов заказа для Kafka сообщения.
     * Фильтрует только успешно обработанные товары и преобразует их в DTO для Kafka.
     *
     * @param results список результатов обработки товаров
     * @return список элементов заказа для Kafka сообщения
     */
    private List<OrderMessage.OrderItemMessage> createOrderItems(List<OrderItemProcessingResult> results) {
        List<OrderMessage.OrderItemMessage> orderItems = results.stream()
                .filter(OrderItemProcessingResult::isAvailable) // Только успешно обработанные товары
                .map(result -> new OrderMessage.OrderItemMessage(
                        result.getProductId(),
                        result.getQuantity(),
                        result.getPrice(),
                        result.getDiscount(),
                        result.getItemTotal()
                ))
                .collect(Collectors.toList());

        log.debug("Создано элементов заказа для Kafka: {}", orderItems.size());
        return orderItems;
    }

    /**
     * Находит пользователя по имени в базе данных.
     * Используется для привязки заказа к конкретному пользователю.
     *
     * @param username имя пользователя для поиска
     * @param orderUuid UUID заказа для логирования
     * @return найденный пользователь
     * @throws UserNotFoundException если пользователь не найден
     */
    private User findUser(String username, String orderUuid) {
        log.debug("[Заказ: {}] Поиск пользователя: {}", orderUuid, username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("[Заказ: {}] Пользователь не найден в базе данных: {}", orderUuid, username);
                    return new UserNotFoundException("Пользователь не найден: " + username);
                });

        log.debug("[Заказ: {}] Пользователь найден: id={}, username={}, role={}",
                orderUuid, user.getId(), user.getUsername(), user.getRole());
        return user;
    }

    /**
     * Рассчитывает стоимость одного товара с учетом скидки и количества.
     * Формула: (Цена × Количество) × (1 - Скидка)
     * Автоматически обрабатывает некорректные значения (null, отрицательные числа).
     *
     * @param price цена товара
     * @param discount скидка на товар (от 0 до 1, где 1 = 100%)
     * @param quantity количество товара
     * @return итоговая стоимость позиции с учетом скидки
     */
    private BigDecimal calculateItemTotal(BigDecimal price, BigDecimal discount, Integer quantity) {
        // Проверяем входные параметры
        if (price == null) {
            log.warn("Цена товара null, используется 0");
            price = BigDecimal.ZERO;
        }
        if (discount == null) {
            log.warn("Скидка товара null, используется 0");
            discount = BigDecimal.ZERO;
        }
        if (quantity == null || quantity <= 0) {
            log.warn("Количество товара null или <= 0, используется 1");
            quantity = 1;
        }

        // Ограничиваем скидку от 0 до 1 (0% до 100%)
        if (discount.compareTo(BigDecimal.ZERO) < 0) {
            log.warn("Скидка {} меньше 0, устанавливается 0", discount);
            discount = BigDecimal.ZERO;
        }
        if (discount.compareTo(BigDecimal.ONE) > 0) {
            log.warn("Скидка {} больше 1, устанавливается 1", discount);
            discount = BigDecimal.ONE;
        }

        BigDecimal total = price
                .multiply(BigDecimal.valueOf(quantity))
                .multiply(BigDecimal.ONE.subtract(discount));

        log.debug("Расчет стоимости: price={}, quantity={}, discount={}, total={}",
                price, quantity, discount, total);
        return total;
    }

    /**
     * Создает сообщение для отправки в Kafka.
     * Содержит всю необходимую информацию о заказе для notification-service.
     *
     * @param orderUuid UUID заказа
     * @param user пользователь, оформивший заказ
     * @param total общая сумма заказа
     * @param orderItems список элементов заказа
     * @return сообщение для Kafka
     */
    private OrderMessage createOrderMessage(String orderUuid, User user, BigDecimal total,
                                            List<OrderMessage.OrderItemMessage> orderItems) {
        log.debug("[Заказ: {}] Создание Kafka сообщения", orderUuid);

        OrderMessage message = new OrderMessage(
                orderUuid,
                user.getId(),
                user.getUsername(),
                total,
                LocalDateTime.now().toString(),
                orderItems
        );

        log.debug("[Заказ: {}] Kafka сообщение создано: {}", orderUuid, message);
        return message;
    }

    /**
     * Вспомогательный класс для хранения результатов обработки товара.
     * Содержит информацию о товаре, его стоимости и статусе обработки.
     * Используется для промежуточного хранения данных между этапами обработки заказа.
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