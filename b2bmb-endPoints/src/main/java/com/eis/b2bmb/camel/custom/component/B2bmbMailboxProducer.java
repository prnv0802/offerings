package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.exception.EDIProfileNotFoundException;
import com.eis.b2bmb.api.v1.model.EDIProfile;
import com.eis.b2bmb.api.v1.model.ExchangedDocument;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.core.api.v1.events.ExchangedDocumentEvent;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.*;
import com.eis.security.multitenancy.model.SecureSession;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Map;
import java.util.UUID;

/**
 * Camel producer for MailboxEntries
 */
public class B2bmbMailboxProducer extends DefaultProducer {
    private static final transient Logger LOG = LoggerFactory.getLogger(B2bmbMailboxEndpoint.class);

    private boolean textIsHtml = false;

    /**
     * Parameterized constructor
     *
     * @param endpoint endpoint Uri
     */
    public B2bmbMailboxProducer(B2bmbMailboxEndpoint endpoint) {

        super(endpoint);
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating  B2bmbMailboxProducer  object  ");
        }
    }



    private String getText(Part p ) throws IOException, MessagingException {



        if (p.isMimeType("text/*")) {
            String s = (String)p.getContent();
            textIsHtml = p.isMimeType("text/html");
            return s;
        }

        if (p.isMimeType("multipart/alternative")) {
            // prefer html text over plain text
            Multipart mp = (Multipart)p.getContent();
            String text = null;
            for (int i = 0; i < mp.getCount(); i++) {
                Part bp = mp.getBodyPart(i);
                if (bp.isMimeType("text/plain")) {
                    if (text == null)
                        text = getText(bp);
                    continue;
                } else if (bp.isMimeType("text/html")) {
                    String s = getText(bp);
                    if (s != null)
                        return s;
                } else {
                    return getText(bp);
                }
            }
            return text;
        } else if (p.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)p.getContent();
            for (int i = 0; i < mp.getCount(); i++) {
                String s = getText(mp.getBodyPart(i));
                if (s != null)
                    return s;
            }
        }

        return null;
    }

    @Override
    public void process(Exchange exchange) throws Exception {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Inside process method of B2bMailBoxProducer ");
        }
        if (exchange == null) {
            throw new IllegalArgumentException("The  exchange cannot be null ");
        }

        Mailbox mailbox = getEndpoint().getMailboxDAO().getByRefName(getEndpoint().getMailboxName(),
                getEndpoint().getDomain());

        if (mailbox == null) {
            throw new IllegalStateException("Could not find mailbox with mailbox name:"
                    + getEndpoint().getMailboxName());
        }

        //TODO implement full api.  this is body to attachment mapping only
        //default content type


        // Seems like we are assuming an attachement here?
        String contentType = "application/octet-stream";


        if (exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class) != null) {
            contentType = exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class);
        }

        Attachment att = new Attachment();

        String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);

