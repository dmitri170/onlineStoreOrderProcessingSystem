package com.example.InventoryService.service;

import com.example.InventoryService.entity.ProductEntity;
import com.example.InventoryService.mapper.GrpcMapper;
import com.example.inventory.InventoryServiceGrpc;
import com.example.inventory.ProductRequest;
import com.example.inventory.ProductResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * gRPC сервис для обработки запросов от Order Service.
 * Предоставляет методы проверки наличия товаров.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GrpcServerService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final ProductService productService;
    private final GrpcMapper grpcMapper;

    /**
     * Обрабатывает запрос на проверку доступности товара.
     *
     * @param request запрос с идентификатором товара
     * @param responseObserver наблюдатель для отправки ответа
     */
    @Override
    public void checkAvailability(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        try {
            Long productId = request.getProductId();
            log.info("Получен gRPC запрос для товара ID: {}", productId);

            // Используем внутренний метод ProductService
            ProductEntity product = productService.findProductEntityById(productId);
            ProductResponse response = grpcMapper.toProductResponse(product);

            log.info("Отправка gRPC ответа для товара {}: количество={}", productId, product.getQuantity());
            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Ошибка обработки gRPC запроса: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Ошибка проверки доступности: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}