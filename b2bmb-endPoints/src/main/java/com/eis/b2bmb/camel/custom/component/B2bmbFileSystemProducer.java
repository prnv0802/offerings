package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.model.FileSystemEntry;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * Camel producer for FileSystemEntries
 *
 * @author sudhakars
 */
public class B2bmbFileSystemProducer extends DefaultProducer {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbFileSystemProducer.class);

    /**
     * Constructor
     * @param endpoint   endPoint instance
     */
    public B2bmbFileSystemProducer(B2bmbFileSystemEndpoint endpoint) {
        super(endpoint);
    }

    /**
     * @param exchange created exchange
     * @throws Exception thrown by camel
     */
    public void process(Exchange exchange) throws Exception {
        FileSystemEntry parentFileSystemEntry = getEndpoint().getFileSystemEntryDAO()
                .getByRefName(getEndpoint().getFilePath(),
                getEndpoint().getDomain());
        if (parentFileSystemEntry == null) {
            throw new B2BNotFoundException("Directory does not exist. Domain: " + getEndpoint().getDomain()
                    + " Path: " + getEndpoint().getFilePath());
        }
        //default content type
        String contentType = "application/octet-stream";
        if (exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class) != null) {
            contentType = exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class);
        }

        String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
        if (fileName == null)
        {
            fileName = exchange.getIn().getHeader("DOWNLOADED_FILE", String.class);

        }

        if (fileName == null)
        {
            throw new IllegalStateException("File Name could not be resolved, need either:" + Exchange.FILE_NAME +
                    " or " + "DOWNLOADED_FILE in header");
        }


        InputStream in = exchange.getIn().getBody(InputStream.class);

        if ( in != null)
        {
            getEndpoint().getFileSystemEntryDAO().createFileFromStream(exchange.getExchangeId(),
                    fileName,
                    parentFileSystemEntry.getId(), in,
                    contentType, parentFileSystemEntry.getDataDomain(),
                    parentFileSystemEntry.getOwnerUserProfileRefName(),
                    exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID, String.class));
            if (LOG.isInfoEnabled()) {
                LOG.info("Endpoint URI at createFileSystemEntry method :: " + getEndpoint().getEndpointUri());
            }
        }

    }


    @Override
    public B2bmbFileSystemEndpoint getEndpoint() {
        return (B2bmbFileSystemEndpoint) super.getEndpoint();
    }
}
