package com.eis.b2bmb.examples.camel;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.jms.JmsComponent;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RoutesDefinition;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.jms.ConnectionFactory;
import java.io.InputStream;

/**
 * User: mingardia
 * Date: 9/30/13
 * Time: 10:21 AM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:META-INF/springContext.xml"})
public class TestCamel {

    @Autowired
    CamelContext camelContext;

    //@Test
    public void testBasicsOfCamel() throws Exception {
        // Auto inject
        //CamelContext context = new DefaultCamelContext();

        Assert.assertNotNull(camelContext);

        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");

        // Note we can explicit refName the component
        camelContext.addComponent("test-jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));
        //context.addComponent("mock-foo", new MockEndPoint())

        camelContext.addRoutes(new RouteBuilder() {
            public void configure() {
                from("test-jms:queue:test.queue").to("file://target/CamelTest.out");
            }
        });

        ProducerTemplate template = camelContext.createProducerTemplate();

        camelContext.start();

        for (int i = 0; i < 10; i++) {
            System.out.println("Sending ..." + i);
            template.sendBody("test-jms:queue:test.queue", "Test Message: " + i);
        }

        camelContext.stop();


    }



    @Test
    public void testRouteFromXMLFile() throws Exception
    {

        Assert.assertNotNull(camelContext);
        // this would be done via a "create end point process"
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("vm://localhost?broker.persistent=false");

        // Note we can explicit refName the component
        camelContext.addComponent("test-jms", JmsComponent.jmsComponentAutoAcknowledge(connectionFactory));


        InputStream xmlIn = getClass().getClassLoader().getResourceAsStream("sampleRoute.xml");

        if (xmlIn == null)
        {

            throw new IllegalStateException("Could not find sampleRoute.xml in classpath");
        }

        RoutesDefinition routes = camelContext.loadRoutesDefinition(xmlIn);
        camelContext.addRouteDefinitions(routes.getRoutes());

        ProducerTemplate template = camelContext.createProducerTemplate();

        camelContext.start();

        for (int i = 0; i < 10; i++) {
            System.out.println("Sending ..." + i);
            template.sendBody("test-jms:queue:test.queue", "Test Message: " + i);
        }



        MockEndpoint endpoint = camelContext.getEndpoint("mock:foo", MockEndpoint.class);

        endpoint.expectedMessageCount(10);

        camelContext.stop();


    }

}
