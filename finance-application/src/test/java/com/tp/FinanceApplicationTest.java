package com.tp;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.wbsoftwareconsutlancy.FinanceApplication;
import org.apache.http.client.fluent.Content;
import org.apache.http.client.fluent.Request;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static junit.framework.TestCase.assertEquals;

public class FinanceApplicationTest {
    @Rule
    public WireMockRule forecastIoService = new WireMockRule(8023);

    private FinanceApplication financeApplication = new FinanceApplication();

    @Before
    public void setUp() throws Exception {
        financeApplication.start();
    }

    @After
    public void tearDown() throws Exception {
        financeApplication.stop();
    }

    @Test
    public void parsesLastPriceFromStockQuote() throws Exception {
        forecastIoService.stubFor(get(urlEqualTo("/MODApis/Api/v2/Quote/json?symbol=AAPL"))
                .willReturn(aResponse().withBody("{\"Status\":\"SUCCESS\",\"Name\":\"Apple Inc\",\"Symbol\":\"AAPL\",\"LastPrice\":103.17}")));

        Content content = Request.Get("http://localhost:" + financeApplication.port + "/stock-quote-last-price")
                .execute()
                .returnContent();

        assertEquals("103.17", content.toString());
    }
}