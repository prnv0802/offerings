package com.eis.b2bmb.endpts.ssh.nsoftware;

import com.eis.b2bmb.api.v1.dao.SFTPServerUserDAO;
import com.eis.b2bmb.api.v1.model.SFTPServerUser;
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
 * User: mingardia Date: 3/13/14 Time: 9:07 PM
 */
public class SFTPServerRealm extends AuthorizingRealm {

    private static final Logger LOG = LoggerFactory
            .getLogger(SFTPServerRealm.class);


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


    /**
     * default constructor
     */
    public SFTPServerRealm() {
        super();
        setAuthenticationTokenClass(AuthenticationToken.class);

    }

    /**
     * @param name Realm Name
     */
    public SFTPServerRealm(String name) {
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
                        + " password:" + password + " realm:" + realm);

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
                throw new AuthenticationException("Error occurred while Authenticating credentials" + e);
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

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection arg0) {
        // TODO Auto-generated method stub
        return null;
    }
}
