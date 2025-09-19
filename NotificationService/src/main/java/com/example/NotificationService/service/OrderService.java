package com.example.NotificationService.service;

import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.OrderItem;
import com.example.NotificationService.entity.dto.OrderDto;
import com.example.NotificationService.repository.OrderRepository;
import com.example.NotificationService.repository.OrderItemRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderService(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    public void processOrder(Order order) {
        orderRepository.save(order);
    }

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAllByOrderByIdAsc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<OrderDto> getOrdersByOrderId(Long orderId) {
        Optional<Order> order = orderRepository.findByOrderId(orderId);
        return order.map(o -> List.of(convertToDto(o))).orElse(List.of());
    }

    public List<OrderDto> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderOrderId(orderId);
    }

    private OrderDto convertToDto(Order order) {
        OrderDto dto = new OrderDto();
        dto.setId(order.getId());
        dto.setOrderId(order.getOrderId());
        dto.setUserId(order.getUserId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setOrderDate(order.getOrderDate());

        List<OrderItem> items = orderItemRepository.findByOrderOrderId(order.getOrderId());
        List<OrderDto.OrderItemDto> itemDtos = items.stream()
                .map(this::convertToItemDto)
                .collect(Collectors.toList());
        dto.setItems(itemDtos);

        return dto;
    }

    private OrderDto.OrderItemDto convertToItemDto(OrderItem item) {
        OrderDto.OrderItemDto dto = new OrderDto.OrderItemDto();
        dto.setProductId(item.getProductId());
        dto.setQuantity(item.getQuantity());
        dto.setPrice(item.getPrice());
        dto.setSale(item.getSale());
        dto.setItemTotal(item.getItemTotal());
        return dto;
    }
}