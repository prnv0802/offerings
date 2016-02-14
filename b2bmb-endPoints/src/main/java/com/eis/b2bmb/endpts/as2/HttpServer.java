package com.eis.b2bmb.endpts.as2;

import inedi.InEDIException;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Generic Http Server
 */
public class HttpServer extends AbstractHandler {

    /** AS2 Receiver **/
    protected AS2Receiver as2Server;
    // InetSocketAddress address= new InetSocketAddress(hostname, port)


    /** HTTP Server **/
    protected Server server;


    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);//CHECKSTYLE:OFF

    /** Handles the requests **/
    public void handle(String target,
                       Request baseRequest,
                       HttpServletRequest request,
                       HttpServletResponse response)
            throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_OK);
        baseRequest.setHandled(true);

        try {
            as2Server.executeRequest(request, response);
        } catch (InEDIException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("InEDIException occurred while executing the Request  ::"
                        + e);
            }
        }


    }


    @Override
    protected void doStart() throws Exception {

        if (LOG.isInfoEnabled()) {
            LOG.info(" *** Starting AS2 server ..");
        }

        server.setHandler(this);
        server.setStopAtShutdown(true);
       // server.setStopTimeout(5000);
        server.start();
        super.start();

        super.doStart();
    }

    @Override
    protected void doStop() throws Exception {

        server.stop();

        super.doStop();
    }

    @Override
    public boolean isStarted() {
        return server.isStarted();
    }



    /**
     * @return the server
     */
    public Server getServer() {
        return server;
    }

    /**
     * @param server the server to set
     */
    public void setServer(Server server) {
        this.server = server;
    }


    public HttpServer(AS2Receiver as2Server, Server server) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("HttpServer 2 para constructor invoked >>> ");
        }
        this.as2Server = as2Server;
        this.server = server;
    }

}
