package com.eis.ssit.api.v1.dao.sprngmongo;

import com.eis.core.api.v1.dao.AccountDAO;
import com.eis.core.api.v1.dao.BlobDAO;
import com.eis.core.api.v1.dao.NotificationDAO;
import com.eis.core.api.v1.dao.SiteDAO;
import com.eis.core.api.v1.events.WebHookEvent;
import com.eis.core.api.v1.events.publishers.WebHookEventPublisher;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.*;
import com.eis.core.api.v1.service.impl.BusinessNumberGenerator;
import com.eis.security.multitenancy.model.SecureSession;
import com.eis.ssit.api.v1.allocengine.*;
import com.eis.ssit.api.v1.allocengine.dto.SalesOrderData;
import com.eis.ssit.api.v1.dao.*;
import com.eis.ssit.api.v1.dao.gen.mongo.impl.SalesOrderDAOBaseImpl;
import com.eis.ssit.api.v1.model.*;
import com.eis.tools.HumanNameParser;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.krysalis.barcode4j.impl.code128.Code128Bean;
import org.krysalis.barcode4j.output.bitmap.BitmapCanvasProvider;
import org.krysalis.barcode4j.tools.UnitConv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoOperations;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.jms.core.JmsTemplate;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.Map.Entry;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * User: mingardia Date: 11/19/13 Time: 8:13 PM
 * FIXME way too much going on in this DAO now, much more than simply persisting a sales order DTO
 * FIXME consider moving to service classes
 */
public class SalesOrderDAOImpl extends SalesOrderDAOBaseImpl implements SalesOrderDAO {

    private static final Logger LOG = LoggerFactory.getLogger(SalesOrderDAOImpl.class);

    /**
     * The notificationDAO implementation
     */
    @Autowired
    NotificationDAO notificationDAO;

    /**
     * The allocationJmsTemplate implementation
     */
    @Autowired
    JmsTemplate allocationJmsTemplate;

    /**
     * The inventoryAllocationEngine implementation
     * FIXME hate having Sales order dependent on inventoryEngine when engine is dependent on sales order
     * FIXME creates circular dependency between engine and DAO.
     */
    @Autowired
    InventoryAllocationEngine inventoryAllocationEngine;

    @Autowired
    InventoryAllocationHelper inventoryAllocationHelper;

    @Autowired
    InventoryValidatorHelper inventoryValidatorHelper;

    @Autowired
    InventoryTransactionHelper inventoryTransactionHelper;

    @Autowired
    TxnDeleteDAO txnDeleteDAO;

    @Autowired
    InventoryPoolEntryAuditLogDAO inventoryPoolEntryAuditLogDAO;

    @Autowired
    InventoryAllocationLoggerDAO inventoryAllocationLoggerDAO;

    @Autowired
    OrderReleaseLogDAO releaseLogDAO;

    @Autowired
    ReservationEntryDAO reservationEntryDAO;

    @Autowired
    ShipmentRequestDAO shipmentRequestDAO;

    @Autowired
    ReservationDAO reservationDAO;

    @Autowired
    ShipmentDAO shipmentDAO;

    @Autowired
    WebHookEventPublisher webHookEventPublisher;

    @Autowired
    BlobDAO blobDAO;

    @Autowired
    BusinessNumberGenerator businessNumberGenerator;


    @Autowired
    ProductDAO productDAO;

    @Autowired
    ChannelDAO channelDAO;

    @Autowired
    AccountDAO accountDAO;


    @Autowired
    InventoryPoolEntryDAO inventoryPoolEntryDAO;

    @Autowired
    SiteDAO siteDAO;

    @Autowired
    Validator validator;


    /**
     * MongoOps
     */
    @Autowired
    @Qualifier("mongoTemplate")
    protected MongoOperations mongoOps;

    /**
     * Gets the next sales order number to be used for the given refName. Once
     * the Sales Order Number is retrieved the control number is incremented.
     *
     * @param refName    - String refName of the VendorControlNumber
     * @param dataDomain - String dataDomain
     * @return a long for the next control number to be used
     * @throws B2BTransactionFailed - if the control number can not be retrieved
     * @throws B2BNotFoundException - if the control number can not be updated
     * @throws ValidationException  - if the sequence is not validated
     */
    public String getNextSalesOrderNumber(String refName, String dataDomain)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        String prefixCorrelationName = "salesOrderNumPrefix";
        String lengthCorrelationName = "salesOrderNumLength";

