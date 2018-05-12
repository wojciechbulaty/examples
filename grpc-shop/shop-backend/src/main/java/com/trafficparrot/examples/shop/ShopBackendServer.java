package com.trafficparrot.examples.shop;

import com.trafficparrot.examples.shop.proto.Item;
import com.trafficparrot.examples.shop.proto.OrderGrpc;
import com.trafficparrot.examples.shop.proto.OrderStatus;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

public class ShopBackendServer {
    private Server server;

    private void start() throws IOException, InterruptedException {
        int port = 12345;
        server = ServerBuilder.forPort(port)
                .addService(new OrderGrpc.OrderImplBase() {
                    @Override
                    public void purchase(Item request, StreamObserver<OrderStatus> responseObserver) {
                        System.out.println("Purchase request = " + request);
                    }
                })
                .build()
                .start();
        System.out.println("Server started on localhost:" + port);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                System.out.println("Shutting down gRPC server since JVM is shutting down");
                server.shutdown();
                System.out.println("Server shut down!");
            }
        });
        server.awaitTermination();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        ShopBackendServer server = new ShopBackendServer();
        server.start();
    }
}
