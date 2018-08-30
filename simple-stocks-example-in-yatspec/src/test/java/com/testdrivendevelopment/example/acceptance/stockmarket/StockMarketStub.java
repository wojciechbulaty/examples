package com.testdrivendevelopment.example.acceptance.stockmarket;

import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.resource.Singleton;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;

import java.util.LinkedList;
import java.util.Queue;

import static com.testdrivendevelopment.example.acceptance.stockmarket.Message.RequestType.BID;

@Path("/")
@Singleton
public class StockMarketStub {
    private final Queue<Message> messages = new LinkedList<Message>();
    private final Queue<String> requests = new LinkedList<String>();

    @POST
    @Path("/buy")
    public void buy(@QueryParam("stockName") String stockName,
                    @QueryParam("units") int units,
                    @Context Request request) {
        requests.add(((ContainerRequest)request).getRequestUri().toASCIIString());
        messages.offer(new Message(stockName, units, BID));
    }

    @GET
    @Path("/popOnlyRequest")
    public String popOnlyRequest() {
        if (messages.size() != 1) {
            throw new RuntimeException("Expected 1 message but found " + messages.size());
        } else {
            return requests.poll();
        }
    }

    @GET
    @Path("/popOnlyMessage")
    public Message popOnlyMessage() {
        if (messages.size() != 1) {
            throw new RuntimeException("Expected 1 message but found " + messages.size());
        } else {
            return messages.poll();
        }
    }
}
