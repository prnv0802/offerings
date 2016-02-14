package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.dao.AvailableDocumentDAO;
import com.eis.b2bmb.api.v1.dao.EDIProfileDAO;
import com.eis.core.common.NotifyAndCreateTaskHelper;
import com.eis.core.api.v1.dao.BlobDAO;
import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import com.eis.core.api.v1.dao.MailboxDAO;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.api.v1.model.BlobStore;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultPollingEndpoint;
import org.apache.camel.processor.idempotent.MemoryIdempotentRepository;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camel endpoint for MapForceServer
 * @author sudhakars
 */
public class B2bmbMapForceServerEndpoint extends DefaultPollingEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbMapForceServerEndpoint.class);
    private String mapUri;
    private String serverUrl;
    private String map997Uri;
    private String generate997 = "N";
    private String to;
    private String from;
    private String subject;
    private String out997MailboxName = "edi-out";
    private String rejected997MailboxName = "validation-suspense";
    private String mapProcessingMailboxName = "map-processing";
    private String mapServerErrorMailboxName = "map-server-error";
    private String domain;
    private String errorMailboxName = "Errors";
    private String processedMailboxName = "Processed";
    private String splitTransactions;
    private String useHttps = "N";
    private String useAfterEnvelopeMap = "N";
    private MailboxEntryDAO mailboxEntryDAO;
    private MailboxDAO  mailboxDAO;
    private BlobDAO blobDAO;
    private BlobStore blobStore;
    private EDIProfileDAO ediProfileDAO;
    private FileSystemEntryDAO fileSystemEntryDAO;
    private AvailableDocumentDAO availableDocumentDAO;
    private NotifyAndCreateTaskHelper taskHelper;
    private IdempotentRepository<String> inProgressRepository = new MemoryIdempotentRepository();

    /**
     * @param endpointUri endpoint Uri
     * @param component   component instance
     */
    public B2bmbMapForceServerEndpoint(String endpointUri, B2bmbMapForceServerComponent component) {
        super(endpointUri, component);
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating B2bmbMailboxEndpoint  ");
        }
    }

    /**
     * creates the producer object
     *
     * @return {@link Producer}
     * @throws Exception thrown by camel runtime system
     */
    public Producer createProducer() throws Exception {
        return new B2bmbMapForceServerProducer(this);

    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("=======Processor=======  "+processor.toString());
        }
        return null;
    }


    /**
     * to create singleton  object of endpoint
     *
     * @return boolean
     */

    public boolean isSingleton() {
        return true;
    }



    @Override
    protected void doStart() throws Exception {
        ServiceHelper.startServices(inProgressRepository);
        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {
        super.doStop();
        ServiceHelper.stopServices(inProgressRepository);
    }

    /**
     * Get the to mail address for the mail.
     *
     * @return - String for to email address
     */
    public String getTo() {
        return to;
    }

    /**
     * Sets the to mail address for the mail.
     *
     * @param to - String for to mail address
     */
    public void setTo(String to) {
        this.to = to;
    }


    /**
     * Get the from mail address for the mail.
     *
     * @return - String for from email address
     */
    public String getFrom() {
        return from;
    }


    /**
     * Sets the useHttps mail address for the mail.
     *
     * @param useHttps - String for the useHttps mail address
     */
    public void setUseHttps(String useHttps) {
        this.useHttps = useHttps;
    }

    /**
     * Get the useHttps mail address for the mail.
     *
     * @return - String for useHttps email address
     */
    public String getUseHttps() {
        return useHttps;
    }

    /**
     * Sets the useHttps mail address for the mail.
     *
     * @param useAfterEnvelopeMap - String 'Y' or 'N' whether to use the after Envelope Map, if not it will user
     *                            regular map
     */
    public void setUseAfterEnvelopeMap(String useAfterEnvelopeMap) {
        this.useAfterEnvelopeMap = useAfterEnvelopeMap;
    }

    /**
     * Get the useAfterEnvelopeMap option - 'Y' or 'N'
     *
     * @return - String for useAfterEnvelopeMapOption
     */
    public String getUseAfterEnvelopeMap() {
        return useAfterEnvelopeMap;
    }


    /**
     * Sets the from mail address for the mail.
     *
     * @param from - String for the from mail address
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Returns the mail subject.
     *
     * @return - String for the mail subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the mail subject.
     *
     * @param subject - String for the mail subject
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the uri for the map on the map force server.
     *
     * @return String for the map uri
     */
    public String getMapUri() {
        return mapUri;
    }

    /**
     * Sets the uri for the map on the map force server.
     *
     * @param mapUri for the map uri
     */
    public void setMapUri(String mapUri) {
        this.mapUri = mapUri;
    }

    /**
     * Returns the map server url.
     *
     * @return String for the map force server url
     */
    public String getServerUrl() {
        return serverUrl;
    }

    /**
     * Setst the map force server url.
     *
     * @param serverUrl - String for the map force server url.
     */
    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    /**
     * Returns a string for the 997 Map MapForce uri.
     *
     * @return string for the 997 map uri
     */
    public String getMap997Uri() {
        return map997Uri;
    }

    /**
     * Sets the 997 Map uri for the map on map force server.
     *
     * @param map997Uri - string for 997 map url
     */
    public void setMap997Uri(String map997Uri) {
        this.map997Uri = map997Uri;
    }

    /**
     * Get the refName of the mailbox where the 997 will be placed.
     *
     * @return - the refName of the mailbox where the 997 will be placed
     */
    public String getOut997MailboxName() {
        return out997MailboxName;
    }

    /**
     * Sets the refName of the mailbox where the 997 will be placed.
     *
     * @param out997MailboxName - the refName of the mailbox where the 997 will be placed
     */
    public void setOut997MailboxName(String out997MailboxName) {
        this.out997MailboxName = out997MailboxName;
    }


    /**
     * Get the refName of the mailbox where the 997 rejections will be placed.
     *
     * @return - the refName of the mailbox where the 997 rejections will be placed
     */
    public String getRejected997MailboxName() {
        return rejected997MailboxName;
    }

    /**
     * Sets the refName of the mailbox where the 997 rejections will be placed.
     *
     * @param rejected997MailboxName - the refName of the mailbox where the 997 rejections will be placed
     */
    public void setRejected997MailboxName(String rejected997MailboxName) {
        this.rejected997MailboxName = rejected997MailboxName;
    }

    /**
     * Get the refName of the mailbox where files will be placed while the map is processing.
     *
     * @return refName of the mailbox where files will be placed while map is processing
     */
    public String getMapProcessingMailboxName() {
        return mapProcessingMailboxName;
    }

    /**
     * Sets the refName of the mailbox where files will be placed while the map is processing.
     *
     * @param mapProcessingMailboxName refName of the mailbox where files will be placed while map is processing
     */
    public void setMapProcessingMailboxName(String mapProcessingMailboxName) {
        this.mapProcessingMailboxName = mapProcessingMailboxName;
    }

    /**
     * Get the refName of the mailbox where files will be placed when
     * there an error communication or error with map force server
     *
     * @return refName of the refName of the mailbox where files will be placed when
     * there an error communication or error with map force server
     */
    public String getMapServerErrorMailboxName() {
        return mapServerErrorMailboxName;
    }

    /**
     * Sets the refName of the mailbox where files will be placed when
     * there an error communication or error with map force server
     *
     * @param mapServerErrorMailboxName refName of the mailbox where files will be placed when
     *                                  there an error communication or error with map force server
     */
    public void setMapServerErrorMailboxName(String mapServerErrorMailboxName) {
        this.mapServerErrorMailboxName = mapServerErrorMailboxName;
    }

    /**
     * Returns the data domain of the route.
     *
     * @return - data domain of the route
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Sets the data domain of the route.
     *
     * @param domain - data domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Returns the name of the error mailbox.
     *
     * @return name of the error mailbox
     */
    public String getErrorMailboxName() {
        return errorMailboxName;
    }

    /**
     * Sets the name of the error mailbox.
     *
     * @param errorMailboxName - refName of the error mailbox
     */
    public void setErrorMailboxName(String errorMailboxName) {
        this.errorMailboxName = errorMailboxName;
    }

    /**
     * Gets the name of the processed mailbox.
     *
     * @return refName of processed mailbox
     */
    public String getProcessedMailboxName() {
        return processedMailboxName;
    }

    /**
     * Sets the name of the processed mailbox.
     *
     * @param processedMailboxName - refName of the processed mailbox
     */
    public void setProcessedMailboxName(String processedMailboxName) {
        this.processedMailboxName = processedMailboxName;
    }

    /**
     * Gets MailboxEntryDAO.
     *
     * @return MailboxEntryDAO
     */
    public MailboxEntryDAO getMailboxEntryDAO() {
        return mailboxEntryDAO;
    }

    /**
     * Sets the MailboxEntryDAO.
     *
     * @param mailboxEntryDAO - MailboxEntryDAO
     */
    public void setMailboxEntryDAO(MailboxEntryDAO mailboxEntryDAO) {
        this.mailboxEntryDAO = mailboxEntryDAO;
    }

    /**
     * Set availableDocumentDAO
     * @param availableDocumentDAO availableDocumentDAO
     */
    public void setAvailableDocumentDAO(AvailableDocumentDAO availableDocumentDAO) {
        this.availableDocumentDAO = availableDocumentDAO;
    }

    /**
     * Get availableDocumentDAO
     * @return availableDocumentDAO
     */
    public AvailableDocumentDAO getAvailableDocumentDAO() {
        return availableDocumentDAO;
    }

    /**
     * Gets the MailboxDAO.
     *
     * @return MailboxDAO
     */
    public MailboxDAO getMailboxDAO() {
        return mailboxDAO;
    }

    /**
     * Sets the MailboxDAO.
     * @param mailboxDAO - MailboxDAO.
     */
    public void setMailboxDAO(MailboxDAO mailboxDAO) {
        this.mailboxDAO = mailboxDAO;
    }

    /**
     * Returns the inProgressRepository.
     *
     * @return IdempotentRepository
     */
    public IdempotentRepository<String> getInProgressRepository() {
        return inProgressRepository;
    }

    /**
     * Sets the inProgressRepository.
     *
     * @param inProgressRepository -  IdempotentRepository
     */
    public void setInProgressRepository(IdempotentRepository<String> inProgressRepository) {
        this.inProgressRepository = inProgressRepository;
    }

    /**
     * Returns the BlobStore.
     *
     * @return BlobStore
     */
    public BlobStore getBlobStore() {
        return blobStore;
    }

    /**
     * Returns the BlobDAO.
     *
     * @return blobDAO
     */
    public BlobDAO getBlobDAO() {
        return blobDAO;
    }

    /**
     * Sets the blobDAO.
     *
     * @param blobDAO - blobDAO
     */
    public void setBlobDAO(BlobDAO blobDAO) {
        this.blobDAO = blobDAO;
    }

    /**
     * Sets the blobStore.
     *
     * @param blobStore - BlobStore
     */
    public void setBlobStore(BlobStore blobStore) {
        this.blobStore = blobStore;
    }

    /**
     * Returns the generate997 parameter which indicates if a 997 should be generated for the map.
     *
     * @return - String true or false which indicates if a 997 should be generated.
     */
    public String getGenerate997() {
        return generate997;
    }


    /**
     * Sets the generate997 parameter which indicates if a 997 should be generated for the map.
     *
     * @param generate997 - String true or false which indicates if a 997 should be generated.
     */
    public void setGenerate997(String generate997) {
        this.generate997 = generate997;
    }

    /**
     * Returns the TaskHelper.
     *
     * @return TaskHelper
     */
    public NotifyAndCreateTaskHelper getTaskHelper() {
        return taskHelper;
    }

    /**
     * Sets the taskHelper.
     *
     * @param taskHelper - TaskHelper
     */
    public void setTaskHelper(NotifyAndCreateTaskHelper taskHelper) {
        this.taskHelper = taskHelper;
    }

    /***
     * Returns the ediProfileDAO.
     *
     * @return EDIProfileDAO
     */
    public EDIProfileDAO getEdiProfileDAO() {
        return ediProfileDAO;
    }

    /**
     * Sets the ediProfileDAO.
     *
     * @param ediProfileDAO - ediProfileDAO
     */
    public void setEdiProfileDAO(EDIProfileDAO ediProfileDAO) {
        this.ediProfileDAO = ediProfileDAO;
    }


    /***
     * Returns the fileSystemEntryDAO.
     *
     * @return FileSystemEntryDAO
     */
    public FileSystemEntryDAO getFileSystemEntryDAO() {
        return fileSystemEntryDAO;
    }

    /**
     * Sets the fileSystemEntryDAO.
     *
     * @param fileSystemEntryDAO - fileSystemEntryDAOO
     */
    public void setFileSystemEntryDAO(FileSystemEntryDAO fileSystemEntryDAO) {
        this.fileSystemEntryDAO = fileSystemEntryDAO;
    }

    /**
     * Returns true or false if the transactions should be split.
     *
     * @return String true or false
     */
    public String getSplitTransactions() {
        return splitTransactions;
    }

    /**
     * Sets true or false if the transactions should be split.
     *
     * @param splitTransactions -  String true or false
     */
    public void setSplitTransactions(String splitTransactions) {
        this.splitTransactions = splitTransactions;
    }
}
