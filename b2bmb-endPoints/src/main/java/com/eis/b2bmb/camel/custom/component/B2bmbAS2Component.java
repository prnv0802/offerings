package com.eis.b2bmb.camel.custom.component;

import java.util.Map;

import com.eis.b2bmb.api.v1.dao.AS2ClientConnectionConfigDAO;
import com.eis.b2bmb.api.v1.model.AS2ClientConnectionConfig;
import com.eis.b2bmb.endpts.as2.AS2Client;
import org.apache.camel.Endpoint;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.impl.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;


import com.eis.core.api.v1.exception.B2BNotFoundException;

/**
 * Camel component for AS2
 * User: harjeets
 */
public class B2bmbAS2Component extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbAS2Component.class);


    /**
     * as2 dao
     */
    @Autowired
    AS2ClientConnectionConfigDAO dao;



    /**
     * aS2Client - injected by camel
     */
    @Autowired
    AS2Client as2Client;


    /**
     * the to as2 id
     */
    protected String toAs2Id;

    /**
     * the from as2 id
     */
    protected String fromAs2Id;


    
    @Override
    protected void validateURI(String uri, String path, Map parameters)
            throws ResolveEndpointFailedException {
        super.validateURI(uri, path, parameters);


      
        // will overload method with dataDomain
        //String dataDomain = path.substring(0, path.indexOf('/'));
        toAs2Id = (String) parameters.get("toAS2Id");
        fromAs2Id = (String) parameters.get("fromAS2Id");

        if (toAs2Id == null)
        {
            throw new ResolveEndpointFailedException("as2ToId must be in the  uri");
        }

        if (fromAs2Id == null)
        {
            throw new ResolveEndpointFailedException("as2FromId must be in the  uri");
        }


        try {

            AS2ClientConnectionConfig config = dao.getByFromAndTo(fromAs2Id, toAs2Id);
            if (config == null) {
                throw new ResolveEndpointFailedException("Unable to resolve As2ClientConfig with fromAs2Id" +
                          fromAs2Id + " and toAs2Id  " + toAs2Id);
            }
            
        }
         catch (B2BNotFoundException e) {
               throw new ResolveEndpointFailedException("Unable to resolve As2ClientConfig with fromAs2Id" +
                        fromAs2Id + " and toAs2Id  " + toAs2Id);
          }
    }

    /**
     * @param uri        endpoint uri
     * @param remaining  string of uri ,after //
     * @param parameters map of parameters
     * @return endpoint created endpoint
     * @throws Exception exception thrown by camel runtime system
     */
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        B2bmbAS2Endpoint endpoint = new B2bmbAS2Endpoint(uri, this);
        setProperties(endpoint, parameters);

        endpoint.setAs2Client(as2Client);
        endpoint.setClientConnectionConfigDAO(dao);

        endpoint.setFromAS2Id(fromAs2Id);
        endpoint.setToAS2Id(toAs2Id);

        return endpoint;
    }
}
