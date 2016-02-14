package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.core.api.v1.dao.BlobDAO;
import com.eis.core.api.v1.dao.MailboxDAO;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.*;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.Map;

/**
 * @author aaldredge
 */
public class GetMailByIdProcessor implements Processor{

    private static final Logger LOG = LoggerFactory.getLogger(GetMailByIdProcessor.class);

    /**
     * MailboxDAO
     */
    @Autowired
    protected MailboxDAO mailboxDAO;

    /**
     * BlobDAO
     */
    @Autowired
    protected BlobDAO blobDAO;

    /**
     * MailboxEntryDAO
     */
    @Autowired
    protected MailboxEntryDAO mailboxEntryDAO;

    @Override
    public void process(Exchange exchange) throws Exception {
        InputStream message = exchange.getIn().getBody(InputStream.class);
        String mailboxEntryId = IOUtils.toString(message);

        try {
            if(mailboxEntryId == null) {
                throw new IllegalArgumentException("mailboxEntryId can not be null.");
            }

            MailboxEntry mailboxEntry = mailboxEntryDAO.getById(mailboxEntryId);

            if(mailboxEntry == null) {
                throw new IllegalStateException("mailboxEntry for id:"+mailboxEntryId +" was not" +
                        "found and the mailbox entry could not be loaded.");
            }

            //TODO: implement flag to provide behavior consistent w/ email
            if (mailboxEntry.getAttachments() != null && !mailboxEntry.getAttachments().isEmpty()) {
                //FIXME dirty, just doing the first one.
                Attachment attachment = mailboxEntry.getAttachments().get(0);
                //everytime I see this logic I hate it more - needs to be encapsulated
                if (attachment.isInlinePayload()) {
                    exchange.getIn().setBody(attachment.getPayload(), String.class);
                } else {
                    Blob attachmentContents = blobDAO
                            .getById(attachment.getPayloadId());
                    exchange.getIn().setBody(attachmentContents);
                }
                exchange.getIn().setHeader(Exchange.FILE_NAME, attachment.getFileName());
                exchange.getIn().setHeader(Exchange.FILE_LENGTH, attachment.getFileSize());
                exchange.getIn().setHeader(Exchange.CONTENT_TYPE, attachment.getContentType());
                exchange.getIn().setHeader(B2bmbCamelConstants.SUBJECT, mailboxEntry.getSubject());
                exchange.getIn().setHeader(B2bmbCamelConstants.TRANSMISSION_ID, mailboxEntry.getTransmissionId());
                exchange.setProperty(B2bmbCamelConstants.TRANSMISSION_ID, mailboxEntry.getTransmissionId());
                if(mailboxEntry.getMetaData() != null &&
                   mailboxEntry.getMetaData().get(B2bmbCamelConstants.DOCUMENT_TYPE) != null) {
                    exchange.getIn().setHeader(B2bmbCamelConstants.DOCUMENT_TYPE,
                            mailboxEntry.getMetaData().get(B2bmbCamelConstants.DOCUMENT_TYPE)) ;
                }

                if(mailboxEntry.getMetaData() != null) {
                    exchange.getIn().setHeader(B2bmbCamelConstants.REFERENCE_DATA,
                            mailboxEntry.getMetaData()) ;
                }

            } else {
                //set body of mbe as body
                exchange.getIn().setBody(mailboxEntry.getMessage(), String.class);
            }


        } finally {
            if(message != null) {
                message.close();
            }
        }
    }

    private void processRouteCompletion(MailboxEntry mailboxEntry,
        Exchange exchange) throws B2BNotFoundException, B2BTransactionFailed, ValidationException{

        Mailbox mailbox = null;

        if(exchange.getIn().getHeader(B2bmbCamelConstants.MAIL_SEQUENCE_NUMBER) != null){
            mailboxEntry.setSequenceNumber(
                    exchange.getIn().getHeader(B2bmbCamelConstants.MAIL_SEQUENCE_NUMBER,
                            BigInteger.class));
        }

        if(exchange.getIn().getHeader(B2bmbCamelConstants.DOCUMENT_TYPE, String.class) != null) {
            mailboxEntry.getMetaData().put(B2bmbCamelConstants.DOCUMENT_TYPE,
                    exchange.getIn().getHeader(B2bmbCamelConstants.DOCUMENT_TYPE, String.class));
        }

        if(exchange.getIn().getHeader(B2bmbCamelConstants.REFERENCE_DATA, Map.class) != null) {
            Map<String, Object> referenceData =    (Map<String, Object>) exchange.getIn().getHeader(
                    B2bmbCamelConstants.REFERENCE_DATA, Map.class);
            mailboxEntry.getMetaData().putAll(referenceData);
        }
        String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);
        String processedMailboxName = "Processed";

        if(exchange.getIn().getHeader("processedMailboxName", String.class) != null) {
            processedMailboxName = exchange.getIn().getHeader("processedMailboxName", String.class);
        }

        mailbox = mailboxDAO.getByRefName(processedMailboxName,
                dataDomain);
        if (mailbox == null){
            Mailbox processedMailbox = new Mailbox();
            processedMailbox.setDataDomain(dataDomain);
            processedMailbox.setRefName(processedMailboxName);
            processedMailbox.setMailboxRole(MailboxRole.INTERMEDIATE);
            processedMailbox.setSystemMailbox(true);
            mailbox = mailboxDAO.save(processedMailbox);
        }

        mailboxEntry.setMailboxId(mailbox.getId());
        mailboxEntryDAO.save(mailboxEntry);

    }
}
