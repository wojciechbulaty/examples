package com.trafficparrot.examples.shop;

import com.trafficparrot.examples.shop.proto.Item;
import com.trafficparrot.examples.shop.proto.OrderGrpc;
import com.trafficparrot.examples.shop.proto.OrderStatus;
import com.trafficparrot.examples.shop.proto.Status;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class ShopBackendServer {
    public static final int PORT = 12345;
    private Server server;

    private void start() throws IOException, InterruptedException {
        server = ServerBuilder.forPort(PORT)
                .addService(new OrderGrpc.OrderImplBase() {
                    @Override
                    public void purchase(Item request, StreamObserver<OrderStatus> responseObserver) {
                        System.out.println("Purchase request = " + request);
                        responseObserver.onNext(OrderStatus.newBuilder().setStatus(Status.SUCCESS).setMessage("Order processed: " + request).build());
                        responseObserver.onCompleted();
                    }
                })
                .build()
                .start();
        System.out.println("Server started on localhost:" + PORT);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server");
            server.shutdown();
            System.out.println("Server shut down!");
        }));
        server.awaitTermination();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ShopBackendServer server = new ShopBackendServer();
        server.start();
    }
}
