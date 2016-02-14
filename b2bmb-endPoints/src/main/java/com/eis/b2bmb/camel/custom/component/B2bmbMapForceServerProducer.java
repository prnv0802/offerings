package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.exception.MailboxRouterNotFoundException;
import com.eis.b2bmb.api.v1.exception.MapForceServerException;
import com.eis.b2bmb.api.v1.model.EDIProfile;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.b2bmb.camel.custom.util.DocumentTypeHelper;
import com.eis.b2bmb.camel.custom.util.MailboxEntryHelper;
import com.eis.b2bmb.util.EDIFACTTransactionReader;
import com.eis.b2bmb.util.EDITransactionReader;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.BlobStoreException;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.*;
import com.eis.core.router.EDIFACTParser;
import com.eis.core.router.Edi997Checker;
import com.eis.security.multitenancy.model.SecureSession;
import com.eis.spring.util.SpringApplicationContext;
import inedi.InEDIException;
import org.apache.camel.Exchange;
import org.apache.camel.NoSuchHeaderException;
import org.apache.camel.converter.stream.CachedOutputStream;
import org.apache.camel.impl.DefaultProducer;
import org.apache.camel.util.ExchangeHelper;
import org.apache.camel.util.IOHelper;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;

/**
 * Camel producer for MapForceServer
 *
 * @author tcostanzo
 */
