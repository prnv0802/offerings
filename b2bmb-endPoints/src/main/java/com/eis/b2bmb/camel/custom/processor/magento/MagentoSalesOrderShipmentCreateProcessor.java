package com.eis.b2bmb.camel.custom.processor.magento;

import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.core.api.v1.dao.CorrelationDAO;
import com.eis.core.api.v1.exception.*;
import com.eis.core.api.v1.model.Correlation;
import com.eis.extsvrs.magento.api.v1.model.OrderItemId;
import com.eis.ssit.api.v1.dao.SalesOrderDAO;
import com.eis.ssit.api.v1.dao.ShipmentDAO;
import com.eis.ssit.api.v1.dao.ShipmentRequestDAO;
import com.eis.ssit.api.v1.model.*;
import com.eis.ssit.api.v1.model.Package;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.transform.TransformerException;
//import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.*;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;


/**
 * Created by Mmeherali on 7/16/2015.
 * Processor for creating Magento SalesOrderShipment creation
 */
public class MagentoSalesOrderShipmentCreateProcessor extends MagentoProcessorBase {

    private static final Logger LOG = LoggerFactory.getLogger(MagentoSalesOrderShipmentCreateProcessor.class);
    private static final String VT_SALES_ORDER_SHIPMENT_CREATE = "soaptemplates/magento/SalesOrderShipmentCreate.vm";
    private static final String VT_ADD_TRACKING = "soaptemplates/magento/SalesOrderCreateAddTrack.vm";
    private static final String VT_SALES_ORDER_ADD_COMMENT = "soaptemplates/magento/SalesOrderAddComment.vm";

    @Autowired
    ShipmentDAO shipmentDAO;

    @Autowired
    ShipmentRequestDAO shipmentRequestDAO;

    @Autowired
    CorrelationDAO correlationDAO;

    @Autowired
    SalesOrderDAO salesOrderDAO;

    /**
     * Calls SalesOrderCreateShipment on Magento with the Shipment JSON that is passed in
     *
     * Shipment JSON should be parsable to the cantata Shipment model.
     * Following parameters are required parameters
     * - There should be a salesOrder existing for the salesOrderId
     * - SalesOrder should have a orderNumber on the header
     * - There should be at least 1 lineItem on the shipmentRequest attached to shipment
     * - Line items should have poItemId populated (This will be passed in as the orderIncrementId to magento)
     * - Tracking numbers from packages will be sent to Magento after a shipment is created
     * - If order is partially shipped, Magento status will be updated to partially_shipped
     */
    @Override
    Object process(String json, String sessionId, VelocityEngine ve, Exchange exchange,
                   ProducerTemplate template, String endPoint)
            throws IOException, XPathExpressionException,
            TransformerException, B2BNotAuthorizedException, B2BTransactionFailed, B2BNotAuthenticatedException,
            B2BNotFoundException, ValidationException {

        ObjectMapper objectMapper = createObjectMapper();

        Shipment shipment = objectMapper.readValue(json,
                Shipment.class);

        String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);

        checkNotNull(dataDomain, "dataDomain can not be null");
        checkNotNull(shipment, "Cannot recreate Shipment from JSON");

        String salesOrderId = shipment.getOrderId();

        SalesOrder salesOrder = salesOrderDAO.getById(salesOrderId);

        checkNotNull(salesOrder, "Could not retrieve salesOrder for Id " + salesOrderId);
        checkNotNull(salesOrder.getHeader(), "Sales Order header is null " + salesOrderId);

        shipment = shipmentDAO.getByRefName(shipment.getRefName(), dataDomain);
        checkNotNull(shipment, String.format("Could not retrieve shipment with refName %s and dataDomain %s",
                shipment.getRefName(), dataDomain));

        String magentoOrderId = null;

        if(salesOrder.getHeader().getOriginalOrderNumber() != null) {
            magentoOrderId = salesOrder.getHeader().getOriginalOrderNumber();
        } else {
            for (Reference referenceIdentification : salesOrder.getReferenceData()) {
                if (referenceIdentification.getType().equals("originalSalesNumber")) {
                    magentoOrderId = referenceIdentification.getValue();
                }
            }
        }
        if(LOG.isInfoEnabled()) {
            LOG.info("Attempting to update shipment in magento for order:"+magentoOrderId);
        }
        checkNotNull(magentoOrderId,
                "Original Sales Number from Magento is null, but expected to be there for order " + salesOrderId);
        String shipmentRequestId = shipment.getShipmentRequestId();
        checkNotNull(shipmentRequestId, "Shipment requestId cannot be null");

