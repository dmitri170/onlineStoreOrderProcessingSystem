package com.example.InventoryService.grpc.server;


import com.example.InventoryService.model.ProductEntity;
import com.example.InventoryService.service.ProductService;
import com.example.InventoryService.grpc.stub.ProductRequest;
import com.example.InventoryService.grpc.stub.ProductResponse;
import com.example.InventoryService.grpc.stub.InventoryServiceGrpc;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

@GrpcService
public class GrpcServerService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final ProductService productService; // Заменяем репозиторий на сервис

    private static final Logger log = LoggerFactory.getLogger(GrpcServerService.class);

    // Внедряем зависимость через конструктор
    public GrpcServerService(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void checkAvailability(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        log.info("Received gRPC request for product ID: {}", request.getProductId());

        // Используем сервис для получения данных
        Optional<ProductEntity> productOptional = productService.getProductInfoForOrder(request.getProductId());

        if (productOptional.isEmpty()) {
            // Если товар не найден, отправляем ответ с quantity = 0
            ProductResponse response = ProductResponse.newBuilder()
                    .setProductId(request.getProductId())
                    .setQuantity(0)
                    .setPrice(0.0)
                    .setSale(0.0)
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.warn("Product with ID {} not found.", request.getProductId());
            return;
        }

        ProductEntity productEntity = productOptional.get();

        // Строим ответ из данных о товаре
        ProductResponse response = ProductResponse.newBuilder()
                .setProductId(productEntity.getId())
                .setPrice(productEntity.getPrice())
                .setSale(productEntity.getSale())
                .setQuantity(productEntity.getQuantity())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        log.info("Response sent for product ID: {}", productEntity.getId());
    }
}
