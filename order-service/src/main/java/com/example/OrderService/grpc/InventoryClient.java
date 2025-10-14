package com.example.OrderService.grpc;

import com.example.OrderService.dto.OrderItemDTO;
import com.example.inventory.BulkProductRequest;
import com.example.inventory.BulkProductResponse;
import com.example.inventory.InventoryServiceGrpc;
import com.example.inventory.ProductRequestItem;
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
                    .setRqUid(orderUuid);  // Добавляем rqUid

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
}