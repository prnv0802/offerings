package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.objectbuilders.JAXBExporter;
import org.apache.camel.Endpoint;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.impl.DefaultComponent;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Map;


/**
 * Camel component for MailboxEntry
 */
public class B2bmbExportBuilderComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbExportBuilderComponent.class);

    @Resource(name="availableExporters")
    private Map<String, JAXBExporter> availableExporters;

    @Override
    protected void validateURI(String uri, String path, Map parameters)
            throws ResolveEndpointFailedException {
        super.validateURI(uri, path, parameters);
        if (!path.contains("/")) {
            throw new ResolveEndpointFailedException("Data domain and mailbox refname must be in the uri");
        }
        String dataDomain = path.substring(0, path.indexOf('/'));
        String exportObjectName = path.substring(path.indexOf('/') + 1);

        if (availableExporters == null || availableExporters.isEmpty()) {
            throw new ResolveEndpointFailedException("No available exporters are registered");
        } else if (!availableExporters.containsKey(exportObjectName)) {
            throw new ResolveEndpointFailedException("No exporter registered for " + exportObjectName
                    + ". Available exporters are " + StringUtils.join(availableExporters.keySet(), ", "));
        } else if (availableExporters.get(exportObjectName) == null){
            throw new ResolveEndpointFailedException("Exporter registered for " + exportObjectName
                    + ". was null.");
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
        B2bmbExportBuilderEndpoint endpoint = new B2bmbExportBuilderEndpoint(uri, this);
        setProperties(endpoint, parameters);

        endpoint.setDomain(remaining.substring(0, remaining.indexOf('/')));
        String exportObjectName = remaining.substring(remaining.indexOf('/') + 1);
        availableExporters.get(exportObjectName);
        endpoint.setExporter(availableExporters.get(exportObjectName));
        endpoint.setExportName(exportObjectName);

        return endpoint;
    }
}
