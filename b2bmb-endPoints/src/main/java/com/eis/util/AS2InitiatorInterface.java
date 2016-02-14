package com.eis.util;

import com.eis.b2bmb.endpts.as2.HttpServer;

/**
 * User: harjeet
 * Date: 10/21/14
 * Time: 4:18 PM
 */

public interface AS2InitiatorInterface {
     
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
     * @param server -     the HttpServer
     */
    void startServer(HttpServer server);
    
}
