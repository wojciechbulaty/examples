package com.testdrivendevelopment.example;

import com.sun.grizzly.http.SelectorThread;
import com.sun.jersey.api.container.grizzly.GrizzlyWebContainerFactory;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Application  {
    private final String baseUri = "http://localhost:9998/";

    private SelectorThread threadSelector;

    public void start() throws IOException {
        final Map<String, String> initParams = new HashMap<String, String>();
        initParams.put("com.sun.jersey.config.property.packages", "com.testdrivendevelopment.example");
        threadSelector = GrizzlyWebContainerFactory.create(baseUri, initParams);
    }

    public void stop() {
        threadSelector.stopEndpoint();
    }

    public String getBaseUri() {
        return baseUri;
    }
}
