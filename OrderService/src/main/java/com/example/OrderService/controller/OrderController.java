package com.example.OrderService.controller;

import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * REST контроллер для обработки запросов связанных с заказами.
 * Предоставляет endpoint для создания новых заказов.
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    /**
     * Создает новый заказ для аутентифицированного пользователя.
     * Требует валидный JWT токен в заголовке Authorization.
     *
     * @param request данные заказа
     * @param authentication данные аутентификации пользователя
     * @return ответ с UUID созданного заказа
     */
    @PostMapping("/order")
    public ResponseEntity<?> createOrder(
            @Valid @RequestBody OrderRequest request,
            Authentication authentication) {

        String username = authentication.getName();
        String orderId = orderService.processOrder(request, username);
        return ResponseEntity.ok(Map.of(
                "message", "Заказ успешно создан",
                "orderId", orderId
        ));
    }
}