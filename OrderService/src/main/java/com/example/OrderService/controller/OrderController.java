package com.example.OrderService.controller;

import com.example.OrderService.dto.OrderItemDTO;
import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.message.model.OrderMessage;
import com.example.OrderService.repository.UserRepository;
import com.example.OrderService.grpc.InventoryClient;
import com.example.OrderService.kafka.OrderProducer;
import com.example.OrderService.service.OrderService;
import com.example.inventory.ProductResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/order")
    public ResponseEntity<?> createOrder(
            @RequestBody OrderRequest request,
            Authentication authentication) {
/*
        // Извлекаем имя пользователя из JWT
        String username = authentication.getName();

        // Проверяем, существует ли пользователь
        Long userId = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"))
                .getId();

        List<OrderItemDTO> validatedItems = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        // Проверка каждого товара и расчёт итоговой суммы
        for (OrderItemDTO item : request.getItems()) {
            ProductResponse product = inventoryClient.checkAvailability(item.getProductId());

            if (product.getQuantity() < item.getQuantity()) {
                return ResponseEntity.badRequest().body("Insufficient stock for product ID " + item.getProductId());
            }

            BigDecimal itemTotal = BigDecimal.valueOf(product.getPrice())
                    .multiply(BigDecimal.valueOf(1 - product.getSale()))
                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            total = total.add(itemTotal);
            validatedItems.add(item);
        }

        // Создаём сообщение и отправляем в Kafka
        OrderMessage message = OrderMessage.builder()
                .orderId(UUID.randomUUID().toString())
                .userId(userId)
                .items(validatedItems)
                .totalPrice(total.doubleValue())
                .build();

        orderProducer.sendOrder(message);
*/

        String username = authentication.getName();
        String orderId = orderService.processOrder(request, username);
        return ResponseEntity.ok("Order created successfully with ID: " + orderId);
    }
}
