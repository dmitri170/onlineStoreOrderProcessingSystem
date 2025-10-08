package com.example.NotificationService.service;

import com.example.NotificationService.dto.OrderDto;
import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.mapper.OrderMapper;
import com.example.NotificationService.repository.OrderRepository;
import com.example.NotificationService.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Унифицированный сервис для работы с заказами.
 * Обрабатывает сохранение заказов из Kafka и предоставляет данные для API.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;

    // === Методы для обработки Kafka сообщений ===

    /**
     * Обрабатывает и сохраняет заказ из Kafka сообщения.
     *
     * @param order заказ для сохранения
     * @param orderItems список товаров заказа
     * @throws RuntimeException если сохранение не удалось
     */
    @Transactional
    public void processOrder(Order order, List<OrderItem> orderItems) {
        // Сохраняем заказ и товары в одной транзакции
        Order savedOrder = orderRepository.save(order);

        // Устанавливаем связь с сохраненным заказом
        orderItems.forEach(item -> item.setOrder(savedOrder));

        orderItemRepository.saveAll(orderItems);

        log.info("Успешно сохранен заказ {} с {} товарами",
                savedOrder.getOrderId(), orderItems.size());
    }

    /**
     * Проверяет существование заказа с указанным orderId.
     *
     * @param orderId уникальный идентификатор заказа
     * @return true если заказ существует, false в противном случае
     */
    public boolean orderExists(String orderId) {
        return orderRepository.findByOrderId(orderId).isPresent();
    }

    // === Методы для REST API ===

    /**
     * Возвращает все заказы в виде DTO.
     *
     * @return список DTO заказов
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getAllOrders() {
        return orderRepository.findAllByOrderByOrderDateDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Находит заказы по orderId (UUID).
     *
     * @param orderId уникальный идентификатор заказа
     * @return список DTO заказов (обычно один элемент)
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByOrderId(String orderId) {
        Optional<Order> order = orderRepository.findByOrderId(orderId);
        return order.map(o -> List.of(convertToDto(o)))
                .orElse(List.of());
    }

    /**
     * Находит все заказы пользователя.
     *
     * @param userId идентификатор пользователя
     * @return список DTO заказов пользователя
     */
    @Transactional(readOnly = true)
    public List<OrderDto> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Находит все товары заказа по orderId.
     *
     * @param orderId уникальный идентификатор заказа
     * @return список товаров заказа
     */
    @Transactional(readOnly = true)
    public List<OrderItem> getOrderItemsByOrderId(String orderId) {
        return orderItemRepository.findByOrderOrderId(orderId);
    }

    /**
     * Находит заказ по orderId с товарами.
     *
     * @param orderId уникальный идентификатор заказа
     * @return заказ с товарами или null если не найден
     */
    @Transactional(readOnly = true)
    public Order findOrderWithItems(String orderId) {
        return orderRepository.findByOrderId(orderId).orElse(null);
    }

    /**
     * Возвращает все заказы с информацией о товарах.
     *
     * @return список всех заказов с товарами
     */
    @Transactional(readOnly = true)
    public List<Order> findAllOrdersWithItems() {
        return orderRepository.findAllWithItems();
    }

    // === Вспомогательные методы ===

    /**
     * Преобразует сущность Order в DTO.
     *
     * @param order сущность заказа
     * @return DTO заказа
     */
    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setOrderDate(order.getOrderDate());

        // Загружаем items для этого заказа
        List<OrderItem> items = orderItemRepository.findByOrderOrderId(order.getOrderId());
        List<OrderDto.OrderItemDto> itemDtos = items.stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());
        dto.setItems(itemDtos);

        return dto;
    }

    /**
     * Преобразует сущность OrderItem в DTO.
     *
     * @param item сущность товара заказа
     * @return DTO товара заказа
     */
    private OrderDto.OrderItemDto convertToItemDto(OrderItem item) {
        OrderDto.OrderItemDto dto = new OrderDto.OrderItemDto();
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setDiscount(item.getDiscount());
        dto.setItemTotal(item.getItemTotal());
        return dto;
    }
}