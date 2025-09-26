package com.example.OrderService.grpc;

import com.example.inventory.InventoryServiceGrpc;
import com.example.inventory.ProductRequest;
import com.example.inventory.ProductResponse;
import io.grpc.StatusRuntimeException;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryClient {

    @GrpcClient("inventory-service")
    private InventoryServiceGrpc.InventoryServiceBlockingStub stub;

    @Retryable(value = StatusRuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ProductResponse checkAvailability(Long productId) {
        try {
            log.info("Sending gRPC request for product ID: {}", productId);

            ProductRequest request = ProductRequest.newBuilder()
                    .setProductId(productId)
                    .build();

            ProductResponse response = stub.checkAvailability(request);
            log.info("gRPC response for product {}: quantity={}, price={}, sale={}",
                    productId, response.getQuantity(), response.getPrice(), response.getSale());

            return response;

        } catch (StatusRuntimeException e) {
            log.error("gRPC call failed for product {}: {}", productId, e.getStatus().getDescription());
            throw new RuntimeException("Failed to check availability for product: " + productId, e);
        }
    }
}