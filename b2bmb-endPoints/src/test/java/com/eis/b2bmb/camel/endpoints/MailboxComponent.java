package com.eis.b2bmb.camel.endpoints;

import java.util.Map;

import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;


public class MailboxComponent extends DefaultComponent{
	
	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        Endpoint endpoint = new MailboxEndpoint(uri, this, parameters);
        setProperties(endpoint, parameters);
        return endpoint;
    }

}
