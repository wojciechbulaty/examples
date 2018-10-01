package com.wbsoftwareconsutlancy;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.json.JSONException;
import org.json.JSONObject;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_OK;

class StockQuoteLastPriceHandler extends AbstractHandler {
    private static final String APPLE_SYMBOL = "AAPL";
    private final Properties properties;

    public StockQuoteLastPriceHandler(Properties properties) {
        this.properties = properties;
    }

    @Override
    public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if ("/stock-quote-last-price".equals(target)) {
            try {
                double lastPrice = parseStockQuoteLastPrice(markitStockQuoteFor(APPLE_SYMBOL));

                response.setContentType("text/html; charset=utf-8");
                response.setStatus(SC_OK);
                response.getWriter().print(lastPrice);

                baseRequest.setHandled(true);
            } catch (JSONException e) {
                throw new ServletException(e);
            }
        }
    }

    private double parseStockQuoteLastPrice(String markitStockQuoteJson) throws JSONException {
        return new JSONObject(markitStockQuoteJson).getDouble("LastPrice");
    }

    private String markitStockQuoteFor(String symbol) throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(format(getMarkitUrl(), symbol));
            httpget.addHeader("accept-encoding", "identity");
            System.out.println("Executing request " + httpget.getRequestLine());

            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    return responseString(response);
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status + " with response body: " + responseString(response));
                }
            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            return responseBody;
        }
    }

    private String responseString(HttpResponse response) throws IOException {
        HttpEntity entity = response.getEntity();
        if (entity == null) {
            return null;
        }
        return EntityUtils.toString(entity);
    }

    private String getMarkitUrl() {
        return properties.getProperty("finance-application.markit.url");
    }
}
