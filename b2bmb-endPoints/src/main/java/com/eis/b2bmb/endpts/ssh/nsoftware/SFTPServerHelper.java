package com.eis.b2bmb.endpts.ssh.nsoftware;

import com.eis.b2bmb.api.v1.dao.SFTPServerUserDAO;
import com.eis.b2bmb.api.v1.model.AuthenticationType;
import com.eis.b2bmb.api.v1.model.SFTPServerUser;
import com.eis.b2bmb.api.v1.services.OpenFileKeyService;
import com.eis.blobstore.gridfs.GFSBlobOutputStream;
import com.eis.core.api.v1.dao.BlobDAO;
import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import com.eis.core.api.v1.dao.UserProfileDAO;
import com.eis.core.api.v1.exception.B2BException;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.*;
import com.eis.core.api.v1.service.BlobOutputStream;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import ipworksssh.*;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * User: mingardia Date: 3/13/14 Time: 2:25 PM
 */


class OpenFile {


    protected String path;
    protected String handle;
    protected OutputStream outputStream;
    protected InputStream inputStream;
    protected long readOffset;
    protected long writeOffset;
    protected String user;
    protected String dataDomain;


    protected int flag;

    protected String connectionId;


    public OutputStream getOutputStream() {

        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {

        this.outputStream = outputStream;
    }

    public InputStream getInputStream() {

        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {

        this.inputStream = inputStream;
    }

    public int getFlag() {

        return flag;
    }

    public void setFlag(int flag) {

        this.flag = flag;
    }

    public String getPath() {

        return path;
    }

    public void setPath(String path) {

        this.path = path;
    }

    public String getHandle() {

        return handle;
    }

    public void setHandle(String handle) {

        this.handle = handle;
    }

    public String getConnectionId() {

        return connectionId;
    }

    public void setConnectionId(String connectionId) {

        this.connectionId = connectionId;
    }

    public String getUser() {

        return user;
    }

    public void setUser(String user) {

        this.user = user;
    }

    public String getDataDomain() {

        return dataDomain;
    }

    public long getReadOffset() {

        return readOffset;
    }

    public void setReadOffset(long readOffset) {

        this.readOffset = readOffset;
    }

    public long getWriteOffset() {

        return writeOffset;
    }

    public void setWriteOffset(long writeOffset) {

        this.writeOffset = writeOffset;
    }

    public void setDataDomain(String dataDomain) {

        this.dataDomain = dataDomain;
    }

    public OpenFileKey getKey() {

        if (connectionId == null) {
            throw new IllegalStateException("ConnectionId must be set before calling this method");
        }

        if (path == null) {
            throw new IllegalStateException("Path must be set before calling this method");
        }

        OpenFileKey k = new OpenFileKey();
        k.connectionId = connectionId;
        k.path = path;
        return k;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        OpenFile openFile = (OpenFile) o;

        if (flag != openFile.flag) {
            return false;
        }
        if (readOffset != openFile.readOffset) {
            return false;
        }
        if (writeOffset != openFile.writeOffset) {
            return false;
        }
        if (connectionId != null ? !connectionId.equals(openFile.connectionId) : openFile.connectionId != null) {
            return false;
        }
        if (dataDomain != null ? !dataDomain.equals(openFile.dataDomain) : openFile.dataDomain != null) {
            return false;
        }
        if (handle != null ? !handle.equals(openFile.handle) : openFile.handle != null) {
            return false;
        }
        if (inputStream != null ? !inputStream.equals(openFile.inputStream) : openFile.inputStream != null) {
            return false;
        }
        if (outputStream != null ? !outputStream.equals(openFile.outputStream) : openFile.outputStream != null) {
            return false;
        }
        if (path != null ? !path.equals(openFile.path) : openFile.path != null) {
            return false;
        }
        if (user != null ? !user.equals(openFile.user) : openFile.user != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {

        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (handle != null ? handle.hashCode() : 0);
        result = 31 * result + (outputStream != null ? outputStream.hashCode() : 0);
        result = 31 * result + (inputStream != null ? inputStream.hashCode() : 0);
        result = 31 * result + (int) (readOffset ^ (readOffset >>> 32));
        result = 31 * result + (int) (writeOffset ^ (writeOffset >>> 32));
        result = 31 * result + (user != null ? user.hashCode() : 0);
        result = 31 * result + (dataDomain != null ? dataDomain.hashCode() : 0);
        result = 31 * result + flag;
        result = 31 * result + (connectionId != null ? connectionId.hashCode() : 0);
        return result;
    }
}

/**
 * @author sudhakars
 */
public class SFTPServerHelper {

    private static final Logger LOG = LoggerFactory.getLogger(SFTPServerHelper.class);


    /**
     * fileSystemEntryService
     */

    private FileSystemEntryDAO fileSystemEntryDAO;

    /**
     * blobService
     */
    private BlobDAO blobDAO;

    /**
     * userProfileDAO
     */
    private UserProfileDAO userProfileDAO;

    /**
     * securityManager
     */
    private org.apache.shiro.mgt.SecurityManager manager;

    /**
     * SFTP Server user DAO implementation to be used
     */
    private SFTPServerUserDAO serverUserDAO;
    /**
     * openFileKeyService service to get OpenFileKey from pool.
     */
    private OpenFileKeyService openFileKeyService;

    /**
     * Length of time in hours to wait to expire entries from the files and handles caches.  Long time
     * to account for large file transfers, but want them to expire unused entries eventually so can be GC.
     */
    private int handleAndFileExpirationHours = 36;

    //TODO: CacheBuilder lets you add a removal handler, but it is called on every removal not only expiration
    //TODO: but also removal called from the code.  Afraid would break w/o refactoring fileClose significantly
    //TODO: because all of these maps are interdependent

    // Tracks openFiles by connectionId and Path
    /**
     * Open Files
     */
    protected Cache<OpenFileKey, OpenFile> openFiles = CacheBuilder.newBuilder().
            expireAfterAccess(handleAndFileExpirationHours, TimeUnit.HOURS).build();

    // Maps a handle to a Path
    /**
     * Handles 2 path map
     */
    protected Cache<String, String> handle2Path = CacheBuilder.newBuilder().
            expireAfterAccess(handleAndFileExpirationHours, TimeUnit.HOURS).build();

    /**
     * The hashmap of currently open blob streams;
     */
    protected Cache<BlobHandle, GFSBlobOutputStream> openBlobs = CacheBuilder.newBuilder().
            expireAfterAccess(handleAndFileExpirationHours, TimeUnit.HOURS).build();

    /**
     * The hashmap of currently open blobs handles;
     */
    protected Cache<String, BlobHandle> openHandles = CacheBuilder.newBuilder().
            expireAfterAccess(handleAndFileExpirationHours, TimeUnit.HOURS).build();

    /**
     * variable  used for tracking if both factors are authenticated when auth is password + public key
     */
    protected Cache<String, String> partialAuths = CacheBuilder.newBuilder().
            expireAfterAccess(5, TimeUnit.MINUTES).build();

    /**
     * to remove headers from public key
     */
    private final String beginPublicKey = "---- BEGIN SSH2 PUBLIC KEY ----";
    private final String endPublicKey = "---- END SSH2 PUBLIC KEY ----";


    /**
     * constructor
     */
    public SFTPServerHelper() {

    }


    /**
     * @param blobDAO blobDAO
     */
    public void setBlobDAO(BlobDAO blobDAO) {

        this.blobDAO = blobDAO;
    }


    /**
     * @param userProfileDAO user profile Dao
     */
    public void setUserProfileDAO(UserProfileDAO userProfileDAO) {

        this.userProfileDAO = userProfileDAO;
    }

    /**
     * @param manager multi tenant manager
     */

    public void setManager(org.apache.shiro.mgt.SecurityManager manager) {

        this.manager = manager;
    }


    /**
     * @param fileSystemEntryDAO - filesytemEntryDAO
     */
    public void setFileSystemEntryDAO(FileSystemEntryDAO fileSystemEntryDAO) {

        this.fileSystemEntryDAO = fileSystemEntryDAO;
    }

    /**
     * @param serverUserDAO the serverUserDAO to be set
     */
    public void setServerUserDAO(SFTPServerUserDAO serverUserDAO) {

        this.serverUserDAO = serverUserDAO;
    }

    /**
     * @return the openFileKeyService
     */
    public OpenFileKeyService getOpenFileKeyService() {

        return openFileKeyService;
    }


    /**
     * @param openFileKeyService the openFileKeyService to set
     */
    public void setOpenFileKeyService(OpenFileKeyService openFileKeyService) {

        this.openFileKeyService = openFileKeyService;
    }


    /**
     * creates new handle
     *
     * @param fullPathName path name
     * @param metaData     meta data
     * @return handle
     */
    String createNewHandle(String fullPathName, BlobMetaData metaData) {

        BlobHandle handle = createBlobHandle(fullPathName, metaData);
        return handle.toString();
    }

    /**
     * close Handle for given handle string
     *
     * @param shandle handle
     * @throws B2BTransactionFailed if the blobHandle could not created
     * @throws IOException          input output error occured
     */
    void closeHandle(String shandle) throws B2BTransactionFailed, IOException {

        BlobHandle handle = getBlobHandleForString(shandle);

        if (handle != null) {
            closeOutputStreamForBlobHandle(handle);
            openHandles.asMap().remove(shandle);
        }
        else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("handle could not found for handle:" + shandle + " close ignored");
            }
        }
    }


    /**
     * gets output Stream for given String handle
     *
     * @param shandle handle name
     * @return {@link OutputStream}
     */
    OutputStream getOutputStreamForHandle(String shandle) {

        BlobHandle handle = getBlobHandleForString(shandle);
        BlobOutputStream stream = null;
        if (handle != null) {
            stream = getStreamForHandle(handle);
            if (stream == null) {
                stream = openOutputStreamForBlob(handle);
            }
        }
        else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("handle could not found for handle:" + shandle + " get returning null");
            }
        }
        return stream;
    }


