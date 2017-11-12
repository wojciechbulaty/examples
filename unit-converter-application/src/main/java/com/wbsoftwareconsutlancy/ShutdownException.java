package com.wbsoftwareconsutlancy;

public class ShutdownException extends RuntimeException {
    public ShutdownException(Exception e) {
        super(e);
    }
}
