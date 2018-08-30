package com.testdrivendevelopment.example.acceptance;

import com.googlecode.yatspec.junit.SpecRunner;
import com.googlecode.yatspec.state.givenwhenthen.TestState;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.testdrivendevelopment.example.Application;
import com.testdrivendevelopment.example.acceptance.stockmarket.Message;
import com.testdrivendevelopment.example.acceptance.stockmarket.StockMarketStubServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.testdrivendevelopment.example.acceptance.stockmarket.Message.RequestType.BID;
import static org.fest.assertions.Assertions.assertThat;

@RunWith(SpecRunner.class)
public class BuyStocksAcceptanceTest extends TestState {
    private final Application application = new Application();
    private final StockMarketStubServer stockMarketStubServer = new StockMarketStubServer();
    private final String googleStockName = "Google";

    @Before
    public void setUp() throws Exception {
        application.start();
        stockMarketStubServer.start();
    }

    @After
    public void tearDown() throws Exception {
        application.stop();
        stockMarketStubServer.stop();
    }

    @Test
    public void buyStocksWhenPriceDrops() throws Exception {
        whenANotificationThatGoogleStockPriceDroppedBy10UnitsWasReceived();
        thenABiddingRequestFor1000googleStocksWasSentToStockMarket();
    }

    private void thenABiddingRequestFor1000googleStocksWasSentToStockMarket() {
        String requestSentToStockMarket = stockMarketStubServer.popOnlyRequest();
        log("Request received by stock market system", requestSentToStockMarket);

        int expectedNumUnits = 1000;
        interestingGivens.add("Expected stocks to buy", expectedNumUnits);
        Message message = stockMarketStubServer.popOnlyMessage();
        assertThat(message.getRequestType()).isEqualTo(BID);
        assertThat(message.getStockName()).isEqualTo(googleStockName);
        assertThat(message.getUnits()).isEqualTo(expectedNumUnits);
    }

    private void whenANotificationThatGoogleStockPriceDroppedBy10UnitsWasReceived() {
        String priceDrop = "-10";

        WebResource webResource = postNotificationToApplication(priceDrop);

        interestingGivens.add("Price drop", priceDrop);
        interestingGivens.add("Stock name", googleStockName);
        log("Received notification", webResource);
    }

    private WebResource postNotificationToApplication(String priceDrop) {
        DefaultClientConfig clientConfig = new DefaultClientConfig();
        Client client = Client.create(clientConfig);
        WebResource resource = client.resource(application.getBaseUri());
        WebResource webResource = resource.path("/notification")
                .queryParam("stockName", googleStockName)
                .queryParam("units", priceDrop);
        webResource.post();
        return webResource;
    }
}
