package com.eis.b2bmb.endpts.ssh.nsoftware;

import com.eis.b2bmb.api.v1.dao.SFTPRestrictedIPDAO;
import com.eis.b2bmb.api.v1.model.CommunicationProtocol;
import com.eis.b2bmb.api.v1.model.SFTPServerUser;
import com.eis.b2bmb.util.TransmissionRecorder;
import com.eis.common.Constants;
import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import com.eis.core.api.v1.dao.UserProfileDAO;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.model.*;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ipworksssh.*;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.subject.support.SubjectThreadState;
import org.apache.shiro.util.ThreadState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


/**
 * User: mingardia
 * Date: 3/13/14
 * Time: 7:40 PM
 */
public class SFTPServerListener implements SftpserverEventListener {

    private static final Logger LOG = LoggerFactory.getLogger(SFTPServerListener.class);

    @Autowired
    SFTPServerHelper helper;
    @Autowired
    FileSystemEntryDAO fileSystemEntryDAO;


    SFTPServer server;

    @Autowired
    UserProfileDAO userProfileDAO;

    @Autowired
    SFTPRestrictedIPDAO restrictedIPDAO;

    @Autowired
    TransmissionRecorder transmissionRecorder;

    // Tracks currently logged in users and maps connectionId to Session
    Cache<String, SFTPSession> sessions = CacheBuilder.newBuilder().expireAfterAccess(60, TimeUnit.MINUTES).build();

    /**
     * sets the FileSystemEntryDAO implementation we will use
     *
     * @param fileSystemEntryDAO - the fileSystemEntryDAO implementation
     */
    public void setFileSystemEntryDAO(FileSystemEntryDAO fileSystemEntryDAO) {

        this.fileSystemEntryDAO = fileSystemEntryDAO;
    }

    /**
     * sets the UserProfileDAO implementation we will use
     *
     * @param userProfileDAO - the userProfileDAO implementation
     */
    public void setUserProfileDAO(UserProfileDAO userProfileDAO) {

        this.userProfileDAO = userProfileDAO;
    }

    /**
     * sets the helper implementation we will use
     *
     * @param serverHelper - the helper implementation
     */
    public void setSFTPServerHelper(SFTPServerHelper serverHelper) {

        this.helper = serverHelper;
    }

    /**
     * @param server {@link SFTPServer}
     */
    public SFTPServerListener(SFTPServer server) {

        this.server = server;
        server.setListener(this);
    }

    // Have to turn it off here due to naming convention violations mainly the "SftpserverSSHStatus" I think is the
    // reason but have not validated that.
    // CHECKSTYLE:OFF

