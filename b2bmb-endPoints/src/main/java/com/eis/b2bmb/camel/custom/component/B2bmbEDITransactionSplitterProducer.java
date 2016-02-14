package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.util.EDIFACTTransactionReader;
import com.eis.b2bmb.util.EDITransactionReader;
import com.eis.core.api.v1.model.Mailbox;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Iterator;

/**
 * Camel producer for MailboxEntries
 */
public class B2bmbEDITransactionSplitterProducer extends DefaultProducer {
    private static final transient Logger LOG = LoggerFactory.getLogger(B2bmbMailboxEndpoint.class);

    /**
     * Parameterized constructor
     *
     * @param endpoint   endpoint Uri
     */
    public B2bmbEDITransactionSplitterProducer(B2bmbEDITransactionSplitterEndpoint endpoint) {

        super(endpoint);
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating  B2bmbMailboxProducer  object  ");
        }
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Inside process method of B2bMailBoxProducer ");
        }
        if (exchange == null) {
            throw new IllegalArgumentException("The  exchange cannot be null ");
        }

        Iterator<String> mailboxEntryIds = null;
        String fileType = exchange.getIn().getHeader(B2bmbCamelConstants.FILE_TYPE, String.class);

        Mailbox mailbox = getEndpoint().getMailboxDAO().getByRefName(getEndpoint().getMailboxName(),
                getEndpoint().getDomain());

        String toUserId = getEndpoint().getTo() != null ? getEndpoint().getTo()
                : ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class);
        String fromUserId = getEndpoint().getFrom() != null ? getEndpoint().getFrom()
                : ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class);

        if("X12".equals(fileType)) {
            EDITransactionReader reader = new EDITransactionReader();
            mailboxEntryIds = reader.getTransactions(exchange.getIn().getBody(InputStream.class),
                    getEndpoint().getBlobStore(), getEndpoint().getMailboxEntryDAO(),
                    getEndpoint().getAvailableDocumentDAO(),
                    mailbox, exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID, String.class),
                    toUserId, fromUserId);
        } else if ("EDIFACT".equals(fileType)) {
            EDIFACTTransactionReader reader = new EDIFACTTransactionReader();
            mailboxEntryIds = reader.getTransactions(exchange.getIn().getBody(InputStream.class),
                    getEndpoint().getBlobStore(), getEndpoint().getMailboxEntryDAO(),
                    getEndpoint().getAvailableDocumentDAO(),
                    mailbox, exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID, String.class),
                    toUserId, fromUserId);
        } else {
            if(LOG.isWarnEnabled()) {
                LOG.warn("An unknown file type has been sent for processing.");
            }
        }


        exchange.getIn().setBody(mailboxEntryIds, Iterator.class);

    }


    @Override
    public B2bmbEDITransactionSplitterEndpoint getEndpoint() {
        return (B2bmbEDITransactionSplitterEndpoint) super.getEndpoint();
    }
}