    /**
     * @param event  {@link ipworksssh.SftpserverSSHUserAuthRequestEvent}
     * @param server (@{@link SFTPServer}
     * @return created for successfully logged in User
     * @throws ipworksssh.IPWorksSSHException - something went wrong
     */
    public SFTPSession authenticateSFTPUser(SftpserverSSHUserAuthRequestEvent event, SFTPServer server) throws
            IPWorksSSHException {

        if (manager == null) {
            throw new IllegalStateException("Security Manager was not set");
        }

        event.accept = false;

        AuthenticationType userAuthType = null;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Sftp User   Authentication Type  setting ::");
        }

        String userId = null;
        String realm = null;

        String[] splitUserId = event.user.split("\\.");

        if (splitUserId.length == 2) {
            userId = splitUserId[1];
            realm = splitUserId[0];

            if (LOG.isDebugEnabled()) {
                LOG.debug("Found user and Realm: UserId:" + userId + " Realm:" + realm);
            }

        }
        else {

            if (LOG.isErrorEnabled()) {
                LOG.error("User was not in the correct format.  Format should be realm.userId but split by . " +
                        "returned:" + splitUserId.length + " UserId Provided:" + event.user);
            }


            server.config("SFTPErrorMessage[" + event.connectionId + "]=User is not in the correct format.  realm" +
                    ".userid" + " user provided:" + event.user);

            //FIXME need to add this error text back to the stream so we can return a message

            return null;
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting Authentication type for Sftp User  ::" + userId
                    + " realm refname  ::" + realm);
        }

        try {

            userAuthType = serverUserDAO.getAuthenticationTypeForUser(userId, realm);

            if (userAuthType == null) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not retrieve userAuth Type");
                }

                throw new IllegalStateException("Authentication type  is null");
            }
            else if (LOG.isDebugEnabled()) {
                LOG.debug("Sftp User ::" + event.user + " has  Authentication Type ::"
                        + userAuthType);
            }

        } catch (B2BNotFoundException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Attempt to retrieve authentication type for user with userID :: " + userId + " failed " +
                        "with reason- authentication type not found.  This indiciateds a configuration error");

                e.printStackTrace();

            }
            event.accept = false;

        }

        Subject subject;

        if (event.authMethod.equals("password")) {

            UsernamePasswordToken uptoken = new UsernamePasswordToken();
            uptoken.setUsername(event.user);

            if (event.authParam == null || event.authParam.isEmpty()) {

                if (LOG.isErrorEnabled()) {
                    LOG.error("auth parameter does not contain the user's password???");
                }


                event.accept = false;
            }
            else {
                uptoken.setPassword(event.authParam.toCharArray());

                // Build a subject using our manager vs. the default
                // SecurityUtils.getSubject() would get the multi-tenant subject
                // which we don't want.

                subject = loginUser(uptoken, event, server);

                if (subject == null) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Could not login because the subject could not be resolved for the token:" +
                                " userId:" + uptoken.getPrincipal());
                    }

                    event.accept = false;

                }
                else if (userAuthType.equals(AuthenticationType.Password)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Attempting   Password  Authentication  only  for User  :: " + event.user);
                    }

                    event.accept = true;


                    return createSFTPSession(event, subject);

                }
                else if (userAuthType.equals(AuthenticationType.PublicKey)) {
                    event.accept = false;
                    //couldn't log in b/c wrong type of login

                }
                else if (userAuthType.equals(AuthenticationType.PasswordAndPublicKey)) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(" perForming Password and Public Authentication for user ::" + event.user);
                    }
                    if (!partialAuths.asMap().containsKey(event.user)) {
                        partialAuths.asMap().put(event.user, "password");
                        event.partialSuccess = true;
                        //password worked but there will be another connect for public key?
                    }
                    else if ("password".equals(partialAuths.asMap().get(event.user))) {
                        //they used password again..
                        event.partialSuccess = true;
                    }
                    else if ("key".equals(partialAuths.asMap().get(event.user))) {
                        //they already did key successfully
                        event.accept = true;
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Login successful for  password and  Public key token");
                        }
                        partialAuths.asMap().remove(event.user);
                        return createSFTPSession(event, subject);
                    }
                }
            }
            //couldn't log in b/c of credentials
        }
        else if (event.authMethod.equals("publickey")) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Attempting  PublicKey Authentication  with public Key :: " + event.authParam);
            }
            String publicKey = event.authParam;
            if (publicKey.contains(beginPublicKey)) {
                publicKey = publicKey.replace(beginPublicKey, "");
            }
            if (publicKey.contains(endPublicKey)) {
                publicKey = publicKey.replace(endPublicKey, "");
            }
            publicKey = publicKey.trim();
            PublicKeyToken publicKeyToken = new PublicKeyToken(event.user, publicKey);
            subject = loginUser(publicKeyToken, event, server);

            if (subject == null) {
                event.accept = false;
                //failed b/c not valid
            }
            else if (userAuthType == AuthenticationType.Password) {
                event.accept = false;
                //failed b/c wrong type auth
            }
            else if (userAuthType.equals(AuthenticationType.PublicKey)) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Attempting   PublicKey Authentication  only  for User  :: " + event.user);
                }
                event.accept = true;
                return createSFTPSession(event, subject);
            }
            else if (userAuthType.equals(AuthenticationType.PasswordAndPublicKey)) {
                if (!partialAuths.asMap().containsKey(event.user)) {
                    partialAuths.asMap().put(event.user, "key");
                    event.partialSuccess = true;
                    //key worked but still have to check password in another call?
                }
                else if ("key".equals(partialAuths.asMap().get(event.user))) {
                    //they used key again..
                    event.partialSuccess = true;
                }
                else if ("password".equals(partialAuths.asMap().get(event.user))) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Login successful for  password and  Public key token");
                    }
                    event.accept = true;
                    partialAuths.asMap().remove(event.user);
                    return createSFTPSession(event, subject);
                }
            }
        }
        else if (event.authMethod.equals("none")) {
            if (userAuthType.equals(AuthenticationType.PasswordAndPublicKey)) {
                event.availableMethods = "password,publickey";
            }
            else if (userAuthType.equals(AuthenticationType.Password)) {
                event.availableMethods = "password";
            }
            else if (userAuthType.equals(AuthenticationType.PublicKey)) {
                event.availableMethods = "publickey";
            }
            else {
                event.accept = false;
            }

        }
        else if (LOG.isErrorEnabled()) {
            LOG.error("Attempt to authenticate with an unsupported auth method:"
                    + event.authMethod);
        }
        return null;
    }


    /**
     * a utility method creates a session for logged in subject and sftp user
     *
     * @param event   Authentication event
     * @param subject @Subject
     */
    private SFTPSession createSFTPSession(SftpserverSSHUserAuthRequestEvent event, Subject subject) {

        SFTPSession sftpSession = new SFTPSession();
        sftpSession.connectionId = event.connectionId;
        sftpSession.user = event.user;
        sftpSession.subject = subject;

        SFTPServerUser serverUser = (SFTPServerUser) SecurityUtils.getSubject().getSession().getAttribute("sftpUser");

        if (serverUser == null) {

            throw new IllegalArgumentException("Sftp Server User Instance is null ");
        }

        sftpSession.setSftpServerUser(serverUser);
/*
        if (LOG.isDebugEnabled()) {

            LOG.debug("setting home directory   :: " +
                    "" + serverUser.getRootDirectory() + "   for user  ::" + serverUser.getUserId());
            }

         event.homeDir=serverUser.getRootDirectory();
     */


        return sftpSession;

    }


    /**
     * a utility method to login a subject with given AuthenticationToken
     *
     * @param uptoken @AuthenticationToken
     * @param event   @SftpserverSSHUserAuthRequestEvent
     * @param server
     * @return logged in subject
     */
    private Subject loginUser(AuthenticationToken uptoken,
                              SftpserverSSHUserAuthRequestEvent event, SFTPServer server) {

        Subject subject = new Subject.Builder(manager).
                host(server.getConnections().get(event.connectionId).getRemoteHost())
                .contextAttribute("host", server.getConnections().
                        get(event.connectionId).getRemoteHost()).buildSubject();


        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Attempting to login with given credentials");

                LOG.debug(">UserId:" + uptoken.getPrincipal());
                LOG.debug(">Password:" + uptoken.getCredentials());
            }

            subject.login(uptoken);

            return subject;

        } catch (AuthenticationException ex) {
            ex.printStackTrace();
            if (LOG.isInfoEnabled()) {
                LOG.info("Failed attempt to login:" + ex.getMessage());
            }
            event.accept = false;

            return null;
        }

    }

    /**
     * creates a directory or returns the status code due to the failure
     *
     * @param session        the session that is making the call.
     * @param path           the directory we want to create
     * @param sftpServerUser the sftpServer User that is creating the directory
     * @return that status code to return
     */
    int createDirectory(SFTPSession session, String path, SFTPServerUser sftpServerUser) {

        int statusCode = SFTPServer.SSH_FX_OK;

       /* UserProfile ownerUserProfile = null;
        try {
            ownerUserProfile = userProfileDAO.getByRefName(sftpServerUser.getB2bMailboxUserProfileRef().getRefName(),
                    sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());

        } catch (B2BTransactionFailed e) {

            statusCode = SFTPServer.SSH_FX_OWNER_INVALID;
            e.printStackTrace();
        }

        if (ownerUserProfile == null) {
            statusCode = SFTPServer.SSH_FX_OWNER_INVALID;
            if (LOG.isErrorEnabled()) {
                LOG.error("user Profile with refName:" +
                        sftpServerUser.getB2bMailboxUserProfileRef() + " could not be found in dataDomain" +
                        sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());

            }
        } */


        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating directory with the name :: "
                    + path);
        }

        try {
            /**
             * Get parent of the directory.
             */
            if (path.contains("/")) {
                String parentPath = path.substring(0, path.lastIndexOf('/'));

                if (LOG.isDebugEnabled()) {
                    LOG.debug("Fetching parent directory from path :: "
                            + parentPath);
                }
                FileSystemEntry parentFileSystemEntry = fileSystemEntryDAO
                        .getByRefName(parentPath,
                                sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());
                if (parentFileSystemEntry != null) {
                    FileSystemEntry fileSystemEntry = fileSystemEntryDAO
                            .getByRefName(path,
                                    sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());
                    if (fileSystemEntry != null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Already exist directory :: dirName = "
                                    + path);
                        }

                        statusCode = SFTPServer.SSH_FX_FILE_ALREADY_EXISTS;


                    }
                    else {
                        String childPath = path.substring(path
                                .lastIndexOf('/') + 1);
                        fileSystemEntry = new FileSystemEntry();
                        fileSystemEntry.setRefName(childPath);
                        fileSystemEntry
                                .setType(FileSystemEntryType.Directory);
                        fileSystemEntry.setName(childPath);
                        fileSystemEntry
                                .setParentFileEntryId(parentFileSystemEntry
                                        .getId());
                        fileSystemEntry.setOwnerUserProfileRefName(sftpServerUser.getB2bMailboxUserProfileRef()
                                .getRefName());
                        fileSystemEntry.setDataDomain(sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());
                        fileSystemEntry.setCreateDate(Calendar.getInstance().getTime());
                        fileSystemEntry.setUpdateDate(Calendar.getInstance().getTime());

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Fetching childPath from the path :: "
                                    + childPath);
                        }
                        fileSystemEntryDAO
                                .save(fileSystemEntry);
                    }
                }
                else {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Parent directory does not exist :: dirName = "
                                + path);
                    }

                    statusCode = SFTPServer.SSH_FX_NO_SUCH_PATH;

                }
            }
            else {
                FileSystemEntry fileSystemEntry = fileSystemEntryDAO
                        .getByRefName(path, sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());
                if (fileSystemEntry != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Already exist directory :: dirName = "
                                + path);
                    }

                    statusCode = SFTPServer.SSH_FX_FILE_ALREADY_EXISTS; // SSH_FX_FILE_ALREADY_EXISTS


                }
                else {
                    fileSystemEntry = new FileSystemEntry();
                    fileSystemEntry.setRefName(path);
                    fileSystemEntry
                            .setType(FileSystemEntryType.Directory);
                    fileSystemEntry.setName(path);
                    fileSystemEntry.setOwnerUserProfileRefName(sftpServerUser.getB2bMailboxUserProfileRef()
                            .getDataDomain());
                    fileSystemEntryDAO
                            .save(fileSystemEntry);
                }
            }
        } catch (B2BTransactionFailed ex) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception occurred while saving fileSystemEntry threw :: "
                        + ex.getMessage());
            }

            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        } catch (B2BNotFoundException ex) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception occurred while saving fileSystemEntry threw :: "
                        + ex.getMessage());
            }

            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        } catch (ValidationException e) {
            e.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        }

        return statusCode;
    }


    /**
     * to List the Directory for  give path
     *
     * @param path       PATH
     * @param dataDomain the dataDomain to use
     * @return Array of directory name String
     */
    String[] getDirectoryListing(String path, String dataDomain) {


        String[] list = null;

        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting entries with parent:" + path);
            }

            List<FileSystemEntry> entries;

            if (path.equals("/")) {
                entries = fileSystemEntryDAO
                        .findEntriesWithParent(0, -1, null, null, null, null);
            }
            else {
                FileSystemEntry fileSystemEntry =
                        fileSystemEntryDAO.getByRefName(path,
                                dataDomain);

                if (LOG.isDebugEnabled()) {
                    LOG.debug("fileSystemEntry instance id :: "
                            + fileSystemEntry);
                }
                entries = fileSystemEntryDAO
                        .findEntriesWithParent(0, -1, fileSystemEntry.getId(), null, null, null);
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("Returned:" + entries.size() + " entries");
            }


            if (!entries.isEmpty()) {
                list = new String[entries.size()];
                int i = 0;

                for (FileSystemEntry entry : entries) {
                    list[i] = entry.getName();

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Putting values in the lastDirectoryListing :: with name :: "
                                + "/" + entry.getRefName());
                    }

                    i++;
                }
            }
            else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(">> Query returned nothing");
                }
            }

        } catch (B2BException ex) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception occurred while getting fileSystemEntries threw :: "
                        + ex.getMessage());
            }

            ex.printStackTrace();


        }

        return list;
    }


    /**
     * @param pathRef    directory to be removed
     * @param dataDomain the dataDomain to use
     * @return statusCode
     */
    public int dirRemove(String pathRef, String dataDomain) {

        int statusCode = SFTPServer.SSH_FX_OK;
        FileSystemEntry fileSystemEntry;
        try {
            String path = pathRef.replaceFirst("/", "");
            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting FileSystemEntry to be removed with refName :: "
                        + path);
            }
            fileSystemEntry = fileSystemEntryDAO.getByRefName(
                    path, dataDomain);
            if (fileSystemEntry != null) {
                deleteChildFileSystemEntry(fileSystemEntry);

                fileSystemEntryDAO.delete(fileSystemEntry);

            }
            else {
                statusCode = SFTPServer.SSH_FX_NO_SUCH_PATH;
            }
        } catch (B2BTransactionFailed ex) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception occurred while saving fileSystemEntry threw :: "
                        + ex.getMessage());
            }

            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        } catch (B2BNotFoundException ex) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception occurred while saving fileSystemEntry threw :: "
                        + ex.getMessage());
            }

            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        }
        return statusCode;
    }

    /**
     * @param fileSystemEntry {@link FileSystemEntry} to be deleted
     * @throws B2BTransactionFailed if the blobHandle could not created
     * @throws B2BNotFoundException not found
     */
    protected void deleteChildFileSystemEntry(FileSystemEntry fileSystemEntry)
            throws B2BNotFoundException, B2BTransactionFailed {

        List<FileSystemEntry> childFileSystemEntries = fileSystemEntryDAO
                .findEntriesWithParent(0, -1, fileSystemEntry.getId(), null, null, null);

        for (FileSystemEntry entry : childFileSystemEntries) {
            deleteChildFileSystemEntry(entry);
            if (LOG.isDebugEnabled()) {
                LOG.debug("Child Found, removing child first with refName:: "
                        + entry.getRefName());
            }
            fileSystemEntryDAO.delete(entry);
        }
    }


    /**
     * @param filePath   file to be removed
     * @param dataDomain - the dataDomain to use
     * @return statusCode status code of the remove
     */
    public int fileRemove(String filePath, String dataDomain) {

        int statusCode = SFTPServer.SSH_FX_OK;


        FileSystemEntry fileSystemEntry;

        try {
            fileSystemEntry = fileSystemEntryDAO.getByRefName(filePath, dataDomain);
            deleteChildFileSystemEntry(fileSystemEntry);
            fileSystemEntryDAO.delete(fileSystemEntry);
            if (LOG.isDebugEnabled()) {
                LOG.debug("FileSystemEntry removed successfully from the path >>> "
                        + filePath);
            }
        } catch (B2BTransactionFailed e) {
            statusCode = SFTPServer.SSH_FX_FAILURE;

            e.printStackTrace();

        } catch (B2BNotFoundException e) {
            statusCode = SFTPServer.SSH_FX_NO_SUCH_FILE;
            e.printStackTrace();
        }

        return statusCode;

    }


    /**
     * @param path           file to be renamed
     * @param newPath        new file name
     * @param sftpServerUser logged in sftpServerUser
     * @return statusCode
     */
    public int fileRename(String path, String newPath, SFTPServerUser sftpServerUser) {

        int statusCode = SFTPServer.SSH_FX_OK;

        if (LOG.isDebugEnabled()) {
            LOG.debug("Renaming from path:" + path + " to newPath:" + newPath);
        }

        UserProfile ownerUserProfile = null;

       /* try {

            ownerUserProfile = userProfileDAO.getByRefName(sftpServerUser.getB2bMailboxUserProfileRef().getRefName(),
                    sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());

        } catch (B2BTransactionFailed e) {

            statusCode = SFTPServer.SSH_FX_FAILURE;

            e.printStackTrace();

            return statusCode;
        }

        if (ownerUserProfile == null) {
            if (LOG.isErrorEnabled()) {
                LOG.error("user Profile with RefName:" +
                                sftpServerUser.getB2bMailboxUserProfileRef() + " could not be found in dataDomain ",
                        sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());
            }

            statusCode = SFTPServer.SSH_FX_FAILURE;

            return statusCode;

        } */

        String actualPath = getOriginalPath(sftpServerUser.getRootDirectory(), path);

        String actualNewPath = getOriginalPath(sftpServerUser.getRootDirectory(), newPath);
        try {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Earlier refName :: " + actualPath);
            }
            if (LOG.isDebugEnabled()) {
                LOG.debug("NewRefName :: " + actualPath);
            }

            String fileName;

            if (actualNewPath.contains("/")) {
                fileName = actualNewPath.substring(actualNewPath
                        .lastIndexOf('/') + 1);
            }
            else {
                fileName = actualNewPath;
            }

            String parentRefName = null;
            String parentFileSystemId = null;

            if (actualNewPath.contains("/")) {
                parentRefName = actualNewPath.substring(0,
                        actualNewPath.lastIndexOf('/'));
                if (LOG.isDebugEnabled()) {
                    LOG.debug("parentRefName >>>>>>>  " + parentRefName);
                }
                FileSystemEntry entry = fileSystemEntryDAO
                        .getByRefName(parentRefName, ownerUserProfile.getDataDomain());
                parentFileSystemId = entry.getId();
            }

            FileSystemEntry fileSystemEntry = fileSystemEntryDAO
                    .getByRefName(actualPath, ownerUserProfile.getDataDomain());

            if (parentFileSystemId != null) {
                fileSystemEntry
                        .setParentFileEntryId(parentFileSystemId);
            }

            if (fileSystemEntry == null) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Could not find the fileSystemEntry with refName:" + actualPath + " in dataDomain:" +
                            ownerUserProfile.getDataDomain());
                }

                statusCode = SFTPServer.SSH_FX_FAILURE;

            }
            else {
                fileSystemEntry.setRefName(actualNewPath);
                fileSystemEntry.setName(fileName);
                fileSystemEntry.setOwnerUserProfileRefName(ownerUserProfile.getRefName());

                fileSystemEntry = fileSystemEntryDAO
                        .save(fileSystemEntry);

                renameChildFileEntry(fileSystemEntry, actualPath, actualNewPath, ownerUserProfile.getRefName());

                if (LOG.isDebugEnabled()) {
                    LOG.debug("File/Directory renamed successfully with the name :: "
                            + fileSystemEntry.getRefName());
                }
            }


        } catch (B2BTransactionFailed ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Exception occurred while saving fileSystemEntry threw :: "
                        + ex.getMessage());
            }

            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        } catch (B2BNotFoundException ex) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Exception occurred while saving fileSystemEntry threw :: "
                        + ex.getMessage());
            }

            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        } catch (ValidationException e) {
            e.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        }

        return statusCode;
    }

    /**
     * @param fileSystemEntry         @fileSystemEntry
     * @param refName                 old file name
     * @param newRefName              new  File Name
     * @param ownerUserProfileRefName - the ownerUserProfileId
     * @throws B2BTransactionFailed transaction failed
     * @throws B2BNotFoundException not found
     */
    private void renameChildFileEntry(FileSystemEntry fileSystemEntry, String refName,
                                      String newRefName, String ownerUserProfileRefName) throws B2BNotFoundException,
            B2BTransactionFailed, ValidationException {

        List<FileSystemEntry> childFileEntryList = fileSystemEntryDAO
                .findEntriesWithParent(0, -1, fileSystemEntry.getId(), null, null, null);

        for (FileSystemEntry child : childFileEntryList) {

            renameChildFileEntry(child, refName, newRefName, ownerUserProfileRefName);

            String childRefName = child.getRefName().replace(
                    refName, newRefName);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Child Found and changing :: "
                        + childRefName);
            }
            child.setRefName(childRefName);

            child.setOwnerUserProfileRefName(ownerUserProfileRefName);

            FileSystemEntry fileEntry = fileSystemEntryDAO
                    .save(child);

            if (LOG.isDebugEnabled()) {
                LOG.debug("Child File/Directory renamed successfully with the name :: "
                        + child.getRefName());
                LOG.debug("Saved File entry with refName :: "
                        + fileEntry.getRefName());
            }

        }
    }

    /**
     * @param path           path of file
     * @param connectionId   connection ID
     * @param handle         handle name
     * @param sftpServerUser server user
     * @return statusCode
     */
    public int fileClose(String path, String connectionId, String handle, SFTPServerUser sftpServerUser) {

        int statusCode = SFTPServer.UNSET_STATUS;
        OpenFileKey k = null;
        try {
            k = openFileKeyService.newOpenFileKey();
        }//CHECKSTYLE:OFF
        catch (Exception ex) {
            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        }//CHECKSTYLE:ON
        k.connectionId = connectionId;
        k.path = path;

        OpenFile file = openFiles.asMap().remove(k);
       /* UserProfile ownerUserProfile = null;
        try {
            ownerUserProfile = userProfileDAO.getByRefName(sftpServerUser.getB2bMailboxUserProfileRef().getRefName(),
                    sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());
        } catch (B2BTransactionFailed e) {

            e.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
            return statusCode;
        }

        if (ownerUserProfile == null) {
            String message = "user Profile with refName:" +
                    sftpServerUser.getB2bMailboxUserProfileRef().getRefName() + " could not be found in datadomain" +
                    sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain();

            if (LOG.isErrorEnabled()) {
                LOG.error(message);
            }

            B2BNotFoundException ex = new B2BNotFoundException(message);
            ex.printStackTrace();

            statusCode = SFTPServer.SSH_FX_FAILURE;
            return statusCode;
        } */


        if (file != null) {
            /**
             * Close inputStream or outputStream if open.
             */
            try {
                if (file.getOutputStream() != null) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Handle from the openFile >> " + file.getHandle());
                    }
                    closeHandle(file.getHandle());
                    String refName = file.path;

                    LOG.debug("Getting file path from file >> " + refName);
                    String blobId = handle;
                    String parentFileEntryId = null;
                    String fileName = null;
                    if (refName != null && refName.contains("/")) {
                        String parentRefName = refName.substring(0, refName.lastIndexOf('/'));
                        fileName = refName.substring(refName.lastIndexOf('/') + 1);
                        LOG.debug("parent found with refName >> " + parentRefName);
                        FileSystemEntry pareFileSystemEntry = fileSystemEntryDAO
                                .getByRefName(parentRefName, sftpServerUser.getB2bMailboxUserProfileRef()
                                        .getDataDomain());
                        LOG.debug("parent fileSystem found >> " + pareFileSystemEntry);
                        parentFileEntryId = pareFileSystemEntry.getId();
                    }
                    else {
                        fileName = refName;
                    }
                    Blob blob = blobDAO.getById(blobId);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Blob found >>>>>>> " + blob);
                    }
                    FileSystemEntry fileSystemEntry = new FileSystemEntry();
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Blob found and blobIdAsString >>>>>>> "
                                + blob.getIdAsString());
                    }
                    fileSystemEntry.setBlobId(blob.getIdAsString());
                    AuditInfo auditInfo = new AuditInfo();
                    auditInfo.setCreateUser(sftpServerUser.getUserId());
                    auditInfo.setCreationTs(new Date());
                    fileSystemEntry.setAuditInfo(auditInfo);
                    fileSystemEntry
                            .setContentType("application/octet-stream");
                    fileSystemEntry.setCreateDate(new Date());
                    fileSystemEntry
                            .setDataDomain(sftpServerUser.getDataDomainToUserForNewFiles());

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Blob found and blob.getMD5() >>>>>>> "
                                + blob.getMD5());
                    }
                    fileSystemEntry.setMd5Hash(blob.getMD5());
                    fileSystemEntry.setName(fileName);
                    if (parentFileEntryId != null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("ParentFileEntry found ID is >>>> "
                                    + parentFileEntryId);
                        }
                        fileSystemEntry
                                .setParentFileEntryId(parentFileEntryId);
                    }
                    fileSystemEntry.setRefName(refName);
                    fileSystemEntry.setSize(blob.getSize());
                    fileSystemEntry
                            .setType(FileSystemEntryType.File);
                    fileSystemEntry.setOwnerUserProfileRefName(sftpServerUser.getB2bMailboxUserProfileRef().getRefName
                            ());

                    fileSystemEntryDAO.save(fileSystemEntry);
                }
                else if (file.getInputStream() != null) {
                    file.getInputStream().close();
                }
                handle2Path.asMap().remove(connectionId + ":" + file.getHandle());
            } catch (IOException ex) {

                if (LOG.isWarnEnabled()) {
                    LOG.warn("Exception occured while closing the stream >> "
                            + ex.getMessage());
                }
                statusCode = SFTPServer.SSH_FX_FAILURE;


            } catch (B2BTransactionFailed ex) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Exception occurred while closing the stream >> "
                            + ex.getMessage());
                }
                statusCode = SFTPServer.SSH_FX_FAILURE;

            } catch (B2BNotFoundException ex) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Exception occurred while closing the stream >> "
                            + ex.getMessage());
                }
                statusCode = SFTPServer.SSH_FX_FAILURE;

            } catch (ValidationException e) {
                e.printStackTrace();
                statusCode = SFTPServer.SSH_FX_FAILURE;
            }


            if (LOG.isDebugEnabled()) {
                LOG.debug("Closed file with path:" + path + " connection:" + connectionId + "Status Code: "
                        + statusCode);
            }

            if (statusCode == SFTPServer.UNSET_STATUS) {
                statusCode = SFTPServer.SSH_FX_OK;
            }

        }
        try {
            //return to pool.
            openFileKeyService.returnOpenFileKey(k);
        }//CHECKSTYLE:OFF
        catch (Exception ex) {
            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        }//CHECKSTYLE:ON

        if (statusCode == SFTPServer.UNSET_STATUS) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Logic error, setting status to ok but was not set");
            }

            statusCode = SFTPServer.SSH_FX_OK;
        }
        return statusCode;
    }


    /**
     * @param event          {@link SftpserverFileOpenEvent}
     * @param server         {@link SFTPServer}
     * @param sftpServerUser {@link SFTPServerUser}
     * @return statusCode
     */
    public int fileOpen(SftpserverFileOpenEvent event, SFTPServer server, SFTPServerUser sftpServerUser) {

        int statusCode = SFTPServer.SSH_FX_OK;

        String path = getOriginalPath(sftpServerUser.getRootDirectory(), event.path);

        OpenFileKey k = null;
        try {
            k = openFileKeyService.newOpenFileKey();
        }//CHECKSTYLE:OFF
        catch (Exception ex) {
            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        }//CHECKSTYLE:ON
        k.connectionId = event.connectionId;
        k.path = path;

        OpenFile existingOpenFile = openFiles.asMap().get(k);

       /* UserProfile ownerUserProfile = null;
        try {
            ownerUserProfile = userProfileDAO.getByRefName(sftpServerUser.getB2bMailboxUserProfileRef().getRefName(),
                    sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());
        } catch (B2BTransactionFailed e) {
            statusCode = SFTPServer.SSH_FX_OWNER_INVALID;
            e.printStackTrace();
        }

        if (ownerUserProfile == null) {
            throw new IllegalStateException("user Profile with RefName:" +
                    sftpServerUser.getB2bMailboxUserProfileRef().getRefName() + " could not be found in dataDomain " +
                    sftpServerUser.getB2bMailboxUserProfileRef().getDataDomain());
        } */


        try {

            FileSystemEntry entry = fileSystemEntryDAO.getByRefName(path, sftpServerUser.getB2bMailboxUserProfileRef
                    ().getDataDomain());

            if (entry != null) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Found entry for path:" + path);
                    LOG.debug("entry Type:" + entry.getType());
                    LOG.debug("entrySize:" + entry.getSize());
                    LOG.debug("entryCreateDate:" + entry.getCreateDate().toString());
                }

                if (entry.getType().equals(FileSystemEntryType.File)
                        && (event.flags & SFTPServer.SSH_FXF_READ) != 0) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug(">> Opening file for read");
                    }


                    if (existingOpenFile != null) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("File is already open ");
                        }

                        if ((existingOpenFile.getFlag() & SFTPServer.SSH_FXF_EXCL) != 0) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug("   !!File is already open with the exclusive flag set.");
                            }

                            statusCode = SFTPServer.SSH_FX_BYTE_RANGE_LOCK_REFUSED;
                            // SSH_FX_RANGE_LOCK_REFUSED

                        }
                        else {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("concurrently accessing file with path:" + path);
                            }
                        }
                    }

                    event.fileSize = entry.getSize();
                    event.fileType = 1;
                    event.fileCreateTime = entry.getCreateDate().getTime();

                    OpenFile of = new OpenFile();
                    of.setConnectionId(event.connectionId);
                    of.setPath(path);

                    String blobId = entry.getBlobId();
                    Blob b = blobDAO.getById(blobId);
                    InputStream in = b.getInputStream();
                    of.setInputStream(in);
                    of.setFlag(event.flags);
                    of.setHandle(b.getIdAsString());
                    event.handle = of.getHandle();

                    of.setDataDomain(entry.getDataDomain());
                    of.setUser(event.user);

                    openFiles.put(of.getKey(), of);
                    handle2Path.put(event.connectionId + ":" + of.getHandle(), path);

                    statusCode = SFTPServer.SSH_FX_OK;

                }
                else if ((event.flags & SFTPServer.SSH_FXF_CREAT) != 0) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Attempt to create a file that already exists, path:" + path);
                    }

                    if (LOG.isDebugEnabled()) {

                        LOG.debug("expectedFileSize >>>> " + event.fileSize);

                    }

                    String fileName = path.substring(path.lastIndexOf('/') + 1);
                    //removing the existing entry
                    FileSystemEntry fileSystemEntry = fileSystemEntryDAO.
                            getByRefName(path, sftpServerUser
                                    .getB2bMailboxUserProfileRef().getDataDomain());
                    fileSystemEntryDAO.delete(fileSystemEntry);
                    //creating new blob
                    BlobMetaData metaData = blobDAO.createMetaData();
                    metaData.setPathString(fileName);
                    SFTPConnection c = server.getConnections().get(event.connectionId);
                    metaData.setOriginIpAddress(c.getRemoteHost());
                    metaData.setOwnerId(event.user);
