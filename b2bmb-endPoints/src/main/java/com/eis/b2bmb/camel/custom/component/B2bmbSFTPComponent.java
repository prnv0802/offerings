package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.dao.CommunicationConfigurationDAO;
import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import com.jcraft.jsch.ChannelSftp;
import org.apache.camel.CamelContext;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.component.file.GenericFileEndpoint;
import org.apache.camel.component.file.remote.SftpComponent;
import org.apache.camel.component.file.remote.SftpConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Camel component for AS2
 * User: harjeets
 */
public class B2bmbSFTPComponent extends SftpComponent {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbSFTPComponent.class);

    /**
     * Communication Configuration
     */
    @Autowired
    CommunicationConfigurationDAO communicationConfigurationDAO;

    /**
     * File System Entry DAO
     */
    @Autowired
    FileSystemEntryDAO fileSystemEntryDAO;

    /**
     * Constructor.
     */
    public B2bmbSFTPComponent() {
        setEndpointClass(B2bmbSFTPEndpoint.class);
    }

    /**
     * Constructor.
     *
     * @param context - CamelContext
     */
    public B2bmbSFTPComponent(CamelContext context) {
        super(context);
        setEndpointClass(B2bmbSFTPEndpoint.class);
    }



    
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
    protected GenericFileEndpoint<ChannelSftp.LsEntry> buildFileEndpoint(String uri, String remaining, Map<String,
            Object> parameters) throws Exception {

        SftpConfiguration sftpConfiguration = new SftpConfiguration();
        sftpConfiguration.setDirectory("");
        sftpConfiguration.setHost("");
        sftpConfiguration.setPort(0);
        sftpConfiguration.setUsername("");
        sftpConfiguration.setPassword("");
        B2bmbSFTPEndpoint endpoint = new B2bmbSFTPEndpoint(uri, this, sftpConfiguration);
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
}
