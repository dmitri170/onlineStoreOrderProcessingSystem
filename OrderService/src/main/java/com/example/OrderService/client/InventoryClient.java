package com.example.OrderService.client;

import com.example.inventory.InventoryServiceGrpc;
import com.example.inventory.ProductRequest;
import com.example.inventory.ProductResponse;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryClient {

    @Autowired
    private InventoryServiceGrpc.InventoryServiceBlockingStub inventoryStub;

    @Retryable(value = StatusRuntimeException.class, maxAttempts = 3, backoff = @Backoff(delay = 1000))
    public ProductResponse checkAvailability(Long productId) {
        try {
            ProductRequest request = ProductRequest.newBuilder().setProductId(productId).build();
            return inventoryStub.checkAvailability(request);
        } catch (StatusRuntimeException e) {
            throw new RuntimeException("Failed to check availability: " + e.getStatus().getDescription());
        }
    }
}
