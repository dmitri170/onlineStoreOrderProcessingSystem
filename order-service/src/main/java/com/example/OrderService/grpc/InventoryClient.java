package com.example.OrderService.grpc;

import com.example.OrderService.dto.OrderItemDTO;
import com.example.inventory.*;
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
     * Проверяет доступность всех товаров в заказе
     */
    @Retryable(value = StatusRuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public BulkProductResponse checkBulkAvailability(List<OrderItemDTO> orderItems, String orderUuid) {
        try {
            log.info("[Заказ: {}] Отправка bulk gRPC запроса для {} товаров", orderUuid, orderItems.size());

            BulkProductRequest.Builder requestBuilder = BulkProductRequest.newBuilder()
                    .setRqUid(orderUuid);  // Устанавливаем rqUid

            for (OrderItemDTO item : orderItems) {
                ProductRequestItem requestItem = ProductRequestItem.newBuilder()
                        .setProductId(item.getProductId())
                        .setRequestedQuantity(item.getQuantity())
                        .build();
                requestBuilder.addItems(requestItem);
            }

            BulkProductResponse response = stub.checkAvailability(requestBuilder.build());

            log.info("[Заказ: {}] Bulk gRPC ответ: доступно {} товаров, недоступно {} товаров",
                    orderUuid, response.getAvailableItemsCount(), response.getUnavailableItemsCount());

            return response;

        } catch (StatusRuntimeException e) {
            log.error("[Заказ: {}] Bulk gRPC вызов не удался: {}", orderUuid, e.getStatus().getDescription());
            throw new RuntimeException("Не удалось проверить доступность товаров", e);
        }
    }
    /**
     * Резервирует товары после успешной проверки заказа
     */
    @Retryable(value = StatusRuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ReserveProductsResponse reserveProducts(String orderId, List<OrderItemDTO> orderItems) {
        try {
            log.info("[Заказ: {}] Резервирование {} товаров", orderId, orderItems.size());

            ReserveProductsRequest.Builder requestBuilder = ReserveProductsRequest.newBuilder()
                    .setOrderId(orderId);

            for (OrderItemDTO item : orderItems) {
                ProductRequestItem requestItem = ProductRequestItem.newBuilder()
                        .setProductId(item.getProductId())
                        .setRequestedQuantity(item.getQuantity())
                        .build();
                requestBuilder.addItems(requestItem);
            }

            ReserveProductsResponse response = stub.reserveProducts(requestBuilder.build());

            if (response.getSuccess()) {
                log.info("[Заказ: {}] Товары успешно зарезервированы. Зарезервировано: {} товаров",
                        orderId, response.getReservedItemsCount());
            } else {
                log.warn("[Заказ: {}] Ошибка резервирования товаров: {}", orderId, response.getMessage());
            }

            return response;

        } catch (StatusRuntimeException e) {
            log.error("[Заказ: {}] gRPC вызов резервирования не удался: {}", orderId, e.getStatus().getDescription());
            throw new RuntimeException("Не удалось зарезервировать товары", e);
        }
    }
}