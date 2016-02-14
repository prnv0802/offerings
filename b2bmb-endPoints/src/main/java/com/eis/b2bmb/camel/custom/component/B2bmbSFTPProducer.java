package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.model.CommunicationConfiguration;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.model.*;
import com.jcraft.jsch.ChannelSftp;
import org.apache.camel.Exchange;
import org.apache.camel.Expression;
import org.apache.camel.component.file.remote.RemoteFileProducer;
import org.apache.camel.component.file.remote.SftpConfiguration;
import org.apache.camel.component.file.remote.SftpOperations;
import org.apache.camel.util.StringHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


/**
 * Producer for B2B SFTP Component which uses Communication Configurations to make SFTP connections
 * dynamically.
 *
 * User: tcostazno
 */
public class B2bmbSFTPProducer extends RemoteFileProducer<ChannelSftp.LsEntry> {
    private static final transient Logger LOG = LoggerFactory.getLogger(B2bmbSFTPProducer.class);

    /**
     * Parameterized constructor
     *
     * @param endpoint endpoint Uri
     * @param operations sftp operations
     */
    public B2bmbSFTPProducer(B2bmbSFTPEndpoint endpoint, SftpOperations operations) {

        super(endpoint, operations);
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating  B2bmbRemoteFileProducer  object  ");
        }
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inside process method of B2bRemoteFileProducer ");
        }
        if (exchange == null) {
            throw new IllegalArgumentException("The exchange cannot be null");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inside process method of B2bRemoteFileProducer ");
        }

        String target = createFileName(exchange);
        if(target != null) {
            processExchange(exchange, target);
        }

    }

    @Override
    public String createFileName(Exchange exchange) {
        String answer;

        // overrule takes precedence
        Object value;
        String b2bmbDirectoryName = exchange.getIn().getHeader(B2bmbCamelConstants.B2B_DIRECTORY_NAME,
                String.class);
        if(b2bmbDirectoryName == null) {
            throw new IllegalArgumentException("B2BDirectoryName must be set in Header and can not be null.");
        }
        String directoryType = "localOutDirectoryName";
        if(exchange.getIn().getHeader(B2bmbCamelConstants.B2B_DIRECTORY_LOCATION, String.class) != null) {
            directoryType = exchange.getIn().getHeader(B2bmbCamelConstants.B2B_DIRECTORY_LOCATION,
                    String.class);
        }
        DynamicSearchRequest dynamicSearchRequest = new DynamicSearchRequest();
        DynamicAttribute parentAttribute = new DynamicAttribute();
        parentAttribute.setType(DynamicAttributeType.String);
        parentAttribute.setValue(b2bmbDirectoryName);
        parentAttribute.setRefName(directoryType);
        dynamicSearchRequest.getSearchFields().getAttributes().put(directoryType, parentAttribute);

        if(getEndpoint().getVendor() != null && !"".equals(getEndpoint().getVendor())) {
            DynamicAttribute connectionAttribute = new DynamicAttribute();
            connectionAttribute.setType(DynamicAttributeType.String);
            connectionAttribute.setValue(getEndpoint().getVendor());
            connectionAttribute.setRefName("vendor");
            dynamicSearchRequest.getSearchFields().getAttributes().put("vendor", connectionAttribute);
        }

        List<String> dataDomains = new ArrayList<String>();
        B2bmbSFTPEndpoint sftpEndpoint = (B2bmbSFTPEndpoint) endpoint;
        String dataDomain = sftpEndpoint.getDataDomain();
        dataDomains.add(dataDomain);
        CommunicationConfiguration cc = null;
        try {
            List<CommunicationConfiguration> communicationConfigurations =
                    sftpEndpoint.getCommunicationConfigurationDAO().getList(
                            dynamicSearchRequest, 0, 10, null, dataDomains);

            if(communicationConfigurations == null || communicationConfigurations.size() == 0) {
                throw new IllegalStateException("No Communication Configuration found for directory:"+
                  b2bmbDirectoryName+ " can not send file.");
            } else if(communicationConfigurations.size() > 1) {
                throw new IllegalStateException("Multiple Communication Configurations found for directory:"+
                        b2bmbDirectoryName+ " can not send file.");
            } else {
                cc = communicationConfigurations.get(0);
            }

        } catch (B2BTransactionFailed e) {
            if(LOG.isErrorEnabled()) {
                LOG.error("Could not query for Communication Configurations", e);
            }

            throw new IllegalStateException("Could not query for Communication Configurations", e);
        }

        if(cc != null) {
            if("Y".equals(cc.getActive())) {
                try {
                    storeFile(exchange, b2bmbDirectoryName, dataDomain);
                    //CHECKSTYLE:OFF
                } catch (Exception e) {
                    throw new IllegalStateException("Could not store file in directory:" + b2bmbDirectoryName
                            + " in dataDomain:" + dataDomain, e);
                }
                //CHECKSTYLE:ON

                SftpConfiguration sftpConfiguration = buildSftpConfiguration(cc);
                this.endpoint.setConfiguration(sftpConfiguration);
                Object overrule = exchange.getIn().getHeader(Exchange.OVERRULE_FILE_NAME);
                if (overrule != null) {
                    if (overrule instanceof Expression) {
                        value = overrule;
                    } else {
                        value = exchange.getContext().getTypeConverter().convertTo(String.class, exchange, overrule);
                    }
                } else {
                    value = exchange.getIn().getHeader(Exchange.FILE_NAME);
                }

                // if we have an overrule then override the existing header to use the overrule computed name
                // from this point forward
                if (overrule != null) {
                    exchange.getIn().setHeader(Exchange.FILE_NAME, value);
                }

                if (value != null && value instanceof String && StringHelper.hasStartToken((String) value, "simple")) {
                    log.warn("Simple expression: {} detected in header: {} of type String. This feature has been " +
                            "removed (see CAMEL-6748).", value, Exchange.FILE_NAME);
                }

                // expression support
                Expression expression = endpoint.getFileName();
                if (value != null && value instanceof Expression) {
                    expression = (Expression) value;
                }

                // evaluate the name as a String from the value
                String name;
                if (expression != null) {
                    log.trace("Filename evaluated as expression: {}", expression);
                    name = expression.evaluate(exchange, String.class);
                } else {
                    name = exchange.getContext().getTypeConverter().convertTo(String.class, exchange, value);
                }

                // flatten name
                if (name != null && endpoint.isFlatten()) {
                    // check for both windows and unix separators
                    int pos = Math.max(name.lastIndexOf("/"), name.lastIndexOf("\\"));
                    if (pos != -1) {
                        name = name.substring(pos + 1);
                    }
                }

                // compute path by adding endpoint starting directory
                String endpointPath = endpoint.getConfiguration().getDirectory();
                String baseDir = "";
                if (endpointPath.length() > 0) {
                    // Its a directory so we should use it as a base path for the filename
                    // If the path isn't empty, we need to add a trailing / if it isn't already there
                    baseDir = endpointPath;
                    boolean trailingSlash = endpointPath.endsWith("/") || endpointPath.endsWith("\\");
                    if (!trailingSlash) {
                        baseDir += getFileSeparator();
                    }
                }
                if (name != null) {
                    answer = baseDir + name;
                } else {
                    // use a generated filename if no name provided
                    answer = baseDir + endpoint.getGeneratedFileName(exchange.getIn());
                }

                if (endpoint.getConfiguration().needToNormalize()) {
                    // must normalize path to cater for Windows and other OS
                    answer = normalizePath(answer);
                }

                return answer;
            } else {
                if(LOG.isWarnEnabled()) {
                    LOG.warn("A file was passed for a Communication Configuration:"+cc.getRefName() +
                            " but the connection is not active.  The file will be stored in directory:"+
                            b2bmbDirectoryName);
                }

                String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
                FileSystemEntry parentFileSystemEntry = null;
                try {
                    parentFileSystemEntry = getEndpoint().getFileSystemEntryDAO()
                            .getByRefName(b2bmbDirectoryName, dataDomain);
                } catch (B2BTransactionFailed e) {
                    throw new IllegalStateException("Could not get fileSystemEntry for path:"+b2bmbDirectoryName,
                            e);
                }
                if (parentFileSystemEntry == null) {
                    throw new IllegalStateException("Directory does not exist. Domain: " + dataDomain
                            + " Path: " + b2bmbDirectoryName);
                }

                InputStream message = exchange.getIn().getBody(InputStream.class);
                try {
                    getEndpoint().getFileSystemEntryDAO().createFileFromStream(exchange.getExchangeId(),
                            fileName,
                            parentFileSystemEntry.getId(), exchange.getIn().getBody(InputStream.class),
                            "application/octet-stream", parentFileSystemEntry.getDataDomain(),
                            parentFileSystemEntry.getOwnerUserProfileRefName(),
                            exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID, String.class));
                    return null;
                } catch (B2BNotFoundException e) {
                    throw new IllegalStateException("Could not save file path:"+b2bmbDirectoryName,
                            e);
                } catch (B2BTransactionFailed e) {
                    throw new IllegalStateException("Could not save file path:"+b2bmbDirectoryName, e);
                }
            }
        } else {
            throw new IllegalStateException("No Communication Configuration found for directory:"+
                    b2bmbDirectoryName+ " can not send file.");
        }
    }


    /**
     * @param exchange created exchange
     * @throws Exception thrown by camel
     */
    private void storeFile(Exchange exchange, String filePath, String dataDomain) throws Exception {
        FileSystemEntry parentFileSystemEntry = getEndpoint().getFileSystemEntryDAO()
                .getByRefName(filePath, dataDomain);
        if (parentFileSystemEntry == null) {
            throw new B2BNotFoundException("Directory does not exist. Domain: " + dataDomain
                    + " Path: " + filePath);
        }

        //moved to success directory
        List<FileSystemEntry> fileSystemEntries = null;

        DynamicSearchRequest dynamicSearchRequest = new DynamicSearchRequest();
        DynamicAttribute parentAttribute = new DynamicAttribute();
        parentAttribute.setType(DynamicAttributeType.String);
        parentAttribute.setValue(parentFileSystemEntry.getId());
        parentAttribute.setRefName("parentFileEntryId");
        dynamicSearchRequest.getSearchFields().getAttributes().put("parentFileEntryId", parentAttribute);

        DynamicAttribute directoryOnlyAttribute = new DynamicAttribute();
        directoryOnlyAttribute.setType(DynamicAttributeType.String);
        directoryOnlyAttribute.setValue(FileSystemEntryType.Directory.value());
        directoryOnlyAttribute.setRefName("type");
        dynamicSearchRequest.getSearchFields().getAttributes().put("type", directoryOnlyAttribute);

        DynamicAttribute directoryNameAttribute = new DynamicAttribute();
        directoryNameAttribute.setType(DynamicAttributeType.String);
        directoryNameAttribute.setValue(".processed");

        directoryNameAttribute.setRefName("name");
        dynamicSearchRequest.getSearchFields().getAttributes().put("name", directoryNameAttribute);

        List<String> dataDomains = new ArrayList<String>();
        dataDomains.add(parentFileSystemEntry.getDataDomain());

        fileSystemEntries = getEndpoint().getFileSystemEntryDAO().getList(
                dynamicSearchRequest, 0, 1, null, dataDomains);

        FileSystemEntry moveToDirectory = null;
        if (fileSystemEntries.isEmpty()) {
            //create directory since it doesn't exist
            FileSystemEntry newDirectory = new FileSystemEntry();
            newDirectory.setParentFileEntryId(parentFileSystemEntry.getId());
            newDirectory.setOwnerUserProfileRefName(parentFileSystemEntry.getOwnerUserProfileRefName());
            newDirectory.setName(".processed");
            newDirectory.setType(FileSystemEntryType.Directory);
            newDirectory.setDataDomain(parentFileSystemEntry.getDataDomain());
            moveToDirectory = getEndpoint().getFileSystemEntryDAO().save(newDirectory);
        } else {
            moveToDirectory = fileSystemEntries.get(0);
        }

        //default content type
        String contentType = "application/octet-stream";
        if (exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class) != null) {
            contentType = exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class);
        }

        String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
        if (fileName == null)
        {
            fileName = exchange.getIn().getHeader("DOWNLOADED_FILE", String.class);

        }

        if (fileName == null)
        {
            throw new IllegalStateException("File Name could not be resolved, need either:" + Exchange.FILE_NAME +
                    " or " + "DOWNLOADED_FILE in header");
        }


        InputStream in = exchange.getIn().getBody(InputStream.class);

        if ( in != null)
        {
            getEndpoint().getFileSystemEntryDAO().createFileFromStream(exchange.getExchangeId(),
                    fileName,
                    moveToDirectory.getId(), in,
                    contentType, parentFileSystemEntry.getDataDomain(),
                    parentFileSystemEntry.getOwnerUserProfileRefName(),
                    exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID, String.class));
            if (LOG.isInfoEnabled()) {
                LOG.info("Endpoint URI at createFileSystemEntry method :: " + getEndpoint().getEndpointUri());
            }
        }

    }


    private SftpConfiguration buildSftpConfiguration(CommunicationConfiguration cc) {
        SftpConfiguration sftpConfiguration = new SftpConfiguration();
        sftpConfiguration.setDirectory(cc.getInDirectoryName());
        sftpConfiguration.setHost(cc.getHost());
        sftpConfiguration.setPort(Integer.valueOf(cc.getPort()));
        sftpConfiguration.setUsername(cc.getUserName());
        sftpConfiguration.setPassword(cc.getPassword());
        return sftpConfiguration;
    }


        @Override
    public B2bmbSFTPEndpoint getEndpoint() {
        return (B2bmbSFTPEndpoint) super.getEndpoint();
    }


}
