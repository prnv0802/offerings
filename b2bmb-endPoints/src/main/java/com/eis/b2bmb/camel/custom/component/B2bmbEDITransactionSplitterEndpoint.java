
package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.dao.AvailableDocumentDAO;
import com.eis.core.api.v1.dao.BlobDAO;
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
 * @author harjeets
 */
public class B2bmbEDITransactionSplitterEndpoint extends DefaultPollingEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(B2bmbEDITransactionSplitterEndpoint.class);
    private String to;
    private String from;
    private String subject;
    private String domain;
    private String idAsBody;
    private String sortMail;
    private String mailboxName;
    private String errorMailboxName = "Errors";
    private String processedMailboxName = "Processed";

    private MailboxEntryDAO mailboxEntryDAO;
    private MailboxDAO mailboxDAO;
    private BlobDAO blobDAO;
    private BlobStore blobStore;
    private AvailableDocumentDAO availableDocumentDAO;

    private IdempotentRepository<String> inProgressRepository = new MemoryIdempotentRepository();

    /**
     * @param endpointUri endpoint Uri
     * @param component   component instance
     */
    public B2bmbEDITransactionSplitterEndpoint(String endpointUri, B2bmbEDITransactionSplitterComponent component) {
        super(endpointUri, component);
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating B2bmbEDITransactionSplitterEndpoint  ");
        }
    }

    /**
     * creates the producer object
     *
     * @return {@link org.apache.camel.Producer}
     * @throws Exception thrown by camel runtime system
     */
    public Producer createProducer() throws Exception {
        return new B2bmbEDITransactionSplitterProducer(this);

    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        System.out.println(">>>>>>>>>>>>>>>>>>"+processor.toString());
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

    /**
     * Get to user
     * @return to user
     */
    public String getTo() {
        return to;
    }

    /**
     * Set to user
     * @param to user
     */
    public void setTo(String to) {
        this.to = to;
    }

    /**
     * Get from user
     * @return from user
     */
    public String getFrom() {
        return from;
    }

    /**
     * Set from user
     * @param from user
     */
    public void setFrom(String from) {
        this.from = from;
    }

    /**
     * Get domain
     * @return domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Set domain
     * @param domain dataDomain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Get mailbox name
     * @return mailbox name
     */
    public String getMailboxName() {
        return mailboxName;
    }

    /**
     * Set mailbox name
     * @param mailboxName mailbox refName
     */
    public void setMailboxName(String mailboxName) {
        this.mailboxName = mailboxName;
    }

    /**
     * Get subject
     * @return subject
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Set subject
     * @param subject subject for message
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Get error mailbox name
     * @return error mailbox name
     */
    public String getErrorMailboxName() {
        return errorMailboxName;
    }

    /**
     * Set error mailbox name
     * @param errorMailboxName error mailbox name
     */
    public void setErrorMailboxName(String errorMailboxName) {
        this.errorMailboxName = errorMailboxName;
    }

    /**
     * Get processed mailbox name
     * @return processed mailbox name
     */
    public String getProcessedMailboxName() {
        return processedMailboxName;
    }

    /**
     * Set processed mailbox name
     * @param processedMailboxName processed mailbox name
     */
    public void setProcessedMailboxName(String processedMailboxName) {
        this.processedMailboxName = processedMailboxName;
    }

    /**
     * Get parameter that indicates if mail should be sorted by email addresses and
     * sequence number.
     * @return 'true' if we want to sort
     */
    public String getSortMail() {
        return sortMail;
    }

    /**
     * Sets parameter that indicates if mail should be sorted by email addresses and
     * sequence number.
     * @param sortMail -  'true' if we want to sort
     */
    public void setSortMail(String sortMail) {
        this.sortMail = sortMail;
    }

    /**
     * Gets the parameter that indicates if we just want the mailbox entry id as the
     * body of the message.
     *
     * @return 'true' if we want just the id in the body
     */
    public String getIdAsBody() {
        return idAsBody;
    }

    /**
     * Gets the parameter that indicates if we just want the mailbox entry id as the
     * body of the message.
     *
     * @param idAsBody 'true' if we want just the id in the body
     */
    public void setIdAsBody(String idAsBody) {
        this.idAsBody = idAsBody;
    }

    /**
     * Get mailboxEntryDAO
     * @return mailboxEntryDAO
     */
    public MailboxEntryDAO getMailboxEntryDAO() {
        return mailboxEntryDAO;
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
     * Set mailboxEntryDAO
     * @param mailboxEntryDAO mailboxEntryDAO
     */
    public void setMailboxEntryDAO(MailboxEntryDAO mailboxEntryDAO) {
        this.mailboxEntryDAO = mailboxEntryDAO;
    }

    /**
     * Get mailboxDAO
     * @return mailboxDAO
     */
    public MailboxDAO getMailboxDAO() {
        return mailboxDAO;
    }

    /**
     * Set mailboxDAO
     * @param mailboxDAO mailboxDAO
     */
    public void setMailboxDAO(MailboxDAO mailboxDAO) {
        this.mailboxDAO = mailboxDAO;
    }

    /**
     * Get blobDAO
     * @return blobDAO
     */
    public BlobDAO getBlobDAO() {
        return blobDAO;
    }

    /**
     * Set blobDAO
     * @param blobDAO blobDAO
     */
    public void setBlobDAO(BlobDAO blobDAO) {
        this.blobDAO = blobDAO;
    }

    /**
     * Get blobStore
     * @return blobStore
     */
    public BlobStore getBlobStore() {
        return blobStore;
    }

    /**
     * Set blobStore
     * @param blobStore blobStore
     */
    public void setBlobStore(BlobStore blobStore) {
        this.blobStore = blobStore;
    }

    /**
     * Get the repository for mailbox entries in process of being sent
     * @return inProgressRepository
     */
    public IdempotentRepository<String> getInProgressRepository() {
        return inProgressRepository;
    }

    /**
     * Set the repository for mailbox entries in process of being sent
     * @param inProgressRepository repository
     */
    public void setInProgressRepository(IdempotentRepository<String> inProgressRepository) {
        this.inProgressRepository = inProgressRepository;
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

    @Override
    public String toString() {
        return "B2bmbMailboxEndpoint{" +
                "to='" + to + '\'' +
                ", from='" + from + '\'' +
                ", subject='" + subject + '\'' +
                ", domain='" + domain + '\'' +
                ", mailboxName='" + mailboxName + '\'' +
                ", errorMailboxName='" + errorMailboxName + '\'' +
                ", processedMailboxName='" + processedMailboxName + '\'' +
                ", sortMail='" + sortMail + '\'' +
                ", idAsBody='" + idAsBody + '\'' +
                '}';
    }
}