        ShipmentRequest shipmentRequest = shipmentRequestDAO.getById(shipmentRequestId);
        checkNotNull(shipmentRequest, "Shipment Request cannot be null, shipmentRequestId " + shipmentRequestId);

        checkState(shipmentRequest.getLineItems().size() > 0, "There are no line items on shipment request");



        Set<String> trackingNumbers = new HashSet<>();
        for(Package p : shipment.getPackages()) {
            if(StringUtils.isNotEmpty(p.getTrackingNumber())) {
                trackingNumbers.add(p.getTrackingNumber());
            }
        }
        if(trackingNumbers.size() == 0
                && StringUtils.isNotEmpty(shipment.getMasterTrackingNumber()) ) {
            trackingNumbers.add(shipment.getMasterTrackingNumber());
        }

        String cantataCarrier = shipment.getCarrier();
        if(trackingNumbers.size() > 0) {
            checkNotNull(cantataCarrier, "Carrier should not be NULL");
        }

        List<OrderItemId> orderItemIds = new ArrayList<>();
        HashMap<String,Integer> itemIdQtyMap = new HashMap<>();
        for(Package p : shipment.getPackages()) {
            for(PackageLine line : p.getLineItems()) {
                /**
                 * If there is a line-split OMS modifies the original item id with -a, -b, -c and so on. But
                 * for Magento the part before the hyphen is the itemId
                 */
                String itemId = line.getPoItemId().split("-")[0];
                Integer qty = itemIdQtyMap.get(itemId);
                if(qty == null) {
                    qty = line.getItemQty();
                }
                else {
                    qty = qty + line.getItemQty();
                }
                itemIdQtyMap.put(itemId, qty);
            }
        }
        for (Map.Entry<String, Integer> entry : itemIdQtyMap.entrySet()) {
            orderItemIds.add(new OrderItemId(entry.getKey(), entry.getValue()));
        }

        Template t = ve.getTemplate(VT_SALES_ORDER_SHIPMENT_CREATE);
        VelocityContext context = new VelocityContext();
        context.put("sessionId", sessionId);
        context.put("orderIncrementId", magentoOrderId);
        /**
         * Create comment with tracking URLs
         */
        String comment = createCommentWithTrackingInfo(trackingNumbers, shipment.getCarrier());
        context.put("comment", comment);
        context.put("includeEmail", 1);
        context.put("includeComment", 1);
        context.put("orderItemIds", orderItemIds);
        StringWriter writer = new StringWriter();
        t.merge(context, writer);
        String body = writer.toString();

        if(LOG.isInfoEnabled()) {
            LOG.info(" *****  !!!! --- *****");
            LOG.info("Sending body:"+body+" to magento");
            LOG.info(" *****  !!!! --- *****");
        }

        DOMSource response = (DOMSource) template.requestBody(endPoint, body);

        String shipmentIncrementId = xpathQuery(response.getNode(), "//shipmentIncrementId");