//                                metaData.setDataDomain(e.user);
                    metaData.setDataDomain(sftpServerUser.getDataDomainToUserForNewFiles());
                    metaData.setTxId(event.connectionId);
                    String handle = createNewHandle(path, metaData);
                                /*blobService.createBlobHandle(e.path,metaData);*/
                    OutputStream out = getOutputStreamForHandle(handle);
                    //blobService.getStreamForHandle (handle);


                    OpenFile openFile = new OpenFile();
                    openFile.connectionId = event.connectionId;
                    openFile.path = path;
                    openFile.setUser(event.user);
                    openFile.setFlag(event.flags);
                    openFile.setOutputStream(out);
                    event.handle = handle;
                    openFile.setHandle(event.handle);

                    LOG.debug("Setting handle for the open file :: handle >> "
                            + openFile.getHandle() + " :: openFile" +
                            ".getKey() >> " + openFile.getKey());

                    openFiles.put(openFile.getKey(), openFile);
                    handle2Path.put(event.connectionId + ":" + openFile.getHandle(), path);

                    statusCode = SFTPServer.SSH_FX_OK; // SSH_FX_FILE_ALREADY_EXISTS

                }
                else {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Unsupported operation");
                    }

                    statusCode = SFTPServer.SSH_FX_OP_UNSUPPORTED; // SSH_FX_OP_UNSUPPORTED
                }


            }
            else // existing file entry not found
            {
                if (existingOpenFile != null) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("No file entry found yet the file server " +
                                "thinks the file is open.  Cleaning up open handle");
                    }

                    handle2Path.asMap().remove(event.connectionId + ":" + existingOpenFile.getHandle());
                    openFiles.asMap().remove(k);
                }

                if ((event.flags & SFTPServer.SSH_FXF_WRITE) != 0 |
                        (event.flags & SFTPServer.SSH_FXF_APPEND) != 0 |
                        (event.flags & SFTPServer.SSH_FXF_TRUNC) != 0) {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Opening file for write");
                    }

                    String filePath = event.path;
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("filePath >>>>>>>>>>> " + filePath);
                        LOG.debug("expectedFileSize >>>> " + event.fileSize);

                    }
                    String refName = null;
                    if (filePath.contains("/")) {
                        refName = filePath.substring(filePath.lastIndexOf('/') + 1);
                    }
                    else {
                        refName = filePath;
                    }

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("File is being stored with the refName >> "
                                + refName);
                    }
                    BlobMetaData metaData = blobDAO.createMetaData();
                    metaData.setPathString(filePath);
                    SFTPConnection c = server.getConnections().get(event.connectionId);
                    metaData.setOriginIpAddress(c.getRemoteHost());
                    metaData.setOwnerId(event.user);
