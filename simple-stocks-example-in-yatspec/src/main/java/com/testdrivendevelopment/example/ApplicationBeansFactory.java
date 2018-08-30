package com.testdrivendevelopment.example;

import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;

import javax.annotation.Resource;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.Type;

@Provider
public class ApplicationBeansFactory implements InjectableProvider<Resource, Type> {
    public Injectable getInjectable(ComponentContext ic, Resource resource, final Type type) {
        return new Injectable() {
            public Object getValue() {
                if (type.equals(StockMarket.class)) {
                    return new StockMarket();
                } else {
                    throw new RuntimeException(type.toString());
                }
            }
        };
    }

    public ComponentScope getScope() {
        return ComponentScope.Singleton;
    }
}
