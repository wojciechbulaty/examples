package com.wbsoftwareconsutlancy;

import com.github.tomakehurst.wiremock.http.RequestListener;
import com.googlecode.yatspec.state.givenwhenthen.TestState;

import static java.lang.String.join;
import static java.nio.charset.StandardCharsets.UTF_8;

public class LogWiremockInYatspecRequest implements RequestListener {
    private final TestState testState;
    private final String sourceSystem;
    private final String targetSystem;

    public LogWiremockInYatspecRequest(TestState testState, String sourceSystem, String targetSystem) {
        this.testState = testState;
        this.sourceSystem = sourceSystem;
        this.targetSystem = targetSystem;
    }

    @Override
    public void requestReceived(com.github.tomakehurst.wiremock.http.Request request, com.github.tomakehurst.wiremock.http.Response response) {
        testState.log("Request from " + sourceSystem + " to " + targetSystem, toString(request));
        testState.log("Response from " + targetSystem + " to " + sourceSystem, toString(response));
    }

    private String toString(com.github.tomakehurst.wiremock.http.Response response) {
        StringBuilder result = new StringBuilder();
        result.append("HTTP").append(" ").append(response.getStatus()).append("\n");
        if (response.getHeaders() != null) {
            response.getHeaders().all().forEach(h -> result.append(h.key()).append(": ").append(join(",", h.values())).append("\n"));
        }
        String body = response.getBody() == null ? "" : new String(response.getBody(), UTF_8);
        result.append("\n").append("\n").append(body);
        return result.toString();
    }

    private String toString(com.github.tomakehurst.wiremock.http.Request request) {
        StringBuilder result = new StringBuilder();
        result.append(request.getMethod()).append(" ").append(request.getUrl()).append("\n");
        request.getHeaders().all().forEach(h -> result.append(h.key()).append(": ").append(join(",", h.values())).append("\n"));
        String body = request.getBody() == null ? "" : new String(request.getBody(), UTF_8);
        result.append("\n").append("\n").append(body);
        return result.toString();
    }
}
