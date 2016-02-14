package com.eis.b2bmb.component.custom.POC;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

/**
 * Represents the component that manages {@link HelloWorldEndpoint}.
 */
public class HelloWorldComponent extends DefaultComponent {

    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new HelloWorldEndpoint(uri, this);
        setProperties(endpoint, parameters);
        return endpoint;
    }
}
