package com.eis.b2bmb.camel.endpoints;

import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MailboxProducer extends DefaultProducer {

	private static final transient Logger LOG = LoggerFactory.getLogger(MailboxEndpoint.class);
    private MailboxEndpoint endpoint;
    private Map<String, Object> parameters;

    public MailboxProducer(MailboxEndpoint endpoint, Map<String, Object> parameters) {
        super(endpoint);
        this.endpoint = endpoint;
        this.parameters = parameters;
    }

    public void process(Exchange exchange) throws Exception {
        System.out.println(exchange.getIn().getBody());    
        System.out.println("domainName =  "+(String)parameters.get("domainName"));
        System.out.println("contentType =  "+(String)parameters.get("contentType"));
    }

}
