package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.model.CommunicationConfiguration;
import com.eis.core.api.v1.model.DynamicAttribute;
import com.eis.core.api.v1.model.DynamicAttributeType;
import com.eis.core.api.v1.model.DynamicSearchRequest;
import com.jcraft.jsch.ChannelSftp;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.remote.*;
import org.apache.camel.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author sudhakars
 */
public class B2bmbSFTPConsumer extends SftpConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbSFTPConsumer.class);

    //package visibility, default delay if none set
    static final long DEFAULT_CONSUMER_DELAY = 60 * 1000L;

    //package visibility, default messages per poll if none set
    static final int DEFAULT_MAX_MESSAGES_PER_POLL = 10;


    private String endpointPath;


    /**
     * Parameterized  constuctor
     *
     * @param endpoint   endpoint instance
     * @param processor  processor instance
     * @param operations sftp operations
     */
    public B2bmbSFTPConsumer(B2bmbSFTPEndpoint endpoint, Processor processor, SftpOperations operations) {

        super(endpoint, processor, operations);
    }

    @Override
    protected int poll() throws Exception {
        if(!this.prepareOnStartup) {
            this.endpoint.getGenericFileProcessStrategy().prepareOnStartup(this.operations, this.endpoint);
            this.prepareOnStartup = true;
        }

        this.fileExpressionResult = null;
        this.shutdownRunningTask = null;
        this.pendingExchanges = 0;

        DynamicSearchRequest dynamicSearchRequest = new DynamicSearchRequest();
        DynamicAttribute parentAttribute = new DynamicAttribute();
        parentAttribute.setType(DynamicAttributeType.String);
        parentAttribute.setValue("SFTP");
        parentAttribute.setRefName("communicationProtocol");
        dynamicSearchRequest.getSearchFields().getAttributes().put("communicationProtocol", parentAttribute);

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

        int batchSize = 25;
        int offset = 0;
        int polledMessages1 = 0;
        long numDocuments = sftpEndpoint.getCommunicationConfigurationDAO().getCount(dynamicSearchRequest,
                dataDomains);
        while (offset < numDocuments) {
            try {
                List<CommunicationConfiguration> communicationConfigurations =
                        sftpEndpoint.getCommunicationConfigurationDAO().getList(
                                dynamicSearchRequest, offset, batchSize, null, dataDomains);
                for (CommunicationConfiguration cc : communicationConfigurations) {
                    if("Y".equals(cc.getActive())) {
                        SftpConfiguration sftpConfiguration = buildSftpConfiguration(cc);
                        this.getEndpoint().setDisconnect(true);
                        this.endpoint.setConfiguration(sftpConfiguration);
                        this.endpointPath = this.endpoint.getConfiguration().getDirectory();
                        if (!this.prePollCheck()) {
                            this.log.debug("Skipping poll as pre poll check returned false");
                            return 0;
                        } else {
                            ArrayList files = new ArrayList();
                            String name = this.endpoint.getConfiguration().getDirectory();

                            StopWatch stop = new StopWatch();

                            boolean limitHit;
                            try {
                                limitHit = !this.pollDirectory(name, files, 0);
                            }//CHECKSTYLE:OFF
                            catch (Exception var12) {
                                //CHECKSTYLE:ON
                                this.log.debug("Error occurred during poll directory: " + name + " due " +
                                        var12.getMessage() + ". Removing " + files.size() +
                                        " files marked as in-progress.");
                                this.removeExcessiveInProgressFiles(files);
                                throw var12;
                            }

                            long delta = stop.stop();
                            if (this.log.isDebugEnabled()) {
                                this.log.debug("Took {} to poll: {}", TimeUtils.printDuration((double) delta), name);
                            }

                            if (limitHit) {
                                this.log.debug("Limiting maximum messages to poll at {} files as there was more " +
                                        "messages in this poll.", Integer.valueOf(this.maxMessagesPerPoll));
                            }

                            if (this.endpoint.getSorter() != null) {
                                Collections.sort(files, this.endpoint.getSorter());
                            }

                            LinkedList exchanges = new LinkedList();
                            Iterator q = files.iterator();

                            while (q.hasNext()) {
                                GenericFile total = (GenericFile) q.next();
                                Exchange polledMessages = this.endpoint.createExchange(total);
                                polledMessages.getIn().setHeader("B2BDirectoryName", cc.getLocalInDirectoryName());
                                polledMessages.getIn().setHeader("B2BActiveConnection", cc.getActive());
                                this.endpoint.configureExchange(polledMessages);
                                this.endpoint.configureMessage(total, polledMessages.getIn());
                                exchanges.add(polledMessages);
                            }

                            if (this.endpoint.getSortBy() != null) {
                                Collections.sort(exchanges, this.endpoint.getSortBy());
                            }

                            if (!this.eagerLimitMaxMessagesPerPoll && this.maxMessagesPerPoll > 0 &&
                                    files.size() > this.maxMessagesPerPoll) {
                                this.log.debug("Limiting maximum messages to poll at {} files as there was more" +
                                        " messages in this poll.", Integer.valueOf(this.maxMessagesPerPoll));
                                this.removeExcessiveInProgressFiles(exchanges, this.maxMessagesPerPoll);
                            }

                            int total1 = exchanges.size();
                            if (total1 > 0) {
                                this.log.debug("Total {} files to consume", Integer.valueOf(total1));
                            }

                            polledMessages1 = this.processBatch(CastUtils.cast((Queue) exchanges)) + polledMessages1;
                            this.postPollCheck(total1);
            //                this.postPollCheck();
                        }
                    }
                }
            }//CHECKSTYLE:OFF
            catch(Exception e){
                //CHECKSTYLE:ON

                if (LOG.isErrorEnabled()) {
                    LOG.error("An error occurred trying to connect to SFTP Site.", e);
                }

            } finally {
                offset = offset + batchSize;
            }

        }

        return polledMessages1;
    }

    @Override
    protected boolean doPollDirectory(String absolutePath, String dirName,
                                      List<GenericFile<ChannelSftp.LsEntry>> fileList, int depth) {
        log.trace("doPollDirectory from absolutePath: {}, dirName: {}", absolutePath, dirName);

        depth++;

        // remove trailing /
        dirName = FileUtil.stripTrailingSeparator(dirName);
        // compute dir depending on stepwise is enabled or not
        String dir;
        if (isStepwise()) {
            dir = ObjectHelper.isNotEmpty(dirName) ? dirName : absolutePath;
            operations.changeCurrentDirectory(dir);
        } else {
            dir = absolutePath;
        }

        log.trace("Polling directory: {}", dir);
        List<ChannelSftp.LsEntry> files;
        if (isStepwise()) {
            files = operations.listFiles();
        } else {
            files = operations.listFiles(dir);
        }
        if (files == null || files.isEmpty()) {
            // no files in this directory to poll
            log.trace("No files found in directory: {}", dir);
            return true;
        } else {
            // we found some files
            log.trace("Found {} in directory: {}", files.size(), dir);
        }

        for (ChannelSftp.LsEntry file : files) {

            if (log.isTraceEnabled()) {
                log.trace("SftpFile[fileName={}, longName={}, dir={}]", new Object[]{file.getFilename(),
                        file.getLongname(), file.getAttrs().isDir()});
            }

            // check if we can continue polling in files
            if (!canPollMoreFiles(fileList)) {
                return false;
            }

            if (file.getAttrs().isDir()) {
                RemoteFile<ChannelSftp.LsEntry> remote = asRemoteFile(absolutePath, file);
                if (endpoint.isRecursive() && depth < endpoint.getMaxDepth() && isValidFile(remote, true, files)) {
                    // recursive scan and add the sub files and folders
                    String subDirectory = file.getFilename();
                    String path = absolutePath + "/" + subDirectory;
                    boolean canPollMore = pollSubDirectory(path, subDirectory, fileList, depth);
                    if (!canPollMore) {
                        return false;
                    }
                }
                // we cannot use file.getAttrs().isLink on Windows, so we dont invoke the method
                // just assuming its a file we should poll
            } else {
                RemoteFile<ChannelSftp.LsEntry> remote = asRemoteFile(absolutePath, file);
                if (depth >= endpoint.getMinDepth() && isValidFile(remote, false, files)) {
                    // matched file so add
                    fileList.add(remote);
                }
            }
        }

        return true;
    }

    private SftpConfiguration buildSftpConfiguration(CommunicationConfiguration cc) {
        SftpConfiguration sftpConfiguration = new SftpConfiguration();
        String directoryName = cc.getOutDirectoryName();
        if(getEndpoint().getB2bDirectoryLocation() != null && !"".equals(getEndpoint().getB2bDirectoryLocation())) {
            if("inDirectoryName".equals(getEndpoint().getB2bDirectoryLocation())) {
                directoryName = cc.getInDirectoryName();
            } else if("outDirectoryName".equals(getEndpoint().getB2bDirectoryLocation())) {
                directoryName = cc.getOutDirectoryName();
            }
        }

        sftpConfiguration.setDirectory(directoryName);
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

    @Override
    public String toString() {
        return "B2bmbRemoteFileConsumer";
    }

    private RemoteFile<ChannelSftp.LsEntry> asRemoteFile(String absolutePath, ChannelSftp.LsEntry file) {
        RemoteFile<ChannelSftp.LsEntry> answer = new RemoteFile<ChannelSftp.LsEntry>();

        answer.setEndpointPath(endpointPath);
        answer.setFile(file);
        answer.setFileNameOnly(file.getFilename());
        answer.setFileLength(file.getAttrs().getSize());
        answer.setLastModified(file.getAttrs().getMTime() * 1000L);
        answer.setHostname(((RemoteFileConfiguration) endpoint.getConfiguration()).getHost());
        answer.setDirectory(file.getAttrs().isDir());

        // absolute or relative path
        boolean absolute = FileUtil.hasLeadingSeparator(absolutePath);
        answer.setAbsolute(absolute);

        // create a pseudo absolute name
        String dir = FileUtil.stripTrailingSeparator(absolutePath);
        String absoluteFileName = FileUtil.stripLeadingSeparator(dir + "/" + file.getFilename());
        // if absolute start with a leading separator otherwise let it be relative
        if (absolute) {
            absoluteFileName = "/" + absoluteFileName;
        }
        answer.setAbsoluteFilePath(absoluteFileName);

        // the relative filename, skip the leading endpoint configured path
        String relativePath = ObjectHelper.after(absoluteFileName, endpointPath);
        // skip trailing /
        relativePath = FileUtil.stripLeadingSeparator(relativePath);
        answer.setRelativeFilePath(relativePath);

        // the file name should be the relative path
        answer.setFileName(answer.getRelativeFilePath());

        return answer;
    }

    private boolean isStepwise() {
        RemoteFileConfiguration config = (RemoteFileConfiguration) endpoint.getConfiguration();
        return config.isStepwise();
    }
}
