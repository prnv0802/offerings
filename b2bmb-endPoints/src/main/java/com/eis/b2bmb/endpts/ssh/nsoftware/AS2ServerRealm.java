package com.eis.b2bmb.endpts.ssh.nsoftware;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.realm.SimpleAccountRealm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * @author sudhakar
 * Date 2 apr 2014
 */

public class AS2ServerRealm extends SimpleAccountRealm{
     /**
      * Logger
      */
     private static final Logger LOG = LoggerFactory.getLogger(AS2ServerRealm.class);
     /**
      * No-args Constructor
      */
     public AS2ServerRealm() {
          super();
          xinit();
     }
     /**
      * Named Constructor
      * @param name - name
      */
     public AS2ServerRealm(String name) {
          super(name);
          xinit();
     }
     /**
      * Init method
      */
     public void xinit()
    {
        addAccount("mingardia@mycompanyxyz.com", "mypassword");

        if (LOG.isDebugEnabled())
        {
            LOG.debug("Added default account");
        }

    }

    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        if (LOG.isDebugEnabled())
        {
            LOG.debug(">>> Attempting authentication of user:" + token.toString());
        }
        return super.doGetAuthenticationInfo(token);
    }
}
