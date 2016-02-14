package com.eis.b2bmb.camel.custom.processor.magento;

import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.core.api.v1.exception.*;
import com.eis.extsvrs.magento.api.v1.model.SalesOrderEntity;
import com.eis.ssit.api.v1.dao.SalesOrderDAO;
import com.eis.ssit.api.v1.model.Reference;
import com.eis.ssit.api.v1.model.SalesOrder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathExpressionException;
import java.io.IOException;
import java.io.StringWriter;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Created by Mmeherali on 7/29/2015.
 * Processor for setting the salesOrder on hold in Magento
 */
public class MagentoSalesOrderHold extends MagentoProcessorBase {

    private static final Logger LOG = LoggerFactory.getLogger(MagentoSalesOrderHold.class);

    // FIXME creates a dependency on data, should be either a default or a configuration item
    private static final String VT_SALES_ORDER_HOLD = "soaptemplates/magento/SalesOrderHold.vm";

    @Autowired
    SalesOrderDAO salesOrderDAO;

    /**
     * Accepts a salesOrder json. Following are the required fields on the json
     * - salesOrder.header.salesOrderNumber
     * - salesOrder.refName
     * - dataDomain should exist in the domain and a valid salesOrder should exist for the refName and dataDomain
     * <p>
     * After successfully updating the status on Magento, salesOrder.magentoSalesOrderHoldStatus will be
     * updated with a value of 'Y'
     */
    @Override
    Object process(String json, String sessionId, VelocityEngine ve, Exchange exchange,
                   ProducerTemplate template, String endPoint) throws IOException, XPathExpressionException,
            TransformerException, B2BNotAuthorizedException, B2BTransactionFailed, B2BNotAuthenticatedException,
            B2BNotFoundException, ValidationException {

        String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);
        checkNotNull(dataDomain, "dataDomain cannot be null");

        ObjectMapper objectMapper = createObjectMapper();
        SalesOrder salesOrder = objectMapper.readValue(json,
                SalesOrder.class);
        checkNotNull(salesOrder, "Not able to serialise salesOrder");
        checkNotNull(salesOrder.getHeader(), "SalesOrderHeader is null");
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

        checkNotNull(magentoOrderId,
                "Original Sales Number from Magento is null, but expected to be there for order "
                        + salesOrder.getId());

        SalesOrder so = salesOrderDAO.getByRefName(salesOrder.getRefName(), dataDomain);
        checkNotNull(so, "Sales Order with refNumber " + salesOrder.getRefName()
                + "Not found for datadomain " + dataDomain);

        SalesOrderEntity magentoSalesOrder = getSalesOrderEntity(sessionId,
                magentoOrderId, ve, exchange, template, endPoint);
        boolean magentoOrderAlreadyOnHold = magentoSalesOrder.isOrderOnHold();
        if(LOG.isInfoEnabled()) {
            LOG.info("magentoSalesOrder.isOrderOnHold(): {} - {} ", magentoOrderId, magentoOrderAlreadyOnHold);
        }
        String magentoCallResult = null, callResponse = null;

        if(!magentoOrderAlreadyOnHold) {
            Template t = ve.getTemplate(VT_SALES_ORDER_HOLD);
            VelocityContext context = new VelocityContext();
            context.put("sessionId", sessionId);
            context.put("orderIncrementId", magentoOrderId);
            StringWriter writer = new StringWriter();
            t.merge(context, writer);
            String body = writer.toString();
            DOMSource response = (DOMSource) template.requestBody(endPoint, body);
            callResponse = domSourceToString(response);
            magentoCallResult = xpathQuery(response.getNode(), "//result");
        }

        if ((magentoCallResult != null && magentoCallResult.equals("true")) || magentoOrderAlreadyOnHold ) {
            return objectMapper.writeValueAsString(new MagentoResponse(false, so.getId()));
        }
        else {
            MagentoResponse magentoResponse = new MagentoResponse(true, so.getId());
            Integer failureCount = 0;
            if(so.getDynAttributes().get("numMagentoHoldAttempts") == null) {
                failureCount = 1;
            }
            else {
                failureCount = ((Integer) so.getDynAttributes().get("numMagentoHoldAttempts"));
                failureCount++;
            }
            LOG.error("Magento call failed - Response from Magento " + magentoCallResult);
            magentoResponse.setFailureCount(failureCount);
            return objectMapper.writeValueAsString(magentoResponse);
        }
    }

    @Override
    Object handleLoginErrors(String json, Exchange exchange) throws IOException, B2BTransactionFailed {

        String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);

        ObjectMapper objectMapper = createObjectMapper();
        SalesOrder salesOrder = objectMapper.readValue(json,
                SalesOrder.class);
        SalesOrder so = salesOrderDAO.getByRefName(salesOrder.getRefName(), dataDomain);
        MagentoResponse magentoResponse = new MagentoResponse(true, so.getId());
        Integer failureCount = 0;
        if(so.getDynAttributes().get("numMagentoHoldAttempts") == null) {
            failureCount = 1;
        }
        else {
            failureCount = ((Integer) so.getDynAttributes().get("numMagentoHoldAttempts"));
            failureCount++;
        }
        LOG.error("Magento login failed - Retry count " + failureCount);
        magentoResponse.setFailureCount(failureCount);
        return objectMapper.writeValueAsString(magentoResponse);
    }

    class MagentoResponse {
        boolean callFailed;
        double failureCount;
        String type = "HOLD";
        String salesOrderId;

        MagentoResponse(boolean callFailed, String salesOrderId) {
            this.callFailed = callFailed;
            this.salesOrderId = salesOrderId;
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

        public String getSalesOrderId() {
            return salesOrderId;
        }

        public void setSalesOrderId(String salesOrderId) {
            this.salesOrderId = salesOrderId;
        }
    }


}
