package com.testdrivendevelopment.example.acceptance.stockmarket;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Message {
    private RequestType requestType;
    private String stockName;
    private int units;

    public Message() {
    }

    public Message(String stockName, int units, RequestType requestType) {
        this.stockName = stockName;
        this.units = units;
        this.requestType = requestType;
    }

    public RequestType getRequestType() {
        return requestType;
    }

    public String getStockName() {
        return stockName;
    }

    public int getUnits() {
        return units;
    }

    public enum RequestType {
        BID
    }

    public String toString() {
        return "Message{" +
                "requestType=" + requestType +
                ", stockName='" + stockName + '\'' +
                ", units=" + units +
                '}';
    }
}
