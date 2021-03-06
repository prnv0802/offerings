package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.dao.EDIProfileDAO;
import com.eis.b2bmb.api.v1.dao.MSIProfileDAO;
import com.eis.core.api.v1.dao.FileSystemEntryDAO;
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
 * Camel endpoint for FileSystemEntry
 * @author sudhakars
 */
public class B2bmbB2BProfileDirectoryEndpoint extends DefaultPollingEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbB2BProfileDirectoryEndpoint.class);
    private String regexFilter;
    private String contentType;
    private String refName;
    private String searchDirectoryList = "N";
    private String b2bProfileType;
    private String successDirectory = ".processed";
    private String errorDirectory = ".errored";
    private String b2bDirectoryLocation;
    private String fromVendor;
    private String toVendor;
    private String filePath;
    private String domain;
    private FileSystemEntryDAO fileSystemEntryDAO;
    private EDIProfileDAO ediProfileDAO;
    private MSIProfileDAO msiProfileDAO;
    private BlobStore blobStore;

    private IdempotentRepository<String> inProgressRepository = new MemoryIdempotentRepository();

    /**
     * Contructor
     * @param endpointUri endpoint uri
     * @param component component
     */
    public B2bmbB2BProfileDirectoryEndpoint(String endpointUri, B2bmbB2BProfileDirectoryComponent component) {
        super(endpointUri, component);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("FileSystemEndpoint createConsumer :: contentType :: " + contentType + " regexFilter :: " +
                    regexFilter);
        }
        B2bmbB2BProfileDirectoryConsumer consumer = new B2bmbB2BProfileDirectoryConsumer(this, processor);
        // ScheduledPollConsumer default delay is 500 millis, override with a new default value.
        // End user can override this value by providing a consumer.delay parameter
        consumer.setDelay(consumer.DEFAULT_CONSUMER_DELAY);
        configureConsumer(consumer);
        consumer.setMaxMessagesPerPoll(consumer.DEFAULT_MAX_MESSAGES_PER_POLL);
        return consumer;
    }

    @Override
    public Producer createProducer() throws Exception {
        return null;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Return the filePath
     * @return file path
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Set the filePath
     * @param filePath path for file
     */
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    /**
     * Get the data domain
     * @return domain data domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Set the data domain
     * @param domain data domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Get the regex filter for refname
     * @return regexFilter
     */
    public String getRegexFilter() {
        return regexFilter;
    }

    /**
     * Set the regex filter for refname
     * @param regexFilter regex filter
     */
    public void setRegexFilter(String regexFilter) {
        this.regexFilter = regexFilter;
    }

    /**
     * Get the refname
     * @return refName refName in uri
     */
    public String getRefName() {
        return refName;
    }

    /**
     * Set the refname
     * @param refName refName in uri
     */
    public void setRefName(String refName) {
        this.refName = refName;
    }

    /**
     * Set the contentType
     * @return contentType
     */
    public String getContentType() {
        return contentType;
    }

    /**
     * Get the contentType
     * @param contentType contentType in uri
     */
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Get the name of directory to move files to if route is successful
     * Default is .processed
     * @return directory name
     */
    public String getSuccessDirectory() {
        return successDirectory;
    }

    /**
     * Get the name of directory to move files to if route is successful
     * @param successDirectory directory name
     */
    public void setSuccessDirectory(String successDirectory) {
        this.successDirectory = successDirectory;
    }

    /**
     * Get the name of directory to move files to if route is unsuccessful
     * Default is .errored
     * @return directory name
     */
    public String getErrorDirectory() {
        return errorDirectory;
    }

    /**
     * Set the name of directory to move files to if route is unsuccessful
     * @param errorDirectory directory name
     */
    public void setErrorDirectory(String errorDirectory) {
        this.errorDirectory = errorDirectory;
    }

    /**
     * An indicator Y or N that indicates if the poll will be from several directories passed in
     * as a Camel Header : B2BDirectoryList.  If this is set to Y and there is a B2BDirectoryList
     * passed in, the file path on the uri will not be used and it will loop over the list of
     * values in the B2BDirectoryList to find files.
     *
     * @return - indicator Y or N that indicates if we will be polling a list of directories
     */
    public String getSearchDirectoryList() {
        return searchDirectoryList;
    }

    /**
     * Sets an indicator Y or N that indicates if the poll will be from several directories passed in
     * as a Camel Header : B2BDirectoryList.  If this is set to Y and there is a B2BDirectoryList
     * passed in, the file path on the uri will not be used and it will loop over the list of
     * values in the B2BDirectoryList to find files.
     *
     * @param searchDirectoryList - indicator Y or N that indicates if we will be polling a list of directories
     */
    public void setSearchDirectoryList(String searchDirectoryList) {
        this.searchDirectoryList = searchDirectoryList;
    }

    /**
     * Get the fileSystemEntryDAO
     * @return fileSystemEntryDAO
     */
    public FileSystemEntryDAO getFileSystemEntryDAO() {
        return fileSystemEntryDAO;
    }

    /**
     * Set the fileSystemEntryDAO
     * @param fileSystemEntryDAO fileSystemEntryDAO
     */
    public void setFileSystemEntryDAO(FileSystemEntryDAO fileSystemEntryDAO) {
        this.fileSystemEntryDAO = fileSystemEntryDAO;
    }

    /**
     * Set the ediProfileDAO.
     *
     * @return ediProfileDAO
     */
    public EDIProfileDAO getEdiProfileDAO() {
        return ediProfileDAO;
    }

    /**
     * Set the ediProfileDAO.
     *
     * @param ediProfileDAO ediProfileDAO
     */
    public void setEdiProfileDAO(EDIProfileDAO ediProfileDAO) {
        this.ediProfileDAO = ediProfileDAO;
    }

    /**
     * Get the msiProfileDAO.
     *
     * @return msiProfileDAO
     */
    public MSIProfileDAO getMsiProfileDAO() {
        return msiProfileDAO;
    }

    /**
     * Set the msiProfileDAO.
     *
     * @param msiProfileDAO msiProfileDAO
     */
    public void setMsiProfileDAO(MSIProfileDAO msiProfileDAO) {
        this.msiProfileDAO = msiProfileDAO;
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
     * Gets the B2BProfile directory location to pull files from.
     *
     * @return - directory location to pull files from.
     */
    public String getB2bDirectoryLocation() {
        return b2bDirectoryLocation;
    }

    /**
     * Sets the B2BProfile directory location to pull files from.
     *
     * @param b2bDirectoryLocation - directory location to pull files from.
     */
    public void setB2bDirectoryLocation(String b2bDirectoryLocation) {
        this.b2bDirectoryLocation = b2bDirectoryLocation;
    }

    /**
     * Returns what type of B2BProfile to look at MSI or EDI.
     *
     * @return B2BProfile type MSI or EDI
     */
    public String getB2bProfileType() {
        return b2bProfileType;
    }

    /**
     * Sets what type of B2BProfile to look at MSI or EDI.
     *
     * @param b2bProfileType - B2BProfile type MSI or EDI
     */
    public void setB2bProfileType(String b2bProfileType) {
        this.b2bProfileType = b2bProfileType;
    }

    /**
     * Gets the from vendor used in the B2B Profile.
     *
     * @return - fromVendor
     */
    public String getFromVendor() {
        return fromVendor;
    }


    /**
     * Gets the from vendor used in the B2B Profile.
     *
     * @param fromVendor - from vendor in B2BProfile
     */
    public void setFromVendor(String fromVendor) {
        this.fromVendor = fromVendor;
    }

    /**
     * Gets the to vendor used in the B2B Profile.
     *
     * @return - toVendor
     */
    public String getToVendor() {
        return toVendor;
    }

    /**
     * Gets the to vendor used in the B2B Profile.
     *
     * @param toVendor - to vendor in B2BProfile
     */
    public void setToVendor(String toVendor) {
        this.toVendor = toVendor;
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
        return "B2bmbFileSystemEndpoint{" +
                "regexFilter='" + regexFilter + '\'' +
                ", contentType='" + contentType + '\'' +
                ", refName='" + refName + '\'' +
                ", successDirectory='" + successDirectory + '\'' +
                ", errorDirectory='" + errorDirectory + '\'' +
                ", filePath='" + filePath + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }
}
