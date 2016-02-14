package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.dao.AS2ClientConnectionConfigDAO;
import com.eis.b2bmb.api.v1.model.AS2ClientConnectionConfig;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;


/**
 * Producer for AS2
 * User: mingardia
 */
public class B2bmbAS2Producer extends DefaultProducer {
    private static final transient Logger LOG = LoggerFactory.getLogger(B2bmbAS2Producer.class);

    /**
     * Parameterized constructor
     *
     * @param endpoint endpoint Uri
     */
    public B2bmbAS2Producer(B2bmbAS2Endpoint endpoint) {

        super(endpoint);
        if (LOG.isDebugEnabled()) {
            LOG.debug("creating  B2bmbAs2Producer  object  ");
        }
    }

    @Override
    public void process(Exchange exchange) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inside process method of B2bMailBoxProducer ");
        }
        if (exchange == null) {
            throw new IllegalArgumentException("The exchange cannot be null");
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Inside process method of B2bMailBoxProducer ");
        }


        // User edi-text if not defined as a header
        String contentType = "application/edi-text";
        if (exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class) != null) {
            contentType = exchange.getIn().getHeader(Exchange.CONTENT_TYPE, String.class);
        }

        // Get the endPoint
        B2bmbAS2Endpoint as2Endpoint = getEndpoint();

        AS2ClientConnectionConfigDAO dao = as2Endpoint.getClientConnectionConfigDAO();

        AS2ClientConnectionConfig as2ClientConnectionConfig = dao.getByFromAndTo(as2Endpoint.getFromAS2Id(),
                as2Endpoint.getToAS2Id());

        String transmissionId = exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID, String.class);

        as2Endpoint.getAs2Client().sendFile(as2ClientConnectionConfig,exchange.getIn()
                        .getHeader(Exchange.FILE_NAME, String.class),
                         exchange.getIn().getBody(InputStream.class),
                transmissionId);




        //AS2RelationShipEntry as2RelationShipEntry =
        //       as2Endpoint.getEntryDAO().getByFromAndTo(as2Endpoint.getFromASId(),
        //               as2Endpoint.getToASId());

        /*ExchangeDefinition exchangeDefinition = as2Endpoint.getDefinitionDAO().
                getByRefName(as2RelationShipEntry.getExchangeDefRefName(),
                        as2RelationShipEntry.getExchangeDefDataDomain());
        if (exchangeDefinition == null) {
            throw new IllegalArgumentException("The  exchangeDefinition cannot be null ");
        }

        List<EDIAttachment> attList = new ArrayList<EDIAttachment>();
        EDIAttachment ediAttachment = new EDIAttachment();
        ediAttachment.setContentType(contentType);
        ediAttachment.setInputStream(exchange.getIn().getBody(InputStream.class));
        ediAttachment.setFilename(
                exchange.getIn().getHeader(Exchange.FILE_NAME, String.class));
        ediAttachment.setName(exchange.getIn().getHeader(Exchange.FILE_NAME, String.class));
        attList.add(ediAttachment);

        EDIData ediData = new EDIData();

        ediData.setInputStream(exchange.getIn().getBody(InputStream.class));
        ediData.setEDIType(contentType);
        ediData.setFilename(exchange.getIn().getHeader(Exchange.FILE_NAME, String.class));


        as2Endpoint.getAs2Client().postDataToServer(exchangeDefinition, ediData,
                attList, as2RelationShipEntry.getFromOrgRef(), exchange.getIn().getMessageId());

        if (LOG.isInfoEnabled()) {
            LOG.info("Endpoint URI at createFileSystemEntry method :: " + getEndpoint().getEndpointUri());
        }*/


    }


    @Override
    public B2bmbAS2Endpoint getEndpoint() {
        return (B2bmbAS2Endpoint) super.getEndpoint();
    }


}
