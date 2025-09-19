package com.example.OrderService.`grpc;

import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class InventoryClient {

    @GrpcClient("inventory-service")
    private com.example.inventoryservice.grpc.stub.InventoryServiceGrpc.InventoryServiceBlockingStub stub;

    public com.example.inventoryservice.grpc.stub.ProductResponse checkAvailability(Long productId) {
        com.example.inventoryservice.grpc.stub.ProductRequest request = com.example.inventoryservice.grpc.stub.ProductRequest.newBuilder()
                .setProductId(productId)
                .build();
        return stub.checkAvailability(request);
    }
}