package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.model.CommunicationConfiguration;
import com.eis.core.api.v1.model.DynamicAttribute;
import com.eis.core.api.v1.model.DynamicAttributeType;
import com.eis.core.api.v1.model.DynamicSearchRequest;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.component.file.GenericFile;
import org.apache.camel.component.file.remote.*;
import org.apache.camel.util.*;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author sudhakars
 */
public class B2bmbFTPConsumer extends FtpConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbFTPConsumer.class);

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
     * @param operations ftp operations
     */
    public B2bmbFTPConsumer(B2bmbFTPEndpoint endpoint, Processor processor, FtpOperations operations) {

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
        parentAttribute.setValue("FTP");
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
        B2bmbFTPEndpoint ftpEndpoint = (B2bmbFTPEndpoint) endpoint;
        String dataDomain = ftpEndpoint.getDataDomain();
        dataDomains.add(dataDomain);

        int batchSize = 25;
        int offset = 0;
        int polledMessages1 = 0;
        long numDocuments = ftpEndpoint.getCommunicationConfigurationDAO().getCount(dynamicSearchRequest,
                dataDomains);
        while (offset < numDocuments) {
            try {
                List<CommunicationConfiguration> communicationConfigurations =
                        ftpEndpoint.getCommunicationConfigurationDAO().getList(
                                dynamicSearchRequest, offset, batchSize, null, dataDomains);
                for (CommunicationConfiguration cc : communicationConfigurations) {
                    if("Y".equals(cc.getActive())) {
                        FtpConfiguration ftpConfiguration = buildFtpConfiguration(cc);
                        this.getEndpoint().setDisconnect(true);
                        this.endpoint.setConfiguration(ftpConfiguration);
                        this.endpointPath = this.endpoint.getConfiguration().getDirectory();
                        if (!this.prePollCheck()) {
                            if (this.log.isDebugEnabled()) {
                                this.log.debug("skipping poll as pre poll check returned false... " +
                                        "continuing to the next communication configuration");
                            }
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
                                if (this.log.isDebugEnabled()) {
                                    this.log.debug("Error occurred during poll directory: " + name + " due " +
                                            var12.getMessage() + ". Removing " + files.size() +
                                            " files marked as in-progress.");
                                }
                                this.removeExcessiveInProgressFiles(files);
                                //throw var12;
                                continue;
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
                                if (this.log.isDebugEnabled()) {
                                    this.log.debug("Limiting maximum messages to poll at {} files as there was more" +
                                            " messages in this poll.", Integer.valueOf(this.maxMessagesPerPoll));
                                }
                                this.removeExcessiveInProgressFiles(exchanges, this.maxMessagesPerPoll);
                            }

                            int total1 = exchanges.size();
                            if (total1 > 0) {
                                this.log.debug("Total {} files to consume", Integer.valueOf(total1));
                            }

                            polledMessages1 = this.processBatch(CastUtils.cast((Queue) exchanges)) + polledMessages1;
                            this.postPollCheck(total1);
                            //this.postPollCheck();
                        }
                    }
                }
            }//CHECKSTYLE:OFF
            catch(Exception e){
                //CHECKSTYLE:ON

                if (LOG.isErrorEnabled()) {
                    LOG.error("An error occurred trying to connect to FTP Site.", e);
                }

            } finally {
                offset = offset + batchSize;
            }

        }

        return polledMessages1;
    }

    @Override
    protected boolean doPollDirectory(String absolutePath, String dirName, List<GenericFile<FTPFile>>
            fileList, int depth) {
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
        List<FTPFile> files = null;
        if (isUseList()) {
            if (isStepwise()) {
                files = operations.listFiles();
            } else {
                files = operations.listFiles(dir);
            }
        } else {
            // we cannot use the LIST command(s) so we can only poll a named file
            // so created a pseudo file with that name
            FTPFile file = new FTPFile();
            file.setType(FTPFile.FILE_TYPE);
            fileExpressionResult = evaluateFileExpression();
            if (fileExpressionResult != null) {
                file.setName(fileExpressionResult);
                files = new ArrayList<FTPFile>(1);
                files.add(file);
            }
        }

        if (files == null || files.isEmpty()) {
            // no files in this directory to poll
            log.trace("No files found in directory: {}", dir);
            return true;
        } else {
            // we found some files
            log.trace("Found {} in directory: {}", files.size(), dir);
        }

        for (FTPFile file : files) {

            if (log.isTraceEnabled()) {
                log.trace("FtpFile[name={}, dir={}, file={}]", new Object[]{file.getName(), file.isDirectory(),
                        file.isFile()});
            }

            // check if we can continue polling in files
            if (!canPollMoreFiles(fileList)) {
                return false;
            }

            if (file.isDirectory()) {
                RemoteFile<FTPFile> remote = asRemoteFile(absolutePath, file);
                if (endpoint.isRecursive() && depth < endpoint.getMaxDepth() && isValidFile(remote, true, files)) {
                    // recursive scan and add the sub files and folders
                    String subDirectory = file.getName();
                    String path = absolutePath + "/" + subDirectory;
                    boolean canPollMore = pollSubDirectory(path, subDirectory, fileList, depth);
                    if (!canPollMore) {
                        return false;
                    }
                }
            } else if (file.isFile()) {
                RemoteFile<FTPFile> remote = asRemoteFile(absolutePath, file);
                if (depth >= endpoint.getMinDepth() && isValidFile(remote, false, files)) {
                    // matched file so add
                    fileList.add(remote);
                }
            } else {
                if (this.log.isDebugEnabled()) {
                    log.debug("Ignoring unsupported remote file type: " + file);
                }
            }
        }

        return true;
    }

    private FtpConfiguration buildFtpConfiguration(CommunicationConfiguration cc) {
        FtpConfiguration ftpConfiguration = new FtpConfiguration();
        String directoryName = cc.getOutDirectoryName();
        if(getEndpoint().getB2bDirectoryLocation() != null && !"".equals(getEndpoint().getB2bDirectoryLocation())) {
            if("inDirectoryName".equals(getEndpoint().getB2bDirectoryLocation())) {
                directoryName = cc.getInDirectoryName();
            } else if("outDirectoryName".equals(getEndpoint().getB2bDirectoryLocation())) {
                directoryName = cc.getOutDirectoryName();
            }
        }

        ftpConfiguration.setDirectory(directoryName);
        ftpConfiguration.setHost(cc.getHost());
        ftpConfiguration.setPort(Integer.valueOf(cc.getPort()));
        ftpConfiguration.setUsername(cc.getUserName());
        ftpConfiguration.setPassword(cc.getPassword());
        ftpConfiguration.setPassiveMode(cc.isUseFTPPassive());
        return ftpConfiguration;
    }

    @Override
    public B2bmbFTPEndpoint getEndpoint() {
        return (B2bmbFTPEndpoint) super.getEndpoint();
    }

    @Override
    public String toString() {
        return "B2bmbRemoteFileConsumer";
    }

    private RemoteFile<FTPFile> asRemoteFile(String absolutePath, FTPFile file) {
        RemoteFile<FTPFile> answer = new RemoteFile<FTPFile>();

        answer.setEndpointPath(this.endpointPath);
        answer.setFile(file);
        answer.setFileNameOnly(file.getName());
        answer.setFileLength(file.getSize());
        answer.setDirectory(file.isDirectory());
        if (file.getTimestamp() != null) {
            answer.setLastModified(file.getTimestamp().getTimeInMillis());
        }
        answer.setHostname(((RemoteFileConfiguration) endpoint.getConfiguration()).getHost());

        // absolute or relative path
        boolean absolute = FileUtil.hasLeadingSeparator(absolutePath);
        answer.setAbsolute(absolute);

        // create a pseudo absolute name
        String dir = FileUtil.stripTrailingSeparator(absolutePath);
        String absoluteFileName = FileUtil.stripLeadingSeparator(dir + "/" + file.getName());
        // if absolute start with a leading separator otherwise let it be relative
        if (absolute) {
            absoluteFileName = "/" + absoluteFileName;
        }
        answer.setAbsoluteFilePath(absoluteFileName);

        // the relative filename, skip the leading endpoint configured path
        String relativePath = ObjectHelper.after(absoluteFileName, endpointPath);
        // skip leading /
        relativePath = FileUtil.stripLeadingSeparator(relativePath);
        answer.setRelativeFilePath(relativePath);

        // the file name should be the relative path
        answer.setFileName(answer.getRelativeFilePath());

        answer.setCharset(endpoint.getCharset());
        return answer;
    }

    private boolean isStepwise() {
        RemoteFileConfiguration config = (RemoteFileConfiguration) endpoint.getConfiguration();
        return config.isStepwise();
    }

    private boolean isUseList() {
        RemoteFileConfiguration config = (RemoteFileConfiguration) endpoint.getConfiguration();
        return config.isUseList();
    }
}
