package com.trafficparrot.examples.shop;

import com.trafficparrot.examples.shop.proto.Item;
import com.trafficparrot.examples.shop.proto.OrderGrpc;
import com.trafficparrot.examples.shop.proto.OrderStatus;
import com.trafficparrot.examples.shop.proto.Status;
import com.trafficparrot.examples.shop.util.Logger;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;

import java.io.IOException;

import static com.trafficparrot.examples.shop.util.Logger.info;

public class ShopBackendServer {
    public static final int PORT = 12345;
    private Server server;

    private void start() throws IOException, InterruptedException {
        server = ServerBuilder.forPort(PORT)
                .addService(new OrderService())
                .build()
                .start();
        info("Server started on localhost:" + PORT);
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

    private static class OrderService extends OrderGrpc.OrderImplBase {
        @Override
        public void purchase(Item request, StreamObserver<OrderStatus> responseObserver) {
            info("Request to purchase received for: \n" + request);
            responseObserver.onNext(OrderStatus.newBuilder().setStatus(Status.SUCCESS).setMessage("Order processed: " + request).build());
            responseObserver.onCompleted();
        }
    }
}
