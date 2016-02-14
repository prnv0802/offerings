package com.eis.b2bmb.camel.custom.component;

import com.eis.core.api.v1.dao.CorrelationDAO;
import com.eis.core.api.v1.dao.ScriptDAO;
import com.eis.core.api.v1.dao.UserProfileDAO;
import com.eis.core.api.v1.service.TenancyManagerService;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultPollingEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Camel endpoint for Script
 * @author aaldredge
 */
public class B2bmbScriptEndpoint extends DefaultPollingEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbScriptEndpoint.class);
    private String scriptName;
    private String domain;
    private ScriptDAO scriptDAO;
    private CorrelationDAO correlationDAO;
    private UserProfileDAO userProfileDAO;
    private TenancyManagerService tenancyManagerService;

    private Map<String, Object> parameters;

    /**
     * Contructor
     * @param endpointUri endpoint uri
     * @param component component
     */
    public B2bmbScriptEndpoint(String endpointUri, B2bmbScriptComponent component) {
        super(endpointUri, component);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        B2bmbScriptConsumer consumer = new B2bmbScriptConsumer(this, processor);

        // ScheduledPollConsumer default delay is 500 millis, override with a new default value.
        // End user can override this value by providing a consumer.delay parameter
        consumer.setDelay(consumer.DEFAULT_CONSUMER_DELAY);
        configureConsumer(consumer);
        return consumer;
    }

    @Override
    public Producer createProducer() throws Exception {
        return new B2bmbScriptProducer(this);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public boolean isLenientProperties() {
        return true;
    }

    /**
     * Get the data domain
     * @return domain data domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Set the data domain
     * @param domain data domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Get the script name
     * @return script ref name
     */
    public String getScriptName() {
        return scriptName;
    }

    /**
     * Set the script name
     * @param scriptName script ref name
     */
    public void setScriptName(String scriptName) {
        this.scriptName = scriptName;
    }

    /**
     * Get the scriptDAO
     * @return ScriptDAO
     */
    public ScriptDAO getScriptDAO() {
        return scriptDAO;
    }

    /**
     * Set the scriptDAO
     * @param scriptDAO scriptDAO to set
     */
    public void setScriptDAO(ScriptDAO scriptDAO) {
        this.scriptDAO = scriptDAO;
    }

    /**
     * get the correlationDAO
     * @return correlationDAO
     */
    public CorrelationDAO getCorrelationDAO() {
        return correlationDAO;
    }

    /**
     * Set the correlationDAO
     * @param correlationDAO correlationDAO to set
     */
    public void setCorrelationDAO(CorrelationDAO correlationDAO) {
        this.correlationDAO = correlationDAO;
    }


    /**
     * gets the user profile dao
     * @return the user profile dao
     */
    public UserProfileDAO getUserProfileDAO() {

        return userProfileDAO;
    }

    /**
     * sets the user profile dao
     * @param userProfileDAO the dao implementation to use
     */
    public void setUserProfileDAO(UserProfileDAO userProfileDAO) {

        this.userProfileDAO = userProfileDAO;
    }

    /**
     * Get the parameters from the endpoint.  These are used to populate matching script input parameters
     * @return parameters
     */
    public Map<String, Object> getParameters(){
        return parameters;
    }

    /**
     * Set the parameters from the endpoint.  These are used to populate matching script input parameters
     * @param parameters parameters from endpoint
     */
    public void setParameters(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    /**
     * gets the tanancy manager service impl
     * @return the service
     */
    public TenancyManagerService getTenancyManagerService() {

        return tenancyManagerService;
    }

    /**
     * sets teh service implementation
     * @param tenancyManagerService the implementation
     */
    public void setTenancyManagerService(TenancyManagerService tenancyManagerService) {

        this.tenancyManagerService = tenancyManagerService;
    }
}
