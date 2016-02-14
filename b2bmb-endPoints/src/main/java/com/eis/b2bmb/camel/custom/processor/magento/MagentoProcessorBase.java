package com.eis.b2bmb.camel.custom.processor.magento;

import com.eis.core.api.v1.exception.*;
import com.eis.extsvrs.magento.api.v1.model.SalesOrderEntity;
import com.eis.security.jacksonFilters.NoOpFilter;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.apache.camel.CamelExecutionException;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

/**
 * Created by Mmeherali on 7/20/2015.
 * Base class for the Magento calls
 */
public abstract class MagentoProcessorBase implements Processor {

    private static final Logger LOG = LoggerFactory.getLogger(MagentoProcessorBase.class);
    private static final String CXF_MAGENTO_ENDPOINT = "cxf:bean:magentoEndpoint";
    private static final String VT_LOGIN = "soaptemplates/magento/Login.vm";
    private static final String VT_ENDSESSION = "soaptemplates/magento/EndSession.vm";
    private static final String VT_GET_SALES_ORDER = "soaptemplates/magento/GetSalesOrderInfo.vm";

    /**
     * enum for Magento status codes
     */
    protected static enum MagentoStatus { partially_shipped }

    /**
     * Expects the following on the header
     * - Magento webservice destination URL
     * - Magento userId
     * - Magento apiKey (password)
     *
     * Makes a call to login first to get the sessionId, then calls the subclass for the business process call.
     * The response from the subclass.process() call will be put on the exchange out body.
     * After the call to the base class, calls endSession to invalidate the magento login token
     *
     * Uses Spring-ws as the webservice engine
     * @param exchange exchange
     * @throws Exception exception
     */
    @Override
    public void process(Exchange exchange) throws Exception {
        try {
            String destination = (String) exchange.getIn().getHeader("destination");
            String magentoUser = (String) exchange.getIn().getHeader("magentoUser");
            String magentoApiKey = (String) exchange.getIn().getHeader("magentoApiKey");
            checkNotNull(destination, "Please provide the destinationUrl on header");
            checkNotNull(magentoUser, "Please provide the magentoUser on header");
            checkNotNull(magentoApiKey, "Please provide the magentoApiKey on header");

            String endPoint = "spring-ws:" + destination;
            ProducerTemplate template = exchange.getContext().createProducerTemplate();
            VelocityEngine ve = createVelocityEngine();
            String json = exchange.getIn().getBody(String.class);
            String sessionId = null;
            try {
                sessionId = login(magentoUser, magentoApiKey, ve, exchange, template, endPoint);
            }
            catch (SOAPException | IOException | XPathExpressionException |
                    TransformerException  | CamelExecutionException e) {
                /**
                 * Any errors like host not reacheable etc
                 */
                LOG.error("Exception while logging in to Magento " + e.getMessage());
                e.printStackTrace();
                exchange.getOut().setBody(handleLoginErrors(json, exchange));
                return;
            }
            if(StringUtils.isEmpty(sessionId)) {
                LOG.error("Could not login to Magento - Did not receive a sessionId from Magento");
                exchange.getOut().setBody(handleLoginErrors(json, exchange));
                return;
            }

            LOG.info("Login to Magento successful");
            Object response = process(json, sessionId, ve, exchange, template, endPoint);
            endSession(sessionId, ve, exchange, template, endPoint);
            LOG.info("Magento session ended..");
            exchange.getOut().setBody(response);

        } catch (IOException | XPathExpressionException e) {
            e.printStackTrace();
            throw new Exception(e);
        }
    }

    /**
     * Process method that the sub classes will implement
     */
    abstract Object process(String json, String sessionId, VelocityEngine ve,
                            Exchange exchange, ProducerTemplate template, String endPoint)
            throws IOException, XPathExpressionException,
            TransformerException, B2BNotAuthorizedException, B2BTransactionFailed, B2BNotAuthenticatedException,
            B2BNotFoundException, ValidationException;


    abstract Object handleLoginErrors(String json, Exchange exchange) throws IOException, B2BTransactionFailed;

    /**
     * Calls Magento login and returns a sessionId which can be used for subsequent calls
     * to Magento
     * @param username username
     * @param apiKey apiKey
     * @param ve VelocitEngine
     * @param exchange exchange
     * @param template ProducerTemplate
     * @return sessionId
     * @throws SOAPException
     * @throws IOException
     * @throws XPathExpressionException
     */
    private String login(String username, String apiKey,
                         VelocityEngine ve, Exchange exchange, ProducerTemplate template,
                         String endPoint)
            throws SOAPException, IOException, XPathExpressionException, TransformerException {
        Template t = ve.getTemplate(VT_LOGIN);
        VelocityContext context = new VelocityContext();
        context.put("username", username);
        context.put("apikey", apiKey);
        StringWriter writer = new StringWriter();
        t.merge(context, writer);
        String request = writer.toString();

        if(LOG.isDebugEnabled()) {
            LOG.debug("MAGENTO LOGIN REQUEST:>>>>" + request);
        }

        DOMSource response = (DOMSource) template.requestBody(endPoint, request);

        if(LOG.isDebugEnabled()) {
            LOG.debug("MAGENTO RESPONSE <<<<" + domSourceToString(response));
        }

        String sessionId = xpathQuery(response.getNode(), "//loginReturn");

        if(StringUtils.isEmpty(sessionId)) {
            LOG.error(domSourceToString(response));
        }
        return sessionId;
    }

