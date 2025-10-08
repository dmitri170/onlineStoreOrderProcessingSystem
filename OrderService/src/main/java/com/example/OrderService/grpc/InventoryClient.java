package com.example.OrderService.grpc;

import com.example.OrderService.dto.OrderItemDTO;
import com.example.inventory.InventoryServiceGrpc;
import com.example.inventory.ProductRequest;
import com.example.inventory.ProductResponse;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * gRPC клиент для взаимодействия с Inventory Service.
 * Отвечает за проверку наличия товаров и резервирование.
 */
@Component
@Slf4j
public class InventoryClient {

    @GrpcClient("inventory-service")
    private InventoryServiceGrpc.InventoryServiceBlockingStub stub;


    /**
     * Проверяет доступность одного товара в Inventory Service.
     *
     * @param productId идентификатор товара
     * @return информация о доступности товара
     * @throws RuntimeException если gRPC вызов не удался
     */
    @Retryable(value = StatusRuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ProductResponse checkAvailability(Long productId) {
        try {
            log.info("Отправка gRPC запроса для товара ID: {}", productId);

            ProductRequest request = ProductRequest.newBuilder()
                    .setProductId(productId)
                    .build();

            ProductResponse response = stub.checkAvailability(request);
            log.info("gRPC ответ для товара {}: количество={}, цена={}, скидка={}",
                    productId, response.getQuantity(), response.getPrice(), response.getSale());

            return response;

        } catch (StatusRuntimeException e) {
            log.error("gRPC вызов не удался для товара {}: {}", productId, e.getStatus().getDescription());
            throw new RuntimeException("Не удалось проверить доступность товара: " + productId, e);
        }
    }
}