//                              metaData.setDataDomain(e.user);
                    metaData.setDataDomain(sftpServerUser.getDataDomainToUserForNewFiles());
                    metaData.setTxId(event.connectionId);
                    String handle = createNewHandle(refName, metaData);
                                /*blobService.createBlobHandle(e.path,  metaData);*/
                    OutputStream out = getOutputStreamForHandle(handle);
                    //blobService.getStreamForHandle (handle);


                    OpenFile openFile = new OpenFile();
                    openFile.connectionId = event.connectionId;
                    openFile.path = path;
                    openFile.setUser(event.user);
                    openFile.setFlag(event.flags);
                    openFile.setOutputStream(out);
                    event.handle = handle;
                    openFile.setHandle(event.handle);

                    LOG.debug("Setting handle for the open file :: handle >> "
                            + openFile.getHandle() + " :: openFile" +
                            ".getKey() >> " + openFile.getKey());

                    openFiles.put(openFile.getKey(), openFile);
                    handle2Path.put(event.connectionId + ":" + openFile.getHandle(), path);

                    statusCode = SFTPServer.SSH_FX_OK;

                }
                else {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Unsupported operation");
                    }

                    statusCode = SFTPServer.SSH_FX_OP_UNSUPPORTED; // SSH_FX_OP_UNSUPPORTED
                }
            }

        } catch (B2BTransactionFailed b2BTransactionFailed) {
            b2BTransactionFailed.printStackTrace();
            event.statusCode = SFTPServer.SSH_FX_FAILURE; // SSH_FX_OPE_FAILURE
        }
        try {
            //return to pool
            openFileKeyService.returnOpenFileKey(k);
        }//CHECKSTYLE:OFF
        catch (Exception ex) {
            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        }//CHECKSTYLE:ON
        return statusCode;
    }


    /**
     * @param event  {@link SftpserverFileWriteEvent}
     * @param server {@link SFTPServer}
     * @return statusCode
     */
    public int fileWrite(SftpserverFileWriteEvent event, SFTPServer server) {

        int statusCode = SFTPServer.SSH_FX_OK;
        OpenFileKey openFileKey = null;
        try {
            openFileKey = openFileKeyService.newOpenFileKey();
        }//CHECKSTYLE:OFF
        catch (Exception ex) {
            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        }//CHECKSTYLE:ON
        openFileKey.connectionId = event.connectionId;
        String path = handle2Path.asMap().get(event.connectionId + ":" + event.handle);
        if (path == null) {
            throw new IllegalStateException("Path not found for handle:" +
                    event.handle + " for connection: " + event.connectionId + " handle2Path corrupt?? ");
        }

        openFileKey.path = path;

        OpenFile openFile = openFiles.asMap().get(openFileKey);

        if (openFile == null) {
            throw new IllegalStateException("OpenFile could not found for connection:"
                    + event.connectionId + " and " +
                    "handle:" + event.handle);
        }

        if (openFile.getHandle() == null) {
            throw new IllegalStateException
                    ("Openfile found but outputSTream is not set logic error?");
        }

        String handle = openFile.getHandle();


        OutputStream stream = getOutputStreamForHandle(handle);

        BufferedOutputStream outputStream = new BufferedOutputStream(stream);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Get outputStream >>> " + stream);
        }

        SFTPConnection connection = server.getConnections().get(
                event.connectionId);

        if (LOG.isDebugEnabled()) {
            LOG.debug("SFTPConnection Obejct >>>" + connection);
        }

        byte[] data = connection.getFileData();

        if (LOG.isDebugEnabled()) {
            LOG.debug("DATA length to be written to the file"
                    + data.length);
        }

        try {
            outputStream.write(data);
            outputStream.flush();
            if (LOG.isDebugEnabled()) {
                LOG.debug("Blob Written successfully >>>>>>>>");
            }
        }//CHECKSTYLE:OFF
        catch (IOException e1) {
            e1.printStackTrace();
        }
        try {
            openFileKeyService.returnOpenFileKey(openFileKey);
        } catch (Exception ex) {
            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE;
        }//CHECKSTYLE:ON

        return statusCode;
    }


    /**
     * @param event  {@link SftpserverFileReadEvent}
     * @param server {@link SFTPServer}
     * @return statusCode
     */
    public int fileRead(SftpserverFileReadEvent event, SFTPServer server) {

        int statusCode = SFTPServer.SSH_FX_OK;

        try {

            OpenFileKey openFileKey = openFileKeyService.newOpenFileKey();
            openFileKey.connectionId = event.connectionId;
            String path = handle2Path.asMap().get(event.connectionId + ":" + event.handle);

            if (path == null) {
                throw new IllegalStateException("Path not found for handle:" +
                        event.handle + " for connection: " + event.connectionId + " handle2Path corrupt?? ");
            }

            openFileKey.path = path;

            OpenFile openFile = openFiles.asMap().get(openFileKey);

            if (openFile == null) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("openFile is null");
                }


                event.statusCode = SFTPServer.SSH_FX_FILE_CORRUPT;

                if (LOG.isErrorEnabled()) {
                    LOG.error("OpenFile could not found for connection:"
                            + event.connectionId + " and " +
                            "handle:" + event.handle);

                }


                return statusCode;
            }

            if (openFile.getInputStream() == null) {

                if (LOG.isDebugEnabled()) {
                    LOG.debug("InputStream is null");
                }

                event.statusCode = SFTPServer.SSH_FX_FILE_CORRUPT;

                if (LOG.isErrorEnabled()) {
                    LOG.error("OpenFile found but inputStream is not set, logic error??");
                }

                return statusCode;
            }

            InputStream in = openFile.getInputStream();

            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting inputStream from BlobInfo >>>>"
                        + in);
            }

            byte[] data = new byte[event.length];

            if (LOG.isDebugEnabled()) {
                LOG.debug("Getting offset  >>>>" + event.fileOffset
                        + " and event.length>>> " + event.length);
            }


            int bytesRead = in.read(data);
            int totalBytesToRead = event.length;
            int totalBytesRead = bytesRead;
            int remaining = totalBytesToRead - totalBytesRead;
            boolean done = bytesRead < 0;
            if (LOG.isDebugEnabled()) {
                LOG.debug(" totalBytesRead:" + totalBytesRead + " remaining:" + remaining +
                        " totalBytesToRead:" + totalBytesToRead);
            }

            if (remaining != 0 && LOG.isDebugEnabled()) {
                LOG.debug("Remaining nonzero:" + remaining);
            }

            if (remaining != 0 && !done) {
                while (remaining != 0 && !done) {

                    bytesRead = in.read(data, totalBytesRead, remaining);

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("> read:" + bytesRead);
                    }

                    if (bytesRead != -1) {
                        totalBytesRead = totalBytesRead + bytesRead;
                        remaining = totalBytesToRead - totalBytesRead;

                        if (LOG.isDebugEnabled()) {
                            LOG.debug("totalBytesRead:" + totalBytesRead + "Keep going");
                        }
                    }
                    else {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("Done!!!");
                        }

                        break;
                    }

                }

                if (LOG.isDebugEnabled()) {
                    LOG.debug(" totalBytesRead:" + totalBytesRead + " remaining:" + remaining +
                            " totalBytesToRead:" + totalBytesToRead);
                }


                if (LOG.isDebugEnabled()) {
                    LOG.debug("Read  data >>>>>>>>>>>>>>>>" + totalBytesRead);
                    LOG.debug("Requested  data >>>>>>>>>>>>>>>>" + event.length);
                }
            }

            if (totalBytesRead > 0) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(">> Writing:" + totalBytesRead);
                }

                SFTPConnection conn = server.getConnections().get(event.connectionId);
                conn.setFileData(data, 0, totalBytesRead);
                openFileKeyService.returnOpenFileKey(openFileKey);

            }

            if (done) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Returning EOF");
                }

                statusCode = SFTPServer.SSH_FX_EOF;
            }
            else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Returning ok");
                }
                statusCode = SFTPServer.SSH_FX_OK;
            }


            /*if (totalBytesRead != totalBytesToRead) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(" totalBytesRead:" + totalBytesRead + " remaining:" + remaining + " " +
                            "totalBytesToRead:" + totalBytesToRead);
                }

                throw new IllegalStateException("Came to end of file before was able to read total??");
            }*/

           /* if (i != data.length) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("read data hasn't match with requested to read");
                }
                while (i != data.length || i == -1) {
                    data[i] = (byte) in.read();
                    i++;
                }
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Total bytes read >> " + i + " and data length >> " + data.length);
                }
            } */

        } catch (IOException ex) {
            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE; // SSH_FX_FAILURE
        } catch (IPWorksSSHException ex) {
            ex.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE; // SSH_FX_FAILURE
//CHECKSTYLE:OFF
        } catch (Exception e) {
            e.printStackTrace();
            statusCode = SFTPServer.SSH_FX_FAILURE; // SSH_FX_FAILURE
        }
