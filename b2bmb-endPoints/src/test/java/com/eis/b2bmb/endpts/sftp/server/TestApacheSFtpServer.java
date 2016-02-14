package com.eis.b2bmb.endpts.sftp.server;

import com.eis.b2bmb.endpts.ssh.apache.B2bmbFilesystemFactory;
import org.apache.sshd.SshServer;
import org.apache.sshd.common.NamedFactory;
import org.apache.sshd.server.Command;
import org.apache.sshd.server.CommandFactory;
import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.command.ScpCommandFactory;
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider;
import org.apache.sshd.server.sftp.SftpSubsystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * User: mingardia
 * Date: 10/14/13
 * Time: 11:07 PM
 */
public class TestApacheSFtpServer {

    private static final Logger LOG = LoggerFactory.getLogger(TestApacheSFtpServer.class);

    //@Test
    public void testServer() throws IOException, InterruptedException {

        if (LOG.isDebugEnabled())
        {
            LOG.debug("#### Starting server ...");
        }

        SshServer sshd = SshServer.setUpDefaultServer();

        sshd.setFileSystemFactory(new B2bmbFilesystemFactory());

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

        System.out.println("Press enter to quit");
        System.in.read();
        sshd.stop();
    }
}
