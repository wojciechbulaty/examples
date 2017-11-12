package com.wbsoftwareconsutlancy;

public class StartupException extends RuntimeException {
    public StartupException(Exception e) {
        super(e);
    }
}