        return businessNumberGenerator.getNextNumber(refName, dataDomain, prefixCorrelationName, lengthCorrelationName);
    }

    @Override
    public List<SalesOrder> findByLookupKey(String lookupKey, int offset, int length, List<String> fields,
                                            List<String> dataDomains) throws B2BTransactionFailed {

        if (dataDomains == null) {
            throw new IllegalArgumentException("dataDomains is a required field but is null");
        }

        if (lookupKey == null) {
            throw new IllegalArgumentException("lookup key must be non-null");
        }

        List<SalesOrder> rc;

        if (fields == null) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("returning full objects");
            }

            if (dataDomains.isEmpty()) {
                throw new IllegalStateException("Domains is empty and should not be.");
            }

            if (!dataDomains.contains("*")) {
                rc = mongoOps.find(new Query(Criteria.where("dataDomain").in(dataDomains)
                                .andOperator(Criteria.where("header.orderLookUpKey").is(lookupKey)))
                                .with(new Sort(Sort.Direction.DESC, "header.purchaseDate")).skip(offset).limit(length),
                        SalesOrder.class);
            }
            else {
                rc = mongoOps.find(
                        new Query(Criteria.where("header.orderLookupKey").is(lookupKey))
                                .with(new Sort(Sort.Direction.DESC, "header.purchaseDate")).skip(offset).limit(length),
                        SalesOrder.class);
            }

        }
        else {
            if (fields.isEmpty()) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Fields passed but is zero length??");
                }
                throw new IllegalArgumentException("Fields passed but length was zero?");
            }

            if (LOG.isDebugEnabled()) {
                LOG.debug("-- > Filtering down to " + fields.size() + " fields");
            }

            Query q;

            if (!dataDomains.contains("*")) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("filtering by dataDomain:");

                    for (String dd : dataDomains) {
                        LOG.debug("  >" + dd);
                    }
                }

                q = new Query(Criteria.where("dataDomain").in(dataDomains))
                        .with(new Sort(Sort.Direction.DESC, "header.purchaseDate")).skip(offset).limit(length);
            }
            else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("Data domains contains a * returning all rows");
                }

                q = new Query(Criteria.where("header.orderLookupKey").is(lookupKey))
                        .with(new Sort(Sort.Direction.DESC, "header.purchaseDate")).skip(offset).limit(length);
            }

            if (fields.size() >= 1) {
                if (LOG.isDebugEnabled()) {
                    LOG.debug("filtering fields size:" + fields.size());
                }

                boolean dataDomainIncluded = false;
                boolean refNameIncluded = false;
                if (fields.size() > 1) {
                    for (String field : fields) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug(" Adding Field:" + field + " to include list");
                        }

                        if (field.equals("dataDomain")) {
                            dataDomainIncluded = true;
                        }

                        if (field.equals("refName")) {
                            refNameIncluded = true;
                        }

                        q.fields().include(field);
                    }

                }
                else {

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("fields size is <=1 size:" + fields.size());
                    }

                    if (fields.size() == 1) {
                        // Assume its a single comma delimited field
                        String compositeField = fields.get(0);
                        String[] compositeFields = compositeField.split(",");

                        if (LOG.isDebugEnabled()) {
                            LOG.debug(" compositeFields Size:" + compositeFields.length);
                        }

                        if (compositeFields.length == 0) {
                            throw new IllegalArgumentException("1 Field has been passed to filter yet splitting field"
                                    + " by , yields a empty set.  Invalid field " + "argument?  Fields:"
                                    + fields.get(0));
                        }

                        for (String field : compositeFields) {
                            if (LOG.isDebugEnabled()) {
                                LOG.debug(" Adding Field:" + field + " to include list");
                            }

                            if (field.equals("dataDomain")) {
                                dataDomainIncluded = true;
                            }

                            if (field.equals("refName")) {
                                refNameIncluded = true;
                            }

                            q.fields().include(field);
                        }

                        if (!dataDomainIncluded) {
                            q.fields().include("dataDomain");
                        }

                        if (!refNameIncluded) {
                            q.fields().include("refName");
                        }
                    }
                    else {
                        throw new IllegalArgumentException(
                                "if you pass in fields you must pass at least 1 value. 0 " + "values received");
                    }

                }

            }
            else {
                throw new IllegalArgumentException("Fields passed but size is not >= 1");
            }

            rc = mongoOps.find(q, SalesOrder.class);
        }
        return rc;

    }

    /**
     * Places an order on hold
     *
     * @param order  - the order we want to place on hold
     * @param header - the header of the notification
     * @param body   -the body of the notification
     * @throws B2BTransactionFailed - the transaction failed
     * @throws B2BNotFoundException - the notification could not be saved
     * @throws ValidationException  - validation exception
     */
    protected void placeOrderOnHold(SalesOrder order, String header, String body)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        order.getHeader().setStatus(OrderStatus.HOLD_MANUAL_REVIEW_REQUIRED);

        // FIXME should notify on a topic
        Notification n = new Notification();
        n.setNotificationType(NotificationType.Alert);
        n.setHeader(header);
        n.setBody(body);
        n.setRefName(order.getRefName() + "-NOT-" + System.nanoTime());
        n.setDataDomain(order.getDataDomain());
        n.setSource("SmartShipIt");
        n.setNotificationDate(new Date());
        n = notificationDAO.save(n);
        order.getNotifications().add(n);

        save(order);
    }

    /**
     * Splits out logic to calculate the amounts in the sales order
     *
     * @param salesOrder order to calculate amounts on
     */
    protected void calculate(SalesOrder salesOrder) {
        // TODO: fully vet
        // make sure the totals are right
        int totalItems = 0;
        float total = 0;
        float totalFulfillmentServiceAmount = 0;
        for (OrderLine line : salesOrder.getLineItems()) {

            if (!line.getStatus().equals(OrderLineStatus.CANCELLED)) {
                if (line.getSystemCatalogProductIdentifier() != null) {
                    Product product = productDAO.findProductByProductIdentifier(
                            line.getSystemCatalogProductIdentifier(),
                            salesOrder.getDataDomains());
                    if (product != null && product.getItem() != null && product.getItem().getWeight() > 0) {
                        line.setItemWeight(product.getItem().getWeight());
                    }
                }

                if (Float.compare(line.getItemUnitDiscount(), 0) == 0) {

                    BigDecimal price = new BigDecimal(line.getItemQty() * line.getItemUnitPrice());
                    line.setLinePriceTotal(price.setScale(2, BigDecimal.ROUND_UP).floatValue());
                }
                else {
                    BigDecimal price = new BigDecimal(
                            line.getItemQty() * (line.getItemUnitPrice() - line.getItemUnitDiscount()));
                    line.setLinePriceTotal(price.setScale(2, BigDecimal.ROUND_UP).floatValue());
                }
                for (FulfillmentService f : line.getFulfillmentServices()) {
                    totalFulfillmentServiceAmount = totalFulfillmentServiceAmount + f.getFee();
                }
                // FIXME: Really the way this is calculated varies by state b/c some
                // use s and h and some do not,
                // right now always taxing b/c the tax is on the sum of extended
                // totals

                BigDecimal extendedTotal = new BigDecimal(
                        line.getLinePriceTotal() + line.getShippingCharges() + line.getHandlingCharges());
                line.setLineExtendedTotal(extendedTotal.setScale(2, BigDecimal.ROUND_UP).floatValue());
                total = total + line.getLineExtendedTotal();
                totalItems = totalItems + line.getItemQty();
            }
        }

        salesOrder.getHeader().setTotalItemsSold(totalItems);

        if (salesOrder.getHeader().isRunCalculations()) {

            SalesOrderHeader orderHeader = (SalesOrderHeader) salesOrder.getHeader();
            orderHeader.setLineItemTotal(total);
            orderHeader.setSubtotalBeforeTax(total - orderHeader.getDiscountAmount());

            // FIXME: Really the way this is calculated varies by state b/c some
            // use s and h and some do not,
            // right now always taxing
            orderHeader.setSubtotalTaxable(orderHeader.getSubtotalBeforeTax()
                    + orderHeader.getShippingCharges() + orderHeader.getHandlingCharges());
            orderHeader.setShippingHandlingTaxExcluded(false);

            orderHeader.setTaxAmount(
                    orderHeader.getSubtotalTaxable() * (orderHeader.getTaxRate() / 100));

            orderHeader
                    .setTotalAmountDue(orderHeader.getSubtotalBeforeTax()
                            + orderHeader.getTaxAmount() + orderHeader.getShippingCharges()
                            + orderHeader.getHandlingCharges()
                            + totalFulfillmentServiceAmount);
        }
        SalesOrderHeader orderHeader = (SalesOrderHeader) salesOrder.getHeader();
        // make sure payments are copied down
        if (salesOrder.getPayments() != null && !salesOrder.getPayments().isEmpty()) {
            float totalPaid = 0;
            for (Payment payment : salesOrder.getPayments()) {
                totalPaid = totalPaid + payment.getAmount();
            }
            orderHeader.setPaymentAmount(totalPaid);
        }

        if (Float.compare(orderHeader.getBalance(), 0) == 0) {
            orderHeader
                    .setBalance(orderHeader.getTotalAmountDue() - orderHeader.getPaymentAmount());
        }

    }

    @Override
    public SalesOrder save(SalesOrder objectToBeSaved)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        if (objectToBeSaved == null) {
            throw new IllegalArgumentException("objectToBeSaved can not be null");
        }

        if (objectToBeSaved.getDataDomain() == null) {
            throw new IllegalArgumentException("data domain must be non-null");
        }

        if (objectToBeSaved.getRefName() == null) {
            throw new IllegalArgumentException("refName must be non-null");
        }
        if (objectToBeSaved.getId() != null && StringUtils.isBlank(objectToBeSaved.getId())) {
            throw new IllegalArgumentException("id must not be blank");
        }
        if (objectToBeSaved.getHeader() == null) {
            throw new IllegalArgumentException("header must be non-null");
        }

        if (LOG.isDebugEnabled()) {
            if (objectToBeSaved.getId() == null) {
                LOG.debug("ID not set so generating one for refName:" + objectToBeSaved.getRefName());
            }
        }

        validate(objectToBeSaved);

        if (objectToBeSaved.getHeader().getOrderNumber() == null
                || objectToBeSaved.getHeader().getOrderNumber().equals("")) {
            String salesOrderNumber = getNextSalesOrderNumber("SalesOrderNumber", objectToBeSaved.getDataDomain());
            objectToBeSaved.getHeader().setOrderNumber(salesOrderNumber);
        }

        if (objectToBeSaved.getHeader().getOriginalOrderNumber() == null
                || objectToBeSaved.getHeader().getOriginalOrderNumber().equals("")) {
            objectToBeSaved.getHeader().setOriginalOrderNumber(objectToBeSaved.getHeader().getOrderNumber());
        }

        if ((objectToBeSaved.getHeader().getOrderLookUpKey() == null
                || objectToBeSaved.getHeader().getOrderLookUpKey().isEmpty())
                && objectToBeSaved.getHeader().getBuyerContactInfo() != null) {
            // default to phone
            objectToBeSaved.getHeader()
                    .setOrderLookUpKey(objectToBeSaved.getHeader().getBuyerContactInfo().getPrimaryPhone());
        }

        if (objectToBeSaved.getHeader().getPromiseDate() == null) {
            Calendar promiseDate = Calendar.getInstance();
            promiseDate.add(Calendar.DATE, 7);
            objectToBeSaved.getHeader().setPromiseDate(promiseDate.getTime());
        }

        if (objectToBeSaved.getHeader().getBuyerContactInfo() != null) {
            if (objectToBeSaved.getHeader().getBuyerContactInfo().getFirstName() == null
                    && objectToBeSaved.getHeader().getBuyerContactInfo().getLastName() == null) {
                if (objectToBeSaved.getHeader().getBuyerContactInfo().getName() != null) {
                    HumanNameParser nameParser = new HumanNameParser();
                    nameParser.parse(objectToBeSaved.getHeader().getBuyerContactInfo().getName());
                    objectToBeSaved.getHeader().getBuyerContactInfo().setFirstName(nameParser.getFirstName());
                    objectToBeSaved.getHeader().getBuyerContactInfo().setLastName(nameParser.getLastName());
                }
            }
        }

        if (objectToBeSaved.getHeader().getBillToContactInfo() != null) {
            if (objectToBeSaved.getHeader().getBillToContactInfo().getFirstName() == null
                    && objectToBeSaved.getHeader().getBillToContactInfo().getLastName() == null) {
                if (objectToBeSaved.getHeader().getBillToContactInfo().getName() != null) {
                    HumanNameParser nameParser = new HumanNameParser();
                    nameParser.parse(objectToBeSaved.getHeader().getBillToContactInfo().getName());
                    objectToBeSaved.getHeader().getBillToContactInfo().setFirstName(nameParser.getFirstName());
                    objectToBeSaved.getHeader().getBillToContactInfo().setLastName(nameParser.getLastName());
                }
            }
        }

        for (ContactInfo shipToContact : objectToBeSaved.getHeader().getShipToContacts()) {
            if (shipToContact.getFirstName() == null && shipToContact.getLastName() == null) {
                if (shipToContact.getName() != null) {
                    HumanNameParser nameParser = new HumanNameParser();
                    nameParser.parse(shipToContact.getName());
                    shipToContact.setFirstName(nameParser.getFirstName());
                    shipToContact.setLastName(nameParser.getLastName());
                }
            }

        }

        // pass to update totals...TODO: may want to only do in certain
        // scenarios??
        calculate(objectToBeSaved);

        // Need to get seller information for sales order and invoices,
        // going to set Account Id to get the organization
        if (objectToBeSaved.getHeader().getToAccountId() == null) {
            List<String> dataDomains = new ArrayList<String>();
            dataDomains.add(objectToBeSaved.getDataDomain());

            List<String> fields = new ArrayList<>();
            fields.add("id");
            List<LinkedHashMap<String, Object>> accounts = accountDAO.getBriefList(0, 1, dataDomains);

            if (accounts != null && accounts.size() == 1) {
                LinkedHashMap<String, Object> account = accounts.get(0);
                objectToBeSaved.getHeader().setToAccountId((String) account.get("id"));
            }
            else {
                throw new IllegalStateException("Found more than one account for dataDomain" + dataDomains);
            }
        }

        // Check that the refName does not exist with in the dataDomains
        // remove if you add a unique index in mongo.
        List<SalesOrder> list = mongoOps.find(new Query(Criteria.where("refName").is(objectToBeSaved.getRefName())
                .and("dataDomain").in(objectToBeSaved.getDataDomains())), SalesOrder.class);

        if (!list.isEmpty()) {
            if (list.size() > 1) {
                List<String> foundDomains = new ArrayList<>();
                List<String> foundIds = new ArrayList<>();
                for (SalesOrder lsalesOrder : list) {
                    foundDomains.addAll(lsalesOrder.getDataDomains());
                    foundIds.add(lsalesOrder.getId());
                }
                throw new IllegalStateException("Can not Save salesOrder with refName:" + objectToBeSaved.getRefName()
                        + " because multiple already exists with in the " + "passed data domains of "
                        + Joiner.on(", ").join(foundDomains) + " with ids " + Joiner.on(", ").join(foundIds));
            }
            else {
                SalesOrder lastSalesOrder = list.get(0);
                /**
                 * If the status transitioned from hold to non-hold, hold status
                 * needs to be released from magento
                 */
                if (StringUtils.equals((String) lastSalesOrder.getDynAttributes().get("fromMagento"), "Y")) {
                    List<String> holdStatuses = Lists.newArrayList(OrderStatus.BACKORDER_HOLD.name(),
                            OrderStatus.CREDIT_HOLD.value(), OrderStatus.PAYMENT_HOLD.value(),
                            OrderStatus.HOLD_MANUAL_REVIEW_REQUIRED.value());
                    if (holdStatuses.contains(lastSalesOrder.getHeader().getStatus().value())
                            && !holdStatuses.contains(objectToBeSaved.getHeader().getStatus().value())) {
                        objectToBeSaved.getDynAttributes().put("magentoHoldToBeReleased", "Y");
                    }
                }
            }

            for (SalesOrder lsalesOrder : list) {
                if (objectToBeSaved.getId() != null) {
                    if (!lsalesOrder.getId().equals(objectToBeSaved.getId())) {
                        throw new IllegalStateException("Can not Save salesOrder with refName:"
                                + objectToBeSaved.getRefName() + " because one already exists with in the"
                                + " passed data domain of " + objectToBeSaved.getDataDomain() + " existing id:"
                                + lsalesOrder.getId() + " passed id:" + objectToBeSaved.getId());
                    }
                }
                else {
                    throw new IllegalStateException("Can not save a SalesOrder with refName:"
                            + objectToBeSaved.getRefName() + " because passed object has a null id implying "
                            + "we want to create a object yet one exists. Use update or remove the existing "
                            + "object first");
                }

            }

        }

        boolean create = false;
        if (objectToBeSaved.getId() == null) {
            objectToBeSaved.setId(String.valueOf(UUID.randomUUID()));
            create = true;
        }

        ByteArrayOutputStream out = null;
        ByteArrayInputStream in = null;
        try {

            if (objectToBeSaved.getHeader().getOrderNumber() != null
                    && objectToBeSaved.getHeader().getBarCodeBlobId() == null) {
                Code128Bean bean = new Code128Bean();
                final int dpi = 300;

                // Configure the barcode generator
                bean.setModuleWidth(UnitConv.in2mm(2.8f / dpi));
                bean.doQuietZone(false);
                out = new ByteArrayOutputStream();

                BitmapCanvasProvider canvas = new BitmapCanvasProvider(out, "image/x-png", dpi, 12, false, 0);
                // Generate the barcode
                bean.generateBarcode(canvas, objectToBeSaved.getHeader().getOrderNumber());

                canvas.finish();
                in = new ByteArrayInputStream(out.toByteArray());

                // Create a New Blob
                BlobMetaData metaData = blobDAO.createMetaData();
                metaData.setPathString("barCodes/" + objectToBeSaved.getHeader().getOrderNumber());
                metaData.setDataDomain(objectToBeSaved.getDataDomain());

                if (SecureSession.getCallerIpAddress() != null) {
                    metaData.setOriginIpAddress(SecureSession.getCallerIpAddress());
                }

                if (SecureSession.getUser() != null) {
                    metaData.setOwnerId(SecureSession.getUser().getId());
                }

                metaData.setTxId(objectToBeSaved.getTxId());

                Blob b = blobDAO.createBlob("barCode:" + objectToBeSaved.getId(), in, "image/x-png", metaData);
                objectToBeSaved.getHeader().setBarCodeBlobId(b.getIdAsString());
            }
            else {
                if (objectToBeSaved.getHeader().getOrderNumber() == null) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("!!! warning sales order does not"
                                + " have a sales order number??? skipping bar code generation");
                    }
                }
            }
        } catch (IOException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Bar Code could not be generated for SalesOrder:"
                        + objectToBeSaved.getHeader().getOrderNumber());
            }
        } finally {
            try {

                if (in != null) {
                    in.close();
                }

                if (out != null) {
                    out.close();
                }
            } catch (IOException ie) {
                throw new B2BTransactionFailed("Could not close streams.", ie);
            }
        }


        objectToBeSaved = assignUUIDToLineItems(objectToBeSaved);

        for (OrderReleaseLog log : objectToBeSaved.getReleaseLogs()) {
            // if the id is null assume the log has not been saved and save it
            if (log.getId() == null) {
                if (log.getDataDomain() == null) {
                    log.setDataDomain(objectToBeSaved.getDataDomain());
                }

                if (log.getRefName() == null) {
                    log.setRefName(new Date().toString());
                }

                log = releaseLogDAO.save(log);
            }
        }

        mongoOps.save(objectToBeSaved);
        WebHookEvent event;
        if (create) {
            event = new WebHookEvent(objectToBeSaved, WebHookEvent.WEBHOOK_CREATE_EVENT);
        }
        else {
            event = new WebHookEvent(objectToBeSaved, WebHookEvent.WEBHOOK_UPDATE_EVENT);
        }
        webHookEventPublisher.publish(event);
        return objectToBeSaved;
    }

    /**
     * Method assignes UUID to LineItems which do not have a UUID set. LineItems
     * may already have an UUID attached to them as a single SO can pass through
     * allocation engine multiple times.
     *
     * @param order Order containing the lineitems
     * @return Order with UUIDs attached to lineitems.
     */
    private SalesOrder assignUUIDToLineItems(SalesOrder order) {

        List<BaseOrderLine> lineList = order.getLineItems();
        int lineNumber = 1;
        for (OrderLine sol : lineList) {
            if (sol.getUid() == null) {
                sol.setUid(String.valueOf(UUID.randomUUID()));
            }

            sol.setOmsLineNumber(lineNumber);
            lineNumber++;
        }

        return order;
    }

    @Override
    public void validate(SalesOrder salesOrder) throws ValidationException {

        super.validate(salesOrder);
        //validateProductsMatchCatalog(salesOrder);
    }

    /**
     * Validates the Products in the salesOrderLines are present in the catalog.
     * If not returns a SalesOrderError with the description of error. Null is
     * returned if validation succeeds
     *
     * @param so         salesOrder
     * @param dataDomain dataDomain
     * @return salesOrderError
     */
    public OrderError validateProductsMatchCatalog(SalesOrder so, String dataDomain) {

        List<String> productsNotFoundInCatalog = new ArrayList<>();
        Catalog catalog = null;
        String errorStr = null;
        if (so != null && so.getHeader() != null && so.getHeader().getSalesChannel() != null) {
            try {
                Channel channel = channelDAO.getByRefName(so.getHeader().getSalesChannel(), dataDomain);
                if (channel != null) {
                    catalog = channel.getCurrentCatalog();
                    checkNotNull(catalog, "currentCatalog Null on channel " + channel.getRefName());

                    for (OrderLine line : so.getLineItems()) {
                        if (OrderLineStatus.OPEN.equals(line.getStatus())) {
                            String productIdentifier = line.getSystemCatalogProductIdentifier();
                            Product product = null;
                            if (productIdentifier == null) {
                                if (line.getUpc() != null) {
                                    product = productDAO.findProductInCatalogByUPC(catalog.getId(), line.getUpc());
                                    line.setSystemCatalogProductIdentifier(product.getProductIdentifier());
                                }
                            }
                            else {
                                product = productDAO.findProductInCatalog(catalog.getId(), productIdentifier);
                            }

                            if (product == null) {
                                productsNotFoundInCatalog.add(productIdentifier);
                                LOG.error(String.format("Product identifier '%s' or not found in catalog %s",
                                        productIdentifier, catalog.getCatalogTitle()));
                            }
                        }
                    }
                }
                else {
                    errorStr = "Could not find channel:" + so.getHeader().getSalesChannel() + " " + "for datadomain:"
                            + dataDomain + " when attempting to validate sales order:" + so.getId();

                }

            } catch (B2BTransactionFailed e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error(e.getMessage(), e);
                }
                errorStr = e.getMessage();
            }
        }
        if (productsNotFoundInCatalog.size() > 0) {
            errorStr = String.format("Following products %s not found in catalog %s",
                    productsNotFoundInCatalog.toString(), catalog.getRefName());
        }
        if (StringUtils.isNotEmpty(errorStr)) {
            OrderError salesOrderError = new OrderError();
            salesOrderError.setErrorCode(OrderError.ERROR_CODES.VALIDATION_ERROR.value());
            salesOrderError.setErrorDate(new Date());
            salesOrderError.setErrorName("Validation Error");
            salesOrderError.setErrorReason(errorStr);
            salesOrderError.setErrorResolution("Please remove line items with the above products");
            return salesOrderError;
        }
        else {
            return null;
        }
    }

    @Override
    public LinkedHashMap<String, List<ShipmentRequest>> releaseSalesOrder(List<String> salesOrderIds)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        if (LOG.isDebugEnabled()) {
            LOG.debug("Releasing Sales Orders");
        }


        for (String id : salesOrderIds) {

            if (LOG.isDebugEnabled()) {
                LOG.debug(">" + id);
            }

            SalesOrder order = getById(id);


            if (order != null) {
                if (order.getHeader().getStatus().equals(OrderStatus.RELEASED)) {
                    throw new ValidationException("Sales order with id:" + id + " is already released");
                }

                Set<ConstraintViolation<SalesOrder>> constraintVioloationSet = validator.validate(order);
                if (!constraintVioloationSet.isEmpty()) {
                    order.getHeader().setStatus(OrderStatus.FAILED_TO_VALIDATE);
                    List<OrderError> errors = order.getOrderErrorList();
                    for (ConstraintViolation<SalesOrder> cv : constraintVioloationSet) {
                        OrderError error = new OrderError();
                        error.setErrorName("Validation Error");
                        error.setErrorReason(cv.getMessage());
                        error.setErrorResolution("Correct validation issue");
                        errors.add(error);
                    }
                    save(order);
                }

            }
            else {
                throw new B2BNotFoundException("Sales Order with Id:" + id + " could not be found");
            }
        }

        LinkedHashMap<String, List<ShipmentRequest>> orderIdMapToShipmentRequest = new
                LinkedHashMap<String, List<ShipmentRequest>>();

        for (String id : salesOrderIds) {
            // order is valid -- initialize SalesOrderReleaseLog
            SalesOrder order = getById(id);
            OrderReleaseLog salesOrderReleaseLog = inventoryAllocationLoggerDAO.initSalesOrderReleaseLog(order);

            SalesOrderData salesOrderData = null;

            salesOrderData = inventoryValidatorHelper.validateSalesOrder(id, salesOrderReleaseLog);

            Map<String, List<Site>> zipToSiteListMap = inventoryAllocationHelper.resolveSites(order,
                    salesOrderReleaseLog, salesOrderData.getHashToken());

            if (zipToSiteListMap.isEmpty()) {

                order.getHeader().setStatus(OrderStatus.FAILED_TO_ALLOCATE);
                mongoOps.save(order);

                // Logging code starts
                if (OrderLogLevel.isDebugEnabled()) {
                    inventoryAllocationLoggerDAO.log(
                            "No Sites resolved for order : " + order.getRefName() + " . Order could not be processed",
                            salesOrderReleaseLog, OrderReleaseLogCategory.DEBUG, OrderLogLevel.INFO);
                }

                inventoryAllocationLoggerDAO.log(
                        "No Sites resolved for order : " + order.getRefName() + " . Order could not be processed",
                        salesOrderReleaseLog, OrderReleaseLogCategory.BUSINESS, OrderLogLevel.INFO);

                if (LOG.isDebugEnabled()) {
                    LOG.debug(
                            "No Sites resolved for order : " + order.getRefName() + " . Order could not be processed");
                }
                // Logging code ends
                throw new IllegalArgumentException("No Sites resolved for order : " + order.getRefName());
            }
            else {

                // Logging code starts
                for (Entry<String, List<Site>> entry : zipToSiteListMap.entrySet()) {
                    String zip = entry.getKey();
                    List<Site> listOfSite = entry.getValue();

                    StringBuilder siteListBuilder = new StringBuilder();
                    int count = 0;
                    for (Site site : listOfSite) {
                        if (site != null) {
                            siteListBuilder.append(site.getRefName());

                            if (count != listOfSite.size()) {
                                siteListBuilder.append(",");
                            }
                            count++;
                        }
                    }

                    if (OrderLogLevel.isDebugEnabled()) {
                        inventoryAllocationLoggerDAO.log(
                                "Found siteList: " + siteListBuilder.toString() + " For zip : " + zip,
                                salesOrderReleaseLog, OrderReleaseLogCategory.DEBUG, OrderLogLevel.INFO);
                    }

                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Found siteList: " + siteListBuilder.toString() + " For zip : " + zip);
                    }

                    // Logging Code ends
                }

            }
            // consider sites according to SiteCalendar
            zipToSiteListMap = filterBySiteCalendarRules(zipToSiteListMap, salesOrderReleaseLog);

            if (zipToSiteListMap == null || zipToSiteListMap.isEmpty()) {
                throw new IllegalArgumentException("No Site found to process SalesOrder after filtering. "
                        + "FilterCriteria: MaxOrderPerDay and Site allocation Days");
            }
            // go for reservation and ShipmentRequest creation
            int maxSplitsAllowed = salesOrderData.getChannel().getMaxSplitsAllowed();

            order = inventoryAllocationEngine.allocateInventory(order, zipToSiteListMap, maxSplitsAllowed,
                    salesOrderReleaseLog, salesOrderData.getHashToken());

            if (order.getHeader().getStatus().equals(OrderStatus.RELEASED)) {
                order.getHeader().setReleaseDate(new Date());
                save(order);
                inventoryAllocationLoggerDAO.log("SalesOrder : " + order.getRefName() + " released successfully" + ".",
                        salesOrderReleaseLog, OrderReleaseLogCategory.BUSINESS, OrderLogLevel.INFO);
            }
            else if (order.getHeader().getStatus().equals(OrderStatus.FAILED_TO_ALLOCATE)) {
                inventoryAllocationLoggerDAO.log(
                        "SalesOrder : " + order.getRefName() + " could not be released. Status: "
                                + OrderStatus.FAILED_TO_ALLOCATE.value() + ".",
                        salesOrderReleaseLog, OrderReleaseLogCategory.BUSINESS, OrderLogLevel.INFO);
            }

            inventoryAllocationLoggerDAO.closeLog(salesOrderReleaseLog, order.getHeader().getStatus());
            if (!order.getShipmentRequests().isEmpty()) {
                orderIdMapToShipmentRequest.put(order.getId(), order.getShipmentRequests());
            }
        }

        if (LOG.isDebugEnabled()) {
            LOG.debug("size of order id map to shipment request is " + orderIdMapToShipmentRequest.size());
        }
        return orderIdMapToShipmentRequest;
    }

    @Override
    public Map<String, List<Site>> filterBySiteCalendarRules(Map<String, List<Site>> zipToSiteListMap,
                                                             OrderReleaseLog salesOrderReleaseLog)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        Objects.requireNonNull(zipToSiteListMap, "Zip to List of Sites map can not be null at this point");

        Map<String, List<Site>> filteredZipToSiteListMap = new LinkedHashMap<>();

        for (Entry<String, List<Site>> entry : zipToSiteListMap.entrySet()) {
            String zip = entry.getKey();
            List<Site> siteList = entry.getValue();

            List<Site> siteListToBePassed = new ArrayList<Site>();
            for (Site site : siteList) {
                if (site != null) {
                    SiteCalendar cal = site.getCalendar();

                    if (cal != null) {

                        Map<DayOfTheWeek, SiteDay> dayMap = cal.getDayMap();

                        Date now = Calendar.getInstance().getTime();
                        DayOfTheWeek dayOfWeek = getDayOfTheWeek(now);
                        SiteDay siteDay = dayMap.get(dayOfWeek);

                        if (siteDay != null) {
                            boolean isAllocationDay = siteDay.isAllocate();

                            int sumOfOrdersProcessedAtSite = siteDAO.getSumOfOrdersProcessedAtSite(site, siteDay);

                            boolean isOrderAllowed = (siteDay.getMaxOrdersPerDay() > sumOfOrdersProcessedAtSite) ? true
                                    : false;

                            // the day should be an allocation day
                            if (isAllocationDay && isOrderAllowed) {

                                inventoryAllocationLoggerDAO.log(
                                        "Site : " + site.getRefName() + " is eligible for consideration",
                                        salesOrderReleaseLog, OrderReleaseLogCategory.BUSINESS,
                                        OrderLogLevel.INFO);

                                siteListToBePassed.add(site);
                            }
                            else {
                                if (OrderLogLevel.isInfoEnabled()) {
                                    inventoryAllocationLoggerDAO.log(
                                            "Site : " + site.getRefName()
                                                    + " does has a SiteCalendar constraint associated with it."
                                                    + " It can not be considered for Allocation as isAllocationDay is "
                                                    + isAllocationDay + " MaxOrdersPerDay > OrdersReceivedToday is "
                                                    + isOrderAllowed,
                                            salesOrderReleaseLog, OrderReleaseLogCategory.DEBUG,
                                            OrderLogLevel.INFO);
                                }
                            }
                        }
                        else {
                            if (OrderLogLevel.isInfoEnabled()) {
                                inventoryAllocationLoggerDAO.log(
                                        "Site : " + site.getRefName() + " has a SiteCalendar constraint associated " +
                                                "with it."
                                                + " It can not be considered for Allocation as day:" + dayOfWeek
                                                + " is not configured in the site calendar.",
                                        salesOrderReleaseLog, OrderReleaseLogCategory.DEBUG, OrderLogLevel.INFO);
                            }
                        }

                    }
                    else {
                        siteListToBePassed.add(site);
                    }

                }
            }

            filteredZipToSiteListMap.put(zip, siteListToBePassed);
        }

        return filteredZipToSiteListMap;
    }

    private DayOfTheWeek getDayOfTheWeek(Date now) {

        Calendar c = Calendar.getInstance();
        c.setTime(now);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        DayOfTheWeek[] dayArray = new DayOfTheWeek[]{DayOfTheWeek.Sunday, DayOfTheWeek.Monday, DayOfTheWeek.Tuesday,
                DayOfTheWeek.Wednesday, DayOfTheWeek.Thursday, DayOfTheWeek.Friday, DayOfTheWeek.Saturday};

        return dayArray[dayOfWeek - 1];
    }

    @Override
    public List<SalesOrder> cancelSalesOrders(List<String> salesOrderIds)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        // Cancel only allows SalesOrder and its associated objects to be
        // cancelled if
        // the SalesOrder is in OPEN, RELEASED or FAILED_TO_ALLOCATE state.
        // It doesnt make sense to allow cancel of an object which is under
        // processing or has been
        // processed. For Processed object we might have to purge the data, for
        // which delete is the option.

        List<SalesOrder> salesOrders = new ArrayList<SalesOrder>();
        for (String orderId : salesOrderIds) {

            SalesOrder order = getById(orderId);
            OrderStatus salesOrderStatus = order.getHeader().getStatus();

            if (!(salesOrderStatus.equals(OrderStatus.RELEASED) || salesOrderStatus.equals(OrderStatus.OPEN)
                    || salesOrderStatus.equals(OrderStatus.FAILED_TO_ALLOCATE)
                    || salesOrderStatus.equals(OrderStatus.BACKORDER_HOLD)
                    || salesOrderStatus.equals(OrderStatus.PARTIALLY_RELEASED))) {

                throw new B2BTransactionFailed(
                        "Can not delete sales order:" + order.getRefName() + " because Sales Order is in "
                                + salesOrderStatus + " state:" + " and should have been in either a RELEASED,"
                                + " FAILED_TO_ALLOCATE, OPEN, BACKORDER_HOLD or PARTIALLY_RELEASED state");
            }

            // Create TxnDelete object
            TxnDelete txnDelete = txnDeleteDAO.createTxnDelete(order, OperationType.CANCEL);

            boolean isCancelled = false;
            boolean skip = false;
            if (!salesOrderStatus.equals(OrderStatus.OPEN)) {
                // Cancel Reservation
                if (!skip) {
                    isCancelled = cancelReservation(order, txnDelete);
                }
                if (!isCancelled) {
                    skip = revokeSalesOrderCancel(order, txnDelete);
                }

                // Cancel ShipmentRequest
                if (!skip) {
                    isCancelled = cancelShipmentRequest(order, txnDelete);
                }

                if (!isCancelled) {
                    skip = revokeSalesOrderCancel(order, txnDelete);
                }

                // Cancel SalesOrderReleaseLog
                if (!skip) {
                    isCancelled = cancelSalesOrderReleaseLog(order, txnDelete);
                }
                if (!isCancelled) {
                    skip = revokeSalesOrderCancel(order, txnDelete);
                }

            }
            // Set SalesOrderStatus to cancelled
            order.getHeader().setStatus(OrderStatus.CANCELED);
            List<BaseOrderLine> lines = order.getLineItems();

            for (OrderLine line : lines) {
                line.setStatus(OrderLineStatus.CANCELLED);
            }
            order.getDynAttributes().put("processCancelledOrder", "Y");
            order = save(order);

            OrderReleaseLog salesOrderReleaseLog = inventoryAllocationLoggerDAO
                    .initSalesOrderReleaseLog(order);
            if (salesOrderReleaseLog != null) {
                String user = SecureSession.getUser() != null ? SecureSession.getUser().getFirstName()
                        + " " + SecureSession.getUser().getLastName() : "";
                String log = String.format("User %s cancelled sales order", user);

                inventoryAllocationLoggerDAO.log(log,
                        salesOrderReleaseLog,
                        OrderReleaseLogCategory.BUSINESS,
                        OrderLogLevel.INFO);
                inventoryAllocationLoggerDAO.closeLog(salesOrderReleaseLog,
                        order.getHeader().getStatus());
            }

            if (order != null) {

                // update TxnDelete Obj as Txn is successful.
                txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "SalesOrder with Id : " + order.getId() + " and refName : "
                        + order.getRefName() + " is " + txnDelete.getOperationType() + " successfully.");

                // Log SalesOrder cancelled to AuditLog
                if (isCancelled) {
                    String description = "ReservationEntry pulled out for SalesOrder : " + orderId
                            + " successfully cancelled";
                    if (order.getReservationEntryList() != null && !order.getReservationEntryList().isEmpty()) {
                        inventoryPoolEntryAuditLogDAO.auditInventoryPoolEntryQtyChange(orderId, null,
                                order.getReservationEntryList(), ActionType.SalesOrderCancel, description);
                    }

                }
            }
            salesOrders.add(order);

        }
        return salesOrders;
    }

    /**
     * Full Text Search of Columns that are indexed.
     *
     * @param searchText  - text to search
     * @param offset      - offset to use
     * @param length      - number to be returned
     * @param dataDomains - list of Data domains
     * @return List<SalesOrder></SalesOrder>
     * @throws B2BTransactionFailed - if the search fails
     */
    public List<SalesOrder> textSearch(String searchText, int offset, int length, List<String> dataDomains)
            throws B2BTransactionFailed {

        if (dataDomains == null) {
            throw new IllegalArgumentException("dataDomains is a required field but is null");
        }

        if (dataDomains.isEmpty()) {
            throw new IllegalArgumentException(
                    "dataDomains is not null but the size is 0. " + "Required at least one dataDomain");
        }

        List<SalesOrder> rc;

        if (LOG.isDebugEnabled()) {
            LOG.debug("returning full objects");
        }

        if (dataDomains.isEmpty()) {
            throw new IllegalStateException("Domains is empty and should not be.");
        }

        TextCriteria criteria = TextCriteria.forDefaultLanguage().matchingAny(searchText);

        Query query = TextQuery.queryText(criteria).sortByScore();

        if (!dataDomains.contains("*")) {
            rc = mongoOps.find(
                    query.addCriteria(Criteria.where("dataDomain").in(dataDomains)).skip(offset).limit(length),
                    SalesOrder.class);
        }
        else {
            mongoOps.find(query.skip(offset).limit(length), SalesOrder.class);
        }

        rc = mongoOps.find(query, SalesOrder.class);

        for (SalesOrder entity : rc) {
            postGet(entity);
        }

        return rc;
    }

    @Override
    public List<SalesOrder> getStatusFilteredList(int offset, int length, List<String> statusValues,
                                                  List<String> fields) throws B2BTransactionFailed {

        return null; // To change body of implemented methods use File |
        // Settings | File Templates.
    }

    @Override
    protected Sort getDefaultSort() {

        return new Sort(Sort.Direction.DESC, "header.purchaseDate");
    }

    @Override
    public SalesOrder delete(SalesOrder objectToBeDeleted) throws B2BTransactionFailed {
        // Since this is really delete we need to ensure that we delete all the
        // children
        // of this sales order and don't orphine a bunch of data.
        // Things I would expect to clean up
        // 1. Reservations
        // - if the reservation is completed then its needs to be deleted.
        // 2. Shipment Requests
        // - if the shipping requests are closed they can be safely be deleted
        // 3. Shipments
        // - Any Labels, Packing Slips etc need to be removed as well : (KK)As
        // per discussion
        // as Shipment is a separate object this would be taken care of by
        // delete option for Shipments.

        /**
         * This deals with purging the data, where SalesOrder status is OPEN,
         * FAILED_TO_ALLOCATE, RELEASED, and SHIPPED. For scenarios other than
         * SHIPPED, reservedQty needs to be returned to available to sell qty as
         * it is not being consumed yet. For SHIPPED scenario, as the
         * reservedQty is already consumed, we just need to delete SalesOrder
         * and the related objects. This does not support deletion for cases
         * where SalesOrder is in PARTIALLY_SHIPPED state as it is still under
         * processing.
         */

        boolean isDeleted = false;
        try {

            Objects.requireNonNull(objectToBeDeleted, "objectToBeDeleted object must not be null");

            OrderStatus salesOrderStatus = objectToBeDeleted.getHeader().getStatus();

            if (!(salesOrderStatus.equals(OrderStatus.RELEASED) || salesOrderStatus.equals(OrderStatus.OPEN)
                    || salesOrderStatus.equals(OrderStatus.FAILED_TO_ALLOCATE)
                    || salesOrderStatus.equals(OrderStatus.SHIPPED)
                    || salesOrderStatus.equals(OrderStatus.CANCELED)
                    || salesOrderStatus.equals(OrderStatus.BACKORDER_HOLD)
                    || salesOrderStatus.equals(OrderStatus.PARTIALLY_RELEASED))) {

                throw new B2BTransactionFailed("Can not delete sales order:" + objectToBeDeleted.getRefName()
                        + " because Sales Order is in " + salesOrderStatus + " state:"
                        + " and should have been in either a RELEASED,"
                        + " FAILED_TO_ALLOCATE, OPEN, CANCELED, SHIPPED,BACKORDER_HOLD or PARTIALLY_RELEASED state");
            }
            // Create TxnDelete object
            TxnDelete txnDelete = txnDeleteDAO.createTxnDelete(objectToBeDeleted, OperationType.DELETE);

            switch (salesOrderStatus) {
                case OPEN:
                    objectToBeDeleted = super.delete(objectToBeDeleted);

                    // update TxnDelete Obj as Txn is successful.
                    if (objectToBeDeleted != null) {
                        txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "SalesOrder with Id : " + objectToBeDeleted.getId()
                                + " and refName : " + objectToBeDeleted.getRefName() + " is deleted successfully.");
                    }
                    break;

                default:
                    isDeleted = deleteReservationEntries(objectToBeDeleted, txnDelete);

                    if (!isDeleted) {
                        revokeSalesOrderDelete(objectToBeDeleted, txnDelete);
                        return objectToBeDeleted;
                    }

                    isDeleted = deleteShipmentRequests(objectToBeDeleted, txnDelete);

                    if (!isDeleted) {
                        revokeSalesOrderDelete(objectToBeDeleted, txnDelete);
                        return objectToBeDeleted;
                    }

                    isDeleted = deleteSalesOrderReleaseLog(objectToBeDeleted, txnDelete);

                    if (!isDeleted) {
                        revokeSalesOrderDelete(objectToBeDeleted, txnDelete);
                        return objectToBeDeleted;
                    }

                    isDeleted = deleteNotifications(objectToBeDeleted, txnDelete);

                    if (!isDeleted) {
                        revokeSalesOrderDelete(objectToBeDeleted, txnDelete);
                        return objectToBeDeleted;
                    }

                    // Log salesOrder delete to AuditLog
                    if (isDeleted) {
                        String description = "SalesOrder : " + objectToBeDeleted.getId() + " successfully deleted";
                        if (objectToBeDeleted.getReservationEntryList() != null
                                && !objectToBeDeleted.getReservationEntryList().isEmpty()) {
                            inventoryPoolEntryAuditLogDAO.auditInventoryPoolEntryQtyChange(objectToBeDeleted.getId(),
                                    null,
                                    objectToBeDeleted.getReservationEntryList(), ActionType.SalesOrderDelete,
                                    description);
                        }

                    }

                    objectToBeDeleted = super.delete(objectToBeDeleted);

                    // clean up blob as well
                    String barCodeBlobId = objectToBeDeleted.getHeader().getBarCodeBlobId();
                    if (barCodeBlobId != null) {
                        List<String> ids = new ArrayList<>();
                        ids.add(barCodeBlobId);
                        blobDAO.delete(ids);
                    }

                    // update TxnDelete Obj as Txn is successful.
                    if (objectToBeDeleted != null) {
                        txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "SalesOrder with Id : " + objectToBeDeleted.getId()
                                + " and refName : " + objectToBeDeleted.getRefName() + " is deleted successfully.");
                    }
                    break;
            }

        } catch (B2BNotFoundException | ValidationException e) {
            throw new B2BTransactionFailed(e);
        }

        return objectToBeDeleted;
    }

    /**
     * Delete reservationEntry for the given salesORder
     *
     * @param objectToBeDeleted - salesOrder to be deleted
     * @param txnDelete         - txnDelete
     * @return - true isf reservationEntry deleted successfully else false
     * @throws B2BTransactionFailed - the transaction failed
     * @throws B2BNotFoundException - the notification could not be saved
     * @throws ValidationException  - validation exception
     */
    private boolean deleteReservationEntries(SalesOrder objectToBeDeleted, TxnDelete txnDelete)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        Objects.requireNonNull(objectToBeDeleted, "objectToBeDeleted object must not be null");
        Objects.requireNonNull(txnDelete, "txnDelete object must not be null");

        boolean isDeleted = true;

        txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "Start Delete operation for ReservationEntry");
        List<ReservationEntry> reservationEntries = objectToBeDeleted.getReservationEntryList();

        List<String> reservationIds = new ArrayList<>();
        // the main question here is what to do with inventory that is currently
        // reserved but not shipped yet
        for (ReservationEntry entry : reservationEntries) {

            // Handles bug in spring mongo where by a null dbref will cause a
            // null entry
            if (entry != null) {
                // get the entry ... if its currently in an allocated state,
                // then put the inventory back
                // other wise fail the delete as there is a request that in a
                // processing state
                Reservation reservationObj = reservationDAO
                        .getByReservationNumber(entry.getReservationRef().getRefName(), entry.getDataDomain());

                if (reservationObj != null) {
                    if (!(reservationObj.getReservationStatus().equals(ReservationStatus.ALLOCATED)
                            || reservationObj.getReservationStatus().equals(ReservationStatus.SHIPPED)
                            || reservationObj.getReservationStatus().equals(ReservationStatus.CANCELLED))) {
                        throw new B2BTransactionFailed("Can not delete sales order:" + objectToBeDeleted.getRefName()
                                + " because reservation:" + reservationObj.getReservationNumber() + " is in state:"
                                + reservationObj.getReservationStatus() + " and should have been in either a allocated,"
                                + " cancelled, or shipped state");
                    }
                    else if ((reservationObj.getReservationStatus().equals(ReservationStatus.ALLOCATED))) {
                        // Need to put the inventory back if the Reservation is
                        // in ALLOCATED
                        boolean isRollbacked = inventoryTransactionHelper.rollbackReservationsByEntryAndReservation(
                                entry, reservationObj.getReservationNumber());

                        if (!isRollbacked) {
                            throw new IllegalStateException(
                                    "ReservedQty could not be rolledback." + " Can not delete the SalesOrder");
                        }
                    }

                    // ReservationEntry Qty need not be reverted if Reservation
                    // is shipped
                    txnDelete.getReservationEntriesList().add(entry);
                    entry = reservationEntryDAO.delete(entry);

                    if (entry == null) {
                        isDeleted = false;
                        break;
                    }
                    txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                            "ReservationEntry - " + entry.getReservationRef().getRefName() + " successfully deleted");

                    if (!reservationIds.contains(reservationObj.getId())) {
                        reservationIds.add(reservationObj.getId());
                        txnDelete.getReservationList().add(reservationObj);
                        txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                "Reservation - " + reservationObj.getReservationNumber() + " marked for deletion");
                    }
                }
                else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(entry.getId() + " pointed to reservation number:"
                                + entry.getReservationRef().getRefName()
                                + " but that reservation could not be found. skipping");
                    }
                }
            }
        }

        // at this point reservationEntries should not be in DB which have
        // orderNumber of the order in question

        List<ReservationEntry> entries = mongoOps.find(
                Query.query(Criteria.where("orderNumber").is(objectToBeDeleted.getHeader().getOrderNumber())
                        .and("dataDomain").in(objectToBeDeleted.getDataDomains())),
                ReservationEntry.class);

        if (!entries.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("System was in inconsistent state. Found Reservation Entries for SalesOrder Number:"
                        + objectToBeDeleted.getHeader().getOrderNumber() + " while deleting sales order "
                        + " which were not referenced by the parent reservation yet, the reservation entries where "
                        + "found " + " with the sales order, attempting to clean up reservations");

            }

            for (ReservationEntry entry : entries) {
                // get Reservations are present
                Reservation res = reservationDAO.getByRefName(entry.getReservationRef().getRefName(),
                        entry.getDataDomain());

                if (res != null) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("   Reservation Entry with Id-->" + res.getId() + " res number:"
                                + res.getReservationNumber() + " found removing");
                    }

                    // add reservation to reservationIds so that it can be
                    // deleted
                    reservationIds.add(res.getId());
                }

                // Check if the entries are referenced in the

                reservationEntryDAO.delete(entry);
            }
        }
        else {
            if (LOG.isWarnEnabled()) {
                LOG.warn("System was in consistent state." + " No extra reservationEntries will be deleted");
            }
        }

        if (!reservationIds.isEmpty()) {
            reservationDAO.delete(reservationIds);

            // Logging to TxnDeleteLogEntry
            for (String reservationID : reservationIds) {
                txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                        "Reservation - " + reservationID + " successfully deleted");
            }
        }

        return isDeleted;
    }

    /**
     * Delete ShipmentRequest for the given SalesOrder
     *
     * @param objectToBeDeleted - salesOrder to be deleted
     * @param txnDelete         - txnDelete
     * @return - true if shipmentRequest deleted successfully else false
     * @throws B2BTransactionFailed - the transaction failed
     * @throws B2BNotFoundException - the notification could not be saved
     * @throws ValidationException  - validation exception
     */
    private boolean deleteShipmentRequests(SalesOrder objectToBeDeleted, TxnDelete txnDelete)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        Objects.requireNonNull(objectToBeDeleted, "objectToBeDeleted object must not be null");
        Objects.requireNonNull(txnDelete, "txnDelete object must not be null");

        boolean isDeleted = true;

        List<ShipmentRequest> shippingRequests = objectToBeDeleted.getShipmentRequests();

        for (ShipmentRequest request : shippingRequests) {
            if (request != null) {
                /*
                 * List<ShipmentRequestLine> lines = request.getLineItems(); for
                 * (ShipmentRequestLine line : lines) { List<String> shipmentIds
                 * = line.getShipmentIds(); if (shipmentIds != null &&
                 * !shipmentIds.isEmpty()) { shipmentDAO.delete(shipmentIds); }
                 * }
                 */
                txnDelete.getShipmentRequestList().add(request);
                request = shipmentRequestDAO.delete(request);

                if (request == null) {
                    isDeleted = false;
                    break;
                }

                txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                        "ShipmentRequest - " + request.getRefName() + " successfully deleted");
            }

            isDeleted = true;
        }

        // Checking if any other ShipmentRequests are present for the order in
        // question
        List<ShipmentRequest> srList = mongoOps.find(Query.query(Criteria
                .where("shipmentRequestHeader.orderNumber").is(objectToBeDeleted.getHeader().getOrderNumber())
                .and("dataDomain").in(objectToBeDeleted.getDataDomains())), ShipmentRequest.class);

        if (!srList.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("System is in incosistent state." + " Deleting orphan shipmentRequests");
            }

            for (ShipmentRequest sr : srList) {
                shipmentRequestDAO.delete(sr);
            }
        }
        else {
            if (LOG.isInfoEnabled()) {
                LOG.info("System is in consistent state." + " No orphan ShipmentRequests found");
            }
        }
        return isDeleted;
    }

    /**
     * Delete SalesOrderReleaseLog for the given salesOrder
     *
     * @param objectToBeDeleted - salesOrder object to be deleted
     * @param txnDelete         - txnDelete
     * @return - true if salesOrderReleaseLogs deleted successfully else false
     * @throws B2BTransactionFailed - the transaction failed
     * @throws B2BNotFoundException - the notification could not be saved
     * @throws ValidationException  - validation exception
     */
    private boolean deleteSalesOrderReleaseLog(SalesOrder objectToBeDeleted, TxnDelete txnDelete)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        Objects.requireNonNull(objectToBeDeleted, "objectToBeDeleted object must not be null");

        boolean isDeleted = true;

        List<OrderReleaseLog> logs = objectToBeDeleted.getReleaseLogs();

        for (OrderReleaseLog log : logs) {
            if (log != null) {
                txnDelete.getOrderReleaseLogList().add(log);
                OrderReleaseLog salesOrderReleaseLog = releaseLogDAO.delete(log);

                if (salesOrderReleaseLog == null) {
                    isDeleted = false;
                    break;
                }
                txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                        "SalesOrder Release Log - " + log.getRefName() + " successfully deleted");
            }
            isDeleted = true;
        }

        return isDeleted;
    }

    /**
     * Delete notifications for the given SalesORder
     *
     * @param objectToBeDeleted - salesOrder object to be deleted
     * @param txnDelete         - txnDelete
     * @return - true if notifications deleted successfully else false
     * @throws B2BTransactionFailed - the transaction failed
     * @throws B2BNotFoundException - the notification could not be saved
     * @throws ValidationException  - validation exception
     */
    private boolean deleteNotifications(SalesOrder objectToBeDeleted, TxnDelete txnDelete)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        Objects.requireNonNull(objectToBeDeleted, "objectToBeDeleted object must not be null");
        Objects.requireNonNull(txnDelete, "txnDelete object must not be null");

        boolean isDeleted = true;

        List<Notification> notificationList = objectToBeDeleted.getNotifications();

        for (Notification notification : notificationList) {
            if (notification != null) {
                txnDelete.getNotificationList().add(notification);
                notification = notificationDAO.delete(notification);

                if (notification == null) {
                    isDeleted = false;
                    break;
                }
                txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                        "Notification - " + notification.getRefName() + " successfully deleted");
            }
            isDeleted = true;
        }

        return isDeleted;
    }

    /**
     * Revoke Delete operation for the given salesOrder
     *
     * @param objectToBeDeleted - salesOrder object to be deleted
     * @param txnDelete         - txnDelete
     * @throws B2BTransactionFailed - the transaction failed
     * @throws B2BNotFoundException - the notification could not be saved
     * @throws ValidationException  - validation exception
     */
    private void revokeSalesOrderDelete(SalesOrder objectToBeDeleted, TxnDelete txnDelete)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        Objects.requireNonNull(objectToBeDeleted, "objectToBeDeleted object must not be null");
        Objects.requireNonNull(txnDelete, "txnDelete object must not be null");

        if (txnDelete.getRefName() != null) {
            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                    "Revoke Delete operation for SalesOrder - " + objectToBeDeleted.getRefName());
            List<ReservationEntry> reservationEntriesList = txnDelete.getReservationEntriesList();
            List<ShipmentRequest> shipmentRequestList = txnDelete.getShipmentRequestList();
            List<OrderReleaseLog> salesOrderReleaseLogList = txnDelete.getOrderReleaseLogList();
            List<Notification> notificationList = txnDelete.getNotificationList();

            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "Revoke ReservationEntry");

            if (reservationEntriesList != null && !reservationEntriesList.isEmpty()) {
                List<String> reservations = new ArrayList<>();
                for (ReservationEntry reservationEntry : reservationEntriesList) {
                    if (reservationEntryDAO.getById(reservationEntry.getId()) == null) {
                        ReservationEntry reservationEntryObj = reservationEntryDAO.save(reservationEntry);

                        if (reservationEntryObj != null) {
                            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                    "ReservationEntry - " + reservationEntryObj.getRefName() + " successfully revoked");
                            if (!reservations.contains(reservationEntryObj.getReservationRef().getRefName())) {
                                reservations.add(reservationEntryObj.getReservationRef().getRefName());
                            }
                        }
                        else {
                            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                    "ReservationEntry - " + reservationEntry.getRefName() + " could not be revoked");
                        }
                    }
                }

                // Fetch Reservations from TxnDelete object and compare it with
                // resrevationEntries. If found, then save it to DB
                List<Reservation> reservationList = txnDelete.getReservationList();
                for (String reservationNum : reservations) {
                    for (Reservation reservation : reservationList) {
                        if (reservationNum.equalsIgnoreCase(reservation.getReservationNumber())) {
                            Reservation reservationObj = reservationDAO.save(reservation);

                            if (reservationObj != null) {
                                txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                        "Reservation - " + reservationObj.getRefName() + " successfully revoked");
                                break;
                            }
                            else {
                                txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                        "Reservation - " + reservation.getRefName() + " could not be revoked");
                            }
                        }
                    }
                }
            }

            // Revoke ShipmentRequest
            if (shipmentRequestList != null && !shipmentRequestList.isEmpty()) {
                for (ShipmentRequest shipmentRequest : shipmentRequestList) {
                    if (shipmentRequestDAO.getById(shipmentRequest.getId()) == null) {
                        ShipmentRequest shipmentRequestObj = shipmentRequestDAO.save(shipmentRequest);

                        if (shipmentRequestObj != null) {
                            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                    "ShipmentRequest - " + shipmentRequestObj.getRefName() + " successfully revoked");
                        }
                        else {
                            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                    "ShipmentRequest - " + shipmentRequest.getRefName() + " could not be revoked");
                        }
                    }
                }
            }

            if (salesOrderReleaseLogList != null && !salesOrderReleaseLogList.isEmpty()) {
                for (OrderReleaseLog salesOrderReleaseLog : salesOrderReleaseLogList) {
                    if (releaseLogDAO.getById(salesOrderReleaseLog.getId()) == null) {
                        OrderReleaseLog salesOrderReleaseLogObj = releaseLogDAO.save(salesOrderReleaseLog);

                        if (salesOrderReleaseLogObj != null) {
                            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "SalesOrderReleaseLog - "
                                    + salesOrderReleaseLogObj.getRefName() + " successfully revoked");
                        }
                        else {
                            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "SalesOrderReleaseLog - "
                                    + salesOrderReleaseLog.getRefName() + " could not be revoked");
                        }
                    }
                }
            }

            if (notificationList != null && !notificationList.isEmpty()) {
                for (Notification notification : notificationList) {
                    if (notificationDAO.getById(notification.getId()) == null) {
                        Notification notificationObj = notificationDAO.save(notification);

                        if (notificationObj != null) {
                            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                    "Notification - " + notificationObj.getRefName() + " successfully revoked");
                        }
                        else {
                            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                    "Notification - " + notification.getRefName() + " could not be revoked");
                        }
                    }
                }
            }
        }
    }

    /**
     * Cancel Reservation
     *
     * @param objectToBeCancelled = salesOrder to be cancelled
     * @param txnDelete           - txnDelete
     * @return true if salesORder cancelled else false
     * @throws B2BTransactionFailed - the transaction failed
     * @throws B2BNotFoundException - the notification could not be saved
     * @throws ValidationException  - validation exception
     */
    private boolean cancelReservation(SalesOrder objectToBeCancelled, TxnDelete txnDelete)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        Objects.requireNonNull(objectToBeCancelled, "objectToBeCancelled object must not be null");

        Objects.requireNonNull(txnDelete, "txnDelete object must not be null");

        boolean isCancelled = true;

        // Get ReservationEntries from salesOrder
        List<ReservationEntry> reservationEntries = objectToBeCancelled.getReservationEntryList();

        List<String> reservationIds = new ArrayList<>();

        for (ReservationEntry entry : reservationEntries) {

            // Handles bug in spring mongo where by a null dbref will cause a
            // null entry
            if (entry != null) {

                // get the entry ... if its currently in an allocated state,
                // then put the inventory back
                // other wise fail the cancel as there is a request that in a
                // processing state
                Reservation reservationObj = reservationDAO.getByRefName(entry.getReservationRef().getRefName(),
                        entry.getDataDomain());

                if (reservationObj != null) {

                    if (!(reservationObj.getReservationStatus().equals(ReservationStatus.ALLOCATED)
                            || reservationObj.getReservationStatus().equals(ReservationStatus.FAILED_ALLOCATE))) {

                        if (LOG.isErrorEnabled()) {
                            LOG.error("Can not cancel reservation:" + entry.getReservationRef().getRefName()
                                    + " because reservation: is in state:" + reservationObj.getReservationStatus()
                                    + " and should have been in either an allocated or"
                                    + " failed allocate state, continuing");
                        }
                        continue;
                    }

                    // Need to put the inventory back if the Reservation is
                    // in ALLOCATED
                    inventoryTransactionHelper.rollbackReservationsByEntryAndReservation(entry,
                            reservationObj.getReservationNumber());

                    txnDelete.getReservationEntriesList().add(entry);

                    // FIXME replace with update ?
                    entry.setReservationEntryStatus(ReservationEntryStatus.CANCELLED);
                    entry = reservationEntryDAO.save(entry);

                    txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                            "ReservationEntry - " + entry.getId() + " successfully Cancelled");

                    // Mark ReservationIds for cancellation
                    if (!reservationIds.contains(reservationObj.getId())) {
                        reservationIds.add(reservationObj.getId());
                        txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                "Reservation - " + reservationObj.getReservationNumber() + " marked for cancellation");
                    }

                }
                else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn(entry.getId() + " pointed to reservation with refName:"
                                + entry.getReservationRef().getRefName()
                                + " but that reservation could not be found. skipping");
                    }
                }

            }
            else {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Invalid data found while canceling order:" + objectToBeCancelled.getId()
                            + " suspect that there reservation entries configured as DBRefs which do not resolve to any"
                            + " real " + "objects");
                }
            }

        }

        if (!reservationIds.isEmpty()) {
            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                    "Start " + txnDelete.getOperationType() + " operation for Reservation");
            // Iterate through reservations
            for (String reservationID : reservationIds) {
                Reservation reservation = reservationDAO.getById(reservationID);

                if (reservation != null) {
                    // Add reservation to transaction object
                    txnDelete.getReservationList().add(reservation);
                    // Set state to cancelled
                    reservation.setReservationStatus(ReservationStatus.CANCELLED);

                    Reservation reservationObj = reservationDAO.save(reservation);

                    if (reservationObj != null) {
                        txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                "Reservation - " + reservationID + " successfully cancelled");
                    }
                    else {
                        txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "Reservation - " + reservationID
                                + " could not be cancelled." + " Reason: Entry not found in DB");
                        isCancelled = false;
                        break;
                    }
                }
            }
        }

        return isCancelled;
    }

    /**
     * Cancel ShipmentRequest
     *
     * @param objectToBeCancelled - salesOrder to be cancelled
     * @param txnDelete           - txnDelete
     * @return true if shipmentRequest is cancelled else false
     * @throws B2BTransactionFailed - the transaction failed
     * @throws B2BNotFoundException - the notification could not be saved
     * @throws ValidationException  - validation exception
     */
    public boolean cancelShipmentRequest(SalesOrder objectToBeCancelled, TxnDelete txnDelete)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        // TODO : Decide how the shipmentRequest be cancelled for PARTIAL_SHIP
        // case
        Objects.requireNonNull(objectToBeCancelled, "objectToBeDeleted object must not be null");
        Objects.requireNonNull(txnDelete, "txnDelete object must not be null");

        boolean isCancelled = true;

        // Get shipmentRequest from salesOrder
        List<ShipmentRequest> shippingRequests = objectToBeCancelled.getShipmentRequests();

        // Iterate
        for (ShipmentRequest request : shippingRequests) {
            if (request != null) {
                // Add shipmentRequest to transaction Object
                if (request.getHeader().getStatus().equals(ShipmentRequestStatus.SHIPMENT_CREATED) ||
                        request.getHeader().getStatus().equals(ShipmentRequestStatus.SHIPMENT_REQUEST_SENT) ||
                        request.getHeader().getStatus().equals(ShipmentRequestStatus.SHIPMENT_IN_PROGRESS)) {

                }
                else {
                    txnDelete.getShipmentRequestList().add(request);

                    // Set status to cancelled
                    request.getHeader().setStatus(ShipmentRequestStatus.CANCELLED);

                    request = shipmentRequestDAO.save(request);

                    if (request == null) {
                        txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "ShipmentRequest could not be cancelled.");
                        isCancelled = false;
                        break;
                    }
                    else {
                        txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                "ShipmentRequest - " + request.getRefName() + " successfully cancelled");
                    }
                }
            }
        }
        return isCancelled;
    }

    /**
     * Revoke SalesOrder cancellation
     *
     * @param objectToBeCancelled - salesOrder to be cancelled
     * @param txnDelete           - txnDelete
     * @return boolean flag
     * @throws B2BTransactionFailed - the transaction failed
     * @throws B2BNotFoundException - the notification could not be saved
     * @throws ValidationException  - validation exception
     */
    private boolean revokeSalesOrderCancel(SalesOrder objectToBeCancelled, TxnDelete txnDelete)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        boolean skip = true;

        Objects.requireNonNull(objectToBeCancelled, "objectToBeDeleted object must not be null");
        Objects.requireNonNull(txnDelete, "txnDelete object must not be null");

        if (txnDelete.getRefName() != null) {

            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "Revoke " + txnDelete.getOperationType()
                    + " operation for SalesOrder - " + objectToBeCancelled.getRefName());

            List<ReservationEntry> reservationEntryList = txnDelete.getReservationEntriesList();
            List<Reservation> reservationList = txnDelete.getReservationList();
            List<ShipmentRequest> shipmentRequestList = txnDelete.getShipmentRequestList();
            List<OrderReleaseLog> salesOrderReleaseLogList = txnDelete.getOrderReleaseLogList();

            // Revoke Reservation from transaction object
            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "Revoke Reservation");

            for (ReservationEntry re : reservationEntryList) {
                ReservationEntry reservationEntryObjInDB = reservationEntryDAO.getById(re.getId());
                if (reservationEntryObjInDB != null) {
                    reservationEntryObjInDB.setReservationEntryStatus(re.getReservationEntryStatus());
                    reservationEntryObjInDB = reservationEntryDAO.save(reservationEntryObjInDB);

                    if (reservationEntryObjInDB != null) {
                        txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                "ReservationEntry " + reservationEntryObjInDB.getRefName() + " successfully revoked");

                        boolean isQtyRevertedInPoolEntry = inventoryTransactionHelper
                                .revertQtyInPoolEntry(reservationEntryObjInDB);

                        if (!isQtyRevertedInPoolEntry) {
                            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                    "InventoryPoolEntry - " + reservationEntryObjInDB.getInventoryPoolEntryId()
                                            + " Qty could not be reverted for" + " ReservationEntry "
                                            + reservationEntryObjInDB.getId());

                            skip = false;
                        }

                    }
                    else {
                        txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                "ReservationEntry " + re.getRefName() + " could not be successfully revoked");
                    }
                }
                // else no needed, as if ReservationEntry is not present then
                // Status can not be reverted.
            }

            for (Reservation reservation : reservationList) {
                // Get Reservation object from dao
                Reservation reservationObj = reservationDAO.getById(reservation.getId());

                if (reservationObj != null) {
                    // Revoke reservation status from transaction object
                    reservationObj.setReservationStatus(reservation.getReservationStatus());
                    reservationObj = reservationDAO.save(reservationObj);

                    if (reservationObj != null) {
                        txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                "Reservation " + reservationObj.getRefName() + " successfully revoked");
                    }
                    else {
                        txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                "Reservation " + reservation.getRefName() + " not revoked successfully");

                        skip = false;
                    }
                }
            }

            // Revoke ShipmentRequest from transaction object

            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "Revoke ShipmentRequest");
            for (ShipmentRequest shipmentRequest : shipmentRequestList) {
                if (shipmentRequest != null) {
                    // Get shipmentRequest object
                    ShipmentRequest shipmentRequestObj = shipmentRequestDAO.getById(shipmentRequest.getId());

                    if (shipmentRequestObj != null) {
                        // Revoke shipmentStatus from transaction object
                        shipmentRequestObj.getHeader().setStatus(shipmentRequest.getHeader().getStatus());

                        shipmentRequestObj = shipmentRequestDAO.save(shipmentRequestObj);

                        if (shipmentRequestObj != null) {
                            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                    "ShipmentRequest " + shipmentRequestObj.getRefName() + " successfully revoked");
                        }
                        else {
                            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                                    "ShipmentRequest " + shipmentRequest.getRefName() + " could not be revoked");

                            skip = false;
                        }
                    }
                }
            }

            // Revoke SalesOrderReleaseLog

            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "Revoke SalesOrderReleaseLog");

            for (OrderReleaseLog salesOrderReleaseLog : salesOrderReleaseLogList) {
                if (salesOrderReleaseLog != null) {
                    // Get SalesOrderReleaseLog object from dao
                    OrderReleaseLog salesOrderReleaseLogObj = releaseLogDAO.getById(salesOrderReleaseLog.getId());

                    if (salesOrderReleaseLogObj != null) {
                        // Revoke log status from transaction object
                        salesOrderReleaseLogObj.setReleaseLogStatus(salesOrderReleaseLog.getReleaseLogStatus());
                        OrderReleaseLog log = releaseLogDAO.save(salesOrderReleaseLogObj);
                        if (log != null) {
                            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "SalesOrderReleaseLog "
                                    + salesOrderReleaseLogObj.getRefName() + " successfully revoked");
                        }
                        else {
                            txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete, "SalesOrderReleaseLog "
                                    + salesOrderReleaseLog.getRefName() + " could not be revoked");

                            skip = false;
                        }
                    }
                }
            }
        }
        return skip;
    }

    /**
     * Cancel salesOrderReleaseLog
     *
     * @param objectToBeCancelled - salesOrder to be cancelled
     * @param txnDelete           - txnDelete
     * @return true if salesOrderReleaseLog is cancelled else false
     * @throws B2BTransactionFailed - the transaction failed
     * @throws B2BNotFoundException - the notification could not be saved
     * @throws ValidationException  - validation exception
     */
    private boolean cancelSalesOrderReleaseLog(SalesOrder objectToBeCancelled, TxnDelete txnDelete)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        Objects.requireNonNull(objectToBeCancelled, "objectToBeCancelled object must not be null");
        Objects.requireNonNull(txnDelete, "txnDelete object must not be null");

        boolean isCancelled = true;

        // Get ReleaseLogs from salesOrder
        List<OrderReleaseLog> logs = objectToBeCancelled.getReleaseLogs();

        // Iterate
        for (OrderReleaseLog log : logs) {
            if (log != null) {
                // add to transaction object
                txnDelete.getOrderReleaseLogList().add(log);

                // Set status to cancelled
                log.setReleaseLogStatus(ReleaseLogStatus.CANCELLED);

                OrderReleaseLog logObj = releaseLogDAO.save(log);

                if (logObj != null) {
                    txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                            "SalesOrderReleaseLog " + logObj.getRefName() + " successfully cancelled");
                }
                else {
                    isCancelled = false;
                    txnDelete = txnDeleteDAO.addTxnDeleteLogEntry(txnDelete,
                            "SalesOrderReleaseLog " + log.getRefName() + " could not be cancelled");
                    break;
                }
            }
        }

        return isCancelled;
    }

    @Override
    public List<SalesOrder> getSOInOpenState() {

        // long hourTimestamp = 1000 * 60 * 60;
        long hourTimestamp = 1000 * 60 * 10; // Set to 10 min for QA Test
        Date date = new Date();
        TimeZone timeZone = Calendar.getInstance().getTimeZone();
        Date gmtDate = new Date(date.getTime() - timeZone.getRawOffset() - hourTimestamp);

        List<SalesOrder> salesOrderList = mongoOps.find(new Query(
                        Criteria.where("header.status").is(OrderStatus.OPEN).and("header.purchaseDate").lte(gmtDate)),
                SalesOrder.class);

        return salesOrderList;
    }

    /**
     * Cancel salesOrder given a orderNumber
     *
     * @param salesOrderNumber orderNumber
     * @param dataDomains      dataDomains
     * @throws B2BNotFoundException - if there is no salesOrder for the orderNumber
     * @throws B2BTransactionFailed - if the transaction failed
     * @throws ValidationException  - if validation fails
     */
    public void cancelSalesOrderBySalesOrderNumber(String salesOrderNumber, List<String> dataDomains)
            throws B2BNotFoundException, B2BTransactionFailed, ValidationException {

        if (StringUtils.isEmpty(salesOrderNumber)) {
            throw new IllegalArgumentException("orderNumber is a required field, but is null or empty");
        }

        if (dataDomains == null || dataDomains.isEmpty()) {
            throw new IllegalArgumentException("dataDomains is a required field, but is null or empty");
        }

        Query query = new Query(Criteria.where("dataDomain").in(dataDomains)
                .andOperator(Criteria.where("header.orderNumber").is(salesOrderNumber)));
        List<SalesOrder> salesOrders = mongoOps.find(query, SalesOrder.class);

        if (salesOrders == null || salesOrders.isEmpty()) {
            throw new B2BNotFoundException(
                    String.format("SalesOrder for %s not found in the system", salesOrderNumber));
        }
        if (salesOrders.size() > 1) {
            throw new IllegalArgumentException(
                    String.format("Expected only only one SalesOrder for %s, but found multiple", salesOrderNumber));
        }
        SalesOrder salesOrder = salesOrders.get(0);
        cancelSalesOrders(Lists.newArrayList(salesOrder.getId()));
    }

    @Override
    public void updateSalesOrderReleasedStatus(SalesOrder order, List<ReservationEntry> reservationEntryList,
                                               List<ShipmentRequest> shipmentRequests)
            throws B2BNotFoundException, B2BTransactionFailed, ValidationException {


        for (ShipmentRequest request : shipmentRequests) {
            if (!order.getShipmentRequests().contains(request)) {
                order.getShipmentRequests().add(request);
            }
        }

        for (ReservationEntry entry : reservationEntryList) {
            if (!order.getReservationEntryList().contains(entry)) {
                order.getReservationEntryList().add(entry);
            }
        }

        if (order.getHeader().getStatus().equals(OrderStatus.OPEN) ||
                order.getHeader().getStatus().equals(OrderStatus.FAILED_TO_ALLOCATE) ||
                order.getHeader().getStatus().equals(OrderStatus.PARTIALLY_RELEASED) ||
                order.getHeader().getStatus().equals(OrderStatus.RELEASED)) {

            boolean fullyAllocated = true;
            for (OrderLine line : order.getLineItems()) {
                if (!line.getStatus().equals(OrderLineStatus.ALLOCATED) &&
                        !line.getStatus().equals(OrderLineStatus.CLOSED)) {
                    fullyAllocated = false;
                }
            }

            if (fullyAllocated) {
                order.getHeader().setStatus(OrderStatus.RELEASED);
            }
            else {
                order.getHeader().setStatus(OrderStatus.PARTIALLY_RELEASED);
            }
        }

        order.rlogClose();

        save(order);

    }

    @Override
    public void rollbackSalesOrderRelease(String orderId, List<String> reservationEntryList,
                                          List<String> shipmentRequests, OrderStatus previousStatus)
            throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        if (orderId == null) {
            throw new IllegalArgumentException("sales order id can not be null");
        }

        if (reservationEntryList == null) {
            throw new IllegalArgumentException("reservationEntryList should not be null");
        }

        if (shipmentRequests == null) {
            throw new IllegalArgumentException("shipmentRequests should not be null");
        }

        SalesOrder salesOrder = getById(orderId);

        if (salesOrder == null) {
            throw new B2BNotFoundException("Sales order with id:" + orderId + " not found");
        }

        if (reservationEntryList.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Rollback of sales order id:" + orderId + " has no reservationEntries passed");
            }
        }

        if (shipmentRequests.isEmpty()) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Rollback of sales order with id:" + orderId + " has not shipment requests passed");
            }
        }

        if (previousStatus == null) {
            throw new IllegalArgumentException("previous status should not be null");
        }

        if (previousStatus.equals(OrderStatus.RELEASED)) {
            throw new IllegalArgumentException("previous status is released, can not rollback to released status");
        }

        if (previousStatus.equals(OrderStatus.CLOSED)) {
            throw new IllegalArgumentException("previous status is closed, can not rollback to closed status");
        }

        if (previousStatus.equals(OrderStatus.CANCELED)) {
            throw new IllegalArgumentException("previous status is cancelled, can not rollback to cancelled status");
        }

        if (!previousStatus.equals(OrderStatus.FAILED_TO_ALLOCATE)
                || previousStatus.equals(OrderStatus.HOLD_MANUAL_REVIEW_REQUIRED)
                || previousStatus.equals(OrderStatus.FTA_UNEXPECTED_CONDITION_IN_SYSTEM)
                || previousStatus.equals(OrderStatus.OPEN)
                || previousStatus.equals(OrderStatus.PARTIALLY_RELEASED)
                || previousStatus.equals(OrderStatus.PARTIALLY_RELEASED_DROPSHIPPRODUCTS_REMAIN)
                || previousStatus.equals(OrderStatus.PARTIALLY_SHIPPED)) {
            throw new IllegalArgumentException(
                    "Argument previous status is not in the right state to " + "rollback, state:" + previousStatus);
        }

        if (!salesOrder.getHeader().getStatus().equals(OrderStatus.RELEASED)) {
            throw new IllegalStateException("Sales order with id:" + orderId + " has a status of:"
                    + salesOrder.getHeader().getStatus() + " but should be in a released status");
        }

        salesOrder.getHeader().setStatus(previousStatus);

        for (String shippingRequestId : shipmentRequests) {
            ShipmentRequest r = shipmentRequestDAO.getById(shippingRequestId);
            if (r == null) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Shipping request with id:" + shippingRequestId + " was not found in shipment request "
                            + "collection");
                }

            }
            else {
                if (!salesOrder.getShipmentRequests().remove(r)) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Shipping request with id:" + shippingRequestId + " was not associated with sales "
                                + "order:" + orderId);
                    }
                }
            }
        }

        for (String reservationEntryId : reservationEntryList) {
            ReservationEntry entry = reservationEntryDAO.getById(reservationEntryId);

            if (entry == null) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("Reservation entry with id:" + reservationEntryId + " was not found in database");
                }

            }
            else {
                if (!salesOrder.getReservationEntryList().remove(entry)) {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("Reservation Entry:" + reservationEntryId + " was not associated with the sales "
                                + "order id:" + orderId);
                    }
                }
            }
        }

        save(salesOrder);

    }

    @Override
    public SalesOrder cancelOrderLine(String orderId, String lineUid, String dataDomain)
            throws B2BNotFoundException, B2BTransactionFailed, ValidationException {

        if (orderId == null) {
            throw new IllegalArgumentException("order id can not be null");
        }

        if (lineUid == null) {
            throw new IllegalArgumentException("lineUid can not be null");
        }

        SalesOrder salesOrder = getById(orderId);

        if (salesOrder == null) {
            throw new IllegalArgumentException("sales order can not be null");
        }

        OrderLine line = null;
        for (OrderLine lineItem : salesOrder.getLineItems()) {
            if (lineItem.getUid().equals(lineUid)) {
                line = lineItem;
                break;
            }
        }

        if (line == null) {
            throw new B2BNotFoundException(
                    String.format("SalesOrder: %s in dataDomain: %s does not have a line with uid: %s",
                            salesOrder.getHeader().getOrderNumber(), salesOrder.getDataDomain(),
                            lineUid));
        }

        if (line.getStatus().equals(OrderLineStatus.BACKORDER) ||
                line.getStatus().equals(OrderLineStatus.FAILED_TO_ALLOCATE) ||
                line.getStatus().equals(OrderLineStatus.REJECTED) ||
                line.getStatus().equals(OrderLineStatus.OPEN)) {

            line.setStatus(OrderLineStatus.CANCELLED);
            OrderStatus status = checkSOStatusBasedOnLineStatus(
                    salesOrder.getHeader().getStatus(), salesOrder.getLineItems());
            salesOrder.getHeader().setStatus(status);
            salesOrder.getDynAttributes().put("processCancelledLine", "Y");
            salesOrder = save(salesOrder);

            OrderReleaseLog salesOrderReleaseLog = inventoryAllocationLoggerDAO
                    .initSalesOrderReleaseLog(salesOrder);
            if (salesOrderReleaseLog != null) {
                String user = SecureSession.getUser() != null ? SecureSession.getUser().getFirstName()
                        + " " + SecureSession.getUser().getLastName() : "";
                String log = String.format("User %s cancelled line item %s with quantity %s",
                        user, line.getSystemCatalogProductIdentifier(), line.getItemQty());

                inventoryAllocationLoggerDAO.log(log,
                        salesOrderReleaseLog,
                        OrderReleaseLogCategory.BUSINESS,
                        OrderLogLevel.INFO);
                inventoryAllocationLoggerDAO.closeLog(salesOrderReleaseLog,
                        salesOrder.getHeader().getStatus());
            }


        }
        else {
            throw new B2BTransactionFailed(String.format("Line item with uid:%s in sales order:%s in " +
                            "dataDomain:%s can not be cancelled because the line status is:%s",
                    lineUid, salesOrder.getHeader().getOrderNumber(),
                    salesOrder.getDataDomain(), line.getStatus()));
        }


        return salesOrder;
    }


    private OrderStatus checkSOStatusBasedOnLineStatus(OrderStatus salesOrderStatus,
                                                       List<BaseOrderLine> lineItems) {

        Objects.requireNonNull(lineItems, "Line items cannot be null");
        OrderStatus status = null;
        int allocatedCount = 0;
        int openCount = 0;
        int closedCount = 0;
        int cancelledCount = 0;
        int backOrderedCount = 0;
        int defaultCount = 0;

        for (OrderLine line : lineItems) {
            switch (line.getStatus()) {
                case OPEN:
                    openCount++;
                    break;
                case CLOSED:
                    closedCount++;
                    break;
                case CANCELLED:
                    cancelledCount++;
                    break;
                case BACKORDER:
                    backOrderedCount++;
                    break;
                case ALLOCATED:
                    allocatedCount++;
                    break;
                default:
                    defaultCount++;
                    break;
            }
        }

        if ((openCount + closedCount + allocatedCount + cancelledCount
                + backOrderedCount + defaultCount) == lineItems.size()) {
/*
            if (cancelledCount == lineItems.size()) {
                status = SalesOrderStatus.CANCELED;
            } else if ((closedCount + cancelledCount) == lineItems.size()) {
                status = SalesOrderStatus.SHIPPED;
            } else if ((allocatedCount + cancelledCount) == lineItems.size()) {
                status = SalesOrderStatus.RELEASED;
            } else if (allocatedCount < lineItems.size()&& allocatedCount >0 && closedCount == 0) {
                status = SalesOrderStatus.PARTIALLY_RELEASED;
            } else if (allocatedCount < lineItems.size() && closedCount > 0) {
                status = SalesOrderStatus.PARTIALLY_SHIPPED;
            }
            else if (backOrderedCount == lineItems.size()) {
                status = SalesOrderStatus.BACKORDER_HOLD;
            }else {
                status = salesOrderStatus;
            }*/

            if (cancelledCount == lineItems.size()) {
                status = OrderStatus.CANCELED;
            }
            else if ((closedCount + cancelledCount) == lineItems.size()) {
                status = OrderStatus.SHIPPED;
            }
            else if (closedCount > 0 && closedCount < lineItems.size()) {
                status = OrderStatus.PARTIALLY_SHIPPED;
            }
            else if ((allocatedCount + cancelledCount) == lineItems.size()) {
                status = OrderStatus.RELEASED;
            }
            else if (allocatedCount > 0 && allocatedCount < lineItems.size() && closedCount == 0) {
                status = OrderStatus.PARTIALLY_RELEASED;
            }
            else if (backOrderedCount == lineItems.size()) {
                status = OrderStatus.FAILED_TO_ALLOCATE;
            }
            else {
                status = salesOrderStatus;
            }

        }
        else {

            // if count mismatch is there, then there is some issue with the
            // state of the program.
            throw new IllegalStateException(
                    "Error in calculating the SO status, mismatch in count"
                            + "lines in OPEN status " + openCount
                            + " lines in CLOSED status " + closedCount
                            + " lines in other status " + allocatedCount);
        }
        return status;
    }


    /**
     * Create Mapping for Product vs. Required ItemQty for the given salesOrder
     * Line Item
     *
     * @param order - SalesOrder
     * @return - Mapping of Product vs. Required ItemQty
     */
    @Override
    public Map<String, Long> populateProductIdQtyPair(SalesOrder order) {

        Objects.requireNonNull(order, "order object cannot be null");

        Map<String, Long> productIdQtyMap = new HashMap<String, Long>();
        List<BaseOrderLine> lineItems = order.getLineItems();

        switch (order.getHeader().getStatus()) {

            case OPEN:
                for (OrderLine line : lineItems) {
                    if (line.getItemQty() > 0 && (!line.getStatus().equals(OrderLineStatus.CANCELLED))) {
                        Long tempQty = productIdQtyMap.get(line.getSystemCatalogProductIdentifier());
                        if (tempQty != null) {
                            productIdQtyMap.put(line.getSystemCatalogProductIdentifier(), tempQty + line.getItemQty());
                        }
                        else {
                            productIdQtyMap.put(line.getSystemCatalogProductIdentifier(), (long) line.getItemQty());
                        }
                    }
                }
                break;
            default:
                for (OrderLine line : lineItems) {
                    if ((line.getStatus().equals(OrderLineStatus.REJECTED)
                            || line.getStatus().equals(OrderLineStatus.OPEN)
                            || line.getStatus()
                            .equals(OrderLineStatus.BACKORDER))
                            && line.getItemQty() > 0) {

                        Long tempQty = productIdQtyMap.get(line.getSystemCatalogProductIdentifier());

                        if (tempQty != null) {
                            productIdQtyMap.put(
                                    line.getSystemCatalogProductIdentifier(),
                                    tempQty + line.getItemQty());
                        }
                        else {
                            productIdQtyMap.put(
                                    line.getSystemCatalogProductIdentifier(),
                                    (long) line.getItemQty());
                        }
                    }
                }
                break;
        }
        return productIdQtyMap;
    }

    /**
     * Create Mapping for Product, Required ItemQty by Line Uid for the given salesOrder
     * Line Item
     *
     * @param order - SalesOrder
     * @return - Mapping of Product, Required ItemQty by Line Uid
     */
    @Override
    public Map<String, Pair<String, Long>> populateProductIdQtyPairByLineUid(SalesOrder order) {

        Objects.requireNonNull(order, "order object cannot be null");


        List<BaseOrderLine> lineItems = order.getLineItems();
        Map<String, Pair<String, Long>> lineUIDToProductQuantityPairs = new
                HashMap<String, Pair<String, Long>>();
        switch (order.getHeader().getStatus()) {

            case OPEN:
                for (OrderLine line : lineItems) {
                    if (line.getItemQty() > 0) {
                        Pair<String, Long> pair = new ImmutablePair<String, Long>(line
                                .getSystemCatalogProductIdentifier(), new Long(line.getItemQty()));

                        lineUIDToProductQuantityPairs.put(line.getUid(), pair);
                    }
                }
                break;
            default:
                for (OrderLine line : lineItems) {
                    if ((line.getStatus().equals(OrderLineStatus.REJECTED)
                            || line.getStatus().equals(OrderLineStatus.OPEN)
                            || line.getStatus().equals(OrderLineStatus.BACKORDER)
                            || line.getStatus().equals(OrderLineStatus.FAILED_TO_ALLOCATE))
                            && line.getItemQty() > 0) {

                        Pair<String, Long> pair = new ImmutablePair<String, Long>(line
                                .getSystemCatalogProductIdentifier(), new Long(line.getItemQty()));

                        lineUIDToProductQuantityPairs.put(line.getUid(), pair);
                    }

                }
                break;
        }
        return lineUIDToProductQuantityPairs;
    }
}
