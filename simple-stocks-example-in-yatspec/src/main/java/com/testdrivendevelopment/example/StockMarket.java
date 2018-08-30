package com.testdrivendevelopment.example;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;

public class StockMarket {
    public void buy(String stockName, int numUnits) {
        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        WebResource resource = client.resource("http://localhost:9999");
        resource.path("/buy")
                .queryParam("stockName", stockName)
                .queryParam("units", String.valueOf(numUnits))
                .post();
    }
}
