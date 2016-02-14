package com.eis.b2bmb.component.custom.POC;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;

/**
 * Represents a HelloWorld endpoint.
 */
public class HelloWorldEndpoint extends DefaultEndpoint {
	
	/*int concurrentConsumers;
	
	public HelloWorldEndpoint() {
    
	}

	public int getConcurrentConsumers() {
		return concurrentConsumers;
	}


	public void setConcurrentConsumers(int concurrentConsumers) {
		this.concurrentConsumers = concurrentConsumers;
	}*/


	public HelloWorldEndpoint(String uri, HelloWorldComponent component) {
        super(uri, component);
    }

    public HelloWorldEndpoint(String endpointUri) {
        super(endpointUri);
    }

    public synchronized Producer createProducer() throws Exception {
        return new HelloWorldProducer(this);
    }

    public synchronized Consumer createConsumer(Processor processor) throws Exception {
        return new HelloWorldConsumer(this, processor);
    }

    public boolean isSingleton() {
    	return true;
    }
    
}
