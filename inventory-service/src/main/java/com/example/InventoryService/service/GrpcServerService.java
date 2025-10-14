package com.example.InventoryService.service;

import com.example.InventoryService.entity.ProductEntity;
import com.example.InventoryService.mapper.GrpcMapper;
import com.example.inventory.BulkProductRequest;
import com.example.inventory.BulkProductResponse;
import com.example.inventory.InventoryServiceGrpc;
import io.grpc.Status;
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

    @Override
    public void checkAvailability(BulkProductRequest request, StreamObserver<BulkProductResponse> responseObserver) {
        String rqUid = request.getRqUid(); // Получаем rqUid из запроса
        log.info("[Inventory: RqUid {}] Получен bulk запрос на проверку {} товаров",
                rqUid, request.getItemsCount());

        try {
            BulkProductResponse response = productService.checkBulkAvailability(request, rqUid);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("[Inventory: RqUid {}] Bulk проверка завершена успешно", rqUid);

        } catch (Exception e) {
            log.error("[Inventory: RqUid {}] Ошибка при bulk проверке: {}", rqUid, e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
} 
