package com.eis.b2bmb.camel.custom.component;

import com.eis.core.api.v1.dao.CorrelationDAO;
import com.eis.core.api.v1.dao.ScriptDAO;
import com.eis.core.api.v1.dao.UserProfileDAO;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.model.Script;
import com.eis.core.api.v1.service.TenancyManagerService;
import org.apache.camel.Endpoint;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.impl.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Camel component for Script
 *
 * @author aaldredge
 */
public class B2bmbScriptComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbScriptComponent.class);

    /**
     * ScriptDAO - injected by camel
     */
    @Autowired
    protected ScriptDAO scriptDAO;

    /**
     * CorrelationDAO - injected by camel
     */
    @Autowired
    protected CorrelationDAO correlationDAO;


    /**
     * UserProfileDAO - injected by camel
     */
    @Autowired
    protected UserProfileDAO userProfileDAO;

    /**
     * TenancyManagerService - injected by camel
     */
    @Autowired
    protected TenancyManagerService tenancyManagerService;

    @Override
    protected void validateURI(String uri, String path, Map parameters)
            throws ResolveEndpointFailedException {
        super.validateURI(uri, path, parameters);
        if (!path.contains("/")) {
            throw new ResolveEndpointFailedException("Data domain and script name must be in the uri");
        }
        String dataDomain = path.substring(0, path.indexOf('/'));
        String scriptName = path.substring(path.indexOf('/')+1);

        try {
            Script script = scriptDAO.getByRefName(scriptName, dataDomain);
            if (script == null) {
                throw new ResolveEndpointFailedException("Unable to resolve script with data domain " +
                        dataDomain + " and script name " + scriptName);
            }
        } catch (B2BTransactionFailed b2BTransactionFailed) {
            throw new ResolveEndpointFailedException("Unable to resolve script with data domain " +
                    dataDomain + " and script name " + scriptName);
        }
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating ScriptEndpoint with uri :: " + uri);
        }
        B2bmbScriptEndpoint endpoint = new B2bmbScriptEndpoint(uri, this);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting parameters in the properties Parameter Map :: " + parameters);
        }
        //instead of using setProperties, Script endpoint gets the list of parameters to pull from for inputParameters
        endpoint.setParameters(parameters);

        endpoint.setScriptDAO(scriptDAO);
        endpoint.setCorrelationDAO(correlationDAO);
        endpoint.setUserProfileDAO(userProfileDAO);
        endpoint.setTenancyManagerService(tenancyManagerService);
        endpoint.setDomain(remaining.substring(0, remaining.indexOf('/')));
        endpoint.setScriptName(remaining.substring(remaining.indexOf('/')+1));
        return endpoint;
    }
}
