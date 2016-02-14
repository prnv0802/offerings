package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.api.v1.dao.EDIProfileDAO;
import com.eis.b2bmb.api.v1.exception.EDIProfileNotFoundException;
import com.eis.b2bmb.api.v1.model.EDIProfile;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.b2bmb.camel.custom.util.DocumentTypeHelper;
import com.eis.core.common.NotifyAndCreateTaskHelper;
import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import com.eis.core.api.v1.dao.MailboxDAO;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.BlobStoreException;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.*;
import com.eis.core.router.EDIFACTParser;
import com.eis.security.multitenancy.model.SecureSession;
import com.eis.spring.util.SpringApplicationContext;
import org.apache.camel.Exchange;
import org.apache.camel.NoSuchHeaderException;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

/**
 * @author aaldredge
 */
public class EDIFactValidator implements Processor{

    private static final Logger LOG = LoggerFactory.getLogger(EDIFactValidator.class);
    /**
     * edi profile dao to get edi profile
     */
    @Autowired
    protected EDIProfileDAO ediProfileDAO;

    /**
     *  mailbox entry dao to get a mailbox entry
     */
    @Autowired
    MailboxEntryDAO mailboxEntryDAO;

    /**
     *  mailbox dao to get a mailbox
     */
    @Autowired
    MailboxDAO mailboxDAO;

    /**
     *  notify and create task helper
     */
    @Autowired
    NotifyAndCreateTaskHelper taskHelper;

    /**
     * file system entry dao to get a file system entry
     */
    @Autowired
    protected FileSystemEntryDAO fileSystemEntryDAO;

    /**
     * blobStore
     */
    @Autowired
    protected BlobStore blobStore;