//        String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);

        MailboxEntry mailboxEntry = new MailboxEntry();

        // FIXME seems odd I guess we are assuming one and only one attachment?
        if (fileName != null) {
            String path = mailbox.getDataDomain().replace(".", "/") + "/" + mailbox.getRefName() + "/" +
                    fileName;

            BlobMetaData metaData = getEndpoint().getBlobStore().createMetaDataObject();
            metaData.setDataDomain(mailbox.getDataDomain());

            metaData.setPathString(path);
            metaData.setTxId(SecureSession.getTxId());

            String refName = fileName.replaceAll("\\s", "");

            Blob blob = getEndpoint().getBlobStore()
                    .createBlobFromStream(refName, exchange.getIn().getBody(InputStream.class), contentType, metaData);

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


            mailboxEntry.getAttachments().add(att);

        } else
        // lets see if we can just create a body
        {

            if(!exchange.getIn().hasAttachments())
            {
                Object obj = exchange.getIn().getBody(String.class);
                mailboxEntry.setMessage(obj.toString());
            }
            else
            {
                // first see if we can pull out the message body
                Object obj = exchange.getIn().getBody();

                if ( obj instanceof Multipart)
                {
                    Multipart mp = (Multipart) obj;

                    String text = getText(mp.getParent());

                    if (text != null)
                    {
                        mailboxEntry.setMessage(text);
                    }

                }

                Map<String, DataHandler> attachments = exchange.getIn().getAttachments();

                for (String key : attachments.keySet())
                {
                    DataHandler handler = attachments.get(key);
                    InputStream in = handler.getInputStream();

                    BlobMetaData metaData = getEndpoint().getBlobStore().createMetaDataObject();
                    metaData.setDataDomain(mailbox.getDataDomain());

                    String path = mailbox.getDataDomain().replace(".", "/") + "/" + mailbox.getRefName() + "/" +
                            key;

                    metaData.setPathString(path);
                    metaData.setTxId(SecureSession.getTxId());

                    Blob blob = getEndpoint().getBlobStore().createBlobFromStream(key,
                            in, handler.getContentType(), metaData);

                    att = new Attachment();

                    att.setInlinePayload(false);
                    String fileId = blob.getIdAsString();

                    att.setPayloadId(fileId);
                    att.setFileSize(blob.getSize());
                    att.setContentType(handler.getContentType());
                    att.setFileName(key);
                    att.setRefName(path);
                    att.setDataDomain(mailbox.getDataDomain());
                    att.setId(String.valueOf(UUID.randomUUID()));


                    mailboxEntry.getAttachments().add(att);


                }


            }
        }


        mailboxEntry.setMailboxId(mailbox.getId());
        mailboxEntry.setDataDomain(mailbox.getDataDomain());
        mailboxEntry.setTransmissionId(exchange.getIn().getHeader(B2bmbCamelConstants.TRANSMISSION_ID,
                String.class));

        if (exchange.getIn().getHeader(B2bmbCamelConstants.MAIL_SEQUENCE_NUMBER) != null) {
            mailboxEntry.setSequenceNumber(
                    exchange.getIn().getHeader(B2bmbCamelConstants.MAIL_SEQUENCE_NUMBER,
                            BigInteger.class));
        }

        if (exchange.getIn().getHeader(B2bmbCamelConstants.DOCUMENT_TYPE, String.class) != null) {
            mailboxEntry.getMetaData().put(B2bmbCamelConstants.DOCUMENT_TYPE,
                    exchange.getIn().getHeader(B2bmbCamelConstants.DOCUMENT_TYPE, String.class));
        }

        if (exchange.getIn().getHeader(B2bmbCamelConstants.REFERENCE_DATA, Map.class) != null) {
            Map<String, Object> referenceData = (Map<String, Object>) exchange.getIn().getHeader(
                    B2bmbCamelConstants.REFERENCE_DATA, Map.class);
            mailboxEntry.getMetaData().putAll(referenceData);
        }

        if(exchange.getIn().getHeader(B2bmbCamelConstants.FILE_TYPE, String.class) != null) {
            mailboxEntry.getMetaData().put(B2bmbCamelConstants.FILE_TYPE,
                    exchange.getIn().getHeader(B2bmbCamelConstants.FILE_TYPE, String.class));
        }

        mailboxEntry.setToUserId(getEndpoint().getTo() != null ? getEndpoint().getTo()
                : ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class));
        mailboxEntry.setFromUserId(getEndpoint().getFrom() != null ? getEndpoint().getFrom()
                : ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class));
        String subject = getEndpoint().getSubject() != null ? getEndpoint().getSubject()
                : exchange.getIn().getHeader(B2bmbCamelConstants.SUBJECT, String.class);

        if (subject == null) {
            //fall back to filename
            subject = fileName;
        }
        mailboxEntry.setSubject(subject);

        if (exchange.getIn().getHeader(B2bmbCamelConstants.MAILBOX_ENTRY_NAME, String.class) != null) {
            mailboxEntry.setRefName(exchange.getIn().getHeader(B2bmbCamelConstants.MAILBOX_ENTRY_NAME, String.class));
        } else {
            mailboxEntry.setRefName(exchange.getExchangeId());
        }

        getEndpoint().getMailboxEntryDAO().save(mailboxEntry);

        if(getEndpoint().getRecordExchangedDocument() != null &&
                getEndpoint().getRecordExchangedDocument().equals("true")) {
            if (mailboxEntry.getAttachments().get(0).getPayloadId()!= null)
            {
                //FIXME TONY:- you have to add each entry as there can be multiple or none at all
                // this assumes the first one if it exists but more can exist
                recordExchangedDocument(mailboxEntry, mailbox, mailboxEntry.getAttachments().get(0).getPayloadId()
                        ,exchange);
            }

        }
    }


    private void recordExchangedDocument(MailboxEntry mbe, Mailbox mailbox, String blobId, Exchange exchange) throws
            B2BNotFoundException, B2BTransactionFailed, ValidationException, EDIProfileNotFoundException {

        String docType = null;
        EDIProfile ediProfile = null;
        if (mbe.getMetaData() != null && mbe.getMetaData().get(B2bmbCamelConstants.DOCUMENT_TYPE) != null) {
            docType = (String) mbe.getMetaData().get(B2bmbCamelConstants.DOCUMENT_TYPE);
        }

        try {
            ediProfile = getEndpoint().getEDIProfileDAO().getEDIProfile(mbe.getFromUserId(),
                    mbe.getToUserId(), docType);
        } catch (B2BNotFoundException nfe) {
            throw new EDIProfileNotFoundException(String.format("There is no EDI setup for %s %s %s",
                    mbe.getFromUserId(), mbe.getToUserId(), docType), nfe);
        } catch (B2BTransactionFailed nfe) {
            throw new EDIProfileNotFoundException(String.format("There is no EDI setup for %s %s %s",
                    mbe.getFromUserId(), mbe.getToUserId(), docType), nfe);
        }

        for (Attachment attachment : mbe.getAttachments()) {

            ExchangedDocument exchangedDocument = new ExchangedDocument();
            exchangedDocument.setId(String.valueOf(UUID.randomUUID()));
            exchangedDocument.setDataDomain(mbe.getDataDomain());
            exchangedDocument.setRefName(attachment.getFileName() + "-" +
                    Calendar.getInstance().getTimeInMillis());

            exchangedDocument.setToAddressId(ediProfile.getIsaReceiverId());
            exchangedDocument.setFromAddressId(ediProfile.getIsaSenderId());
            exchangedDocument.setFromVendor(ediProfile.getFromVendor());
            exchangedDocument.setToVendor(ediProfile.getToVendor());

            if (exchange.getIn().getHeader("Channel") != null) {
                exchangedDocument.setChannel(String.valueOf(exchange.getIn().getHeader("Channel")));
            }

            if (MailboxRole.IN.equals(mailbox.getMailboxRole()) || mailbox.getRefName().contains("-in")) {
                exchangedDocument.setDirection(MailboxRole.IN.value());
            } else if (MailboxRole.OUT.equals(mailbox.getMailboxRole()) || mailbox.getRefName().contains("-out")) {
                exchangedDocument.setDirection(MailboxRole.OUT.value());
            }

            exchangedDocument.setDocumentByteSize(attachment.getFileSize());
            if (mbe.getMetaData() != null && mbe.getMetaData().get(B2bmbCamelConstants.DOCUMENT_TYPE) != null) {
                exchangedDocument.setDocumentType((String) mbe.getMetaData().get(B2bmbCamelConstants.DOCUMENT_TYPE));
            }

            if (mbe.getSequenceNumber() != null) {
                exchangedDocument.setInterchangeControlNumber(String.valueOf(mbe.getSequenceNumber()));
            }

            exchangedDocument.setMailboxName(mailbox.getRefName());
            exchangedDocument.setStartDateTime(Calendar.getInstance().getTime());
            exchangedDocument.setFromInternalEmailAddress(mbe.getFromUserId());
            exchangedDocument.setToInternalEmailAddress(mbe.getToUserId());
            exchangedDocument.setTransmissionId(mbe.getTransmissionId());
            exchangedDocument.setFileName(attachment.getFileName());
            exchangedDocument.getReferenceData().putAll(mbe.getMetaData());
            exchangedDocument.setBlobId(blobId);

            getEndpoint().getExchangedDocumentDAO().save(exchangedDocument);
            getEndpoint().getLocalEventPublisher().publish(new ExchangedDocumentEvent(exchangedDocument));
        }

    }


    @Override
    public B2bmbMailboxEndpoint getEndpoint() {
        return (B2bmbMailboxEndpoint) super.getEndpoint();
    }


}
