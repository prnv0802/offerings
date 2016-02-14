package com.eis.b2bmb.camel.custom.component;

import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.model.BlobStore;
import com.eis.core.api.v1.model.FileSystemEntry;
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
public class B2bmbFileSystemComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbFileSystemComponent.class);

    /**
     * fileSystemEntryDAO - injected by camel
     */
    @Autowired
    protected FileSystemEntryDAO fileSystemEntryDAO;

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
        String filePath = path.substring(path.indexOf('/') + 1);

        try {
            FileSystemEntry fileSystemEntry = fileSystemEntryDAO.getByRefName(filePath, dataDomain);
            if (fileSystemEntry == null) {
                throw new ResolveEndpointFailedException("Unable to resolve file path with data domain " +
                        dataDomain + " and file path " + filePath);
            }
        } catch (B2BTransactionFailed b2BTransactionFailed) {
            throw new ResolveEndpointFailedException("Unable to resolve file path with data domain " +
                    dataDomain + " and file path " + filePath);
        }
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating FileSystemEndpoint with uri :: " + uri);
        }
        B2bmbFileSystemEndpoint endpoint = new B2bmbFileSystemEndpoint(uri, this);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting parameters in the properties Parameter Map :: " + parameters);
        }
        setProperties(endpoint, parameters);
        endpoint.setBlobStore(blobStore);
        endpoint.setFileSystemEntryDAO(fileSystemEntryDAO);
        endpoint.setDomain(remaining.substring(0, remaining.indexOf('/')));
        endpoint.setFilePath(remaining.substring(remaining.indexOf('/')+1));
        return endpoint;
    }
}
