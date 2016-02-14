package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.dao.AvailableDocumentDAO;
import com.eis.b2bmb.api.v1.dao.EDIProfileDAO;
import com.eis.core.common.NotifyAndCreateTaskHelper;
import com.eis.core.api.v1.dao.BlobDAO;
import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import com.eis.core.api.v1.dao.MailboxDAO;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
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
public class B2bmbMapForceServerComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbAS2Component.class);

    /**
     * mailboxEntryDAO - injected by camel
     */
    @Autowired
    MailboxEntryDAO mailboxEntryDAO;
    /**
     * mailboxDAO - injected by camel
     */
    @Autowired
    MailboxDAO mailboxDAO;

    /**
     * blobStore - injected by camel
     */
    @Autowired
    BlobStore blobStore;

    /**
     * blobDAO - injected by camel
     */
    @Autowired
    BlobDAO blobDAO;

    /**
     * NotifyAndCreateTaskHelper - injected by camel
     */
    @Autowired
    NotifyAndCreateTaskHelper taskHelper;

    /**
     * ediProfileDAO - injected by camel
     */
    @Autowired
    EDIProfileDAO ediProfileDAO;

    /**
     * availableDocumentDAO - injected by camel
     */
    @Autowired
    AvailableDocumentDAO availableDocumentDAO;

    /**
     * fileSystemEntryDAO - injected by camel
     */
    @Autowired
    FileSystemEntryDAO fileSystemEntryDAO;

    @Override
    protected void validateURI(String uri, String path, Map parameters)
            throws ResolveEndpointFailedException {
        super.validateURI(uri, path, parameters);
        if (!path.contains("/")) {
            throw new ResolveEndpointFailedException("Data domain and mailbox refname must be in the uri");
        }

        // will overload method with dataDomain
        String dataDomain = path.substring(0, path.indexOf('/'));

    }

    /**
     * @param uri        endpoint uri
     * @param remaining  string of uri ,after //
     * @param parameters map of parameters
     * @return endpoint created endpoint
     * @throws Exception exception thrown by camel runtime system
     */
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        B2bmbMapForceServerEndpoint endpoint = new B2bmbMapForceServerEndpoint(uri, this);
        setProperties(endpoint, parameters);
        endpoint.setMailboxEntryDAO(mailboxEntryDAO);
        endpoint.setMailboxDAO(mailboxDAO);
        endpoint.setBlobStore(blobStore);
        endpoint.setBlobDAO(blobDAO);
        endpoint.setEdiProfileDAO(ediProfileDAO);
        endpoint.setTaskHelper(taskHelper);
        endpoint.setAvailableDocumentDAO(availableDocumentDAO);
        endpoint.setFileSystemEntryDAO(fileSystemEntryDAO);
        endpoint.setDomain(remaining.substring(0, remaining.indexOf('/')));
        endpoint.setServerUrl(remaining.substring(remaining.indexOf('/') + 1, remaining.length()));
        return endpoint;
    }
}
