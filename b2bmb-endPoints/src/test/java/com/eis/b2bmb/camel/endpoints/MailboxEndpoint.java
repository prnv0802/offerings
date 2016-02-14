package com.eis.b2bmb.camel.endpoints;

import java.util.Map;

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;


public class MailboxEndpoint extends DefaultEndpoint{
	
	private String domainName;
	private String contentType;
	private Map<String, Object> parameters; 
	

	public String getDomainName() {
		return domainName;
	}

	public void setDomainName(String domainName) {
		this.domainName = domainName;
		/*parameters.put("domainName", domainName);*/
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
		/*parameters.put("contentType", contentType);*/
	}

	public MailboxEndpoint(String uri, MailboxComponent component, Map<String, Object> parameters) {
        super(uri, component);
        this.parameters = parameters;
    }

    /*public MailboxEndpoint(String endpointUri, MailboxComponent mailboxComponent, Map<String, Object> parameters) {
        super(endpointUri);
        this.parameters = parameters;
    }*/

    public synchronized Producer createProducer() throws Exception {
    	parameters.put("domainName", domainName);
    	parameters.put("contentType", contentType);
        return new MailboxProducer(this, parameters);
    }

    public synchronized Consumer createConsumer(Processor processor) throws Exception {
        return new MailboxConsumer(this, processor, parameters);
    }

    public boolean isSingleton() {
    	return true;
    }

}
