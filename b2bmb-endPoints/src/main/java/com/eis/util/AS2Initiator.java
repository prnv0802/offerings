package com.eis.util;

import com.eis.b2bmb.endpts.as2.HttpServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;


/**
 * User: harjeet
 * Date: 10/21/14
 * Time: 4:18 PM
 */
public class AS2Initiator implements AS2InitiatorInterface, DisposableBean {
    private static final Logger LOG = LoggerFactory.getLogger(AS2Initiator.class);
    HttpServer server;

    /**
     * @param server - default constructor
     */
    public AS2Initiator(HttpServer server) {
        if (server == null)
        {
            throw new IllegalArgumentException("Server can not be null");
        }

        this.server = server;

        if (!server.isRunning())
        {
            startServer(server);
        }
        else
        {
            if (LOG.isWarnEnabled())
            {
                LOG.warn("Server was already running");
            }
        }

    }

    @Override
    public boolean isStarted() {
        return this.server.isStarted();
    }

    @Override
    public void shutdownServer() {
        try {
            if (LOG.isInfoEnabled()) {
                LOG.info("** AS2 Server shutting down.");
            }

            this.server.stop();


        }//CHECKSTYLE:OFF
        catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception occurred while shutting down the server " + e.getMessage());
            }
        }//CHECKSTYLE:ON
    }

    @Override
    public void startServer(HttpServer httpServer) {

        if (LOG.isInfoEnabled()) {
            LOG.info("********** Starting AS2 Server !!!!");
        }

        try {
            if (!this.server.isStarted()) {


                this.server.start();

                if (LOG.isInfoEnabled()) {
                    LOG.info("********** Started AS2 Server !!!!");
                }
            } else {
                if (LOG.isInfoEnabled()) {
                    LOG.info("********** AS2 Server Already Started !!!!");
                }
            }


        }//CHECKSTYLE:OFF
        catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Exception occurred while starting the server " + e.getMessage());
            }
        }//CHECKSTYLE:ON
    }

    @Override
    public void destroy() throws Exception {
        if (LOG.isInfoEnabled()) {
            LOG.info("********** Shutting down AS2 Server !!!!");
            shutdownServer();
        }

    }
}
