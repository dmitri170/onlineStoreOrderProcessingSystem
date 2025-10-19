package com.example.InventoryService.service;

import com.example.InventoryService.entity.ProductEntity;
import com.example.InventoryService.mapper.GrpcMapper;
import com.example.inventory.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import org.springframework.stereotype.Service;

/**
 * gRPC сервис для обработки запросов от Order Service.
 * Предоставляет методы проверки наличия товаров.
 */
@Service
@Slf4j
@GrpcService
@RequiredArgsConstructor
public class GrpcServerService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final ProductService productService;

    @Override
    public void checkAvailability(BulkProductRequest request, StreamObserver<BulkProductResponse> responseObserver) {
        String rqUid = request.getRqUid();
        log.info("[Inventory: RqUid {}] Получен bulk запрос на проверку {} товаров",
                rqUid, request.getItemsCount());

        try {
            BulkProductResponse response = productService.checkBulkAvailability(request,rqUid);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("[Inventory: RqUid {}] Bulk проверка завершена успешно", rqUid);

        } catch (Exception e) {
            log.error("[Inventory: RqUid {}] Ошибка при bulk проверке: {}", rqUid, e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
    @Override
    public void reserveProducts(ReserveProductsRequest request, StreamObserver<ReserveProductsResponse> responseObserver) {
        String orderId = request.getOrderId();
        log.info("[Inventory] Получен запрос на резервирование товаров для заказа: {}", orderId);

        try {
            ReserveProductsResponse response = productService.reserveProducts(request);
            responseObserver.onNext(response);
            responseObserver.onCompleted();

            log.info("[Inventory] Резервирование для заказа {} завершено: {}", orderId,
                    response.getSuccess() ? "успешно" : "с ошибками");

        } catch (Exception e) {
            log.error("[Inventory] Ошибка при резервировании товаров для заказа {}: {}", orderId, e.getMessage(), e);
            responseObserver.onError(Status.INTERNAL.withDescription(e.getMessage()).asRuntimeException());
        }
    }
} 
