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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Properties;

import static java.lang.String.format;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static javax.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;

class WindHandler extends AbstractHandler {
    private static final Logger LOG = LoggerFactory.getLogger(WindHandler.class);

    public static final String LONDON_LATITUDE = "51.507253";
    public static final String LONDON_LONGITUDE = "-0.127755";
    private final Properties properties;

    public WindHandler(Properties properties) {
        this.properties = properties;
    }

    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response) throws IOException,
            ServletException {
        if ("/wind-speed".equals(target)) {
            try {
                String windSpeed = parseWindSpeed(forecastIoFor(LONDON_LATITUDE, LONDON_LONGITUDE)) + "mph";
                response.setStatus(SC_OK);
                response.getWriter().print(windSpeed);
            } catch (Exception e) {
                LOG.error("Unknown problem while retrieving wind speed", e);
                response.setStatus(SC_SERVICE_UNAVAILABLE);
                response.getWriter().print("ERROR");
            }
            baseRequest.setHandled(true);
        }
    }

    private String parseWindSpeed(String forecastIo) throws JSONException {
        return new JSONObject(forecastIo).getJSONObject("currently").getString("windSpeed");
    }

    private String forecastIoFor(String latitude, String longitude) throws IOException {
        try (CloseableHttpClient httpclient = HttpClients.createDefault()) {
            HttpGet httpget = new HttpGet(format(getForecastIoUrl() + "/%s,%s", latitude, longitude));
            httpget.addHeader("accept-encoding", "identity");

            ResponseHandler<String> responseHandler = response -> {
                int status = response.getStatusLine().getStatusCode();
                if (status >= 200 && status < 300) {
                    HttpEntity entity = response.getEntity();
                    return entity != null ? EntityUtils.toString(entity) : null;
                } else {
                    throw new ClientProtocolException("Unexpected response status: " + status);
                }
            };
            return httpclient.execute(httpget, responseHandler);
        }
    }

    private String getForecastIoUrl() {
        return properties.getProperty("weather-application.forecastio.url");
    }
}
