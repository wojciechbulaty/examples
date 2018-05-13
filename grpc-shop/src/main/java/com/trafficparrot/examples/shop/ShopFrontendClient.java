package com.trafficparrot.examples.shop;

import com.trafficparrot.examples.shop.proto.Item;
import com.trafficparrot.examples.shop.proto.OrderGrpc;
import com.trafficparrot.examples.shop.proto.OrderStatus;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

import java.util.concurrent.TimeUnit;

public class ShopFrontendClient {
    private final ManagedChannel channel;
    private final OrderGrpc.OrderBlockingStub blockingStub;

    public ShopFrontendClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext(true));
    }

    public ShopFrontendClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        blockingStub = OrderGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void order(int sku, int quantity) {
        Item item = Item.newBuilder().setSku(sku).setQuantity(quantity).build();
        OrderStatus orderStatus = blockingStub.purchase(item);
        System.out.println("orderStatus = " + orderStatus);
    }

    public static void main(String[] args) throws InterruptedException {
        ShopFrontendClient client = new ShopFrontendClient("localhost", ShopBackendServer.PORT);
        try {
            client.order(43423232, 444);
        } finally {
            client.shutdown();
        }
    }
}
