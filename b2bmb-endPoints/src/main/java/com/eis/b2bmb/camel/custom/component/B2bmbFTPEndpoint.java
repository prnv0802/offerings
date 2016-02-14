package com.eis.b2bmb.camel.custom.component;


import com.eis.b2bmb.api.v1.dao.CommunicationConfigurationDAO;
import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFileProducer;
import org.apache.camel.component.file.remote.FtpConfiguration;
import org.apache.camel.component.file.remote.FtpEndpoint;
import org.apache.camel.component.file.remote.FtpOperations;
import org.apache.camel.component.file.remote.RemoteFileConsumer;
import org.apache.camel.processor.idempotent.MemoryIdempotentRepository;
import org.apache.camel.spi.IdempotentRepository;
import org.apache.camel.util.ServiceHelper;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;


/**
 * Endpoint for B2B SFTP Component.
 *
 * User: tcostanzo
 */
public class B2bmbFTPEndpoint extends FtpEndpoint {
    private static final Logger LOG = LoggerFactory.getLogger(B2bmbFTPEndpoint.class);
    private String dataDomain;
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
    public B2bmbFTPEndpoint(String endpointUri, B2bmbFTPComponent component, FtpConfiguration configuration) {
        super(endpointUri, component, configuration);
        this.communicationConfigurationDAO = component.getCommunicationConfigurationDAO();
        this.fileSystemEntryDAO = component.getFileSystemEntryDAO();

        if (LOG.isDebugEnabled()) {
            LOG.debug("creating B2bmbRemoteFileEndpoint  ");
        }
    }

    @Override
    public FtpConfiguration getConfiguration() {
        if(this.configuration == null) {
            this.configuration = getDefaultFtpConfiguration();
        }

        return (FtpConfiguration) this.configuration;
    }


    private FtpConfiguration getDefaultFtpConfiguration()  {
        FtpConfiguration ftpConfiguration = new FtpConfiguration();
        ftpConfiguration.setDirectory("");
        ftpConfiguration.setHost("");
        ftpConfiguration.setPort(0);
        ftpConfiguration.setUsername("");
        ftpConfiguration.setPassword("");
        return ftpConfiguration;
    }
    /**
     * creates the producer object
     *
     * @return {@link org.apache.camel.Producer}
     * @throws Exception thrown by camel runtime system
     */
    public GenericFileProducer<FTPFile> createProducer() throws Exception {
        return new B2bmbFTPProducer(this, createRemoteFileOperations());

    }

    @Override
    public RemoteFileConsumer<FTPFile> createConsumer(Processor processor) throws Exception {
        return new B2bmbFTPConsumer(this, processor, createRemoteFileOperations());
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
     * Set the DAO implementation to use
     *
     * @param communicationConfigurationDAO the dao
     */
    public void setCommunicationConfigurationDAO(CommunicationConfigurationDAO communicationConfigurationDAO) {
        this.communicationConfigurationDAO = communicationConfigurationDAO;
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
     * Creates remote file operations for ftp communication.
     *
     * @return - FTP Operation
     * @throws Exception - if there is a problem
     */
    public FtpOperations createRemoteFileOperations() throws Exception {
        // configure ftp client
        FTPClient client = ftpClient;

        if (client == null) {
            // must use a new client if not explicit configured to use a custom client
            client = createFtpClient();
        }

        // set any endpoint configured timeouts
        if (getConfiguration().getConnectTimeout() > -1) {
            client.setConnectTimeout(getConfiguration().getConnectTimeout());
        }
        if (getConfiguration().getSoTimeout() > -1) {
            soTimeout = getConfiguration().getSoTimeout();
        }
        dataTimeout = getConfiguration().getTimeout();

        // then lookup ftp client parameters and set those
        if (ftpClientParameters != null) {
            Map<String, Object> localParameters = new HashMap<String, Object>(ftpClientParameters);
            // setting soTimeout has to be done later on FTPClient (after it has connected)
            Object timeout = localParameters.remove("soTimeout");
            if (timeout != null) {
                soTimeout = getCamelContext().getTypeConverter().convertTo(int.class, timeout);
            }
            // and we want to keep data timeout so we can log it later
            timeout = localParameters.remove("dataTimeout");
            if (timeout != null) {
                dataTimeout = getCamelContext().getTypeConverter().convertTo(int.class, dataTimeout);
            }
            setProperties(client, localParameters);
        }

        if (ftpClientConfigParameters != null) {
            // client config is optional so create a new one if we have parameter for it
            if (ftpClientConfig == null) {
                ftpClientConfig = new FTPClientConfig();
            }
            Map<String, Object> localConfigParameters = new HashMap<String, Object>(ftpClientConfigParameters);
            setProperties(ftpClientConfig, localConfigParameters);
        }

        if (dataTimeout > 0) {
            client.setDataTimeout(dataTimeout);
        }

        if (log.isDebugEnabled()) {
            log.debug("Created FTPClient [connectTimeout: {}, soTimeout: {}, dataTimeout: {}]: {}",
                    new Object[]{client.getConnectTimeout(), getSoTimeout(), dataTimeout, client});
        }

        FtpOperations operations = new FtpOperations(client, getFtpClientConfig());
        operations.setEndpoint(this);
        return operations;
    }

    /**
     * Returns the FTP Client.
     *
     * @return - FTP Client
     * @throws Exception - if there is a problem creating the FTP Client
     */
    protected FTPClient createFtpClient() throws Exception {
        return new FTPClient();
    }

    @Override
    public String getScheme() {
        return "ftp";
    }

}
