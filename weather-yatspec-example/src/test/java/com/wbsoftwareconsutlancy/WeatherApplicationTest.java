package com.wbsoftwareconsutlancy;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.googlecode.yatspec.junit.SpecRunner;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.fluent.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static junit.framework.TestCase.assertEquals;

@RunWith(SpecRunner.class)
public class WeatherApplicationTest {
    @Rule
    public WireMockRule darkSkyAPIStub = new WireMockRule();

    private WeatherApplication weatherApplication = new WeatherApplication();
    private Response response;

    @Before
    public void setUp() {
        weatherApplication.start();
    }

    @After
    public void tearDown() {
        weatherApplication.stop();
    }

    @Test
    public void servesWindSpeedBasedOnDarkSkyResponse() throws IOException {
        givenDarkSkyForecastForLondonContainsWindSpeed("12.34");
        whenIRequestForecast();
        thenTheWindSpeedIs("12.34mph");
    }

    @Test
    public void reportsErrorWhenDarkSkyReturnsANonSuccessfulResponse() throws IOException {
        givenDarkSkyReturnsAnError(SC_INTERNAL_SERVER_ERROR);
        whenIRequestForecast();
        thenTheResponseContains("Error while fetching data from DarkSky APIs");
    }

    private void thenTheResponseContains(String error) throws IOException {
        HttpResponse httpResponse = response.returnResponse();
        assertEquals(503, httpResponse.getStatusLine().getStatusCode());
        assertEquals(error, IOUtils.toString(httpResponse.getEntity().getContent()));
    }

    private void givenDarkSkyReturnsAnError(int status) {
        darkSkyAPIStub.stubFor(get(urlEqualTo("/forecast/e67b0e3784104669340c3cb089412b67/51.507253,-0.127755"))
                .willReturn(aResponse().withStatus(status)));
    }

    private void whenIRequestForecast() throws IOException {
        response = Request.Get("http://localhost:" + weatherApplication.port() + "/wind-speed")
                .execute();
    }

    private void thenTheWindSpeedIs(String expected) throws IOException {
        assertEquals(expected, response.returnContent().toString());
    }

    private void givenDarkSkyForecastForLondonContainsWindSpeed(String windSpeed) throws IOException {
        darkSkyAPIStub.stubFor(get(urlEqualTo("/forecast/e67b0e3784104669340c3cb089412b67/51.507253,-0.127755"))
                .willReturn(aResponse().withBody(darkSkyResponseBody(windSpeed))));
    }

    private String darkSkyResponseBody(String windSpeed) throws IOException {
        return String.format(IOUtils.toString(getClass().getClassLoader().getResourceAsStream("darksky-response-body.json")), windSpeed);
    }
}