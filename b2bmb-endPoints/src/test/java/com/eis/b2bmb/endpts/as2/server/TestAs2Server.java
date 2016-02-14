 package com.eis.b2bmb.endpts.as2.server;


 public class TestAs2Server
 {

 }

 /**

import junit.runner.BaseTestRunner;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eis.b2bmb.endpts.ssh.nsoftware.HttpServer;
import com.eis.core.api.v1.service.TenancyManagerService;
import com.eis.core.api.v1.service.UserProfileService;
import com.eis.security.multitenancy.model.SecureSession;
import com.eis.base.test.TestBase;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/springContext.xml"})
public class TestAs2Server extends TestBase{

    private static final Logger LOG = LoggerFactory.getLogger(TestAs2Server.class);

    @Autowired
    HttpServer server;

    @Autowired
    TenancyManagerService tenancyManagerService;

    @Autowired
    UserProfileService userProfileService;
    @Test
    public void mock(){
        LOG.debug("Mock Test");
    }
  
       //  @Test
    public void test(){
        try {
            LOG.debug("login server side");
            serverSideLogin();
            LOG.debug("Starting the Server::::");
            server.startServer();
            
        } catch (Exception e) {
            LOG.error("Problem with starting the server");
            e.printStackTrace();
        }finally{
        	logout();
        }
    }
   


}   **/
