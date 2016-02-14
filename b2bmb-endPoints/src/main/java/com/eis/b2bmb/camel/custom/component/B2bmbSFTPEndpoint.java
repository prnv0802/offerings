package com.eis.b2bmb.camel.custom.component;


import com.eis.b2bmb.api.v1.dao.CommunicationConfigurationDAO;
import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Proxy;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFileProducer;
import org.apache.camel.component.file.remote.RemoteFileConsumer;
import org.apache.camel.component.file.remote.SftpConfiguration;
import org.apache.camel.component.file.remote.SftpEndpoint;
import org.apache.camel.component.file.remote.SftpOperations;
import org.apache.camel.processor.idempotent.MemoryIdempotentRepository;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.util.ServiceHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Endpoint for B2B SFTP Component.
 *
 * User: tcostanzo
 */
public class B2bmbSFTPEndpoint extends SftpEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(B2bmbSFTPEndpoint.class);
    private String dataDomain;
    Proxy proxy;

    private String vendor;
    private String b2bDirectoryLocation;

    private CommunicationConfigurationDAO communicationConfigurationDAO;

    private FileSystemEntryDAO fileSystemEntryDAO;

    private IdempotentRepository<String> inProgressRepository = new MemoryIdempotentRepository();

    /**
     * Endpoint for the B2B SFTP Component.
     *
     * @param endpointUri endpoint Uri
     * @param component   component instance
     * @param configuration sftp configuration
     */
    public B2bmbSFTPEndpoint(String endpointUri, B2bmbSFTPComponent component, SftpConfiguration configuration) {
        super(endpointUri, component, configuration);
        this.communicationConfigurationDAO = component.getCommunicationConfigurationDAO();
        this.fileSystemEntryDAO = component.getFileSystemEntryDAO();
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating B2bmbRemoteFileEndpoint  ");
        }
    }

    @Override
    public SftpConfiguration getConfiguration() {
        if(this.configuration == null) {
            this.configuration = getDefaultSftpConfiguration();
        }

        return (SftpConfiguration) this.configuration;
    }


    private SftpConfiguration getDefaultSftpConfiguration()  {
        SftpConfiguration sftpConfiguration = new SftpConfiguration();
        sftpConfiguration.setDirectory("");
        sftpConfiguration.setHost("");
        sftpConfiguration.setPort(0);
        sftpConfiguration.setUsername("");
        sftpConfiguration.setPassword("");
        return sftpConfiguration;
    }
    /**
     * creates the producer object
     *
     * @return {@link org.apache.camel.Producer}
     * @throws Exception thrown by camel runtime system
     */
    public GenericFileProducer<ChannelSftp.LsEntry> createProducer() throws Exception {
        return new B2bmbSFTPProducer(this, createRemoteFileOperations());

    }

    @Override
    public RemoteFileConsumer<ChannelSftp.LsEntry> createConsumer(Processor processor) throws Exception {
        return new B2bmbSFTPConsumer(this, processor, createRemoteFileOperations());
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
     * @return String domain
     */
    public String getDataDomain() {
        return dataDomain;
    }

    /**
     * Set the domain
     *
     * @param dataDomainx name
     */
    public void setDataDomain(String dataDomainx) {
        this.dataDomain = dataDomainx;
    }

    /**
     * Returns a string which identifies if the Communication Configurations should be filtered
     * by a specific vendor.
     *
     * @return - String identifying the vendor to filter by
     */
    public String getVendor() {
        return vendor;
    }

    /**
     * Sets a string which identifies if the Communication Configurations should be filtered
     * by a specific vendor.
     *
     * @param vendor - string identify the vendor to filter by
     */
    public void setVendor(String vendor) {
        this.vendor = vendor;
    }


    /**
     * Returns the String identifying which directory location from the Communication Configuration
     * should be used - inDirectoryName or outDirectoryName.
     *
     * @return - the String identifying which directory
     */
    public String getB2bDirectoryLocation() {
        return b2bDirectoryLocation;
    }

    /**
     * Sets the String identifying which directory location from the Communication Configuration
     * should be used - inDirectoryName or outDirectoryName.
     *
     * @param b2bDirectoryLocation - String identifying which directory location should be used
     */
    public void setB2bDirectoryLocation(String b2bDirectoryLocation) {
        this.b2bDirectoryLocation = b2bDirectoryLocation;
    }

    /**
     * Get the Communication Configuration DAO
     * @return the DAO
     */
    public CommunicationConfigurationDAO getCommunicationConfigurationDAO() {
        return communicationConfigurationDAO;
    }


    /**
     * Returns the File System Entry DAO.
     *
     * @return file system entry dao
     */
    public FileSystemEntryDAO getFileSystemEntryDAO() {
        return fileSystemEntryDAO;
    }

    /**
     * Sets the file System Entry DAO.
     *
     * @param fileSystemEntryDAO - file System Entry dao
     */
    public void setFileSystemEntryDAO(FileSystemEntryDAO fileSystemEntryDAO) {
        this.fileSystemEntryDAO = fileSystemEntryDAO;
    }

    /**
     * Set the DAO implementation to use
     *
     * @param communicationConfigurationDAO the dao
     */
    public void setCommunicationConfigurationDAO(CommunicationConfigurationDAO communicationConfigurationDAO) {
        this.communicationConfigurationDAO = communicationConfigurationDAO;
    }

    /**
     * Creates the SFTP Operations.
     *
     * @return  SFTP Operations
     */
    public SftpOperations createRemoteFileOperations() {
        SftpOperations operations = new SftpOperations(proxy);
        operations.setEndpoint(this);
        return operations;
    }

    @Override
    public String getScheme() {
        return "sftp";
    }

    /**
     * Sets the Proxy.
     *
     * @param proxy - Proxy object
     */
    public void setProxy(Proxy proxy) {
        this.proxy = proxy;
    }
}
