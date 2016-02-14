package com.eis.b2bmb.camel.endpoints;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.junit.Test;

public class MailboxComponentTest {
	
	/*@Test*/
	public static void testMailboxEndpoint() throws Exception{
		MailboxComponentTest test = new MailboxComponentTest();
		CamelContext camelContext = new DefaultCamelContext();
		camelContext.addRoutes(test.createRouteBuilder());
		camelContext.start();
		Thread.sleep(2000);
		camelContext.stop();
	}
	
    protected RouteBuilder createRouteBuilder() throws Exception {
    	return new RouteBuilder() {
            public void configure() {
                from("b2bmb://mycompanyxyz/inbox").to("b2bmb://fileEntry?contentType=application/octet-stream&domainName=com.mycompanyxyz");
            }
        };
    }
    public static void main(String[] args) throws Exception {
    	testMailboxEndpoint();
	}

}
