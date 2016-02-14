package com.eis.b2bmb.endpts.ssh.nsoftware;

import org.apache.shiro.authc.AuthenticationToken;


/**
 * @author harjeets
 *  22/04/2014
 *
 */
public class PublicKeyToken implements AuthenticationToken {
    
       /**
      *  Principal of user
      */
     private final Object principal;
      /**
       * public key
       */
       private final String key;    
    
    /**
     * parameterized constructor
     * 
     * @param principal user info
     * @param key public key
     */
    public PublicKeyToken(Object principal, String key) {
          
          this.principal = principal;
          this.key = key;
     }

     @Override
      public Object getCredentials() {
         
            return key;
      }

      @Override
      public Object getPrincipal() {
          
            return principal;
      }

     

}
