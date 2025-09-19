package com.example.NotificationService.service;

import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.dto.OrderDto;
import com.example.NotificationService.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;
import com.example.NotificationService.entity.OrderItem;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    public OrderService(OrderRepository orderRepository, ModelMapper modelMapper) {
        this.orderRepository = orderRepository;
        this.modelMapper = modelMapper;
    }

    public void saveOrder(OrderDto dto) {
        Order order = new Order();
        order.setOrderId(Long.valueOf(dto.getOrderId()));
        order.setUserId(dto.getUserId());
        order.setTotalPrice(dto.getTotalPrice());
        order.setOrderDate(dto.getOrderDate() != null ? dto.getOrderDate() : LocalDateTime.now());
        order.setStatus("CREATED");

        List<OrderItem> items = dto.getItems().stream()
                .map(itemDto -> {
                    OrderItem item = new OrderItem();
                    item.setOrder(order); // Устанавливаем связь
                    item.setProductId(itemDto.getProductId());
                    item.setQuantity(itemDto.getQuantity());
                    item.setPrice(itemDto.getPrice());
                    item.setSale(itemDto.getSale());
                    item.setItemTotal(calculateItemTotal(itemDto));
                    return item;
                })
                .collect(Collectors.toList());

        order.setItems(items);
        orderRepository.save(order);
    }

    public List<OrderDto> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(order -> modelMapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }

    public List<OrderDto> getOrdersByOrderId(String orderId) {
        return orderRepository.findByOrderId(orderId).stream()
                .map(order -> modelMapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }

    public List<OrderDto> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(String.valueOf(userId)).stream()
                .map(order -> modelMapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }

    private BigDecimal calculateItemTotal(OrderDto.OrderItemDto itemDto) {
        BigDecimal basePrice = itemDto.getPrice().multiply(BigDecimal.valueOf(itemDto.getQuantity()));
        BigDecimal discount = basePrice.multiply(itemDto.getSale());
        return basePrice.subtract(discount);
    }
}