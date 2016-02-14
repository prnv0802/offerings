package com.eis.b2bmb.endpts.ftp;

import com.eis.b2bmb.api.v1.dao.SFTPServerUserDAO;
import com.eis.b2bmb.api.v1.model.SFTPServerUser;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import org.apache.ftpserver.ftplet.*;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * User: mingardia
 * Date: 6/14/14
 * Time: 4:42 PM
 */
public class FTPUserManager implements UserManager {

    /**
     * Access to the realm
     */
    @Autowired
    SFTPServerUserDAO serverUserDAO;

    /**
     * The data domain this manager is for
     */
    String dataDomain;

    /**
     * The constructor
     * @param dataDomain the datadomain this manager is for
     */
    public FTPUserManager(String dataDomain)
    {
        this.dataDomain = dataDomain;
    }

    @Override
    public User getUserByName(String name) throws FtpException {

        FTPUser u = null;

        try {
            SFTPServerUser user = serverUserDAO.getByRefName(name, dataDomain);

            if (user != null)
            {
                u = new FTPUser();
                u.setName(user.getRefName());
                u.setHomeDirectory(user.getRootDirectory());
                u.setPassword(user.getEncPassword());
                u.setEnabled(true);
            }


        } catch (B2BTransactionFailed b2BTransactionFailed) {
            throw new FtpException(b2BTransactionFailed);
        }

        return u;
    }

    @Override
    public String[] getAllUserNames() throws FtpException {
        return new String[0];
    }

    @Override
    public void delete(String s) throws FtpException {

    }

    @Override
    public void save(User user) throws FtpException {

    }

    @Override
    public boolean doesExist(String s) throws FtpException {
        return false;
    }

    @Override
    public User authenticate(Authentication authentication) throws AuthenticationFailedException {
        return null;
    }

    @Override
    public String getAdminName() throws FtpException {
        return null;
    }

    @Override
    public boolean isAdmin(String s) throws FtpException {
        return false;
    }
}
