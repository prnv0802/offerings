package com.eis.b2bmb.examples.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

//@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration(locations = {"classpath:META-INF/springContext.xml"})
public class TestAs2Component {
	@Autowired
    CamelContext camelContext;

	//@Test
	public void Test() throws Exception{
		camelContext.addRoutes(new RouteBuilder() {
            public void configure() {
                //from("file://D:/Awdh/bnk/").to("b2bmbAS2://target");
            }
        });
		camelContext.start();
		Thread.sleep(1000);
		camelContext.stop();
	}
}
