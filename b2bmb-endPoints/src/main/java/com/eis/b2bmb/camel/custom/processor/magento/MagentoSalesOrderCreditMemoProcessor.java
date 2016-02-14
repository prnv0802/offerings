package com.eis.b2bmb.camel.custom.processor.magento;

import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.core.api.v1.exception.*;
import com.eis.extsvrs.magento.api.v1.model.OrderItemId;
import com.eis.extsvrs.magento.api.v1.model.SalesOrderEntity;
import com.eis.extsvrs.magento.api.v1.model.SalesOrderItemEntity;
import com.eis.ssit.api.v1.dao.SalesOrderDAO;
import com.eis.ssit.api.v1.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.commons.lang.StringUtils;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;


/**
 * Created by Mmeherali on 7/16/2015.
 * Processor for creating Magento refunds after a line item is cancelled in OMS
 */
public class MagentoSalesOrderCreditMemoProcessor extends MagentoProcessorBase {

    private static final Logger LOG = LoggerFactory.getLogger(MagentoSalesOrderCreditMemoProcessor.class);
    private static final String VT_SALES_ORDER_CREDITMEMO_CREATE="soaptemplates/magento/SalesOrderCreditmemoCreate.vm";

    @Autowired
    SalesOrderDAO salesOrderDAO;

    @Override
    Object process(String json, String sessionId, VelocityEngine ve, Exchange exchange,
                   ProducerTemplate template, String endPoint)
            throws IOException, XPathExpressionException,
            TransformerException, B2BNotAuthorizedException, B2BTransactionFailed, B2BNotAuthenticatedException,
            B2BNotFoundException, ValidationException {

        ObjectMapper objectMapper = createObjectMapper();

        SalesOrder order = objectMapper.readValue(json,
                SalesOrder.class);

        String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);

        checkNotNull(dataDomain, "dataDomain can not be null");
        checkNotNull(order, "Cannot recreate SalesOrder from JSON");

        SalesOrder salesOrder = salesOrderDAO.getByRefName(order.getRefName(), dataDomain);
        checkNotNull(salesOrder, "Could not retrieve salesOrder for refId " + order.getRefName());

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

        /**
         * Magento salesOrder entity
         */
        SalesOrderEntity magentoSalesOrder = getSalesOrderEntity(sessionId, magentoOrderId, ve, exchange,
                template, endPoint);

        /**
         * create orderItemId entities for all items that are cancelled, but not updated in Magento
         */
        List<OrderItemId> orderItemIds = getOrderItemsIdsToCancel(salesOrder.getLineItems(), magentoSalesOrder);

        /**
         * Filter out orderItems with qty greater than zero. These are the ones that will be sent to Magento.
         * Orders with qty zero are the ones cancelled from Magento admin, for which we just need to
         * update the OMS cancelledInMagento flag
         */
        List<OrderItemId> orderItemIdsToSentToMagento =Lists.newArrayList(Iterables.filter(orderItemIds,
                new Predicate<OrderItemId>() {
            @Override
            public boolean apply(OrderItemId orderItemId) {
                return orderItemId != null && orderItemId.getQuantity() > 0;
            }
        }));

        String creditMemoId = "";
        boolean callFailed = false;
        String responseString = null;
        if(orderItemIdsToSentToMagento.size() > 0) {
            Template t = ve.getTemplate(VT_SALES_ORDER_CREDITMEMO_CREATE);
            VelocityContext context = new VelocityContext();
            context.put("sessionId", sessionId);
            context.put("orderIncrementId", salesOrder.getHeader().getOriginalOrderNumber());
            context.put("comment", "");
            context.put("notifyCustomer", 1);
            context.put("includeComment", 0);
            context.put("orderItemIds", orderItemIds);
            StringWriter writer = new StringWriter();
            t.merge(context, writer);
            String body = writer.toString();

            if (LOG.isInfoEnabled()) {
                LOG.info("Sending body:" + body + " to magento");
            }

            DOMSource response = (DOMSource) template.requestBody(endPoint, body);
            responseString = domSourceToString(response);

            if (LOG.isInfoEnabled()) {
                LOG.info("Magento response : " + responseString);
            }

            creditMemoId = xpathQuery(response.getNode(), "//result");
            callFailed = StringUtils.isEmpty(creditMemoId);
        }

