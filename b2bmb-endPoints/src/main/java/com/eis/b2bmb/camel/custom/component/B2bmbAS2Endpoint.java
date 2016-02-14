package com.eis.b2bmb.camel.custom.component;


import com.eis.b2bmb.api.v1.dao.AS2ClientConnectionConfigDAO;
import com.eis.b2bmb.endpts.as2.AS2Client;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultPollingEndpoint;
import org.apache.camel.processor.idempotent.MemoryIdempotentRepository;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Endpoint for AS2
 * User: harjeets
 */
public class B2bmbAS2Endpoint extends DefaultPollingEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(B2bmbAS2Endpoint.class);
    private String toAS2Id;
    private String fromAS2Id;
    private String dataDomain;
    private String errorMailboxName = "Errors";
    private String processedMailboxName = "Processed";
    private AS2Client as2Client;


    private AS2ClientConnectionConfigDAO clientConnectionConfigDAO;


    private IdempotentRepository<String> inProgressRepository = new MemoryIdempotentRepository();

    /**
     * @param endpointUri endpoint Uri
     * @param component   component instance
     */
    public B2bmbAS2Endpoint(String endpointUri, B2bmbAS2Component component) {
        super(endpointUri, component);
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating B2bmbMailboxEndpoint  ");
        }
    }

    /**
     * creates the producer object
     *
     * @return {@link Producer}
     * @throws Exception thrown by camel runtime system
     */
    public Producer createProducer() throws Exception {
        return new B2bmbAS2Producer(this);

    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("=======Processor=======  " + processor.toString());
        }
        return null;
    }


    /**
     * to create singleton  object of endpoint
     *
     * @return boolean
     */

    public boolean isSingleton() {
        return true;
    }


    @Override
    protected void doStart() throws Exception {
        ServiceHelper.startServices(inProgressRepository);
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        ServiceHelper.stopServices(inProgressRepository);
    }

    /**
     * @return String toASId
     */
    public String getToAS2Id() {
        return toAS2Id;
    }

    /**
     * Set the to ASId
     *
     * @param toAS2Id reference
     */
    public void setToAS2Id(String toAS2Id) {
        this.toAS2Id = toAS2Id;
    }

    /**
     * gets the as2 id
     * @return String fromASId
     */
    public String getFromAS2Id() {
        return fromAS2Id;
    }

    /**
     * Set the from ASId
     *
     * @param fromAS2Id reference
     */
    public void setFromAS2Id(String fromAS2Id) {
        this.fromAS2Id = fromAS2Id;
    }

    /**
     * @return String domain
     */
    public String getDataDomain() {
        return dataDomain;
    }

    /**
     * Set the domain
     *
     * @param dataDomainx name
     */
    public void setDataDomain(String dataDomainx) {
        this.dataDomain = dataDomainx;
    }

    /**
     * @return AS2Client
     */
    public AS2Client getAs2Client() {
        return as2Client;
    }

    /**
     * Set the as2 Client
     *
     * @param as2Client reference
     */
    public void setAs2Client(AS2Client as2Client) {
        this.as2Client = as2Client;
    }

    /**
     * Get the Client Connection DAO
     * @return the DAO
     */
    public AS2ClientConnectionConfigDAO getClientConnectionConfigDAO() {
        return clientConnectionConfigDAO;
    }

    /**
     * Set the DAO implementation to use
     * @param clientConnectionConfigDAO the dao
     */
    public void setClientConnectionConfigDAO(AS2ClientConnectionConfigDAO clientConnectionConfigDAO) {
        this.clientConnectionConfigDAO = clientConnectionConfigDAO;
    }
}
