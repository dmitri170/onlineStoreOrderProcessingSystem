package com.example.NotificationService.service;

import com.example.NotificationService.entity.Order;
import com.example.NotificationService.entity.dto.OrderDto;
import com.example.NotificationService.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ModelMapper modelMapper;

    public Order saveOrder(OrderDto orderDto) {
        Order order = modelMapper.map(orderDto, Order.class);
        return orderRepository.save(order);
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
        return orderRepository.findByUserId(userId).stream()
                .map(order -> modelMapper.map(order, OrderDto.class))
                .collect(Collectors.toList());
    }
}