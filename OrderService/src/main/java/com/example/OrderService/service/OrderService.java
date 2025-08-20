package com.example.OrderService.service;

import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.entity.User;
import com.example.OrderService.grpc.InventoryClient;
import com.example.OrderService.kafka.OrderProducer;
import com.example.OrderService.message.model.OrderMessage;
import com.example.OrderService.repository.UserRepository;
import com.example.inventory.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final InventoryClient inventoryClient;
    private final OrderProducer orderProducer;
    private final UserRepository userRepository;

    public String processOrder(OrderRequest request, String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("User not found: " + username));

        BigDecimal total = BigDecimal.ZERO;
        List<OrderItemDTO> items = new ArrayList<>();

        for (OrderItemDTO item : request.getItems()) {
            ProductResponse product = inventoryClient.checkAvailability(item.getProductId());

            if (product.getQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for product ID " + item.getProductId());
            }

            BigDecimal price = BigDecimal.valueOf(product.getPrice());
            BigDecimal sale = BigDecimal.valueOf(product.getSale());

            BigDecimal itemTotal = price
                    .multiply(BigDecimal.valueOf(item.getQuantity()))
                    .multiply(BigDecimal.ONE.subtract(sale));

            total = total.add(itemTotal);
            items.add(item);
        }

        OrderMessage message = OrderMessage.builder()
                .orderId(UUID.randomUUID().toString())
                .userId(user.getId())
                .items(items)
                .totalPrice(total.doubleValue())
                .build();

        orderProducer.sendOrder(message);

        return message.getOrderId();
    }

}
