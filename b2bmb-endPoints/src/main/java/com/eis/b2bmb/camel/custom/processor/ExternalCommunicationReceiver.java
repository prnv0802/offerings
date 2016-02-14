package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.api.v1.dao.CommunicationConfigurationDAO;
import com.eis.b2bmb.api.v1.model.CommunicationConfiguration;
import com.eis.b2bmb.api.v1.model.CommunicationProtocol;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.b2bmb.util.TransmissionRecorder;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.model.*;
import com.google.common.base.Throwables;
import org.apache.camel.*;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ModelCamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Envista Tech on 6/16/2014.
 */
public class ExternalCommunicationReceiver implements Processor  {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalCommunicationReceiver.class);
    /**
     * edi profile dao to get edi profile
     */
    @Autowired
    protected CommunicationConfigurationDAO communicationConfigurationDAO;

    /**
     * camelContext - camelContext object from SpringContext
     */
    @Autowired
    protected ModelCamelContext camelContext;

    /**
     * transmission recorder
     */
    @Autowired
    protected TransmissionRecorder transmissionRecorder;

    @Override
    public void process(Exchange exchange) throws Exception {

        String direction = "IN";
        DynamicSearchRequest dynamicSearchRequest = new DynamicSearchRequest();
        DynamicAttribute parentAttribute = new DynamicAttribute();
        parentAttribute.setType(DynamicAttributeType.String);
        parentAttribute.setValue("SFTP");
        parentAttribute.setRefName("communicationProtocol");
        dynamicSearchRequest.getSearchFields().getAttributes().put("communicationProtocol", parentAttribute);

        List<String> dataDomains = new ArrayList<String>();
        String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);
        exchange.getIn().setHeader(B2bmbCamelConstants.DATA_DOMAIN, dataDomain);
        dataDomains.add(dataDomain);

        int batchSize = 2;
        int offset = 0;

        long numDocuments = communicationConfigurationDAO.getCount(dataDomains);
        String ftpRouteId = null;
        String directoryMoveRouteId = null;
        while (offset < numDocuments) {
            try {
                List<CommunicationConfiguration> communicationConfigurations = communicationConfigurationDAO.getList(
                        dynamicSearchRequest, offset, batchSize, null, dataDomains);
                for (CommunicationConfiguration cc : communicationConfigurations) {
                    ftpRouteId = null;
                    directoryMoveRouteId = null;

                    if (cc != null) {
                        if ("Y".equals(cc.getActive())) {
                            String inDirectoryName = cc.getLocalInDirectoryName();
                            Transmission transmission = transmissionRecorder.createTransmission(
                                    TransmissionDirection.OUTBOUND,
                                    dataDomain, CommunicationProtocol.valueOf(
                                            cc.getCommunicationProtocol().toUpperCase()),
                                    cc.getVendor(),
                                    "B2B", "",
                                    cc.getCommunicationProtocol() + " Client");

                            exchange.getIn().setHeader(B2bmbCamelConstants.DATA_DOMAIN, dataDomain);
                            exchange.setProperty(B2bmbCamelConstants.DATA_DOMAIN, dataDomain);
                            ftpRouteId = cc.getDataDomain() + "." + cc.getRefName();

                            Map<String, Object> transmissionData = new HashMap<String, Object>();
                            transmissionData.put("User Id", cc.getUserName());
                            transmissionData.put("Host", cc.getHost());
                            transmissionData.put("Route", ftpRouteId);

                            FtpRouteBuilder ftpRouteBuilder = new FtpRouteBuilder(cc, ftpRouteId);
                            transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.valueOf(
                                            cc.getCommunicationProtocol().toUpperCase()), transmissionData,
                                    transmission.getDataDomain(), TransmissionStatus.INPROCESS,
                                    "Attempting to connect to Server:" + cc.getHost(),
                                    cc.getVendor(),
                                    "B2B", "",
                                    cc.getCommunicationProtocol() + " Client", TransmissionDirection.OUTBOUND);

                            camelContext.addRoutes(ftpRouteBuilder);
                            CamelDataDomainHelper.setDataDomainInRoute(camelContext.getRoute(ftpRouteId),
                                    cc.getDataDomain());

                            camelContext.startRoute(ftpRouteId);


                            directoryMoveRouteId = cc.getDataDomain() + ".moveFiles" + cc.getRefName();
                            DirectoryMoveRouteBuilder directoryMoveRouteBuilder = new DirectoryMoveRouteBuilder(cc,
                                    directoryMoveRouteId);
                            camelContext.addRoutes(directoryMoveRouteBuilder);
                            CamelDataDomainHelper.setDataDomainInRoute(camelContext.getRoute(directoryMoveRouteId),
                                    cc.getDataDomain());
                            camelContext.startRoute(directoryMoveRouteId);

                            stopRoute(camelContext.getRoute(ftpRouteId));
                            stopRoute(camelContext.getRoute(directoryMoveRouteId));


                        /*
                        transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2,
                        transmissionData, transmission.getDataDomain(), TransmissionStatus.INPROCESS,
                        "SFTP Client Sent File:" + fileName + " to Server:" + cc.getHost(),
                        fromAddress,
                        toAddress, fileName,
                        "SFTP Client", TransmissionDirection.OUTBOUND);
                        */
                        }
                    }

                }
            }//CHECKSTYLE:OFF
            catch(Exception e){
                //CHECKSTYLE:ON

                if (LOG.isErrorEnabled()) {
                    LOG.error("An error occurred trying to build and run dynamic routes, " +
                            "attempting to stop the routes and " +
                            "remove them from the camel context.", e);
                }
                if (ftpRouteId != null) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Stopping route:" + ftpRouteId);
                    }
                    stopRoute(camelContext.getRoute(ftpRouteId));
                }

                if (directoryMoveRouteId != null) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Stopping route:" + directoryMoveRouteId);
                    }
                    stopRoute(camelContext.getRoute(directoryMoveRouteId));
                }
            } finally {
                offset = offset + batchSize;
            }
        }
    }

    private void stopRoute(Route route) throws InterruptedException, Exception {

        try {
            if (camelContext.getRoute(route.getId()) != null
                    && camelContext.getRouteStatus(route.getId()) != null) {

                int count = 0;
                while (!camelContext.getRouteStatus(route.getId()).equals(ServiceStatus.Started)
                        && count < 5) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Waiting for route " + route.getId() + " to start");
                    }

                    Thread.currentThread().sleep(2000);
                    count++;
                }

                if (!camelContext.getRouteStatus(route.getId()).equals(ServiceStatus.Started)) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Timed out waiting for route to start");
                    }
                } else {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Route " + route.getId() + " started, going to stop");
                    }
                    Thread.currentThread().sleep(2000);
                    RouteTerminator routeTerminator = new RouteTerminator(route.getId(), camelContext);
                    routeTerminator.start();
                }
            }
        }
