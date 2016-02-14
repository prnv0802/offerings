package com.eis.b2bmb.endpts.sftp.server;

import ipworksssh.Certificate;
import ipworksssh.IPWorksSSHException;

import java.util.TooManyListenersException;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.eis.b2bmb.endpts.ssh.nsoftware.SFTPServer;
import com.eis.base.test.TestBase;
import com.eis.core.api.v1.exception.B2BNotAuthenticatedException;
import com.eis.core.api.v1.exception.B2BNotAuthorizedException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.service.BlobService;
import com.eis.core.api.v1.service.FileSystemEntryService;

/**
 * User: mingardia
 * Date: 11/5/13
 * Time: 6:31 PM
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:/META-INF/springContext.xml"})
public class  TestNSoftwareSFTPServer extends TestBase {

    @Autowired
    FileSystemEntryService fseService;

    @Autowired
    BlobService blobService;

    @Autowired
    SFTPServer server;


 @Test
    public void test() {
        System.out.println("Hello world");
    }
    
   /* public static void main(String[] args) throws TooManyListenersException {
        TestNSoftwareSFTPServer testNSoftwareSFTPServer = new TestNSoftwareSFTPServer();
        testNSoftwareSFTPServer.testSFTPServer();
    }*/

//    @Test
    public void testSFTPServer() throws TooManyListenersException, B2BNotAuthorizedException, B2BTransactionFailed,
            B2BNotAuthenticatedException {

        serverSideLogin();

        try {

            try {
                System.out.println("**********************************************************");
                System.out.println("* This demo shows how to use the SFTPServer component to *");
                System.out.println("* create a simple SFTP Server.                           *");
                System.out.println("* Use the following credentials to connect.              *");
                System.out.println("* User: test                                             *");
                System.out.println("* Password: test                                         *");
                System.out.println("**********************************************************");

                //For the purposes of this demo we are using the included certificate. You may change this line to
                // specify your own certificate.
                ipworksssh.Certificate cert = new Certificate();

                ipworksssh.Certmgr certmgr = new ipworksssh.Certmgr();


                System.out.println(">>> SFTP License:" + server.getRuntimeLicense());
                System.out.println(">>> CertMgr:" + certmgr.getRuntimeLicense());

                server.setSSHCert(new ipworksssh.Certificate(Certificate.cstPFXFile, "sftpserver.pfx", "demo",
                        "*"));

                server.setListening(true);

                System.out.println("Server listening on port " + server.getLocalPort() + ".");
                System.out.println("Press Q to exit.\r\n\r\n");



           /* try {
                Thread.currentThread().sleep(600000);
            } catch (Exception ex)
            {
                ex.printStackTrace();
            } */


                while (true) {
                    if (System.in.available() > 0) {
                        if (String.valueOf(System.in.read()).equalsIgnoreCase("Q")) {
                            System.out.println("Server shutting down. Goodbye!");
                            server.shutdown();
                        }
                    }

                    Thread.currentThread().sleep(3000);
                }

            } catch (IPWorksSSHException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            logout();
        }

    }


}

