package com.eis.b2bmb.component.custom.POC;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

public class HelloWorldComponentTest {
	String toAddress = null;
    /*@Test
    public void testHelloWorld() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMinimumMessageCount(1);       
        
        assertMockEndpointsSatisfied();
    }*/
	
	public static void main(String[] args) throws Exception {
		HelloWorldComponentTest test = new HelloWorldComponentTest();
		CamelContext camelContext = new DefaultCamelContext();
		camelContext.addRoutes(test.createRouteBuilder());
		camelContext.start();
		Thread.sleep(2000);
		camelContext.stop();
	}
	
    protected RouteBuilder createRouteBuilder() throws Exception {
    	return new RouteBuilder() {
            public void configure() {
                from("helloworld://foo").dynamicRouter(bean(MyRoutingAlg.class, "getRoute"));
            }
        };
    }
}
