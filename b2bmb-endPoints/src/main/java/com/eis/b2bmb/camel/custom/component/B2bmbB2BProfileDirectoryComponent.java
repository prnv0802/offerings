package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.dao.EDIProfileDAO;
import com.eis.b2bmb.api.v1.dao.MSIProfileDAO;
import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import com.eis.core.api.v1.model.BlobStore;
import org.apache.camel.Endpoint;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.impl.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Camel component for FileSystemEntry
 *
 * @author sudhakars
 */
public class B2bmbB2BProfileDirectoryComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbB2BProfileDirectoryComponent.class);

    /**
     * fileSystemEntryDAO - injected by camel
     */
    @Autowired
    protected FileSystemEntryDAO fileSystemEntryDAO;


    /**
     * edi profile dao to get edi profile
     */
    @Autowired
    protected EDIProfileDAO ediProfileDAO;

    /**
     * msi profile dao to get msi profile
     */
    @Autowired
    protected MSIProfileDAO msiProfileDAO;

    /**
     * blobStore - injected by camel
     */
    @Autowired
    protected BlobStore blobStore;

    @Override
    protected void validateURI(String uri, String path, Map parameters)
            throws ResolveEndpointFailedException {
        super.validateURI(uri, path, parameters);
        if (!path.contains("/")) {
            throw new ResolveEndpointFailedException("Data domain and file path must be in the uri");
        }
        String dataDomain = path.substring(0, path.indexOf('/'));

    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating FileSystemEndpoint with uri :: " + uri);
        }
        B2bmbB2BProfileDirectoryEndpoint endpoint = new B2bmbB2BProfileDirectoryEndpoint(uri, this);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting parameters in the properties Parameter Map :: " + parameters);
        }
        setProperties(endpoint, parameters);
        endpoint.setBlobStore(blobStore);
        endpoint.setFileSystemEntryDAO(fileSystemEntryDAO);
        endpoint.setMsiProfileDAO(msiProfileDAO);
        endpoint.setEdiProfileDAO(ediProfileDAO);
        endpoint.setDomain(remaining.substring(0, remaining.indexOf('/')));
        return endpoint;
    }
}
