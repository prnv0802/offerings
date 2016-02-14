package com.eis.b2bmb.endpts.ftp;

import org.apache.ftpserver.ftplet.FileSystemFactory;
import org.apache.ftpserver.ftplet.FileSystemView;
import org.apache.ftpserver.ftplet.FtpException;
import org.apache.ftpserver.ftplet.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: mingardia
 * Date: 6/14/14
 * Time: 10:35 AM
 */
public class B2BMBFTPFileSystemFactory implements FileSystemFactory {
    private static final Logger LOG = LoggerFactory.getLogger(B2BMBFTPFileSystemFactory.class);


    @Override
    public FileSystemView createFileSystemView(User user) throws FtpException {

        if (LOG.isDebugEnabled())
        {
            LOG.debug("creating ftp file system view for user:" + user.getName());
        }

       // return new B2BMBFTPFileSystemView(user);
        return null;
    }
}