public class B2bmbMapForceServerProducer extends DefaultProducer {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbMapForceServerProducer.class);

    /**
     * Constructor
     * @param endpoint   endPoint instance
     */
    public B2bmbMapForceServerProducer(B2bmbMapForceServerEndpoint endpoint) {
        super(endpoint);
    }

    /**
     * @param exchange created exchange
     * @throws Exception thrown by camel
     */
    public void process(Exchange exchange) throws Exception {
        if(LOG.isDebugEnabled()){
            LOG.debug("Inside process method of B2bMapForceServerProducer ");
        }
        if(exchange == null){
            throw new IllegalArgumentException("The exchange cannot be null");
        }
        InputStream message = null;

        try {
            message = exchange.getIn().getBody(InputStream.class);
            String to = getEndpoint().getTo() != null ? getEndpoint().getTo()
                    : ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class);
            String from = getEndpoint().getFrom() != null ? getEndpoint().getFrom()
                    : ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class);

            String subject = getEndpoint().getSubject() != null ? getEndpoint().getSubject()
                    : ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.SUBJECT, String.class);
            String fileType = exchange.getIn().getHeader(B2bmbCamelConstants.FILE_TYPE, String.class);
            String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);
            EDIProfile ediProfile = getEDIProfile(from, to, exchange);

            String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);

            String mailboxEntryId = null;
            MailboxEntry mailboxEntry = null;

            Mailbox processedMailbox = getEndpoint().getMailboxDAO().getByRefName(
                    getEndpoint().getProcessedMailboxName(),
                    getEndpoint().getDomain());

            String idAsBody = exchange.getIn().getHeader(B2bmbCamelConstants.ID_AS_BODY,
                    String.class);

            if ("MailboxEntryId".equals(fileName) || "true".equals(idAsBody)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Exchange came from a queue, getting mailbox entry id to load mailbox entry.");
                }
                mailboxEntryId = IOUtils.toString(message);
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Exchange contains a file in the input stream, storing as a mailboxEntry so a 997 and " +
                            "other document can be generated.");
                }

                Mailbox mailbox = getEndpoint().getMailboxDAO().getByRefName
                        (getEndpoint().getMapProcessingMailboxName(),
                        getEndpoint().getDomain());
                mailboxEntry = MailboxEntryHelper.createMailboxEntry(exchange, mailbox,
                        getEndpoint().getBlobStore(), getEndpoint().getMailboxEntryDAO(),
                        to, from, subject, message, UUID.randomUUID() + "-processing");

                mailboxEntryId = mailboxEntry.getId();
            }

            if (mailboxEntry == null) {
                mailboxEntry = getEndpoint().getMailboxEntryDAO().getById(mailboxEntryId);
            }

            InputStream file = null;
            InputStream file997 = null;
            FunctionalAckResult result = null;
            for (Attachment attachment : mailboxEntry.getAttachments()) {
                try {
                    if ("Y".equals(ediProfile.getSend997()) && !"Y".equals(getEndpoint().getUseAfterEnvelopeMap())) {
                        boolean wasRejected = false;
                        file = MailboxEntryHelper.getInputStreamFromAttachment(
                                attachment, getEndpoint().getBlobDAO());

                        if (B2bmbCamelConstants.FILE_TYPE_X12.equals(fileType)) {
                            result = checkX12FunctionalAck(exchange, ediProfile, file, attachment.getFileName(),
                                    attachment);
                        } else if (B2bmbCamelConstants.FILE_TYPE_EDIFACT.equals(fileType)) {
                            result = checkEDIFACTFunctionalAck(ediProfile, file, attachment.getFileName(), dataDomain);
                        } else {
                            if (LOG.isErrorEnabled()) {
                                LOG.error("An unknown file type has been sent for processing.");
                            }
                            throw new B2BNotFoundException("An unknown file type has been sent for processing.");
                        }
                        Mailbox mailbox997Out = null;
                        if (ediProfile.getFuncAckOutMailbox() != null && !"".equals(ediProfile.getFuncAckOutMailbox())){
                            mailbox997Out = getEndpoint().getMailboxDAO().getByRefName(
                                    ediProfile.getFuncAckOutMailbox(),
                                    getEndpoint().getDomain());
                        } else {
                            mailbox997Out = getEndpoint().getMailboxDAO().getByRefName(
                                    getEndpoint().getOut997MailboxName(),
                                    getEndpoint().getDomain());
                        }

                        file = MailboxEntryHelper.getInputStreamFromAttachment(
                                attachment, getEndpoint().getBlobDAO());

                        MailboxEntry mailboxEntry997 = MailboxEntryHelper.createMailboxEntry(exchange, mailbox997Out,
                                getEndpoint().getBlobStore(), getEndpoint().getMailboxEntryDAO(),
                                from, to, subject, result.getAckFile(), UUID.randomUUID() + "-997");

                        processGenerated997(result.isWasRejected(), exchange, mailboxEntry997, file,
                                attachment.getFileName(), to, from,
                                result.getInterchangeControlNumber());

                        if (!result.isWasRejected()) {
                            exchange.getIn().setHeader(B2bmbCamelConstants.FUNCT_ACK_REJECTED_INDICATOR, "N");
                            if ("true".equals(getEndpoint().getSplitTransactions())) {
                                splitAndMapMainDocuments(exchange, attachment, to, from, subject, ediProfile);
                            } else {
                                mapMainDocument(exchange, attachment, to, from, subject, ediProfile);
                            }
                        } else  {
                            exchange.getIn().setHeader(B2bmbCamelConstants.FUNCT_ACK_REJECTED_INDICATOR, "Y");
                        }
                    } else {
                        if ("true".equals(getEndpoint().getSplitTransactions())) {
                            splitAndMapMainDocuments(exchange, attachment, to, from, subject, ediProfile);
                        } else {
                            mapMainDocument(exchange, attachment, to, from, subject, ediProfile);
                        }
                    }
                } finally {
                    if(file != null) {
                        file.close();
                    }

                    if(file997 != null) {
                        file997.close();
                    }
                }
            }
            if (mailboxEntry != null && processedMailbox != null) {
                mailboxEntry.setMailboxId(processedMailbox.getId());
                getEndpoint().getMailboxEntryDAO().save(mailboxEntry);
            }
        } finally {
            if(message != null) {
                message.close();
            }
        }
    }

    private FunctionalAckResult checkX12FunctionalAck(Exchange exchange,
        EDIProfile ediProfile, InputStream file, String fileName, Attachment attachment)
            throws IOException,MapForceServerException, B2BNotFoundException, B2BTransactionFailed,
            InEDIException, TooManyListenersException, ValidationException, NoSuchHeaderException,
            BlobStoreException
    {
        InputStream file997 = null;
        Edi997Checker checker = new Edi997Checker();

        String map997Url = getEndpoint().getServerUrl() + "/service/" +
                ediProfile.getMapName() + "997";
        file997 = callMapForceServer(exchange, map997Url, file, fileName,attachment);
        String ackFile = IOUtils.toString(file997);
        FunctionalAckResult result = new FunctionalAckResult();
        result.setWasRejected(checker.wasDocumentRejected(IOUtils.toInputStream(ackFile)));
        result.setInterchangeControlNumber(checker.getInterchangeControlNumber());
        result.setAckFile(IOUtils.toInputStream(ackFile));

        return result;
    }


    private FunctionalAckResult checkEDIFACTFunctionalAck(EDIProfile ediProfile,
        InputStream file, String fileName, String dataDomain) throws B2BTransactionFailed,
        B2BNotFoundException,TooManyListenersException, InEDIException, IOException{
        InputStream file997 = null;
        InputStream validationFile = null;
        EDIFACTParser parser = (EDIFACTParser) SpringApplicationContext.getBean("edifactParser");

        if(parser == null) {
            throw new IllegalStateException("No spring bean named: edifactParser was found in Spring Context.");
        }

        if(ediProfile.getSchemaFileName() == null || ediProfile.getSchemaFileName().equals("")) {
            throw new B2BNotFoundException("No schema file name has be set for ediProfile " +
                    ediProfile.getRefName());
        }

        String filePath = "schemas/" + ediProfile.getSchemaFileName();


        FileSystemEntry fileSystemEntry = getEndpoint().getFileSystemEntryDAO().
                getByRefName(filePath, dataDomain);
        if (fileSystemEntry == null) {
            throw new B2BNotFoundException("Unable to resolve file path with data domain " +
                    dataDomain + " and file path " + filePath);
        }

        Blob fileContents = getEndpoint().getBlobStore().getBlobByStringId(
                fileSystemEntry.getBlobId());

        if (fileContents == null) {
            throw new B2BNotFoundException("Unable to find blob for id:" + fileSystemEntry.getBlobId());
        }
        validationFile = fileContents.getInputStream();

        String controlFile = parser.buildControlResponse(file, fileName, ediProfile,
                validationFile);
        file997 = IOUtils.toInputStream(controlFile);

        boolean isRejected = !parser.isDocumentValid();
        FunctionalAckResult result = new FunctionalAckResult();
        result.setWasRejected(isRejected);
        result.setInterchangeControlNumber(parser.getInterchangeControlNumber());
        result.setAckFile(file997);

        return result;
    }

    private void splitAndMapMainDocuments(Exchange exchange, Attachment attachment, String to, String from,
        String subject, EDIProfile ediProfile)  throws B2BTransactionFailed, IOException, ValidationException,
        B2BNotFoundException, BlobStoreException, NoSuchHeaderException, TooManyListenersException, InEDIException,
        MapForceServerException {

        Iterator<String> mailboxEntryIds = null;
        String fileType = exchange.getIn().getHeader(B2bmbCamelConstants.FILE_TYPE, String.class);
        List<String>  mappedMailboxEntryIds = new ArrayList<String>();

        Mailbox mailbox = getEndpoint().getMailboxDAO().getByRefName(getEndpoint().getMapProcessingMailboxName(),
                getEndpoint().getDomain());

        if(B2bmbCamelConstants.FILE_TYPE_X12.equals(fileType)) {
            EDITransactionReader reader = new EDITransactionReader();
            mailboxEntryIds = reader.getTransactions(exchange.getIn().getBody(InputStream.class),
                    getEndpoint().getBlobStore(), getEndpoint().getMailboxEntryDAO(),
                    getEndpoint().getAvailableDocumentDAO(),
                    mailbox, exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID, String.class),
                    to, from);
        } else if (B2bmbCamelConstants.FILE_TYPE_EDIFACT.equals(fileType)) {
            EDIFACTTransactionReader reader = new EDIFACTTransactionReader();
            mailboxEntryIds = reader.getTransactions(exchange.getIn().getBody(InputStream.class),
                    getEndpoint().getBlobStore(), getEndpoint().getMailboxEntryDAO(),
                    getEndpoint().getAvailableDocumentDAO(),
                    mailbox, exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID, String.class),
                    to, from);
        } else {
            if(LOG.isErrorEnabled()) {
                LOG.error("An unknown file type has been sent for processing.");
            }

            throw new IllegalStateException("An unknown file type has been sent for processing.");
        }
        int mbeCount = 0;
        InputStream file = null;
        InputStream mappedFile = null;
        while(mailboxEntryIds.hasNext())  {

            try {
                String mbId = mailboxEntryIds.next();
                MailboxEntry mbe = getEndpoint().getMailboxEntryDAO().getById(mbId);

                if (mbe != null) {
                    int count = 0;
                    for (Attachment a : mbe.getAttachments()) {
                        String mapUrl = getEndpoint().getServerUrl() +
                                "/service/" + ediProfile.getMapName();
                        if("Y".equals(getEndpoint().getUseAfterEnvelopeMap())) {
                            if(ediProfile.getAfterEnvelopeMapName() == null ||
                                    ediProfile.getAfterEnvelopeMapName().equals("")) {
                                throw new B2BNotFoundException("UserAfterEnvelopeMap was " +
                                        "specified, but there was no " +
                                        "afterEnvelopeMap set in ediProfile:"+ediProfile.getRefName());
                            }

                            mapUrl = getEndpoint().getServerUrl() +
                                    "/service/" + ediProfile.getAfterEnvelopeMapName();
                        }

                        file = MailboxEntryHelper.getInputStreamFromAttachment(
                                a, getEndpoint().getBlobDAO());

                        mappedFile = callMapForceServer(exchange, mapUrl, file,
                                a.getFileName() + "-mapped" + "-" + count,
                                a);

                        MailboxEntry mappedMBE = MailboxEntryHelper.createMailboxEntry(exchange, mailbox,
                                getEndpoint().getBlobStore(), getEndpoint().getMailboxEntryDAO(),
                                to, from, subject, mappedFile, UUID.randomUUID() + "-mapped" + "-" +
                                        mbeCount + "-" + count);

                        mappedMailboxEntryIds.add(mappedMBE.getId());

                        count++;
                    }
                    mbeCount++;
                }

                exchange.getIn().setBody(mappedMailboxEntryIds.iterator(), Iterator.class);
            } finally {
                if(file != null) {
                    file.close();
                }

                if(mappedFile != null) {
                    mappedFile.close();
                }
            }
        }
    }



    private void mapMainDocument(Exchange exchange, Attachment attachment, String to, String from, String subject,
        EDIProfile ediProfile) throws B2BTransactionFailed, IOException, ValidationException, B2BNotFoundException,
        BlobStoreException,  NoSuchHeaderException, MapForceServerException {

        InputStream file = null;
        InputStream mainFile = null;

        try {
            file = MailboxEntryHelper.getInputStreamFromAttachment(
                    attachment, getEndpoint().getBlobDAO());

            String mapUrl = getEndpoint().getServerUrl() + "/service/" + ediProfile.getMapName();
            if("Y".equals(getEndpoint().getUseAfterEnvelopeMap())) {
                if(ediProfile.getAfterEnvelopeMapName() == null || ediProfile.getAfterEnvelopeMapName().equals("")) {
                    throw new B2BNotFoundException("UserAfterEnvelopeMap was specified, but there was no " +
                            "afterEnvelopeMap set in ediProfile:"+ediProfile.getRefName());
                }

                mapUrl = getEndpoint().getServerUrl() + "/service/" + ediProfile.getAfterEnvelopeMapName();
            }

            mainFile = callMapForceServer(exchange, mapUrl, file, attachment.getFileName(), attachment);

            Mailbox mailbox = getEndpoint().getMailboxDAO().getByRefName(getEndpoint().getMapProcessingMailboxName(),
                    getEndpoint().getDomain());
            MailboxEntry mailboxEntry = MailboxEntryHelper.createMailboxEntry(exchange, mailbox,
                    getEndpoint().getBlobStore(), getEndpoint().getMailboxEntryDAO(),
                    to, from, subject, mainFile, UUID.randomUUID() + "-mapped");

            String idAsBody = exchange.getIn().getHeader(B2bmbCamelConstants.SEND_ID_AS_BODY,

                    String.class);

            if ("true".equals(idAsBody)) {
                    exchange.getIn().setBody(mailboxEntry.getId(),
                            String.class);
            } else {
                InputStream mappedFile = null;
                try {
                    Attachment mainAttachment = mailboxEntry.getAttachments().get(0);
                    if (mainAttachment != null) {
                        mappedFile = MailboxEntryHelper.getInputStreamFromAttachment(
                                mainAttachment, getEndpoint().getBlobDAO());

                        exchange.getIn().setHeader(Exchange.FILE_NAME, mainAttachment.getFileName() + "." +
                                ediProfile.getOutputFileType());
                        //todo might be nice if I could call the mapper to find out what the new content type is??
                        exchange.getIn().setHeader(Exchange.CONTENT_TYPE,
                                ediProfile.getOutputFileType().equals("xml") ? "text/xml" : "text/plain");
                        exchange.getIn().setHeader(Exchange.FILE_LENGTH, mainAttachment.getFileSize());
                        exchange.getIn().setBody(mappedFile);
                    }
                } finally {
                    if(mappedFile != null) {
                        mappedFile.close();
                    }
                }
            }

            exchange.getIn().setHeader(B2bmbCamelConstants.FUNCT_ACK_REJECTED_INDICATOR, "N");
        } finally {
            if(file != null) {
                file.close();
            }

            if(mainFile != null) {
                mainFile.close();
            }
        }
    }

    private void processGenerated997(boolean wasRejected, Exchange exchange, MailboxEntry mailboxEntry997,
                                     InputStream mainDoc, String fileName, String to , String from,
                                     String interchangeControlNumber)
            throws B2BTransactionFailed, BlobStoreException, NoSuchHeaderException, B2BNotFoundException,
            TooManyListenersException, InEDIException,
            ValidationException, IOException {

        InputStream file997 = null;

        try {

            Attachment attachment997 = mailboxEntry997.getAttachments().get(0);
            if (attachment997 != null) {

                file997 = MailboxEntryHelper.getInputStreamFromAttachment(
                        attachment997, getEndpoint().getBlobDAO());

                if (wasRejected) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("997 was rejected from :" + from + ", to:" + to);
                    }

                    Mailbox mailbox = getMailbox(exchange, getEndpoint().getRejected997MailboxName());

                    Attachment docMain = createAttachment(mainDoc, fileName,
                            mailbox);

                    createMailboxEntryForRejectedDocument(mailbox, exchange, attachment997, docMain);

                    StringBuilder builder = new StringBuilder();
                    builder.append("A 997 generated for a document received  indicates one or more " +
                            "documents has been " +
                            "rejected . The email was addressed from:" + from + ", to:" + to + "\n");
                    builder.append("Please check the '" + mailbox.getRefName() + "' mailbox to see the 997 and " +
                            "follow the " +
                            "process per the trading partner to resolve the issue.  None of the documents have been " +
                            "processed and will need to be processed manually.");
                    builder.append("\n\nTransmission Id:" + exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID,
                            String.class));
                    builder.append("\n\nInterchange Control Number:" + interchangeControlNumber);

                    String subject = "Document received has rejections.";
                    String body = builder.toString();
                    String displayName = "Document with Interchange Control Number:"+ interchangeControlNumber +
                            " received has rejections.";
                    getEndpoint().getTaskHelper().notifyAndCreateTask(
                            "997-" + interchangeControlNumber + new Date(),displayName,
                            body, subject, CamelDataDomainHelper.getDataDomainFromExchange(exchange),
                            "997 Rejection", "fa-rejections", "Lindsay Karlowsky");
                    exchange.getIn().setHeader(B2bmbCamelConstants.FUNCT_ACK_REJECTED_INDICATOR, "Y");

                }
            }
        } finally {
            if(file997 != null) {
                file997.close();
            }
        }
    }


    private Mailbox getMailbox(Exchange exchange, String mailboxName) throws B2BTransactionFailed, ValidationException,
            B2BNotFoundException{
       String domain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);


        Mailbox mailbox = getEndpoint().getMailboxDAO().getByRefName(mailboxName,
                domain);
        if (mailbox == null) {
            Mailbox processedMailbox = new Mailbox();
            processedMailbox.setDataDomain(domain);
            processedMailbox.setRefName(mailboxName);
            processedMailbox.setMailboxRole(MailboxRole.INTERMEDIATE);
            processedMailbox.setSystemMailbox(true);
            mailbox = getEndpoint().getMailboxDAO().save(processedMailbox);
        }
        return mailbox;
    }


    private InputStream callMapForceServer(Exchange exchange, String url, InputStream file, String fileName,
                                           Attachment attachment)
            throws IOException, MapForceServerException, B2BNotFoundException, ValidationException,
            B2BTransactionFailed,NoSuchHeaderException, BlobStoreException
    {
        HttpClient client = HttpClientBuilder.create().build();
        String fullUrl = "http://";
        if("Y".equals(getEndpoint().getUseHttps())) {
            fullUrl = "https://";
        }

        fullUrl = fullUrl + url;
        HttpPost post = new HttpPost(fullUrl);
        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
        builder.addBinaryBody("file", file, ContentType.DEFAULT_TEXT, fileName);

        if(LOG.isInfoEnabled()) {
            LOG.info("Sending mapping request to MapForce Server, url is:"+fullUrl);
        }
        HttpEntity entity = builder.build();
        post.setEntity(entity);
        HttpResponse response = client.execute(post);
        if(response.getStatusLine().getStatusCode() == 200) {

            if(LOG.isInfoEnabled()) {
                LOG.info("Mapping request to MapForce Server url:"+fullUrl+" was successful. Response was:"+
                        response.getStatusLine().getStatusCode());
            }
            InputStream inputStream = doExtractResponseBodyAsStream(response.getEntity().getContent(),
                    exchange);
            return inputStream;
        } else {
            if(LOG.isErrorEnabled()) {
                LOG.error("There was a problem generating map from MapForceServer at:" +
                        url + "The status code was:" + response.getStatusLine().getStatusCode() + ", reason:"+
                        response.getStatusLine().getReasonPhrase());
            }
            createMapForceServerErrorMailboxEntry(exchange,attachment);
            throw new MapForceServerException("There was a problem generating map from MapForceServer at:"+
            url+"The status code was:"+response.getStatusLine().getStatusCode()+ ", reason:"+
                    response.getStatusLine().getReasonPhrase());


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
        mailboxEntry.setRefName(UUID.randomUUID()+"-Rejected");

        getEndpoint().getMailboxEntryDAO().save(mailboxEntry);
    }


    private static InputStream doExtractResponseBodyAsStream(InputStream is, Exchange exchange) throws IOException {
        // As httpclient is using a AutoCloseInputStream, it will be closed when the connection is closed
        // we need to cache the stream for it.
        CachedOutputStream cos = null;
        try {
            // This CachedOutputStream will not be closed when the exchange is onCompletion
            cos = new CachedOutputStream(exchange, false);
            IOHelper.copy(is, cos);
            // When the InputStream is closed, the CachedOutputStream will be closed
            return cos.getWrappedInputStream();
        } catch (IOException ex) {
            // try to close the CachedOutputStream when we get the IOException
            try {
                cos.close();
            } catch (IOException ignore) {
                //do nothing here
            }
            throw ex;
        } finally {
            IOHelper.close(is, "Extracting response body", LOG);
        }
    }

    private void createMapForceServerErrorMailboxEntry(Exchange exchange, Attachment attachment) throws
            NoSuchHeaderException, BlobStoreException, ValidationException, B2BTransactionFailed,
            B2BNotFoundException {

        String to = getEndpoint().getTo() != null ? getEndpoint().getTo()
                : ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class);
        String from = getEndpoint().getFrom() != null ? getEndpoint().getFrom()
                : ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class);

        String subject = getEndpoint().getSubject() != null ? getEndpoint().getSubject()
                : ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.SUBJECT, String.class);
        String fileType = exchange.getIn().getHeader(B2bmbCamelConstants.FILE_TYPE, String.class);
        String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);
        InputStream file = MailboxEntryHelper.getInputStreamFromAttachment(
                attachment, getEndpoint().getBlobDAO());
        Mailbox mailbox = getMailbox(exchange, getEndpoint().getMapServerErrorMailboxName());
        MailboxEntry mailboxEntry = MailboxEntryHelper.createMailboxEntry(exchange, mailbox,
                getEndpoint().getBlobStore(), getEndpoint().getMailboxEntryDAO(),to
                ,from, subject, file, UUID.randomUUID() + "-map-server-error");
    }

    private Attachment createAttachment(InputStream inputStream, String fileName, Mailbox mailbox)
            throws BlobStoreException {
        Attachment att = new Attachment();
        String newFileName =  UUID.randomUUID() +"_"+ fileName;
        String path = mailbox.getDataDomain().replace(".", "/") + "/" + mailbox.getRefName() + "/" +
                newFileName;
        BlobMetaData metaData = getEndpoint().getBlobStore().createMetaDataObject();
        metaData.setDataDomain(mailbox.getDataDomain());

        metaData.setPathString(path);
        metaData.setTxId(SecureSession.getTxId());

        String refName = newFileName.replaceAll("\\s", "");

        Blob blob = getEndpoint().getBlobStore()
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

    private EDIProfile getEDIProfile(String fromAddress, String toAddress, Exchange exchange)
            throws B2BNotFoundException, IOException,
            B2BTransactionFailed, NoSuchHeaderException, TooManyListenersException, InEDIException {
        String docType = null;
        try {
            docType = DocumentTypeHelper.getDocumentType(exchange);
            if (docType != null) {
                exchange.getIn().setHeader(B2bmbCamelConstants.DOCUMENT_TYPE, docType);
            }
        } catch(MailboxRouterNotFoundException e) {

            if(LOG.isWarnEnabled()) {
                LOG.warn("An unrecognized document has enter the system and mailbox router" +
                        "has not been found.", e.getMessage());
            }
        }


        return getEndpoint().getEdiProfileDAO().getEDIProfile(fromAddress, toAddress, docType);
    }

    @Override
    public B2bmbMapForceServerEndpoint getEndpoint() {
        return (B2bmbMapForceServerEndpoint) super.getEndpoint();
    }

    private class FunctionalAckResult {
        private boolean wasRejected = false;
        private String interchangeControlNumber = null;
        private InputStream ackFile = null;

        public InputStream getAckFile() {
            return ackFile;
        }

        public void setAckFile(InputStream ackFile) {
            this.ackFile = ackFile;
        }

        public String getInterchangeControlNumber() {
            return interchangeControlNumber;
        }

        public void setInterchangeControlNumber(String interchangeControlNumber) {
            this.interchangeControlNumber = interchangeControlNumber;
        }

        public boolean isWasRejected() {
            return wasRejected;
        }

        public void setWasRejected(boolean wasRejected) {
            this.wasRejected = wasRejected;
        }
    }

}
