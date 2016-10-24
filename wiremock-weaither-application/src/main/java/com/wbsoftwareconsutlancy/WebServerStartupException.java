package com.wbsoftwareconsutlancy;

public class WebServerStartupException extends RuntimeException {
    public WebServerStartupException(Exception e) {
        super(e);
    }
}
