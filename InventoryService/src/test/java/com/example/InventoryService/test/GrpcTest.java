package com.example.InventoryService.test;

import com.example.InventoryService.grpc.stub.InventoryProto;
import com.example.InventoryService.grpc.stub.InventoryServiceGrpc;

public class GrpcTest {
    public void test() {
        // Если эти импорты работают - все ок
        InventoryProto.ProductRequest request = InventoryProto.ProductRequest.newBuilder()
                .setProductId(1L)
                .build();

        System.out.println("gRPC classes are available!");
    }
}