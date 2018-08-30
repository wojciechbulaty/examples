package com.wbsoftwareconsutlancy;

import java.io.IOException;

public class PropertiesLoadingException extends RuntimeException {
    public PropertiesLoadingException(IOException e) {
        super(e);
    }
}
