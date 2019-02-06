package com.trafficparrot.examples.shop;

import com.trafficparrot.examples.shop.proto.Item;
import com.trafficparrot.examples.shop.proto.OrderGrpc;
import com.trafficparrot.examples.shop.proto.OrderStatus;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static java.awt.BorderLayout.*;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class ShopFrontendClient {
    private static final String NEW_LINE = System.getProperty("line.separator");
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

    public void order(int sku, int quantity, ShopApplicationLogger shopApplicationLogger) {
        Item item = Item.newBuilder().setSku(sku).setQuantity(quantity).build();
        try {
            OrderStatus orderStatus = blockingStub.purchase(item);
            shopApplicationLogger.info("Received " + orderStatus);
        } catch (StatusRuntimeException e) {
            shopApplicationLogger.info("Received an error: " + e.getMessage() + "\n" + e.getStatus()  + "\n" + e.getTrailers());
            throw new IllegalArgumentException(e);
        }
    }

    public static void main(String[] args) {
        runUi();
    }

    private static void runUi() {
        JFrame frame = new JFrame("Shopping Application");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container container = frame.getContentPane();

        JPanel serverDetails = new JPanel();
        serverDetails.add(new JLabel("Server host"));
        JTextField host = new JTextField("localhost", 15);
        serverDetails.add(host);
        serverDetails.add(new JLabel("Server port"));
        JTextField port = new JTextField("12345", 6);
        serverDetails.add(port);
        container.add(serverDetails, PAGE_START);

        JTextArea output = new JTextArea("", 20, 40);
        JScrollPane outputPanel = new JScrollPane(output, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        output.setAutoscrolls(true);
        container.add(outputPanel, PAGE_END);

        JPanel orderDetails = new JPanel();
        orderDetails.add(new JLabel("Item ID"));
        JTextField sku = new JTextField("1", 10);
        orderDetails.add(sku);
        orderDetails.add(new JLabel("Quantity"));
        JTextField quantity = new JTextField("1", 3);
        orderDetails.add(quantity);
        JButton order = new JButton("Order");
        order.addActionListener(event -> {
            try {
                ShopFrontendClient client = new ShopFrontendClient(host.getText(), Integer.parseInt(port.getText()));
                int skuInt = Integer.parseInt(sku.getText());
                int quantityInt = Integer.parseInt(quantity.getText());
                try {
                    client.order(skuInt, quantityInt, message -> output.append(message + NEW_LINE));
                    output.append("Ordered " + quantityInt + " of " + skuInt + NEW_LINE);
                } finally {
                    try {
                        client.shutdown();
                    } catch (Exception e) {
                        e.printStackTrace();
                        output.append("ERROR! " + e.getMessage() + NEW_LINE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                output.append("ERROR! " + e.getMessage() + NEW_LINE);
            }
        });
        orderDetails.add(order);
        container.add(orderDetails, CENTER);

        frame.pack();
        frame.setVisible(true);
    }

    interface ShopApplicationLogger {
        void info(String message);
    }
}