//CHECKSTYLE:ON
        return statusCode;
    }


    /**
     * @param handle - blobHandle
     * @throws IOException IoException
     */
    public void closeOutputStreamForBlobHandle(BlobHandle handle) throws IOException {

        GFSBlobOutputStream outputStream = openBlobs.asMap().remove(handle);

        if (outputStream == null) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("the output stream for the handle for blobId:" +
                        handle.getBlobIdAsString() + " could not be" +
                        " found ignoring close request");
            }
        }
        else {
            outputStream.closeRealStream();
        }
    }


    /**
     * @param handle blobHandle
     * @return created stream for blob
     */
    public BlobOutputStream getStreamForHandle(BlobHandle handle) {

        if (handle == null) {
            throw new IllegalArgumentException("Handle can not be null ");
        }

        return openBlobs.asMap().get(handle);
    }

    /**
     * Get a blobhandle for the given string
     *
     * @param handle must be non-null and should be a handle that was created from this service.
     * @return {@link BlobHandle}
     */

    private BlobHandle getBlobHandleForString(String handle) {

        if (handle == null) {
            throw new IllegalArgumentException("Handle can not be null ");
        }
        return openHandles.asMap().get(handle);
    }

    /**
     * create new  a blobhandle for the given string and {@link BlobMetaData}
     *
     * @param blobName must be non-null and should the blobName that was created from this service.
     * @param metaData {@link BlobMetaData}
     * @return {@link BlobHandle}
     */
    private BlobHandle createBlobHandle(String blobName, BlobMetaData metaData) {

        BlobHandle blobHandle = blobDAO.createBlobHandle(blobName, metaData);
        openHandles.put(blobHandle.getBlobIdAsString(), blobHandle);
        return blobHandle;
    }


    /**
     * open  a outputStream for the given Handle
     *
     * @param handle must be non-null and should be a handle that was created from this service.
     * @return {@link BlobOutputStream}
     */
    private BlobOutputStream openOutputStreamForBlob(BlobHandle handle) {

        BlobOutputStream outStream = new GFSBlobOutputStream(blobDAO.createStreamForBlob(handle), blobDAO);
        openBlobs.put(handle, (GFSBlobOutputStream) outStream);
        return outStream;
    }


    /**
     * return a  path string after merging realPath and root directory and removing slashes
     *
     * @param path    - incoming system path
     * @param rootdir - home directory of sftp user
     * @return cleaned string
     */
    protected String getOriginalPath(String rootdir, String path) {

        String fullPath = new String(rootdir + path);

        if (fullPath.startsWith("/")) {

            fullPath = fullPath.replaceFirst("/", "");
        }
        if (fullPath.endsWith("/")) {

            fullPath = fullPath.substring(0, fullPath.lastIndexOf('/'));
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("original path created   ::  " + fullPath);
        }
        return fullPath.toString().trim();
    }


}
