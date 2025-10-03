package com.example.InventoryService.grpc.server;

import com.example.InventoryService.model.ProductEntity;
import com.example.InventoryService.mapper.GrpcMapper;
import com.example.InventoryService.repository.ProductRepository;
import com.example.inventory.InventoryServiceGrpc;
import com.example.inventory.ProductRequest;
import com.example.inventory.ProductResponse;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class GrpcServerService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final ProductRepository productRepository;
    private final GrpcMapper grpcMapper;

    @Override
    public void checkAvailability(ProductRequest request, StreamObserver<ProductResponse> responseObserver) {
        try {
            Long productId = request.getProductId();
            log.info("Received gRPC request for product ID: {}", productId);

            ProductEntity product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

            // Используем маппер для преобразования
            ProductResponse response = grpcMapper.toProductResponse(product);

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