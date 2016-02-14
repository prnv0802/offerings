package com.eis.util;

import com.eis.b2bmb.endpts.ssh.nsoftware.SFTPServer;
import ipworksssh.Certificate;
import ipworksssh.IPWorksSSHException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * sftp server starter
 * User: mingardia Date: 4/21/14 Time: 3:23 PM
 */

public class SFTPInitiator implements SFTPInitiatorInterface {
    private static final Logger LOG = LoggerFactory.getLogger(SFTPInitiator.class);

    /**
     * injecting  sftp server instance from context
     */
    SFTPServer server;

    /**
     * Will create an initiator
     *
     * @param server the server to use
     */
    public SFTPInitiator(SFTPServer server) {
        this.server = server;
        startServer(server);
    }


    /**
     * will determine if the server is started / listening or not
     * @return true listening false down.
     */
    @Override
    public boolean isStarted()
    {
        return server.isListening();
    }

    /**
     * will shut down the server
     */
    @Override
    public void shutdownServer()
    {
        try {
            if (LOG.isInfoEnabled())
            {
                LOG.info("sFTP Server on port:" + server.getLocalPort() + " shutting down.");
            }

            server.shutdown();

            if (LOG.isInfoEnabled())
            {
                LOG.info("sFTP Server shutting down.");
            }

        } catch (IPWorksSSHException e) {
            e.printStackTrace();
        }
    }


    /**
     * sftp server starter and getting secure session
     *
     * @param xserver Sftp server instance
     */
    @Override
    public void startServer(SFTPServer xserver) {
        if (LOG.isInfoEnabled())
        {
            LOG.info("********** Starting SFTP Server !!!!");
        }

        boolean started=false;
        int attemptCount=0;

        while (!started && attemptCount<5)
        {
            attemptCount++;

            try {
                xserver.setSSHCert(new Certificate(Certificate.cstPFXFile, "sftpserver.pfx", "demo", "*"));

                xserver.setListening(true);
                if (LOG.isInfoEnabled())
                {
                    LOG.info("****** SFTP Server Started on port:" + xserver.getLocalPort());
                }
                started = true;

            } catch (IPWorksSSHException e) {
                if (LOG.isInfoEnabled())
                {
                    LOG.info("SFTPServer failed to start on port:" + xserver.getLocalPort() + " trying next port");
                }

                try {
                    xserver.setLocalPort(xserver.getLocalPort() + 1);
                } catch (IPWorksSSHException e1) {
                    e1.printStackTrace();
                    if (LOG.isErrorEnabled())
                    {
                        LOG.error("Could not set port to one higher than:" + xserver.getLocalPort());

                    }
                    break;
                }

            }
        }


    }

}
