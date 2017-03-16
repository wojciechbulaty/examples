package com.wbsoftwareconsutlancy;

import org.apache.http.HttpEntity;
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

class GbpToEurHandler extends AbstractHandler {
    private final Properties properties;

    public GbpToEurHandler(Properties properties) {
        this.properties = properties;
    }

    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException,
            ServletException {
        if ("/gbp-to-eur".equals(target)) {
            try {
                String gbpToEur = parse(convertGbpToEur());

                response.setContentType("text/html; charset=utf-8");
                response.setStatus(SC_OK);
                response.getWriter().print(format("{\"gbpToEur\": %s, \"buy\": %s}", gbpToEur, Double.parseDouble(gbpToEur) > 1.5));

                baseRequest.setHandled(true);
            } catch (JSONException e) {
                throw new ServletException(e);
            }
        }
    }

    private String parse(String forecastIo) throws JSONException {
        return new JSONObject(forecastIo).getString("Result");
    }

    private String convertGbpToEur() throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(getXigniteUrl() + "/xGlobalCurrencies.json/ConvertRealTimeValue?_Token=" + getXigniteToken() + "&From=GBP&To=EUR&Amount=1");
            httpget.addHeader("accept-encoding", "identity");
            System.out.println("Executing request " + httpget.getRequestLine());

            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            String responseBody = httpclient.execute(httpget, responseHandler);
            System.out.println("----------------------------------------");
            System.out.println(responseBody);
            return responseBody;
        }
    }

    private String getXigniteUrl() {
        return properties.getProperty("xignite.url");
    }

    private String getXigniteToken() {
        return properties.getProperty("xignite.token");
    }
}
