package com.eis.b2bmb.endpts.ssh.nsoftware;

import com.eis.b2bmb.endpts.as2.AS2Receiver;
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
 * @author sudhakars
 *         date 07-04-2014
 */
public class HttpServer extends AbstractHandler {

    AS2Receiver as2Server;
    // InetSocketAddress address= new InetSocketAddress(hostname, port)
    Server server;


    /**
     * Logger
     */
    private static final Logger LOG = LoggerFactory.getLogger(HttpServer.class);//CHECKSTYLE:OFF

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
            
            /*catch (B2BNotAuthenticatedException e) {
              if (LOG.isErrorEnabled()) {
                      LOG.error("B2BNotAuthenticatedException occurred while executing the Request :: "
                                      + e);
              }
         } catch (B2BNotAuthorizedException e) {
              if (LOG.isErrorEnabled()) {
                      LOG.error("B2BNotAuthorizedException occurred while executing the Request   ::  "
                                      + e);
              }
         } catch (B2BTransactionFailed e) {
              if (LOG.isErrorEnabled()) {
                      LOG.error("B2BTransactionFailed occurred while executing the Request  ::"
                                      + e);
              }
         } 
         } catch (B2BNotFoundException e) {
             if (LOG.isErrorEnabled()) {
                     LOG.error("B2BNotFoundException occurred while executing the Request  ::"
                                     + e);
             }
         }
          catch (ValidationException e) {
             if (LOG.isErrorEnabled()) {
                  LOG.error("ValidationException occurred while executing the Request  ::"
                                  + e);
          }
         }*/

    }

    public void startServer() throws Exception {

        if (LOG.isInfoEnabled()) {
            LOG.info(" *** Starting AS2 server ..");
        }
        // Server server = new Server(8080);
        server.setHandler(this);
        server.start();
        //server.join();
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
