package com.example.OrderService.controller;

import com.example.OrderService.dto.ErrorResponse;
import com.example.OrderService.dto.OrderRequest;
import com.example.OrderService.dto.OrderResponseDto;
import com.example.OrderService.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST контроллер для обработки запросов связанных с заказами.
 * Предоставляет endpoint для создания новых заказов.
 */
@Slf4j
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

        try {
            String username = authentication.getName();
            log.info("Creating order for user: {}, items: {}", username, request.getItems());

            String orderId = orderService.processOrder(request, username);

            OrderResponseDto response = new OrderResponseDto(
                    "Заказ успешно создан",
                    orderId
            );

            log.info("Order created successfully. Order ID: {}, User: {}", orderId, username);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error creating order for user: {}", authentication.getName(), e);

            // Исправленный конструктор ErrorResponse
            ErrorResponse errorResponse = new ErrorResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    "Ошибка при создании заказа: " + e.getMessage(),
                    "/api/order"
            );

            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}