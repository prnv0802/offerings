package com.eis.b2bmb.camel.custom.util;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.core.api.v1.dao.BlobDAO;
import com.eis.core.api.v1.dao.MailboxDAO;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.BlobStoreException;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.*;
import com.eis.security.multitenancy.model.SecureSession;
import org.apache.camel.Exchange;
import org.apache.camel.NoSuchHeaderException;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.UUID;

/**
 * Utility class used to create mailbox entries in camel.
 */
public class MailboxEntryHelper {

    private static final Logger LOG = LoggerFactory.getLogger(MailboxEntryHelper.class);

    /**
     * Creates a mailbox entry object from camel exchange information.
     *
     * @param exchange - Camel Exchange object
     * @param mailbox - mailbox to create it in
     * @param blobStore - reference to the blob store
     * @param mailboxEntryDAO - reference to the mailbox entry dao
     * @param to - to address of the mailbox entry
     * @param from - from addrss of the mailbox entry
     * @param subject - subject of the mailbox entry
     * @param inputStream - the input stream to create the mailbox entry
     * @param refName - the refName to use for the mailbox entry
     * @return MailboxEntry object
     * @throws NoSuchHeaderException - if a required camel header is not passed
     * @throws BlobStoreException - if there is a problem with the blob store
     * @throws B2BTransactionFailed - if the blob or mailbox entry can not be saved
     * @throws B2BNotFoundException - in there is problem finding the object
     * @throws ValidationException - if the mailbox entry is not valid
     */
    public static MailboxEntry createMailboxEntry(Exchange exchange, Mailbox mailbox, BlobStore blobStore,
            MailboxEntryDAO mailboxEntryDAO, String to, String from, String subject, InputStream inputStream,
            String refName)  throws NoSuchHeaderException, BlobStoreException, B2BTransactionFailed,
            B2BNotFoundException, ValidationException {


        if (exchange == null) {
            throw new IllegalStateException("exchange can not be null.");
        }

        if (mailbox == null) {
            throw new IllegalStateException("mailbox can not be null.");
        }

        if (blobStore == null) {
            throw new IllegalStateException("blobStore can not be null.");
        }

        if (mailboxEntryDAO == null) {
            throw new IllegalStateException("mailboxEntry can not be null.");
        }

        if (to == null) {
            throw new IllegalStateException("to can not be null.");
        }

        if (from == null) {
            throw new IllegalStateException("from can not be null.");
        }

        if (subject == null) {
            throw new IllegalStateException("subject can not be null.");
        }

        if (inputStream == null) {
            throw new IllegalStateException("inputStream can not be null.");
        }

        if (refName == null) {
            throw new IllegalStateException("refName can not be null.");
        }

        //TODO implement full api.  this is body to attachment mapping only
        //default content type
        String contentType = "application/octet-stream";
        Attachment att = new Attachment();

        String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
        String path = mailbox.getDataDomain().replace(".", "/") + "/" + mailbox.getRefName() + "/" +
                fileName;

        BlobMetaData metaData = blobStore.createMetaDataObject();
        metaData.setDataDomain(mailbox.getDataDomain());

        metaData.setPathString(path);
        metaData.setTxId(SecureSession.getTxId());

        String blobRefName = fileName.replaceAll("\\s", "");
        blobRefName = blobRefName + String.valueOf(UUID.randomUUID());
        Blob blob = blobStore
                .createBlobFromStream(blobRefName, inputStream, contentType, metaData);


        att.setInlinePayload(false);
        String fileId = blob.getIdAsString();

        att.setPayloadId(fileId);
        att.setFileSize(blob.getSize());

        if (LOG.isInfoEnabled()) {
            LOG.info("Done, file id: " + fileId);
        }
        att.setContentType(contentType);
        att.setFileName(fileName);
        att.setRefName(path);
        att.setDataDomain(mailbox.getDataDomain());
        att.setId(String.valueOf(UUID.randomUUID()));

        MailboxEntry mailboxEntry = new MailboxEntry();
        mailboxEntry.getAttachments().add(att);
        mailboxEntry.setMailboxId(mailbox.getId());
        mailboxEntry.setDataDomain(mailbox.getDataDomain());
        mailboxEntry.setTransmissionId(exchange.getIn().getHeader(B2bmbCamelConstants.TRANSMISSION_ID,
                String.class));

        if(exchange.getIn().getHeader(B2bmbCamelConstants.MAIL_SEQUENCE_NUMBER) != null){
            mailboxEntry.setSequenceNumber(
                    exchange.getIn().getHeader(B2bmbCamelConstants.MAIL_SEQUENCE_NUMBER,
                            BigInteger.class));
        }

        if(exchange.getIn().getHeader(B2bmbCamelConstants.DOCUMENT_TYPE, String.class) != null) {
            mailboxEntry.getMetaData().put(B2bmbCamelConstants.DOCUMENT_TYPE,
                    exchange.getIn().getHeader(B2bmbCamelConstants.DOCUMENT_TYPE, String.class));
        }

        if(exchange.getIn().getHeader(B2bmbCamelConstants.FUNCT_GROUP_CONTROL_NUMBERS, String.class) != null) {
            mailboxEntry.getMetaData().put(B2bmbCamelConstants.FUNCT_GROUP_CONTROL_NUMBERS,
                    exchange.getIn().getHeader(B2bmbCamelConstants.FUNCT_GROUP_CONTROL_NUMBERS, String.class));
        }

        if(exchange.getIn().getHeader(B2bmbCamelConstants.TRANSACTION_CONTROL_NUMBERS, String.class) != null) {
            mailboxEntry.getMetaData().put(B2bmbCamelConstants.TRANSACTION_CONTROL_NUMBERS,
                    exchange.getIn().getHeader(B2bmbCamelConstants.TRANSACTION_CONTROL_NUMBERS, String.class));
        }


        //these could be on from the endpoint or from headers...endpoint are default.  seems good

        mailboxEntry.setToUserId(to != null ? to
                : ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class));
        mailboxEntry.setFromUserId(from != null ? from
                : ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class));
        subject = subject != null ? subject
                : exchange.getIn().getHeader(B2bmbCamelConstants.SUBJECT, String.class);

        if (subject == null) {
            //fall back to filename
            subject = fileName;
        }
        mailboxEntry.setId(String.valueOf(UUID.randomUUID()));
        mailboxEntry.setSubject(subject);

        if(refName != null) {
            mailboxEntry.setRefName(refName);
        }
        else if(exchange.getIn().getHeader(B2bmbCamelConstants.MAILBOX_ENTRY_NAME, String.class) != null) {
            mailboxEntry.setRefName(exchange.getIn().getHeader(B2bmbCamelConstants.MAILBOX_ENTRY_NAME, String.class));
        } else {
            mailboxEntry.setRefName(exchange.getExchangeId());
        }

        return mailboxEntryDAO.save(mailboxEntry);
    }

    /**
     * Returns an InputStream from an attachment.
     *
     * @param attachment - the attachment
     * @param blobDAO - the blob DAO
     * @return InputStream
     * @throws B2BTransactionFailed - if something fails
     */
    public static InputStream getInputStreamFromAttachment(Attachment attachment, BlobDAO blobDAO )
            throws B2BTransactionFailed {

        InputStream file = null;
        //everytime I see this logic I hate it more - needs to be encapsulated
        if (attachment.isInlinePayload()) {
            file = IOUtils.toInputStream(attachment.getPayload());
        } else {
            Blob attachmentContents = blobDAO
                    .getById(attachment.getPayloadId());

            file = attachmentContents.getInputStream();
        }

        return file;
    }

    /**
     * Creates a Mailbox with the passed in name if one is currently not in the data domain.
     *
     * @param mailboxDAO - mailboxDAO
     * @param exchange - camel exchange
     * @param mailboxName - mailbox name to be created
     * @return Mailbox that was found or created
     * @throws B2BTransactionFailed - if the blob or mailbox entry can not be saved
     * @throws B2BNotFoundException - in there is problem finding the object
     * @throws ValidationException - if the mailbox entry is not valid
     */
    public static Mailbox getMailbox(MailboxDAO mailboxDAO, Exchange exchange,
                                     String mailboxName)
            throws B2BTransactionFailed, ValidationException, B2BNotFoundException{
        String domain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);

        Mailbox mailbox = mailboxDAO.getByRefName(mailboxName,
                domain);
        if (mailbox == null) {
            Mailbox processedMailbox = new Mailbox();
            processedMailbox.setDataDomain(domain);
            processedMailbox.setRefName(mailboxName);
            processedMailbox.setMailboxRole(MailboxRole.INTERMEDIATE);
            processedMailbox.setSystemMailbox(true);
            mailbox = mailboxDAO.save(processedMailbox);
        }
        return mailbox;
    }

}