    @Override
    public void process(Exchange exchange) throws Exception {
        String toAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class);
        String fromAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class);
        InputStream inputStream = exchange.getIn().getBody(InputStream.class);
        InputStream mainDoc = null;

        try {
            String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
            EDIProfile ediProfile = null;
            InputStream validationFile = null;

            String docType = DocumentTypeHelper.getDocumentType(exchange);
            String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);
            try {
                ediProfile = ediProfileDAO.getEDIProfile(fromAddress, toAddress, docType);
            } catch (B2BNotFoundException nfe) {
                throw new EDIProfileNotFoundException(String.format("There is no EDI setup for %s %s %s",
                        fromAddress, toAddress, docType), nfe);
            } catch (B2BTransactionFailed nfe) {
                throw new EDIProfileNotFoundException(String.format("There is no EDI setup for %s %s %s",
                        fromAddress, toAddress, docType), nfe);
            }


            EDIFACTParser parser = null;

            String filePath = "schemas/" + ediProfile.getSchemaFileName();
            FileSystemEntry fileSystemEntry = fileSystemEntryDAO.getByRefName(filePath,
                    dataDomain);
            if (fileSystemEntry == null) {
                throw new B2BNotFoundException("Unable to resolve file path with data domain " +
                        dataDomain + " and file path " + filePath);
            }

            Blob fileContents = blobStore.getBlobByStringId(
                    fileSystemEntry.getBlobId());

            if (fileContents == null) {
                throw new B2BNotFoundException("Unable to find blob for id:" + fileSystemEntry.getBlobId());
            }
            validationFile = fileContents.getInputStream();

            parser = (EDIFACTParser) SpringApplicationContext.getBean("edifactParser");
            parser.parseFile(inputStream, fileName, validationFile);

            String controlFile = parser.buildControlResponse(inputStream, fileName, ediProfile,
                    validationFile);

            boolean isRejected = parser.isDocumentValid();
            Mailbox mailboxCONTRL = getCONTRLOutMailbox(exchange);
            Attachment doc997 = createAttachment(IOUtils.toInputStream(controlFile), "CONTRL.edi",
                    mailboxCONTRL);
            createMailboxEntryForCONTRLOut(mailboxCONTRL, exchange, doc997);

            if (isRejected) {
                Mailbox mailbox = getSuspenseMailbox(exchange);
                mainDoc = exchange.getIn().getBody(InputStream.class);

                Attachment docMain = createAttachment(mainDoc, fileName,
                        mailbox);

                createMailboxEntryForRejectedDocument(mailbox, exchange, doc997, docMain);

                StringBuilder builder = new StringBuilder();
                builder.append("A CONTRL message generated for a document received  indicates one or more documents " +
                        "has been " +
                        "rejected . The email was addressed from:" + fromAddress + ", to:" + toAddress + "\n");
                builder.append("Please check the '" + mailbox.getRefName() + "' mailbox to see the CONTRL message and "
                        + "follow the " +
                        "process per the trading partner to resolve the issue.  None of the documents have been " +
                        "processed and will need to be processed manually.");
                builder.append("\n\nTransmission Id:" + exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID,
                        String.class));
                builder.append("\n\nInterchange Control Number:" + parser.getInterchangeControlNumber());

                String subject = "Document received has rejections..";
                String body = builder.toString();

                taskHelper.notifyAndCreateTask("997-" + parser.getInterchangeControlNumber() + new Date(), subject,
                        body, subject, CamelDataDomainHelper.getDataDomainFromExchange(exchange),
                        "CONTRL Rejection", "fa-rejections", "Lindsay Karlowsky");
                exchange.getIn().setHeader(B2bmbCamelConstants.FUNCT_ACK_REJECTED_INDICATOR, "Y");
            } else {
                String idAsBody = exchange.getIn().getHeader(B2bmbCamelConstants.ID_AS_BODY,
                        String.class);

                if ("true".equals(idAsBody)) {
                    if (exchange.getProperty("MailboxEntryId") != null) {
                        exchange.getIn().setBody(exchange.getProperty("MailboxEntryId"),
                                String.class);
                    }
                }

                exchange.getIn().setHeader(B2bmbCamelConstants.FUNCT_ACK_REJECTED_INDICATOR, "N");
            }
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }

            if (mainDoc != null) {
                mainDoc.close();
            }

            if (LOG.isInfoEnabled()) {
                LOG.info("Closed All Streams.");
            }
        }
    }


    private void createMailboxEntryForRejectedDocument(Mailbox mailbox, Exchange exchange,
                                                       Attachment doc997, Attachment docMain)
            throws NoSuchHeaderException, BlobStoreException, ValidationException, B2BTransactionFailed,
            B2BNotFoundException {

        MailboxEntry mailboxEntry = new MailboxEntry();
        mailboxEntry.getAttachments().add(doc997);
        mailboxEntry.getAttachments().add(docMain);
        mailboxEntry.setMailboxId(mailbox.getId());
        mailboxEntry.setDataDomain(mailbox.getDataDomain());
        mailboxEntry.setTransmissionId(exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID, String.class));
        mailboxEntry.setSequenceNumber(
                exchange.getIn().getHeader(B2bmbCamelConstants.MAIL_SEQUENCE_NUMBER,
                        BigInteger.class));


        //these could be on from the endpoint or from headers...endpoint are default.  seems good
        mailboxEntry.setToUserId(ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class));
        mailboxEntry.setFromUserId( ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM,
                String.class));
        String subject = exchange.getIn().getHeader(B2bmbCamelConstants.SUBJECT, String.class);

        if (subject == null) {
            //fall back to filename
            subject = "Document has been rejected. Check 997 and original document.";
        }
        mailboxEntry.setSubject(subject);
        mailboxEntry.setRefName(exchange.getExchangeId()+"-Rejected");

        mailboxEntryDAO.save(mailboxEntry);
    }


    private void createMailboxEntryForCONTRLOut(Mailbox mailbox, Exchange exchange,
                                             Attachment doc997)
            throws NoSuchHeaderException, BlobStoreException, ValidationException, B2BTransactionFailed,
            B2BNotFoundException {

        MailboxEntry mailboxEntry = new MailboxEntry();
        mailboxEntry.getAttachments().add(doc997);
        mailboxEntry.setMailboxId(mailbox.getId());
        mailboxEntry.setDataDomain(mailbox.getDataDomain());
        mailboxEntry.setTransmissionId(exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID, String.class));

        if(exchange.getIn().getHeader(B2bmbCamelConstants.MAIL_SEQUENCE_NUMBER,
                BigInteger.class) != null) {
            mailboxEntry.setSequenceNumber(
                    exchange.getIn().getHeader(B2bmbCamelConstants.MAIL_SEQUENCE_NUMBER,
                            BigInteger.class));
        }


        //these could be on from the endpoint or from headers...endpoint are default.  seems good
        mailboxEntry.setToUserId(ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class));
        mailboxEntry.setFromUserId( ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO,
                String.class));
        String subject = exchange.getIn().getHeader(B2bmbCamelConstants.SUBJECT, String.class);

        if (subject == null) {
            //fall back to filename
            subject = "Document has been rejected. Check 997 and original document.";
        }
        mailboxEntry.setSubject(subject);
        mailboxEntry.setRefName(exchange.getExchangeId()+"997");

        mailboxEntryDAO.save(mailboxEntry);
    }

    private Attachment createAttachment(InputStream inputStream, String fileName, Mailbox mailbox)
            throws BlobStoreException{
        Attachment att = new Attachment();
        String newFileName =  UUID.randomUUID() +"_"+ fileName;
        String path = mailbox.getDataDomain().replace(".", "/") + "/" + mailbox.getRefName() + "/" +
                newFileName;
        BlobMetaData metaData = blobStore.createMetaDataObject();
        metaData.setDataDomain(mailbox.getDataDomain());

        metaData.setPathString(path);
        metaData.setTxId(SecureSession.getTxId());

        String refName = newFileName.replaceAll("\\s", "");

        Blob blob = blobStore
                .createBlobFromStream(refName,inputStream, "text/plain", metaData);

        att.setInlinePayload(false);
        String fileId = blob.getIdAsString();

        att.setPayloadId(fileId);
        att.setFileSize(blob.getSize());

        if (LOG.isInfoEnabled()) {
            LOG.info("Done, file id: " + fileId);
        }
        att.setContentType("text/plain");
        att.setFileSize(blob.getSize());
        att.setFileName(newFileName);
        att.setRefName(path);
        att.setDataDomain(mailbox.getDataDomain());
        att.setId(String.valueOf(UUID.randomUUID()));
        return att;
    }

    private Mailbox getSuspenseMailbox(Exchange exchange) throws B2BTransactionFailed, ValidationException,
            B2BNotFoundException{
        String mailboxName = exchange.getIn().getHeader("SuspenseMailboxName", String.class);
        String domain = exchange.getIn().getHeader("Domain", String.class);
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

    private Mailbox getCONTRLOutMailbox(Exchange exchange) throws B2BTransactionFailed, ValidationException,
            B2BNotFoundException{
        String mailboxName = exchange.getIn().getHeader("CONTRLOutMailboxName", String.class);
        String domain = exchange.getIn().getHeader("Domain", String.class);
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