    /**
     * @param event {@link SftpserverSSHStatusEvent}
     */
    @Override
    public void SSHStatus(SftpserverSSHStatusEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug(event.connectionId + ":" + event.message);
        }

    }

    /**
     * called when authentication request comes
     *
     * @param event {@link SftpserverSSHUserAuthRequestEvent}
     */
    @Override
    public void SSHUserAuthRequest(SftpserverSSHUserAuthRequestEvent event) {


        if (LOG.isDebugEnabled()) {
            LOG.debug("[" + event.connectionId + "] Attempting to authenticate user:" + event.user);
        }
        Map<String, Object> data = new HashMap<String, Object>();

        Transmission transmission = transmissionRecorder.createTransmission(TransmissionDirection.INBOUND,
                Constants.CANTATA_APP_DATADOMAIN, CommunicationProtocol.SFTP,
                event.user,
                "",
                "",
                "SFTP Server", event.connectionId);

        transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                transmission.getDataDomain(), TransmissionStatus.INPROCESS,
                "SFTP Server Authentication Attempt from:" + event.user,
                event.user, "", "",
                "SFTP Server", TransmissionDirection.INBOUND);
        SFTPSession session = null;

        try {
            session = helper.authenticateSFTPUser(event, server);
        } catch (IPWorksSSHException e) {
            data.put("Error", e.getMessage());
            transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                    transmission.getDataDomain(), TransmissionStatus.FAILED,
                    "SFTP Server Authentication Error from:" + event.user,
                    event.user, "", "",
                    "SFTP Server", TransmissionDirection.INBOUND);

            e.printStackTrace();
        }

        if (session != null) {
            session.setTransmission(transmission);
            sessions.put(event.connectionId, session);
            transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                    transmission.getDataDomain(), TransmissionStatus.INPROCESS,
                    "SFTP Server Authentication Successful from:" + event.user,
                    event.user, "", "",
                    "SFTP Server", TransmissionDirection.INBOUND);
        }
        else {
            transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                    transmission.getDataDomain(), TransmissionStatus.FINISHED,
                    "SFTP Server Authentication Initial Attempt from:" + event.user + " is finished.",
                    event.user, "", "",
                    "SFTP Server", TransmissionDirection.INBOUND);

            if (LOG.isDebugEnabled()) {
                LOG.debug("calling SSHUserAuthRequest  to authenticate user:" + event.user + "  again >>>>>>>>");
            }
        }
    }

    // CHECKSTYLE:ON


    /**
     * called when SftpServer connected successfully
     *
     * @param event {@link SftpserverConnectedEvent}
     */
    @Override
    public void connected(SftpserverConnectedEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("[" + event.connectionId + "] Now Connected ");
        }

        event.description = "Connected to the Cantata SFTPServer all rights reserved";

    }

    /**
     * new connection request comes, it called by sftpServer
     *
     * @param event {@link SftpserverConnectionRequestEvent}
     */
    @Override
    public void connectionRequest(SftpserverConnectionRequestEvent event) {

        if (LOG.isInfoEnabled()) {
            LOG.info(event.address + ":" + event.port
                    + " is attempting to connect.");

        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("Address", event.address);
        data.put("Port", String.valueOf(event.port));

        Transmission transmission = transmissionRecorder.createTransmission(TransmissionDirection.INBOUND,
                Constants.CANTATA_APP_DATADOMAIN, CommunicationProtocol.SFTP,
                "Unknown",
                "Unknown",
                "Unknown",
                "SFTP Server",
                event.address,
                String.valueOf(event.port));

        transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                transmission.getDataDomain(), TransmissionStatus.INPROCESS,
                "SFTP Server Connection Attempt From:" + event.address + ":" + event.port,
                "Unknown", "Unknown", "Unknown",
                "SFTP Server", TransmissionDirection.INBOUND);

        if (restrictedIPDAO.getBlackListedAddress(event.address)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug(event.address + ":" + event.port
                        + " are in black list ignoring connection request.");
            }

            transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                    transmission.getDataDomain(), TransmissionStatus.BLOCKED,
                    "SFTP Server Connection Attempt Blacklisted Address:" + event.address + ":" + event.port,
                    "Unknown", "Unknown", "Unknown",
                    "SFTP Server", TransmissionDirection.INBOUND);

            event.accept = false;
        }
        else {
            if (LOG.isDebugEnabled()) {
                LOG.debug(event.address + ": : is allowed to be connected");
            }

            transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                    transmission.getDataDomain(), TransmissionStatus.FINISHED,
                    "SFTP Server Connection Allowed From:" + event.address + ":" + event.port,
                    "Unknown", "Unknown", "Unknown",
                    "SFTP Server", TransmissionDirection.INBOUND);
        }


    }

    /**
     * creates new directory
     *
     * @param event {@link SftpserverDirCreateEvent}
     */
    @Override
    public void dirCreate(SftpserverDirCreateEvent event) {

        SFTPSession session = getSession(event.connectionId);

        SFTPServerUser sftpServerUser = session.getSftpServerUser();
        if (sftpServerUser == null) {
            throw new IllegalStateException("sftpServerUser for connection Id:" +
                    event.connectionId + " could not be found");
        }
        String userHomeDirectory = sftpServerUser.getRootDirectory();

        if (userHomeDirectory == null) {
            throw new IllegalStateException(" user Home Directory could not be found for sftp user ::"
                    + sftpServerUser.getUserId());
        }
        Subject subject = getSubject(session, event.connectionId);

        //bind the subject
        ThreadState threadState = new SubjectThreadState(subject);
        threadState.bind();
        try {

            String path = helper.getOriginalPath(userHomeDirectory, event.path);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Attempt to create directory:" + path
                        + " from user:" + event.user + " fileType:" + event.fileType);
                LOG.debug(">> FileOwner:" + event.fileOwner + " Grp:"
                        + event.fileGroup + " CreateTime:" +
                        event.fileCreateTime + " permissions:" + event.filePermissions);
            }

            event.fileATime = event.fileCreateTime;
            event.fileMTime = event.fileCreateTime;
            if (event.fileType == 1) {
                int statusCode = helper.createDirectory(session, path, sftpServerUser);
                if (statusCode == 0) {
                    event.fileOwner = sftpServerUser.getB2bMailboxUserProfileRef().getRefName();
                }
                event.statusCode = statusCode;
            }
            else {
                event.statusCode = SFTPServer.SSH_FX_OP_UNSUPPORTED; // OP_UNSUPPORTED
            }

        } finally {
            threadState.clear();
        }

    }

    /**
     * list the directory for  a request
     *
     * @param event {@link SftpserverDirListEvent}
     */
    @Override
    public void dirList(SftpserverDirListEvent event) {

        if (LOG.isInfoEnabled()) {
            LOG.debug(event.user + " list the directory " + event.path);
        }

        String[] list = null;
        SFTPSession session = getSession(event.connectionId);

        SFTPServerUser sftpServerUser = session.getSftpServerUser();
        if (sftpServerUser == null) {
            throw new IllegalStateException("sftpServerUser for connection Id:" +
                    event.connectionId + " could not be found");
        }

        String userHomeDirectory = sftpServerUser.getRootDirectory();

        if (userHomeDirectory == null) {
            throw new IllegalStateException(" user Home Directory could not be found for sftp user ::"
                    + sftpServerUser.getUserId());
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("blocking the user to there home Directory ::" + userHomeDirectory);
        }


        String path = helper.getOriginalPath(userHomeDirectory, event.path);

        list = helper.getDirectoryListing(path, sftpServerUser.getDataDomain());

        try {
            server.setFileList(event.connectionId, list);
        } catch (ipworksssh.IPWorksSSHException exx) {
            exx.printStackTrace();
            event.statusCode = SFTPServer.SSH_FX_FAILURE;
        }

        event.statusCode = SFTPServer.SSH_FX_OK;


    }

    /**
     * removes given directory
     *
     * @param event {@link SftpserverDirRemoveEvent}
     */
    @Override
    public void dirRemove(SftpserverDirRemoveEvent event) {


        SFTPSession session = getSession(event.connectionId);

        SFTPServerUser sftpServerUser = session.getSftpServerUser();
        if (sftpServerUser == null) {
            throw new IllegalStateException("sftpServerUser for connection Id:" +
                    event.connectionId + " could not be found");
        }
        String userHomeDirectory = sftpServerUser.getRootDirectory();

        if (userHomeDirectory == null) {
            throw new IllegalStateException(" user Home Directory could not be found for sftp user ::"
                    + sftpServerUser.getUserId());
        }
        String path = event.path;

        String actualPath = "" + userHomeDirectory + path;
        if (LOG.isDebugEnabled()) {
            LOG.debug(event.user + " is deleting the directory "
                    + actualPath);
        }


        event.statusCode = helper.dirRemove(actualPath, session.getSftpServerUser().getDataDomain());

    }

    /**
     * called when user disconnect the server
     *
     * @param event {@link SftpserverDisconnectedEvent}
     */
    @Override
    public void disconnected(SftpserverDisconnectedEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("[" + event.connectionId + "]  Now Disconnected Status Code:" + event.statusCode + " " +
                    event.description);
        }

        SFTPSession session = getSessionIgnoreNull(event.connectionId);

        if (session != null) {
            Transmission transmission = session.getTransmission();
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("Status Code", String.valueOf(event.statusCode));
            data.put("Description", event.description);
            transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                    transmission.getDataDomain(), TransmissionStatus.DISCONNECTED,
                    "SFTP Server User:" + session.getUser() + " Disconnected",
                    session.getUser(), "", "",
                    "SFTP Server", TransmissionDirection.INBOUND);
        }
        sessions.asMap().remove(event.connectionId);

    }

    /**
     * close file
     *
     * @param event {@link SftpserverFileCloseEvent}
     */
    @Override
    public void error(SftpserverErrorEvent event) {

        if (LOG.isErrorEnabled()) {
            LOG.error("!!!!! SFTP Transmission ERROR !!!!!");
            LOG.error("[" + event.connectionId + "] Error: errorId :: "
                    + event.errorCode + " errorDescription :: "
                    + event.description + ":" + event.toString());
        }

        if (event.connectionId != null) {
            SFTPSession session = getSession(event.connectionId);

            if (session != null) {
                Transmission transmission = session.getTransmission();
                Map<String, Object> data = new HashMap<String, Object>();

                data.put("Error Code", String.valueOf(event.errorCode));
                data.put("Description", event.description);
                String user = "Unknown";
                if (session != null && session.getUser() != null) {
                    user = session.getUser();
                }
                transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                        transmission.getDataDomain(), TransmissionStatus.FAILED,
                        "SFTP Server Error User:" + session.getUser() + " Disconnected",
                        session.getUser(), "", "",
                        "SFTP Server", TransmissionDirection.INBOUND);
            }
        }
    }

    /**
     * close file
     *
     * @param event {@link SftpserverFileCloseEvent}
     */
    public void fileClose(SftpserverFileCloseEvent event) {


        if (LOG.isDebugEnabled()) {
            LOG.debug("[" + event.connectionId + "] fileClose :: path: " + event.path + " user:" + event.user +
                    " status:" + event.statusCode);
        }

        SFTPSession session = getSession(event.connectionId);

        SFTPServerUser sftpServerUser = getSftpServerUser(session, event.connectionId);
        String userHomeDirectory = sftpServerUser.getRootDirectory();

        if (userHomeDirectory == null) {
            throw new IllegalStateException(" user Home Directory could not be found for sftp user ::"
                    + sftpServerUser.getUserId());
        }

        Subject subject = getSubject(session, event.connectionId);

        //bind the subject
        ThreadState threadState = new SubjectThreadState(subject);
        threadState.bind();
        try {
            String path = helper.getOriginalPath(userHomeDirectory, event.path);
            event.statusCode = helper.fileClose(path, event.connectionId, event.handle, sftpServerUser);

            if (session != null) {
                Transmission transmission = session.getTransmission();
                Map<String, Object> data = new HashMap<String, Object>();
                data.put("Path", path);
                data.put("Status", String.valueOf(event.statusCode));
                transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                        transmission.getDataDomain(), TransmissionStatus.DELIVERED,
                        "SFTP Server File:" + path + " Closed",
                        session.getUser(), "", "",
                        "SFTP Server", TransmissionDirection.INBOUND);
            }

        } finally {
            threadState.clear();
        }

    }

    /**
     * open a give file
     *
     * @param event {@link SftpserverFileOpenEvent}
     */
    public void fileOpen(SftpserverFileOpenEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("[" + event.connectionId + "] FileOpen request for Path:" + event.path);
            LOG.debug("Opening for:");
        }

        LockOperation lockOperation = LockOperation.NA;
        if ((event.flags & SFTPServer.SSH_FXF_READ) != 0) {
            lockOperation = LockOperation.READ;
            if (LOG.isDebugEnabled()) {
                LOG.debug("Read requested");
            }
        }

        if ((event.flags & SFTPServer.SSH_FXF_WRITE) != 0) {
            lockOperation = LockOperation.WRITE;
            if (LOG.isDebugEnabled()) {
                LOG.debug("write requested");
            }
        }

        if ((event.flags & SFTPServer.SSH_FXF_APPEND) != 0) {
            lockOperation = LockOperation.APPEND;
            if (LOG.isDebugEnabled()) {
                LOG.debug("append requested");
            }
        }

        if ((event.flags & SFTPServer.SSH_FXF_CREAT) != 0) {
            lockOperation = LockOperation.CREATE;
            if (LOG.isDebugEnabled()) {
                LOG.debug("create requested");
            }
        }

        if ((event.flags & SFTPServer.SSH_FXF_TRUNC) != 0) {
            lockOperation = LockOperation.TRUNCATE;
            if (LOG.isDebugEnabled()) {
                LOG.debug("truncate requested");
            }
        }

        if ((event.flags & SFTPServer.SSH_FXF_EXCL) != 0) {
            lockOperation = LockOperation.EXCLUSIVE;
            if (LOG.isDebugEnabled()) {
                LOG.debug("exclusive requested");
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug(">>File Size:" + event.fileSize);
            LOG.debug(" File Owner:" + event.fileOwner);
            LOG.debug(" File Group:" + event.fileGroup);
            LOG.debug(" File Create Date:" + event.fileCreateTime);
            LOG.debug(" File Type:" + event.fileType);
            LOG.debug(" Status code:" + event.statusCode);
            LOG.debug(" Desired Access:" + event.desiredAccess);

        }

        Map<String, Object> data = new HashMap<String, Object>();
        data.put("File Path", event.path);
        data.put("File Size", String.valueOf(event.fileSize));
        data.put("File Owner", event.fileOwner);
        data.put("File Group", event.fileGroup);
        data.put("File Create Date", String.valueOf(event.fileCreateTime));
        data.put("File Type", String.valueOf(event.fileType));
        data.put("Status", String.valueOf(event.statusCode));
        data.put("Desired Access", String.valueOf(event.desiredAccess));

        SFTPSession session = getSession(event.connectionId);

        SFTPServerUser sftpServerUser = getSftpServerUser(session, event.connectionId);

        Subject subject = getSubject(session, event.connectionId);

        //bind the subject
        ThreadState threadState = new SubjectThreadState(subject);
        threadState.bind();
        try {
            event.statusCode = helper.fileOpen(event, server, sftpServerUser);

            if (session != null) {
                Transmission transmission = session.getTransmission();

                transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                        transmission.getDataDomain(), TransmissionStatus.INPROCESS,
                        "SFTP Server File:" + event.path + " Opened for lock operation: " + lockOperation,
                        session.getUser(), "", "",
                        "SFTP Server", TransmissionDirection.INBOUND);
            }
        } finally {
            threadState.clear();
        }
    }

    /**
     * reads a file
     *
     * @param event {@link SftpserverFileReadEvent}
     */
    public void fileRead(SftpserverFileReadEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Attempt to read fileOffset:" + event.fileOffset
                    + " length:" + event.length);
        }

        event.statusCode = helper.fileRead(event, server);

    }

    /**
     * removes given file
     *
     * @param event {@link SftpserverFileRemoveEvent}
     */
    public void fileRemove(SftpserverFileRemoveEvent event) {


        if (LOG.isDebugEnabled()) {
            LOG.debug("[" + event.connectionId + "] " + event.user + " deleted the file " + event.path);
        }

        SFTPSession session = getSession(event.connectionId);

        SFTPServerUser sftpServerUser = getSftpServerUser(session, event.connectionId);
        Subject subject = getSubject(session, event.connectionId);

        //bind the subject
        ThreadState threadState = new SubjectThreadState(subject);
        threadState.bind();
        try {
            String path = helper.getOriginalPath(sftpServerUser.getRootDirectory(), event.path);

            event.statusCode = helper.fileRemove(path, sftpServerUser.getDataDomain());

            if (session != null) {
                Transmission transmission = session.getTransmission();

                Map<String, Object> data = new HashMap<String, Object>();
                data.put("File Path", event.path);
                data.put("Status Code", String.valueOf(event.statusCode));
                transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                        transmission.getDataDomain(), TransmissionStatus.INPROCESS,
                        "SFTP Server File:" + path + " Deleted",
                        session.getUser(), "", "",
                        "SFTP Server", TransmissionDirection.INBOUND);
            }
        } finally {
            threadState.clear();
        }

    }

    /**
     * renames the given  file  name
     *
     * @param event {@link SftpserverFileRenameEvent}
     */
    public void fileRename(SftpserverFileRenameEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("[" + event.connectionId + "] " + event.user + "rename the file " + event.path);
        }

        SFTPSession session = getSession(event.connectionId);

        SFTPServerUser sftpServerUser = getSftpServerUser(session, event.connectionId);
        Subject subject = getSubject(session, event.connectionId);

        //bind the subject
        ThreadState threadState = new SubjectThreadState(subject);
        threadState.bind();
        try {
            event.statusCode = helper.fileRename(event.path, event.newPath, sftpServerUser);

            if (session != null) {
                Transmission transmission = session.getTransmission();

                Map<String, Object> data = new HashMap<String, Object>();
                data.put("File Path", event.path);
                data.put("Status", String.valueOf(event.statusCode));
                transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                        transmission.getDataDomain(), TransmissionStatus.INPROCESS,
                        "SFTP Server File:" + event.path + " Renamed",
                        session.getUser(), "", "",
                        "SFTP Server", TransmissionDirection.INBOUND);
            }

        } finally {
            threadState.clear();
        }
    }

    /**
     * for new file write
     *
     * @param event {@link SftpserverFileWriteEvent}
     */
    public void fileWrite(SftpserverFileWriteEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("[" + event.connectionId + "] File Write :: user:" + event.user + " :: handle:" +
                    event.handle + " offset:" + event.fileOffset);
        }

        event.statusCode = helper.fileWrite(event, server);

    }

    /**
     * get all the attribute for request
     *
     * @param event {@link SftpserverGetAttributesEvent}
     */
    public void getAttributes(SftpserverGetAttributesEvent event) {


        SFTPSession session = getSession(event.connectionId);

        SFTPServerUser sftpServerUser = getSftpServerUser(session, event.connectionId);

        /*  We don't need to check this for each and every file on a ls or any other operation
            if the user is not there we won't find it
        boolean exists=false;
          try {

               exists = userProfileDAO.exists(
                       sftpServerUser.getB2bMailboxUserProfileRef().getRefName(),
                       sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());

          } catch (B2BTransactionFailed e) {

               e.printStackTrace();
          }

        if (!exists) {
            throw new IllegalStateException("user Profile with RefName:" +
            sftpServerUser.getB2bMailboxUserProfileRef().getRefName() + " could not be found in dataDomain "+
            sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());
        }  */

        String userHomeDirectory = sftpServerUser.getRootDirectory();
        String path = helper.getOriginalPath(userHomeDirectory, event.path);

        if (LOG.isDebugEnabled()) {

            LOG.debug(event.user + " getAttributes:: path:" + path
                    + "::Create time:" + event.fileCreateTime
                    + "::FileType: " + event.fileType + "::Flags: "
                    + event.flags + "::otherAttributes : "
                    + event.otherAttributes + "::statusCode : "
                    + event.statusCode + "::FileOwner : "
                    + event.fileOwner);
        }

        // retrieve the file entry associated with the path.
        try {

            if (path == "/") {
                event.fileType = 2; // SSH_FILEXFER_TYPE_DIRECTORY
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Returning a fileType Directory ");
                }
            }
            else {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("getting file  entry with refName  ::" + path);
                }
                FileSystemEntry entry = fileSystemEntryDAO.
                        getByRefName(path, sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());

                if (entry != null) {
                    if (entry.getType().equals(FileSystemEntryType.File)) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Returning a fileType file ");
                        }

                        event.fileType = 1; // SSH_FILEXFER_TYPE_REGULAR
                        event.fileSize = entry.getSize();
                        event.fileCreateTime = entry.getCreateDate().getTime();
                        event.fileMTime = entry.getUpdateDate().getTime();
                        event.fileATime = entry.getUpdateDate().getTime();
                        // event.fileGroup=entry.getDataDomain();

                        if (entry.getDataDomain().equals(sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain()
                        )) {
                            event.filePermissions = 444;
                        }

                    }
                    else if (entry.getType().equals(FileSystemEntryType.Directory)) {

                        event.fileType = 2; // SSH_FILEXFER_TYPE_DIRECTORY
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Returning a fileType Directory ");
                        }
                    }
                    else {
                        throw new IllegalStateException("Unknown file entry type:" + event.fileType);
                    }

                    event.fileCreateTime = entry.getCreateDate().getTime();
                    if (entry.getUpdateDate() != null) {
                        event.fileMTime = entry.getUpdateDate().getTime();
                        event.fileATime = entry.getUpdateDate().getTime();
                    }
                    else {
                        event.fileMTime = entry.getCreateDate().getTime();
                        event.fileATime = entry.getCreateDate().getTime();
                    }
                    // event.fileOwner = entry.getDataDomain();
                    event.fileAttribBits = 777;
                    event.statusCode = 0;


                }
                else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Could not find an entry with the path:" + path);
                    }

                    event.statusCode = 2; // SSH_FX_NO_SUCH_FILE
                }
            }


        } catch (B2BTransactionFailed b2BTransactionFailed) {
            b2BTransactionFailed.printStackTrace();
            event.statusCode = 4; // SSH_FX_FAILURE
        }


    }

    /**
     * resolves the path one time after login for a user
     *
     * @param event {@link SftpserverResolvePathEvent}
     */
    public void resolvePath(SftpserverResolvePathEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolve Path:" + event.originalPath
                    + " Event Control flag  ::" + event.controlFlags);
        }


        switch (event.controlFlags) {


            case 0: {


                // event.realPath=event.originalPath;

             /* if (LOG.isDebugEnabled()) {
                      LOG.debug("Resolve path :" + event.controlFlags);
                  }  */
            }
            break;
            case 1: // SSH_FXP_REALPATH_NO_CHECK
            {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("SSH_FXP_REALPATH_NO_CHECK :" + event.controlFlags);
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("RealPath :: " + event.realPath
                            + " :::: originalPath :: " + event.originalPath);
                }

                if (event.realPath.equals("/")) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("User home directory is root ");
                    }

                }
                else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("resolve path is being called for another operation");
                    }
                }


                event.statusCode = 0; // ok;
            }
            break;
            case 2:// SSH_FXP_REALPATH_STAT_IF
            {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("SSH_FXP_REALPATH_STAT_IF :" + event.controlFlags);
                }
            }
            break;

            default:
                break;


        }

    }

    /**
     * sets the attribute
     *
     * @param event {@link SftpserverSetAttributesEvent}
     */
    public void setAttributes(SftpserverSetAttributesEvent event) {

        if (LOG.isDebugEnabled()) {
            LOG.debug("!!!! Set Attributes FileSize:" + event.fileSize);


        }
        SFTPSession session = getSession(event.connectionId);

        SFTPServerUser sftpServerUser = getSftpServerUser(session, event.connectionId);
        event.statusCode = 0;

    }

    private SFTPSession getSession(String connectionId) {

        SFTPSession session = sessions.asMap().get(connectionId);


        if (session == null) {
            Transmission transmission = transmissionRecorder.createTransmission(TransmissionDirection.INBOUND,
                    Constants.CANTATA_APP_DATADOMAIN, CommunicationProtocol.SFTP,
                    "Unknown",
                    "Unknown",
                    "Unknown",
                    "SFTP Server");
            String user = "Unknown";
            if (session != null && session.getUser() != null) {
                user = session.getUser();
            }
            Map<String, Object> data = new HashMap<String, Object>();
            transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                    transmission.getDataDomain(), TransmissionStatus.FAILED,
                    "Session with Id:" + connectionId + " could not be found",
                    user, "", "",
                    "SFTP Server", TransmissionDirection.INBOUND);

            throw new IllegalStateException("Session with Id:" + connectionId + " could not be found");
        }

        return session;
    }

    private SFTPSession getSessionIgnoreNull(String connectionId) {

        return sessions.asMap().get(connectionId);
    }

    private SFTPServerUser getSftpServerUser(SFTPSession session, String connectionId) {

        SFTPServerUser sftpServerUser = session.getSftpServerUser();
        if (sftpServerUser == null) {
            Transmission transmission = session.getTransmission();
            if (transmission != null) {
                Map<String, Object> data = new HashMap<String, Object>();
                transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                        transmission.getDataDomain(), TransmissionStatus.FAILED,
                        "Session with Id:" + connectionId + " could not be found",
                        session.getUser(), "", "",
                        "SFTP Server", TransmissionDirection.INBOUND);
            }
            throw new IllegalStateException("sftpServerUser for connection Id:" +
                    connectionId + " could not be found");
        }

        return sftpServerUser;
    }

    private Subject getSubject(SFTPSession session, String connectionId) {

        Subject subject = session.getSubject();
        if (subject == null) {
            Transmission transmission = session.getTransmission();
            if (transmission != null) {
                Map<String, Object> data = new HashMap<String, Object>();
                transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.SFTP, data,
                        transmission.getDataDomain(), TransmissionStatus.FAILED,
                        "Subject for connection Id:" + connectionId + " could not be found",
                        session.getUser(), "", "",
                        "SFTP Server", TransmissionDirection.INBOUND);
            }

            throw new IllegalStateException("subject for connection Id:" +
                    connectionId + " could not be found");
        }

        return subject;
    }


}
