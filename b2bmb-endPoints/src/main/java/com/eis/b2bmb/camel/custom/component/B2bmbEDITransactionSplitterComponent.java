package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.dao.AvailableDocumentDAO;
import com.eis.core.api.v1.dao.BlobDAO;
import com.eis.core.api.v1.dao.MailboxDAO;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.model.BlobStore;
import com.eis.core.api.v1.model.Mailbox;
import org.apache.camel.Endpoint;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.impl.DefaultComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;


/**
 * Camel component for MailboxEntry
 */
public class B2bmbEDITransactionSplitterComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbEDITransactionSplitterComponent.class);


    /**
     * mailboxEntryDAO - injected by camel
     */
    @Autowired
    protected MailboxEntryDAO mailboxEntryDAO;
    /**
     * mailboxDAO - injected by camel
     */
    @Autowired
    protected MailboxDAO mailboxDAO;
    /**
     * blobDAO - injected by camel
     */
    @Autowired
    protected BlobDAO blobDAO;

    /**
     * blobStore - injected by camel
     */
    @Autowired
    protected BlobStore blobStore;

    /**
     * availableDocumentDAO - injected by camel
     */
    @Autowired
    protected AvailableDocumentDAO availableDocumentDAO;

    @Override
    protected void validateURI(String uri, String path, Map parameters)
            throws ResolveEndpointFailedException {
        super.validateURI(uri, path, parameters);
        if (!path.contains("/")) {
            throw new ResolveEndpointFailedException("Data domain and mailbox refname must be in the uri");
        }
        String dataDomain = path.substring(0, path.indexOf('/'));
        String mailboxName = path.substring(path.indexOf('/') + 1);


        try {
            Mailbox mailbox = mailboxDAO.getByRefName(mailboxName, dataDomain);
            if (mailbox == null) {
                throw new ResolveEndpointFailedException("Unable to resolve mailbox with data domain " +
                        dataDomain + " and mailbox name " + mailboxName);
            }
        } catch (B2BTransactionFailed b2BTransactionFailed) {
            throw new ResolveEndpointFailedException("Unable to resolve mailbox with data domain " +
                    dataDomain + " and mailbox name " + mailboxName);
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
        B2bmbEDITransactionSplitterEndpoint endpoint = new B2bmbEDITransactionSplitterEndpoint(uri, this);
        setProperties(endpoint, parameters);

        endpoint.setBlobDAO(blobDAO);
        endpoint.setBlobStore(blobStore);
        endpoint.setMailboxDAO(mailboxDAO);
        endpoint.setMailboxEntryDAO(mailboxEntryDAO);
        endpoint.setAvailableDocumentDAO(availableDocumentDAO);
        endpoint.setDomain(remaining.substring(0, remaining.indexOf('/')));
        endpoint.setMailboxName(remaining.substring(remaining.indexOf('/')+1));

        return endpoint;
    }
}