    /**
     * Returns SalesOrderEntity (Representation of Magento sales order)
     * @param sessionId magentoSessionId
     * @param orderIncrementId magentoOrderId
     * @param ve velocityEngine
     * @param exchange exchange
     * @param template template
     * @param endPoint endPoint
     * @return SalesOrderEntity SalesOrderEntity
     * @throws TransformerException response parsing error
     */
    protected SalesOrderEntity getSalesOrderEntity(String sessionId, String orderIncrementId,
                                                   VelocityEngine ve, Exchange exchange,
                                                   ProducerTemplate template, String endPoint)
            throws TransformerException {
        Template t = ve.getTemplate(VT_GET_SALES_ORDER);
        VelocityContext context = new VelocityContext();
        context.put("sessionId", sessionId);
        context.put("orderIncrementId", orderIncrementId);
        StringWriter writer = new StringWriter();
        t.merge(context, writer);
        String body = writer.toString();

        if(LOG.isInfoEnabled()) {
            LOG.info(" *****  !!!! --- *****");
            LOG.info("Sending body:"+body+" to magento");
            LOG.info(" *****  !!!! --- *****");
        }

        DOMSource response = (DOMSource) template.requestBody(endPoint, body);
        SalesOrderEntity salesOrderEntity = null;
        String soapResponse = domSourceToString(response);
        salesOrderEntity = parseMagentoResponse(soapResponse, SalesOrderEntity.class, 2);
        return salesOrderEntity;
    }

    /**
     * Parses SoapResponse from Magento using JaxB
     * @param soapResponse
     * @param klass
     * @param tagDepth
     * @param <T>
     * @return
     */
    private <T> T parseMagentoResponse(String soapResponse, Class<T> klass, int tagDepth) {
        try {

            Element node =  DocumentBuilderFactory
                    .newInstance()
                    .newDocumentBuilder()
                    .parse(new ByteArrayInputStream(soapResponse.getBytes()))
                    .getDocumentElement();
            node.setAttributeNS("http://www.w3.org/2000/xmlns/","xmlns:xsd","http://www.w3.org/2001/XMLSchema");
            node.setAttributeNS("http://www.w3.org/2000/xmlns/","xmlns:xsi",
                    "http://www.w3.org/2001/XMLSchema-instance");
            Transformer t = TransformerFactory.newInstance().newTransformer();
            StreamResult result = new StreamResult(new StringWriter());
            t.transform(new DOMSource(node), result);
            String responseWithNamespaces = result.getWriter().toString();

            XMLInputFactory xif = XMLInputFactory.newFactory();
            XMLStreamReader xsr = xif.createXMLStreamReader(new StringReader(responseWithNamespaces));
            for (int i =1; i <= tagDepth; i++) {
                //Advancing the tags, like say SoapEnvelope to Soapbody to result
                xsr.nextTag();
            }

            JAXBContext jc = JAXBContext.newInstance(klass);
            Unmarshaller unmarshaller = jc.createUnmarshaller();
            JAXBElement<T> je = unmarshaller.unmarshal(xsr, klass);
            checkState(je != null && je.getValue() != null, "Could not parse entity from magento " + klass.getName());
            return je.getValue();
        }
        catch (XMLStreamException | JAXBException | TransformerException | IOException | SAXException
                | ParserConfigurationException e) {
            e.printStackTrace();
            LOG.error("Could not parse salesOrderEntity from magento " + e.getMessage());
            throw new IllegalStateException(e);
        }

    }

    /**
     * Ends Magento session
     * @param sessionId sessionId
     * @param ve VelocityEngine
     * @param exchange exchange
     * @param template ProducerTemplate
     */
    private void endSession(String sessionId, VelocityEngine ve, Exchange exchange,
                            ProducerTemplate template, String endPoint) throws TransformerException {
        Template t = ve.getTemplate(VT_ENDSESSION);
        VelocityContext context = new VelocityContext();
        context.put("sessionId", sessionId);
        StringWriter writer = new StringWriter();
        t.merge(context, writer);
        String message = writer.toString();
        if(LOG.isDebugEnabled()) {
            LOG.debug(message);
        }
        template.requestBody(endPoint, message);
    }

    /**
     * Creates a objectMapper
     * @return objectMapper
     */
    protected ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter("PrototypeFilter", new NoOpFilter());
        objectMapper.setFilters(filterProvider);
        return objectMapper;
    }

    private VelocityEngine createVelocityEngine() {
        VelocityEngine ve = new VelocityEngine();
        ve.setProperty("resource.loader", "classpath");
        ve.setProperty("classpath.resource.loader.class",
                "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        ve.init();
        return ve;
    }

    /**
     * Converts a DomSource to String
     * @param domSource domSource
     * @return String String
     * @throws TransformerException TransformerException
     */
    protected String domSourceToString(DOMSource domSource) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        StreamResult result = new StreamResult(new StringWriter());
        transformer.transform(domSource, result);
        return result.getWriter().toString();
    }

    /**
     * Performs a xpathquery
     * @param node node
     * @param query query
     * @return value
     * @throws XPathExpressionException XPathExpressionException
     */
    protected String xpathQuery(org.w3c.dom.Node node, String query) throws XPathExpressionException {
        XPathFactory xPathfactory = XPathFactory.newInstance();
        XPath xpath = xPathfactory.newXPath();
        XPathExpression expr = xpath.compile(query);
        return expr.evaluate(node);
    }

}
