package com.example.InventoryService.grpc.server;


import com.example.InventoryService.model.Product;
import com.example.InventoryService.service.ProductService;
import com.example.InventoryService.grpc.stub.ProductRequest;
import com.example.InventoryService.grpc.stub.ProductResponse;
import com.example.InventoryService.grpc.stub.InventoryServiceGrpc;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.Optional;

@Slf4j
@GrpcService
public class GrpcServerService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final ProductService productService; // Заменяем репозиторий на сервис

    // Внедряем зависимость через конструктор
    public GrpcServerService(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void checkAvailability(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        log.info("Received gRPC request for product ID: {}", request.getProductId());

        // Используем сервис для получения данных
        Optional<Product> productOptional = productService.getProductInfoForOrder(request.getProductId());

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

        Product product = productOptional.get();

        // Строим ответ из данных о товаре
        ProductResponse response = ProductResponse.newBuilder()
                .setProductId(product.getId())
                .setPrice(product.getPrice())
                .setSale(product.getSale())
                .setQuantity(product.getQuantity())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
        log.info("Response sent for product ID: {}", product.getId());
    }
}
