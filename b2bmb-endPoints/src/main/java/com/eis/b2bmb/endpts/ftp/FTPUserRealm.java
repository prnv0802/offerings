package com.eis.b2bmb.endpts.ftp;

import com.eis.b2bmb.api.v1.dao.SFTPServerUserDAO;
import com.eis.b2bmb.api.v1.model.SFTPServerUser;
import com.eis.b2bmb.endpts.ssh.nsoftware.PublicKeyToken;
import com.eis.core.api.v1.exception.B2BException;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

/**
 * User: mingardia
 * Date: 6/14/14
 * Time: 4:33 PM
 *
 * FIXME should consolidate this and the SFTP / AS2 Relams into one class
 */
public class FTPUserRealm extends AuthorizingRealm {

    private static final Logger LOG = LoggerFactory
            .getLogger(FTPUserRealm.class);

    @Autowired
    SFTPServerUserDAO serverUserDAO;

    // purposefully not autowired
    org.apache.shiro.mgt.SecurityManager manager;

    /**
     * @return sftp security manager
     */
    public org.apache.shiro.mgt.SecurityManager getManager() {
        return manager;
    }

    /**
     * @param manager sftp security manager
     */
    public void setManager(org.apache.shiro.mgt.SecurityManager manager) {
        this.manager = manager;
        SecurityUtils.setSecurityManager(manager);
    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(
            AuthenticationToken token) throws AuthenticationException {

        AuthenticationInfo info = null;
        if (LOG.isDebugEnabled()) {
            LOG.debug(">>> Attempting authentication of user:"
                    + token.toString());
        }

        if (token == null) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Null token received throwing illegal argument exception");
            }
            throw new IllegalArgumentException("token can not be null");
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("Getting AuthenticationInfo");
        }

        String userId = null;
        String password = null;
        String realm = null;

        SFTPServerUser sftpUser = null;

        if (token instanceof UsernamePasswordToken) {

            UsernamePasswordToken upt = (UsernamePasswordToken) token;
            userId = upt.getUsername();

            String[] splitUserId = userId.split("\\.");
            if (splitUserId.length == 2) {
                userId = splitUserId[1];
                realm = splitUserId[0];
            }

            password = new String(upt.getPassword());

            if (LOG.isDebugEnabled()) {
                LOG.debug("UserNamePasswordToken::Logging in with credentials userID:" + userId
                        + " password:" + password + "realm:" + realm);

                LOG.debug("UPT Principles:" + upt.getPrincipal().toString());
            }

            try {

                sftpUser = serverUserDAO.authenticate(userId, password, realm);


                if (LOG.isDebugEnabled()) {
                    LOG.debug("Setting values in Session for sftpUser");
                }

                SecurityUtils.getSubject().getSession().setAttribute("sftpUser", sftpUser);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(" values set in Session for sftpUser !!!!!!!!!!!");
                }
                //CHECKSTYLE:OFF
            } catch (Exception e) {
                throw new AuthenticationException("Error occured while Authenticating credentials" + e);
            }
            info = new SimpleAuthenticationInfo(userId,
                    password, "SFTPServerRealm");

        } else if (token instanceof PublicKeyToken) {

            PublicKeyToken upt = (PublicKeyToken) token;
            userId = (String) upt.getPrincipal();

            String[] splitUserId = userId.split("\\.");
            if (splitUserId.length == 2) {
                userId = splitUserId[1];
                realm = splitUserId[0];
            }

            String key = (String) upt.getCredentials();

            if (LOG.isDebugEnabled()) {
                LOG.debug("PublicKeyToken::Logging in with credentials userID:" + userId
                        + "realm:" + realm);

                LOG.debug("UPT Principles:" + upt.getPrincipal().toString());
            }

            try {


                sftpUser = serverUserDAO.authenticateByKey(userId, key.replaceAll("\\r\\n|\\n", ""), realm);

            } catch (B2BException e) {
                throw new AuthenticationException(e);
            } catch (IOException e) {
                throw new AuthenticationException(e);
            }


            SecurityUtils.getSubject().getSession().setAttribute("sftpUser", sftpUser);
            info = new SimpleAuthenticationInfo(userId, key
                    , "SFTPServerRealm");

        } else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not use token as its not a UserPassword Token?");
            }
        }

        if (sftpUser == null) {
            throw new IllegalStateException(
                    "unable to find sftp user with id ::" + userId);
        }


        return info;

    }

    /**
     * Retrieves the AuthorizationInfo for the given principals from the underlying data store.  When returning
     * an instance from this method, you might want to consider using an instance of
     * {@link org.apache.shiro.authz.SimpleAuthorizationInfo SimpleAuthorizationInfo}, as it is suitable in most cases.
     *
     * @param principals the primary identifying principals of the AuthorizationInfo that should be retrieved.
     * @return the AuthorizationInfo associated with this principals.
     * @see org.apache.shiro.authz.SimpleAuthorizationInfo
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        return null;
    }
}
