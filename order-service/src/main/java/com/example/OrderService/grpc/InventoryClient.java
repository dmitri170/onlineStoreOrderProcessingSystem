package com.example.OrderService.grpc;

import com.example.OrderService.dto.OrderItemDTO;
import com.example.inventory.*;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class InventoryClient {

    @GrpcClient("inventory-service")
    private InventoryServiceGrpc.InventoryServiceBlockingStub stub;

    public BulkProductResponse checkBulkAvailability(List<OrderItemDTO> orderItems, String orderUuid) {
        try {
            log.info("[Заказ: {}] Отправка bulk gRPC запроса для {} товаров", orderUuid, orderItems.size());

            // Детальная информация о товарах
            for (OrderItemDTO item : orderItems) {
                log.debug("[Заказ: {}] Товар ID: {}, Количество: {}",
                        orderUuid, item.getProductId(), item.getQuantity());
            }

            BulkProductRequest.Builder requestBuilder = BulkProductRequest.newBuilder()
                    .setRqUid(orderUuid);

            for (OrderItemDTO item : orderItems) {
                ProductRequestItem requestItem = ProductRequestItem.newBuilder()
                        .setProductId(item.getProductId())
                        .setRequestedQuantity(item.getQuantity())
                        .build();
                requestBuilder.addItems(requestItem);
            }

            BulkProductRequest request = requestBuilder.build();
            log.debug("[Заказ: {}] gRPC Request: {}", orderUuid, request);

            BulkProductResponse response = stub.checkAvailability(request);

            log.info("[Заказ: {}] Bulk gRPC ответ: доступно {} товаров, недоступно {} товаров",
                    orderUuid, response.getAvailableItemsCount(), response.getUnavailableItemsCount());

            // Детальная информация о доступных товарах
            for (ProductResponseItem item : response.getAvailableItemsList()) {
                log.debug("[Заказ: {}] Доступен товар ID: {}, Название: {}, Цена: {}, Доступно: {}",
                        orderUuid, item.getProductId(), item.getName(), item.getPrice(), item.getAvailableQuantity());
            }

            // Детальная информация о недоступных товарах
            for (ProductResponseItem item : response.getUnavailableItemsList()) {
                log.debug("[Заказ: {}] Недоступен товар ID: {}, Название: {}, Запрошено: {}, Доступно: {}",
                        orderUuid, item.getProductId(), item.getName(), item.getRequestedQuantity(), item.getAvailableQuantity());
            }

            return response;

        } catch (StatusRuntimeException e) {
            log.error("[Заказ: {}] Bulk gRPC вызов не удался. Статус: {}, Описание: {}",
                    orderUuid, e.getStatus().getCode(), e.getStatus().getDescription(), e);
            throw new RuntimeException("Не удалось проверить доступность товаров: " + e.getStatus().getDescription(), e);
        } catch (Exception e) {
            log.error("[Заказ: {}] Неожиданная ошибка при gRPC вызове: {}", orderUuid, e.getMessage(), e);
            throw new RuntimeException("Неожиданная ошибка при проверке доступности товаров", e);
        }
    }

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

            ReserveProductsRequest request = requestBuilder.build();
            log.debug("[Заказ: {}] gRPC Reserve Request: {}", orderId, request);

            ReserveProductsResponse response = stub.reserveProducts(request);

            log.info("[Заказ: {}] Ответ резервирования: успех={}, сообщение={}, зарезервировано={}",
                    orderId, response.getSuccess(), response.getMessage(), response.getReservedItemsCount());

            return response;

        } catch (StatusRuntimeException e) {
            log.error("[Заказ: {}] gRPC вызов резервирования не удался. Статус: {}, Описание: {}",
                    orderId, e.getStatus().getCode(), e.getStatus().getDescription(), e);
            throw new RuntimeException("Не удалось зарезервировать товары: " + e.getStatus().getDescription(), e);
        } catch (Exception e) {
            log.error("[Заказ: {}] Неожиданная ошибка при резервировании: {}", orderId, e.getMessage(), e);
            throw new RuntimeException("Неожиданная ошибка при резервировании товаров", e);
        }
    }
}