package com.testdrivendevelopment.example;

import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class NotificationTest {
    private final StockMarket stockMarket = mock(StockMarket.class);

    private final Notification notificationResource = new Notification(stockMarket);

    @Test
    public void buysStocksWhenPriceDropMoreThen10() throws Exception {
        notificationResource.create("Google", -12);

        verify(stockMarket).buy("Google", 1000);
    }

    @Test
    public void doesNotBuyWhenPriceDroppedLessThen10() throws Exception {
        notificationResource.create("Google", -9);

        verifyNoMoreInteractions(stockMarket);
    }
}
