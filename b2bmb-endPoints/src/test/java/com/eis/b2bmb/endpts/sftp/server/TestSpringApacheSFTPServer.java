package com.eis.b2bmb.endpts.sftp.server;

import com.eis.b2bmb.endpts.ssh.apache.B2bmbFilesystemFactory;
import com.eis.core.api.v1.exception.B2BNotAuthenticatedException;
import com.eis.core.api.v1.exception.B2BNotAuthorizedException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.model.UserProfile;
import com.eis.core.api.v1.service.TenancyManagerService;
import com.eis.core.api.v1.service.UserProfileService;
import com.eis.security.multitenancy.model.SecureSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.config.IniSecurityManagerFactory;
import org.apache.shiro.subject.Subject;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * User: mingardia
 * Date: 10/16/13
 * Time: 5:01 PM
 */
//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/META-INF/springContext.xml"})
public class TestSpringApacheSFTPServer {


    private static final Logger LOG = LoggerFactory.getLogger(TestSpringApacheSFTPServer.class);

    @Autowired
    B2bmbFilesystemFactory fileSystemFactory;

    @Autowired
    UserProfileService userProfileService;

    @Autowired
    TenancyManagerService tenancyManagerService;



    protected void serverSideLogin() throws B2BNotAuthorizedException, B2BTransactionFailed, B2BNotAuthenticatedException {
        IniSecurityManagerFactory factory = new IniSecurityManagerFactory("file:src/test/resources/META-INF/shiro.ini");
        org.apache.shiro.mgt.SecurityManager manager = factory.getInstance();
        SecurityUtils.setSecurityManager(manager);

        assertNotNull(tenancyManagerService);

        SecureSession.setSecurityManager(tenancyManagerService);


        Subject subject = SecurityUtils.getSubject();
        UsernamePasswordToken uptoken = new UsernamePasswordToken();
        uptoken.setUsername("mingardia@mycompanyxyz.com");
        uptoken.setPassword(new String("mypassword").toCharArray());
        subject.login(uptoken);

        assertNotNull(userProfileService);
        UserProfile profile = userProfileService.getByUserId("mingardia@mycompanyxyz.com");
        assertNotNull(profile);
        SecureSession.setUser(profile);

    }

    protected void logout() {
        Subject subject = SecurityUtils.getSubject();
        subject.logout();
        SecureSession.unsetSecurityManager();
        SecureSession.unsetUser();
    }


    //@Test
    public void testSFtpServer() throws IOException, InterruptedException, B2BNotAuthorizedException, B2BTransactionFailed, B2BNotAuthenticatedException {
        if (LOG.isDebugEnabled()) {
            LOG.debug("#### Starting server ...");
        }


        this.serverSideLogin();

        SshServer sshd = SshServer.setUpDefaultServer();

        if (fileSystemFactory == null) {
            throw new IllegalStateException("Factory is null and should not be");
        }

        sshd.setFileSystemFactory(fileSystemFactory);

        sshd.setPort(22999);

        sshd.setKeyPairProvider(new SimpleGeneratorHostKeyProvider("hostkey.ser"));
        sshd.setPasswordAuthenticator(
                new PasswordAuthenticator() {

                    public boolean authenticate(String username, String password,
                                                org.apache.sshd.server.session.ServerSession session) {

                        System.out.println(" UserName:" + username + " password:" + password);
                        // TODO Auto-generated method stub
                        return true;
                    }
                });


        CommandFactory myCommandFactory = new CommandFactory() {

            public Command createCommand(String command) {
                System.out.println("Command: " + command);
                return null;
            }
        };
        sshd.setCommandFactory(new ScpCommandFactory(myCommandFactory));


        List<NamedFactory<Command>> namedFactoryList = new ArrayList<NamedFactory<Command>>();
        // this needs to be stubbed out based on your implementation
//	    namedFactoryList.add( new NamedFactory<Command>(){
//
//			public Command create() {
//				SftpSubsystem sftp = new SftpSubsystem();
//
//				return sftp;
//			}
//
//			public String getName() {
//				return "SFTP stuff";
//			}
//
//	    });

        namedFactoryList.add(new SftpSubsystem.Factory());
        sshd.setSubsystemFactories(namedFactoryList);


        sshd.start();

      // System.out.println("Press enter to quit");
      // System.in.read();
        sshd.stop();

        logout();
    }

}
