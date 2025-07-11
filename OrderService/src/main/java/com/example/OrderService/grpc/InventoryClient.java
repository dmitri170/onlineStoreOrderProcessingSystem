package com.example.OrderService.grpc;

import com.example.inventory.InventoryServiceGrpc;
import com.example.inventory.ProductRequest;
import com.example.inventory.ProductResponse;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;

@Component
public class InventoryClient {

    @GrpcClient("inventory-service")  // имя должно совпадать с именем в конфиге
    private InventoryServiceGrpc.InventoryServiceBlockingStub stub;

    public ProductResponse checkAvailability(Long productId) {
        ProductRequest request = ProductRequest.newBuilder()
                .setProductId(productId)
                .build();

        return stub.checkAvailability(request);
    }
}
