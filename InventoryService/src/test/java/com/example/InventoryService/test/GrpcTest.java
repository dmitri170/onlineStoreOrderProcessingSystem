package com.example.InventoryService.test;

import com.example.InventoryService.grpc.InventoryProto;
import com.example.InventoryService.grpc.InventoryServiceGrpc;

public class GrpcTest {
    public void test() {
        InventoryProto.ProductRequest request = InventoryProto.ProductRequest.newBuilder()
                .setProductId(1L)
                .build();
        System.out.println("gRPC imports work correctly!");
    }
}