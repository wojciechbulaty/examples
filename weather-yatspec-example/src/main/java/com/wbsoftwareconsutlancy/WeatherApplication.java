package com.wbsoftwareconsutlancy;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.util.resource.Resource;

import java.io.*;
import java.util.Date;
import java.util.Properties;

public class WeatherApplication {
    private Server server;
    private int port = 8282;

    public static void main(String[] args) throws Exception {
        WeatherApplication weatherApplication = new WeatherApplication();
        weatherApplication.start();
        weatherApplication.join();
    }

    public void start()  {
        info("Starting weather application...");
        server = new Server(port);
        HandlerList handlers = new HandlerList();
        handlers.setHandlers(new Handler[]{
                new WindHandler(loadProperties()),
                new DefaultHandler()});
        server.setHandler(handlers);
        try {
            server.start();
        } catch (Exception e) {
            throw new WebServerStartupException(e);
        }
        info("Started!");
    }

    private static void info(String msg) {
        System.out.println(new Date() + ": " + msg);
    }

    public void join() throws InterruptedException {
        server.join();
    }

    private static Properties loadProperties() {
        try {
            InputStream propertiesInputStream = getPropertiesInputStream();
            Properties properties = new Properties();
            properties.load(propertiesInputStream);
            return properties;
        } catch (IOException e) {
            throw new PropertiesLoadingException(e);
        }
    }

    private static InputStream getPropertiesInputStream() throws FileNotFoundException {
        String fileLocation = System.getProperty("weather.application.properties");
        if (fileLocation != null) {
            return new FileInputStream(new File(fileLocation));
        } else {
            String classpathFileName = "weather-application.properties";
            InputStream resourceAsStream = WeatherApplication.class.getClassLoader().getResourceAsStream(classpathFileName);
            if (resourceAsStream != null) {
                return resourceAsStream;
            } else {
                throw new FileNotFoundException("property file '" + classpathFileName + "' not found in the classpath");
            }
        }
    }

    public void stop() {
        try {
            server.stop();
            server.join();
        } catch (Exception e) {
            throw new WebServerShutdownException(e);
        }
    }

    public int port() {
        return port;
    }
}
