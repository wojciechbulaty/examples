package com.trafficparrot.examples.shop.util;

import java.util.Date;

public class Logger {
    public static void info(String msg) {
        System.out.println(new Date() + " " + msg);
    }
}

