package com.testdrivendevelopment.example.acceptance.stockmarket;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StockMarketStubServer {
    private SelectorThread threadSelector;
    private final String baseUri = "http://localhost:9999/";
    private final DefaultClientConfig clientConfig = new DefaultClientConfig();
    private final Client client = Client.create(clientConfig);

    public void start() throws IOException {
        final Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.packages", "com.testdrivendevelopment.example.acceptance.stockmarket");
        threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);
    }

    public void stop() {
        threadSelector.stopEndpoint();
    }

    public Message popOnlyMessage() {
        WebResource resource = client.resource(baseUri);
        return resource.path("/popOnlyMessage").get(Message.class);
    }

    public String popOnlyRequest() {
        WebResource resource = client.resource(baseUri);
        return resource.path("/popOnlyRequest").get(String.class);
    }
}
