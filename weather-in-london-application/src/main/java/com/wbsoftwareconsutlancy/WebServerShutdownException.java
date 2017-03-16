package com.wbsoftwareconsutlancy;

public class WebServerShutdownException extends RuntimeException {
    public WebServerShutdownException(Exception e) {
        super(e);
    }
}
