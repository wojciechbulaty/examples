package com.testdrivendevelopment.example;

import com.googlecode.totallylazy.Pair;

import javax.annotation.Resource;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;

@Path("/notification")
public class Notification {
    @Resource
    private StockMarket stockMarket;

    public Notification() {
    }

    public Notification(StockMarket stockMarket) {
        this.stockMarket = stockMarket;
    }

    @POST
    public void create(@QueryParam("stockName") String stockName,
                       @QueryParam("units") int units) {
        if (units <= -10) {
            stockMarket.buy(stockName, 1000);
        }
    }
}