        MagentoResponse magentoResponse = null;
        if(StringUtils.isNotEmpty(shipmentIncrementId)) {
            LOG.info("Shipment Successfully created in Magento - " +
                    "ShipmentIncrementID from Magento is " + shipmentIncrementId);
            if(LOG.isDebugEnabled()) {
                //response
                LOG.debug("Response from Magento : " + domSourceToString(response));
            }
            /**
             * If there are tracking numbers, add that to Magento
             */
            List<String> magentoTrackingIds = new ArrayList<>();
            for(String trackingNumber : trackingNumbers) {
                String magentoTrackingId = null;
                t = ve.getTemplate(VT_ADD_TRACKING);
                context = new VelocityContext();
                context.put("sessionId", sessionId);
                context.put("shipmentIncrementId", shipmentIncrementId);
                String magentoCarrier = getMagentoCarrierFromCantataCarrier(cantataCarrier);
                context.put("carrier", magentoCarrier);
                context.put("title", "Shipment for " + salesOrder.getHeader().getOrderNumber());
                context.put("trackingNumber", trackingNumber);
                writer = new StringWriter();
                t.merge(context, writer);
                body = writer.toString();
                response = (DOMSource)template.requestBody(endPoint, body);
                magentoTrackingId = xpathQuery(response.getNode(), "//result");
                LOG.info("Magento TrackingID is " + magentoTrackingId);
                magentoTrackingIds.add(magentoTrackingId);
            }

            boolean isPartiallyShipped = false;
            if(salesOrder.getHeader().getStatus() != null) {
                isPartiallyShipped = salesOrder.getHeader().getStatus().value().equals(OrderStatus
                        .PARTIALLY_SHIPPED.value());
            }
            /**
             * If order is partially shipped change the status in Magento
             */
            if(isPartiallyShipped) {
                LOG.info("Order is partially shipped, changing the status in Magento  for {}" ,magentoOrderId);
                changeMagentoStatus(ve,sessionId, magentoOrderId, MagentoStatus.partially_shipped.name(),
                        template, endPoint);
            }

            magentoResponse = new MagentoResponse(shipmentIncrementId, StringUtils.join(magentoTrackingIds,","),
                    shipment.getId(),
                    salesOrderId, shipmentRequestId);
            String magentoResponseJson = objectMapper.writeValueAsString(magentoResponse);
            if(LOG.isDebugEnabled()) {
                LOG.debug(magentoResponseJson);
            }
            return magentoResponseJson;
        }
        else {
            String responseString = domSourceToString(response);
            magentoResponse = new MagentoResponse();
            Integer failureCount = 0;
            if(shipment.getDynAttributes().get("numMagentoShipmentAttempts") == null) {
                failureCount = 1;
            }
            else {
                failureCount = ((Integer) shipment.getDynAttributes().get("numMagentoShipmentAttempts"));
                failureCount++;
            }
            LOG.error("Magento call failed - Response from Magento " + responseString);
            magentoResponse.setFailureCount(failureCount);
            magentoResponse.setShipmentId(shipment.getId());
            magentoResponse.setCallFailed(true);
        }
        return objectMapper.writeValueAsString(magentoResponse);
    }

    @Override
    Object handleLoginErrors(String json, Exchange exchange) throws IOException, B2BTransactionFailed {
        ObjectMapper objectMapper = createObjectMapper();
        Shipment shipment = objectMapper.readValue(json,
                Shipment.class);

        String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);
        shipment = shipmentDAO.getByRefName(shipment.getRefName(), dataDomain);
        MagentoResponse magentoResponse = new MagentoResponse();
        Integer failureCount = 0;
        if(shipment.getDynAttributes().get("numMagentoShipmentAttempts") == null) {
            failureCount = 1;
        }
        else {
            failureCount = ((Integer) shipment.getDynAttributes().get("numMagentoShipmentAttempts"));
            failureCount++;
        }
        LOG.error("Magento Login Failed - Retry Count " + failureCount);
        magentoResponse.setFailureCount(failureCount);
        magentoResponse.setShipmentId(shipment.getId());
        magentoResponse.setCallFailed(true);
        return objectMapper.writeValueAsString(magentoResponse);
    }

    /**
     * Get the magento carrier from cantata
     * @param cantataCarrier cantataCarrier
     * @return magentoCarrier
     * @throws B2BTransactionFailed
     */
    private String getMagentoCarrierFromCantataCarrier(String cantataCarrier) throws B2BTransactionFailed {
        if(cantataCarrier != null) {
            Correlation correlation =
                    correlationDAO.getByRefName("CantataToMagentoCarriers", "app.cantata");
            checkNotNull("CantataToMagentoCarriers correlation Null.. check the magentoCorrelations.js is executed");
            return (String)correlation.getValuesForKey(cantataCarrier);
        }
        return "";
    }

    /**
     * Get the shipment tracking url for a carrier from correlation
     * @param cantataCarrier cantataCarrier
     * @return magentoCarrier
     * @throws B2BTransactionFailed
     */
    private String getTrackingUrlForCarrier(String cantataCarrier) throws B2BTransactionFailed {
        if(cantataCarrier != null) {
            Correlation correlation =
                    correlationDAO.getByRefName("ShipmentTrackingUrl", "app.cantata");
            checkNotNull("ShipmentTrackingUrl correlation Null..");
            return (String)correlation.getValuesForKey(cantataCarrier);
        }
        return "";
    }


    private String changeMagentoStatus(VelocityEngine ve, String sessionId, String magentoOrderId,
                                       String status, ProducerTemplate template, String endPoint)
            throws XPathExpressionException, TransformerException {
        Template t = ve.getTemplate(VT_SALES_ORDER_ADD_COMMENT);
        VelocityContext context = new VelocityContext();
        context.put("sessionId", sessionId);
        context.put("orderIncrementId", magentoOrderId);
        context.put("status", status);
        StringWriter writer = new StringWriter();
        t.merge(context, writer);
        String body = writer.toString();

        if(LOG.isInfoEnabled()) {
            LOG.info(" *****  !!!! --- *****");
            LOG.info("Sending body:"+body+" to magento");
            LOG.info(" *****  !!!! --- *****");
        }

        DOMSource response = (DOMSource) template.requestBody(endPoint, body);

        String result = xpathQuery(response.getNode(), "//result");

        if(LOG.isInfoEnabled()) {
            LOG.info("Status change for {} resulted in {}", magentoOrderId, result);
            LOG.info("Response: " + domSourceToString(response));
        }
        return result;

    }

    /**
     * Create comment with tracking number URLs
     * @param trackingNumbers
     * @param carrier
     * @return
     * @throws B2BTransactionFailed
     */
    private String createCommentWithTrackingInfo(Set<String> trackingNumbers, String carrier)
            throws B2BTransactionFailed {
        if(trackingNumbers.size() > 0 && carrier != null) {
            StringBuilder comment = new StringBuilder("View Order Tracking Details - ");
            List<String> trackingLinks = new ArrayList<>();
            String trackingUrl = getTrackingUrlForCarrier(carrier);
            if (trackingUrl != null) {
                for (String trackingNumber : trackingNumbers) {
                    /**
                     * Substitute trackingNumber on tracking url
                     */
                    String url = String.format(trackingUrl, trackingNumber);
                    trackingLinks.add(String.format("<a href='%s'>%s</a>", url, trackingNumber));
                }
            }
            comment.append(Joiner.on(",").join(trackingLinks));
            return StringEscapeUtils.escapeHtml(comment.toString());
        }
        else {
            return "";
        }
    }

    /**
     * This class will be serialised in to a JSON and will be set in to the Body.
     */
    class MagentoResponse {
        String magentoShipmentIncrementId;
        String magentoTrackingNumber;
        String shipmentId;
        String salesOrderId;
        String shipmentRequestId;
        boolean callFailed;
        double failureCount;
        String type = "SHIPMENT_CREATE";

        public MagentoResponse() {}

        public MagentoResponse(String magentoShipmentIncrementId, String magentoTrackingNumber,
                               String shipmentId, String salesOrderId, String shipmentRequestId) {
            this.magentoShipmentIncrementId = magentoShipmentIncrementId;
            this.magentoTrackingNumber = magentoTrackingNumber;
            this.shipmentId = shipmentId;
            this.salesOrderId = salesOrderId;
            this.shipmentRequestId = shipmentRequestId;
        }

        public String getMagentoShipmentIncrementId() {
            return magentoShipmentIncrementId;
        }

        public void setMagentoShipmentIncrementId(String magentoShipmentIncrementId) {
            this.magentoShipmentIncrementId = magentoShipmentIncrementId;
        }

        public String getMagentoTrackingNumber() {
            return magentoTrackingNumber;
        }

        public void setMagentoTrackingNumber(String magentoTrackingNumber) {
            this.magentoTrackingNumber = magentoTrackingNumber;
        }

        public String getShipmentId() {
            return shipmentId;
        }

        public void setShipmentId(String shipmentId) {
            this.shipmentId = shipmentId;
        }

        public String getSalesOrderId() {
            return salesOrderId;
        }

        public void setSalesOrderId(String salesOrderId) {
            this.salesOrderId = salesOrderId;
        }

        public String getShipmentRequestId() {
            return shipmentRequestId;
        }

        public void setShipmentRequestId(String shipmentRequestId) {
            this.shipmentRequestId = shipmentRequestId;
        }

        public boolean isCallFailed() {
            return callFailed;
        }

        public void setCallFailed(boolean callFailed) {
            this.callFailed = callFailed;
        }

        public double getFailureCount() {
            return failureCount;
        }

        public void setFailureCount(double failureCount) {
            this.failureCount = failureCount;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }
    }

}
