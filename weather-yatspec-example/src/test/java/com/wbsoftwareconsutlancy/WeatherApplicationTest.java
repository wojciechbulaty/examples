package com.wbsoftwareconsutlancy;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.google.common.collect.ImmutableSet;
import com.googlecode.yatspec.junit.*;
import com.googlecode.yatspec.plugin.sequencediagram.ByNamingConventionMessageProducer;
import com.googlecode.yatspec.plugin.sequencediagram.SequenceDiagramGenerator;
import com.googlecode.yatspec.plugin.sequencediagram.SvgWrapper;
import com.googlecode.yatspec.rendering.html.DontHighlightRenderer;
import com.googlecode.yatspec.rendering.html.HtmlResultRenderer;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.apache.http.util.EntityUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.Arrays;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.googlecode.yatspec.plugin.sequencediagram.SequenceDiagramGenerator.getHeaderContentForModalWindows;
import static java.lang.String.format;
import static junit.framework.TestCase.assertEquals;

@RunWith(SpecRunner.class)
public class WeatherApplicationTest extends TestState implements WithCustomResultListeners {
    private static final String WEATHER_APPLICATION = "WeatherApplication";

    private final WeatherApplication weatherApplication = new WeatherApplication();

    @Rule
    public WireMockRule darkSkyAPIStub = new WireMockRule();

    private HttpResponse httpResponse;
    private String responseBody;

    @Before
    public void setUp() {
        weatherApplication.start();
        darkSkyAPIStub.addMockServiceRequestListener(new LogWiremockInYatspecRequest(this, WEATHER_APPLICATION, "DarkSky"));
    }

    @After
    public void tearDown() {
        weatherApplication.stop();
        addSequenceDiagram();
    }

    @Test
    @Notes("The DarkSky response is quite big and complex, out weather application extracts one attribute from it")
    public void servesWindSpeedBasedOnDarkSkyResponse() throws IOException {
        givenDarkSkyForecastForLondonContainsWindSpeed("12.34");
        whenIRequestForecast();
        thenTheWindSpeedIs("12.34mph");
    }

    @Test
    @Table({@Row("500"), @Row("503")})
    public void reportsErrorWhenDarkSkyReturnsANonSuccessfulResponse(String darkSkyResponseCode) throws IOException {
        givenDarkSkyReturnsAnError(darkSkyResponseCode);
        whenIRequestForecast();
        thenTheResponseContains("Error while fetching data from DarkSky APIs");
    }

    private void addSequenceDiagram() {
        super.log("Sequence diagram", new SequenceDiagramGenerator()
                .generateSequenceDiagram(new ByNamingConventionMessageProducer().messages(capturedInputAndOutputs)));
    }

    private void thenTheResponseContains(String error) throws IOException {
        assertEquals(503, httpResponse.getStatusLine().getStatusCode());
        assertEquals(error, IOUtils.toString(httpResponse.getEntity().getContent()));
    }

    private void givenDarkSkyReturnsAnError(String status) {
        interestingGivens.add("DarkSky response status code", status);
        darkSkyAPIStub.stubFor(get(urlEqualTo("/forecast/e67b0e3784104669340c3cb089412b67/51.507253,-0.127755"))
                .willReturn(aResponse().withStatus(Integer.parseInt(status))));
    }

    private void whenIRequestForecast() throws IOException {
        Request get = Request.Get("http://localhost:" + weatherApplication.port() + "/wind-speed");
        log("Request from client to " + WEATHER_APPLICATION, get);
        Response response = get.execute();
        httpResponse = response.returnResponse();
        responseBody = EntityUtils.toString(httpResponse.getEntity());
        log("Response from " + WEATHER_APPLICATION + " to client", toString(httpResponse, responseBody));
    }

    private String toString(HttpResponse response, String responseBody) throws IOException {
        StringBuilder result = new StringBuilder();
        result.append("HTTP").append(" ").append(response.getStatusLine().getStatusCode()).append("\n");
        if (response.getAllHeaders() != null) {
            Arrays.stream(response.getAllHeaders()).forEach(h -> result.append(h.getName()).append(": ").append(h.getValue()).append("\n"));
        }
        result.append("\n").append("\n").append(responseBody);
        return result.toString();
    }

    private void thenTheWindSpeedIs(String expected) throws IOException {
        assertEquals(expected, responseBody);
    }

    private void givenDarkSkyForecastForLondonContainsWindSpeed(String windSpeed) throws IOException {
        interestingGivens.add("Wind speed", windSpeed);
        darkSkyAPIStub.stubFor(get(urlEqualTo("/forecast/e67b0e3784104669340c3cb089412b67/51.507253,-0.127755"))
                .willReturn(aResponse().withBody(darkSkyResponseBody(windSpeed))));
    }

    private String darkSkyResponseBody(String windSpeed) throws IOException {
        return format(IOUtils.toString(getClass().getClassLoader().getResourceAsStream("darksky-response-body.json")), windSpeed);
    }

    @Override
    public Iterable<SpecResultListener> getResultListeners() throws Exception {
        return ImmutableSet.of(new HtmlResultRenderer().withCustomHeaderContent(getHeaderContentForModalWindows()).withCustomRenderer(SvgWrapper.class, new DontHighlightRenderer<>()));
    }
}