        MagentoResponse magentoResponse = null;
        callFailed = true;
        if(!callFailed) {
            if(LOG.isInfoEnabled()) {
                LOG.info("Refund created in Magento - CreditMemoId is " + creditMemoId);
            }
            magentoResponse = new MagentoResponse(creditMemoId, orderItemIds, salesOrder.getId());
        }
        else {
            magentoResponse = new MagentoResponse();
            Integer failureCount = 0;
            if(salesOrder.getDynAttributes().get("numMagentoCancelLineItemAttempts") == null) {
                failureCount = 1;
            }
            else {
                failureCount = ((Integer) salesOrder.getDynAttributes().get("numMagentoCancelLineItemAttempts"));
                failureCount++;
            }
            LOG.error("Magento call failed - Response from Magento " + responseString);
            magentoResponse.setFailureCount(failureCount);
            magentoResponse.setSalesOrderId(salesOrder.getId());
            magentoResponse.setCallFailed(true);
        }
        String magentoResponseJson = objectMapper.writeValueAsString(magentoResponse);
        if(LOG.isDebugEnabled()) {
            LOG.debug(magentoResponseJson);
        }
        return magentoResponseJson;

    }


    /**
     * Computes the quantity for a refund and returns a list of orderItemIds
     * that will contain the itemId and qty to refund.
     */
    private List<OrderItemId> getOrderItemsIdsToCancel(List<BaseOrderLine> omsLineItems, SalesOrderEntity magentoSO) {
        Map<String, Integer> itemIdsCancelled = new HashMap<>();
        if(magentoSO.getSalesOrderItems() != null) {
            for(int i =0; i < magentoSO.getSalesOrderItems().length; i++) {
                SalesOrderItemEntity magentoSalesOrderItem = magentoSO.getSalesOrderItems()[i];
                itemIdsCancelled.put( magentoSalesOrderItem.getItem_id(),
                        parseNumeric(magentoSalesOrderItem.getQty_refunded()));
            }
        }
        List<OrderItemId> orderItemIds = new ArrayList<>();
        for(OrderLine line : omsLineItems) {
            if(OrderLineStatus.CANCELLED.value().equals(line.getStatus().value())
                    && line.getDynAttributes().get("cancelledInMagento") != null
                    && line.getDynAttributes().get("cancelledInMagento").equals("N")) {
                String itemId = line.getLineItemNumber();
                int refundedQtyInMagento = itemIdsCancelled.get(itemId);
                int totalToCancel = line.getItemQty() - refundedQtyInMagento;
                OrderItemId orderItemId = new OrderItemId();
                orderItemId.setOrderItemId(itemId);
                orderItemId.setQuantity(totalToCancel);
                orderItemIds.add(orderItemId);
                LOG.info( String.format("%s.ItemId to cancel - %s with Qty %s",
                        magentoSO.getIncrement_id(), itemId, totalToCancel));
            }
        }
        return orderItemIds;
    }

    private static int parseNumeric(String numeric) {
        int i = 0;
        if(numeric != null) {
            try {
                i = (int)Float.parseFloat(numeric.trim());
            }
            catch (NumberFormatException n) {
                i = 0;
            }
        }
        return i;
    }

    @Override
    Object handleLoginErrors(String json, Exchange exchange) throws IOException, B2BTransactionFailed {
        ObjectMapper objectMapper = createObjectMapper();
        SalesOrder salesOrder = objectMapper.readValue(json,
                SalesOrder.class);

        String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);
        salesOrder = salesOrderDAO.getByRefName(salesOrder.getRefName(), dataDomain);
        MagentoResponse magentoResponse = new MagentoResponse();
        Integer failureCount = 0;
        if(salesOrder.getDynAttributes().get("numMagentoCancelLineItemAttempts") == null) {
            failureCount = 1;
        }
        else {
            failureCount = ((Integer) salesOrder.getDynAttributes().get("numMagentoCancelLineItemAttempts"));
            failureCount++;
        }
        LOG.error("Magento Login Failed - Retry Count " + failureCount);
        magentoResponse.setFailureCount(failureCount);
        magentoResponse.setSalesOrderId(salesOrder.getId());
        magentoResponse.setCallFailed(true);
        return objectMapper.writeValueAsString(magentoResponse);
    }

    /**
     * This class will be serialised in to a JSON and will be set in to the Body.
     */
    class MagentoResponse {
        String creditMemoId;
        String itemIds;
        String salesOrderId;
        boolean callFailed;
        double failureCount;
        String type = "CREDIT_MEMO";

        public MagentoResponse() {}

        public MagentoResponse(String creditMemoId, List<OrderItemId> orderItemIds, String salesOrderId) {
            this.creditMemoId = creditMemoId;
            List<String> tempList = new ArrayList<>();
            for(OrderItemId o : orderItemIds) {
                tempList.add(o.getOrderItemId());
            }
            this.itemIds = Joiner.on(",").join(tempList);
            this.salesOrderId = salesOrderId;
        }

        public String getSalesOrderId() {
            return salesOrderId;
        }

        public void setSalesOrderId(String salesOrderId) {
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

        public String getCreditMemoId() {
            return creditMemoId;
        }

        public void setCreditMemoId(String creditMemoId) {
            this.creditMemoId = creditMemoId;
        }

        public String getItemId() {
            return itemIds;
        }

        public void setItemId(String i) {
            this.itemIds = i;
        }

    }

}
