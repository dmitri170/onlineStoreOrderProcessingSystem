package com.example.OrderService.service;

import com.example.OrderService.InventoryClient;
import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.entity.User;
import com.example.OrderService.kafka.OrderProducer;
import com.example.OrderService.message.model.OrderMessage;
import com.example.OrderService.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final InventoryClient inventoryClient;
    private final OrderProducer orderProducer;
    private final UserRepository userRepository;

    @Transactional
    public String processOrder(OrderRequest request, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        BigDecimal total = BigDecimal.ZERO;

        for (OrderItemDTO item : request.getItems()) {
            com.example.InventoryService.grpc.stub.ProductResponse product =
                    inventoryClient.checkAvailability(item.getProductId());

            if (product.getQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product ID " + item.getProductId());
            }

            BigDecimal price = BigDecimal.valueOf(product.getPrice());
            BigDecimal sale = BigDecimal.valueOf(product.getSale());
            BigDecimal quantity = BigDecimal.valueOf(item.getQuantity());

            BigDecimal itemTotal = price.multiply(quantity).multiply(BigDecimal.ONE.subtract(sale));
            total = total.add(itemTotal);
        }

        OrderMessage message = OrderMessage.builder()
                .orderId(UUID.randomUUID().toString())
                .userId(user.getId())
                .items(request.getItems())
                .totalPrice(total.doubleValue())
                .build();

        orderProducer.sendOrder(message);
        return message.getOrderId();
    }
}