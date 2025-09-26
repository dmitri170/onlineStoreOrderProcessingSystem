package com.example.InventoryService.grpc.server;

import com.example.InventoryService.model.ProductEntity;
import com.example.InventoryService.service.ProductService;
import com.example.InventoryService.grpc.InventoryServiceGrpc;
import com.example.InventoryService.grpc.InventoryProto;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

@GrpcService
public class GrpcServerService extends InventoryServiceGrpc.InventoryServiceImplBase {

    private final ProductService productService;
    private static final Logger log = LoggerFactory.getLogger(GrpcServerService.class);

    @Autowired
    public GrpcServerService(ProductService productService) {
        this.productService = productService;
    }

    @Override
    public void checkAvailability(InventoryProto.ProductRequest request,
                                  StreamObserver<InventoryProto.ProductResponse> responseObserver) {
        log.info("Received gRPC request for product ID: {}", request.getProductId());

        try {
            Optional<ProductEntity> productOptional = productService.getProductInfoForOrder(request.getProductId());

            InventoryProto.ProductResponse.Builder responseBuilder = InventoryProto.ProductResponse.newBuilder()
                    .setProductId(request.getProductId());

            if (productOptional.isPresent()) {
                ProductEntity productEntity = productOptional.get();
                responseBuilder
                        .setPrice(productEntity.getPrice())
                        .setSale(productEntity.getSale() != null ? productEntity.getSale() : 0.0)
                        .setQuantity(productEntity.getQuantity());
                log.info("Product found: ID {}", productEntity.getId());
            } else {
                responseBuilder
                        .setPrice(0.0)
                        .setSale(0.0)
                        .setQuantity(0);
                log.warn("Product with ID {} not found", request.getProductId());
            }

            InventoryProto.ProductResponse response = responseBuilder.build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
            log.info("Response sent for product ID: {}", request.getProductId());

        } catch (Exception e) {
            log.error("Error processing gRPC request for product ID: {}", request.getProductId(), e);
            responseObserver.onError(e);
        }
    }
}