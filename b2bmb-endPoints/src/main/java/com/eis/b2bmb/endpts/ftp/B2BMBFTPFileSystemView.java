package com.eis.b2bmb.endpts.ftp;

import com.eis.core.api.v1.dao.BlobDAO;
import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.FtpFile;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;

/**
 * User: mingardia
 * Date: 6/14/14
 * Time: 10:31 AM
 */
public class B2BMBFTPFileSystemView implements FileSystemView {

    private static final Logger LOG = LoggerFactory
            .getLogger(B2BMBFTPFileSystemView.class);

    /**
     * fileSystemEntryService
     */
    @Autowired
    protected FileSystemEntryDAO fileSystemEntryDAO;

    /**
     * blobService
     */
    @Autowired
    protected BlobDAO blobDAO;

    /**
    * the root directory will always end with '/'.
    */
    private String rootDir;

    /** the first and the last character will always be '/'
    * It is always with respect to the root directory.
    */
    private String currDir;

    /**
     * The user
     */
    private User user;

    /**
     * determines if the userId / password is case Insensitive or not
     */
    private boolean caseInsensitive = false;

    /**
     * constructor needs to know who is logged in
     *
     * @param user the user
     * @param caseInsensitivex - boolean if the userid / password is case insenstive or not
     */
    public B2BMBFTPFileSystemView(User user, boolean caseInsensitivex) {
       this.user = user;

        if (user == null) {
            throw new IllegalArgumentException("user can not be null");
        }
        if (user.getHomeDirectory() == null) {
            throw new IllegalArgumentException(
                    "User home directory can not be null");
        }

        this.caseInsensitive = caseInsensitivex;

        // add last '/' if necessary
        String userRootDir = user.getHomeDirectory();


        rootDir = normalizeSeparateChar(userRootDir);
        if (!rootDir.endsWith("/")) {
            rootDir += '/';
        }

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Native filesystem view created for user \"{}\" with root \"{}\"", user.getName(), rootDir);
        }


        this.rootDir = rootDir;

        this.user = user;

        currDir = "/";


    }

    /**
     * Normalize separate character. Separate character should be '/' always.
     * @param pathName the pathName
     * @return  the normalized string
     */
    public final static String normalizeSeparateChar(final String pathName) {
        String normalizedPathName = pathName.replace(File.separatorChar, '/');
        normalizedPathName = normalizedPathName.replace('\\', '/');
        return normalizedPathName;
    }


    @Override
    public FtpFile getHomeDirectory() throws FtpException {
        return null;
    }

    @Override
    public FtpFile getWorkingDirectory() throws FtpException {
        return null;
    }

    @Override
    public boolean changeWorkingDirectory(String s) throws FtpException {
        return false;
    }

    @Override
    public FtpFile getFile(String s) throws FtpException {
        return null;
    }

    @Override
    public boolean isRandomAccessible() throws FtpException {
        return false;
    }

    @Override
    public void dispose() {

    }
}
