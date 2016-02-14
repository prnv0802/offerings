package com.eis.b2bmb.component.custom.POC;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The HelloWorld producer.
 */
public class HelloWorldProducer extends DefaultProducer {
    private static final transient Logger LOG = LoggerFactory.getLogger(HelloWorldProducer.class);
    private HelloWorldEndpoint endpoint;

    public HelloWorldProducer(HelloWorldEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    public void process(Exchange exchange) throws Exception {
        System.out.println(exchange.getIn().getBody());    
    }

}
