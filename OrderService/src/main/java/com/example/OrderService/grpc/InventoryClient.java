package com.example.OrderService.grpc;

import com.example.InventoryService.grpc.stub.InventoryProto;
import com.example.InventoryService.grpc.stub.InventoryServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component
public class InventoryClient {

    private ManagedChannel channel;
    private InventoryServiceGrpc.InventoryServiceBlockingStub stub;

    @PostConstruct
    public void init() {
        channel = ManagedChannelBuilder.forAddress("localhost", 9090)
                .usePlaintext()
                .build();
        stub = InventoryServiceGrpc.newBlockingStub(channel);
    }

    public InventoryProto.ProductResponse checkAvailability(long productId) {
        InventoryProto.ProductRequest request = InventoryProto.ProductRequest.newBuilder()
                .setProductId(productId)
                .build();
        return stub.checkAvailability(request);
    }

    @PreDestroy
    public void shutdown() {
        if (channel != null) {
            channel.shutdown();
        }
    }
}