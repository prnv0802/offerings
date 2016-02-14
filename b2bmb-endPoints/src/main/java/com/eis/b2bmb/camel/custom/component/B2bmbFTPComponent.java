package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.dao.CommunicationConfigurationDAO;
import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import org.apache.camel.CamelContext;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.component.file.GenericFileEndpoint;
import org.apache.camel.component.file.remote.FtpComponent;
import org.apache.camel.component.file.remote.FtpConfiguration;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Camel component for AS2
 * User: harjeets
 */
public class B2bmbFTPComponent extends FtpComponent {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbFTPComponent.class);

    /**
     * Constructor.
     */
    public B2bmbFTPComponent() {
        setEndpointClass(B2bmbFTPEndpoint.class);
    }

    /**
     * Constructor.
     *
     * @param context - CamelContext
     */
    public B2bmbFTPComponent(CamelContext context) {
        super(context);
        setEndpointClass(B2bmbSFTPEndpoint.class);
    }

    /**
     * Communication Configuration
     */
    @Autowired
    CommunicationConfigurationDAO communicationConfigurationDAO;

    /**
     * FileSystemEntryDAO
     */
    @Autowired
    FileSystemEntryDAO fileSystemEntryDAO;

    
    @Override
    protected void validateURI(String uri, String path, Map parameters)
            throws ResolveEndpointFailedException {
        //super.validateURI(uri, path, parameters);

    }

    /**
     * @param uri        endpoint uri
     * @param remaining  string of uri ,after //
     * @param parameters map of parameters
     * @return endpoint created endpoint
     * @throws Exception exception thrown by camel runtime system
     */
    protected GenericFileEndpoint<FTPFile> buildFileEndpoint(String uri, String remaining, Map<String,
            Object> parameters) throws Exception {

        FtpConfiguration ftpConfiguration = new FtpConfiguration();
        ftpConfiguration.setDirectory("");
        ftpConfiguration.setHost("");
        ftpConfiguration.setPort(0);
        ftpConfiguration.setUsername("");
        ftpConfiguration.setPassword("");
        B2bmbFTPEndpoint endpoint = new B2bmbFTPEndpoint(uri, this, ftpConfiguration);
        endpoint.setDataDomain(remaining.substring(0, remaining.indexOf('/')));
        setProperties(endpoint, parameters);
        return endpoint;
    }

    /**
     * Returns the Communication Configuration DAO.
     *
     * @return communciation configuration dao
     */
    public CommunicationConfigurationDAO getCommunicationConfigurationDAO() {
        return communicationConfigurationDAO;
    }

    /**
     * Sets the communication configuration DAO.
     *
     * @param communicationConfigurationDAO - communication configuration dao
     */
    public void setCommunicationConfigurationDAO(CommunicationConfigurationDAO communicationConfigurationDAO) {
        this.communicationConfigurationDAO = communicationConfigurationDAO;
    }

    /**
     * Returns the Communication Configuration DAO.
     *
     * @return communciation configuration dao
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
}
