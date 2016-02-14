package com.eis.b2bmb.component.custom.POC;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledPollConsumer;

/**
 * The HelloWorld consumer.
 */
public class HelloWorldConsumer extends ScheduledPollConsumer {
    private final HelloWorldEndpoint endpoint;

    public HelloWorldConsumer(HelloWorldEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
        this.endpoint = endpoint;
    }

    @Override
    protected int poll() throws Exception {
        Exchange exchange = endpoint.createExchange();
        // create a message body
        Date now = new Date();
        exchange.getIn().setBody("Hello World! The time is " + now);
        Map<String, Object> headers = new HashMap<String, Object>();
        headers.put("toAddress", "file://testfile");
        exchange.getIn().setHeaders(headers);
        try {
            // send message to next processor in the route
            getProcessor().process(exchange);
            return 1; // number of messages polled
        } finally {
            // log exception if an exception occurred and was not handled
            if (exchange.getException() != null) {
                getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
            }
        }
    }
}
