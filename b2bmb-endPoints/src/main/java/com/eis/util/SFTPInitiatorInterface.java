package com.eis.util;

import com.eis.b2bmb.endpts.ssh.nsoftware.SFTPServer;

/**
 * User: mingardia
 * Date: 6/14/14
 * Time: 4:18 PM
 */
public interface SFTPInitiatorInterface {

    /**
     * Determines if the service is started or not
     * @return true if it is started other wise false
     */
    boolean isStarted();

    /**
     * Shuts the server down
     */
    void shutdownServer();

    /**
     * starts the server
     * @param xserver    the server configuration to use to start
     */
    void startServer(SFTPServer xserver);
}
