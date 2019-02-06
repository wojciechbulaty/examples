package com.trafficparrot.examples.shop;

import com.trafficparrot.examples.shop.proto.Item;
import com.trafficparrot.examples.shop.proto.OrderGrpc;
import com.trafficparrot.examples.shop.proto.OrderStatus;
import com.trafficparrot.examples.shop.proto.Status;
import io.grpc.Metadata;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.StatusRuntimeException;
import io.grpc.internal.GrpcUtil;
import io.grpc.stub.StreamObserver;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicReference;

import static com.trafficparrot.examples.shop.util.Logger.info;
import static java.awt.BorderLayout.*;
import static javax.swing.WindowConstants.EXIT_ON_CLOSE;

public class ShopBackendServer {
    private static final String NEW_LINE = System.getProperty("line.separator");

    private Server server;

    private void start(int port, OrderService.OrderLogger logger) throws IOException, InterruptedException {
        server = ServerBuilder.forPort(port)
                .addService(new OrderService(logger))
                .build()
                .start();
        info("Server started on localhost:" + port);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Shutting down gRPC server");
            server.shutdown();
            System.out.println("Server shut down!");
        }));
    }

    private void stop() throws InterruptedException {
        server.shutdown();
        server.awaitTermination();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        runUi();
    }

    private static void runUi() {
        JFrame frame = new JFrame("Shop Server");
        frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
        Container container = frame.getContentPane();

        JPanel serverDetails = new JPanel();
        serverDetails.add(new JLabel("Server port"));
        JTextField port = new JTextField("12345", 6);
        serverDetails.add(port);
        container.add(serverDetails, PAGE_START);

        JTextArea output = new JTextArea("", 20, 40);
        JScrollPane outputPanel = new JScrollPane(output, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        container.add(outputPanel, PAGE_END);

        JPanel startStop = new JPanel();
        JButton stop = new JButton("Stop");
        stop.setEnabled(false);
        JButton start = new JButton("Start");
        AtomicReference<ShopBackendServer> server = new AtomicReference<>();
        start.addActionListener(event -> {
            try {
                server.set(new ShopBackendServer());
                int portInt = Integer.parseInt(port.getText());
                server.get().start(portInt, message -> output.append(message + NEW_LINE));
                start.setEnabled(false);
                stop.setEnabled(true);
                output.append("Started server on host localhost and port " + portInt + NEW_LINE);
            } catch (Exception e) {
                e.printStackTrace();
                output.append("ERROR! " + e.getMessage() + NEW_LINE);
            }
        });
        startStop.add(start);
        container.add(startStop, CENTER);
        stop.addActionListener(event -> {
            try {
                server.get().stop();
                start.setEnabled(true);
                stop.setEnabled(false);
                output.append("Stopped." + NEW_LINE);
            } catch (Exception e) {
                e.printStackTrace();
                output.append("ERROR! " + e.getMessage() + NEW_LINE);
            }
        });
        startStop.add(stop);
        container.add(startStop, CENTER);

        frame.pack();
        frame.setVisible(true);
    }

    private static class OrderService extends OrderGrpc.OrderImplBase {
        private final OrderLogger logger;

        public OrderService(OrderLogger logger) {
            this.logger = logger;
        }

        @Override
        public void purchase(Item request, StreamObserver<OrderStatus> responseObserver) {
            logger.info("Request to purchase received for: \n" + request);
            if (request.getQuantity() == 999) {
                Metadata metadata = new Metadata();
                metadata.put(Metadata.Key.of("reason-why", Metadata.ASCII_STRING_MARSHALLER), "If you send quantity 999 then this service will always blow up");
                metadata.put(Metadata.Key.of("so-what", Metadata.ASCII_STRING_MARSHALLER), "This is to allow for testing of recording exceptions");
                StatusRuntimeException statusRuntimeException = new StatusRuntimeException(io.grpc.Status.INVALID_ARGUMENT, metadata);
                responseObserver.onError(statusRuntimeException);
            } else {
                responseObserver.onNext(OrderStatus.newBuilder().setStatus(Status.SUCCESS).setMessage("Order processed: " + request).build());
                responseObserver.onCompleted();
            }
        }

        interface OrderLogger {
            void info(String message);
        }
    }
}