//CHECKSTYLE:OFF
        catch (Exception e) {
//CHECKSTYLE:ON

            if (LOG.isDebugEnabled()) {
                LOG.debug("Route with id :: " + route.getId() + " failed to stop");
            }

            throw new B2BTransactionFailed("Failed to Stop Route:" + route.getId(), e);
        }
    }

    class FtpRouteBuilder extends RouteBuilder {

        CommunicationConfiguration cc = null;
        String routeId = null;
        public FtpRouteBuilder(CommunicationConfiguration cc, String routeId) {
            this.cc = cc;
            this.routeId = routeId;
        }

        @Override
        public void configure() throws Exception {

            String protocol = cc.getCommunicationProtocol();
            from(protocol.toLowerCase()+"://" + cc.getUserName() + "@" + cc.getHost() + ":"
                    + cc.getPort() + "/" +
                    cc.getOutDirectoryName() + "?password=" + cc.getPassword() + "&moveFailed=error&" +
                    "move=archive&disconnect=false&streamDownload=true&stepwise=false")
                    .routeId(routeId)
                    .autoStartup(false)
                    .setProperty(B2bmbCamelConstants.DATA_DOMAIN, constant(cc.getDataDomain()))
            .shutdownRunningTask(ShutdownRunningTask.CompleteAllTasks)
            .setHeader(B2bmbCamelConstants.DATA_DOMAIN, constant(cc.getDataDomain()))
            .to("b2bmbFileSystem://" + cc.getDataDomain() + "/" + cc.getLocalInDirectoryName())
                    .setHeader(B2bmbCamelConstants.DATA_DOMAIN, constant(cc.getDataDomain()));
        }
    }

    class DirectoryMoveRouteBuilder extends RouteBuilder {

        CommunicationConfiguration cc = null;
        String routeId = null;
        public DirectoryMoveRouteBuilder(CommunicationConfiguration cc, String routeId) {
            this.cc = cc;
            this.routeId = routeId;
        }

        @Override
        public void configure() throws Exception {
            from("b2bmbFileSystem://" + cc.getDataDomain() + "/" + cc.getLocalInDirectoryName())
            .routeId(routeId)
                    .setProperty(B2bmbCamelConstants.DATA_DOMAIN, constant(cc.getDataDomain()))
                    .shutdownRunningTask(ShutdownRunningTask.CompleteAllTasks)
                    .autoStartup(false)
                    .setHeader(B2bmbCamelConstants.DATA_DOMAIN, constant(cc.getDataDomain()))
            .setProperty(B2bmbCamelConstants.DATA_DOMAIN, constant(cc.getDataDomain()))
            .to("b2bmbFileSystem://" + cc.getDataDomain() + "/sftp-in")
            .setHeader(B2bmbCamelConstants.DATA_DOMAIN, constant(cc.getDataDomain()));
        }
    }

    class RouteTerminator extends Thread {
        private String routeId;
        private CamelContext camelContext;

        public RouteTerminator(String routeId, CamelContext camelContext) {
            this.routeId = routeId;
            this.camelContext = camelContext;
        }

        @Override
        public void run() {
            try {
                camelContext.stopRoute(routeId);
                camelContext.removeRoute(routeId);
            //CHECKSTYLE:OFF
            } catch (Exception e) {
                throw Throwables.propagate(e);
            }
            //CHECKSTYLE:ON
        }
    }
}




