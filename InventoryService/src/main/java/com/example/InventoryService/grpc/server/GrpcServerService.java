package com.example.InventoryService.grpc.server;

import com.example.inventory.InventoryServiceGrpc;
import com.example.inventory.ProductRequest;
import com.example.inventory.ProductResponse;
import com.example.InventoryService.model.ProductEntity;
import com.example.InventoryService.repository.ProductRepository;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class GrpcServerService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final ProductRepository productRepository;

    @Override
    public void checkAvailability(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        try {
            Long productId = request.getProductId();
            log.info("Received gRPC request for product ID: {}", productId);

            ProductEntity product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

            // Строим gRPC ответ
            ProductResponse response = ProductResponse.newBuilder()
                    .setProductId(product.getId())
                    .setName(product.getName() != null ? product.getName() : "")
                    .setQuantity(product.getQuantity() != null ? product.getQuantity() : 0)
                    .setPrice(product.getPrice() != null ? product.getPrice() : 0.0)
                    .setSale(product.getSale() != null ? product.getSale() : 0.0)
                    .setAvailable(product.getQuantity() != null && product.getQuantity() > 0)
                    .build();

            log.info("Sending gRPC response for product {}: quantity={}", productId, product.getQuantity());

            responseObserver.onNext(response);
            responseObserver.onCompleted();

        } catch (Exception e) {
            log.error("Error processing gRPC request: {}", e.getMessage());
            responseObserver.onError(io.grpc.Status.INTERNAL
                    .withDescription("Error checking availability: " + e.getMessage())
                    .asRuntimeException());
        }
    }
}