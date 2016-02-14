package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.api.v1.exception.MailboxRouterNotFoundException;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.DocumentTypeHelper;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.*;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledBatchPollingConsumer;
import org.apache.camel.support.SynchronizationAdapter;
import org.apache.camel.util.CastUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.*;

/**
 * @author harjeets
 */
public class B2bmbMailboxConsumer extends ScheduledBatchPollingConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbMailboxConsumer.class);

    //package visibility, default delay if none set
    static final long DEFAULT_CONSUMER_DELAY = 60 * 1000L;

    //package visibility, default messages per poll if none set
    static final int DEFAULT_MAX_MESSAGES_PER_POLL = 10;


    /**
     * @param endpoint   endpoint object
     * @param processor  processor
     */
    public B2bmbMailboxConsumer(B2bmbMailboxEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    @Override
    protected int poll() throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Polling: B2BMB:" + this.getEndpoint().getEndpointUri().toString());
        }

        String domainName = getEndpoint().getDomain();
        String mailboxRefName = getEndpoint().getMailboxName();

        if (domainName == null) {
            throw new IllegalArgumentException(
                    "The domainName can not be null");
        }

        final Mailbox mailbox = getEndpoint().getMailboxDAO().getByRefName(mailboxRefName, domainName);

        if (mailbox == null) {
            throw new IllegalStateException("Mail box with domain Name ::" + domainName + "and refName" +
                    mailboxRefName + "not found ");
        }

        List<String> dataDomains = new ArrayList<String>();
        dataDomains.add(mailbox.getDataDomain());

        DynamicSearchRequest dynamicSearchRequest = new DynamicSearchRequest();

        DynamicAttribute mailboxAttribute = new DynamicAttribute();
        mailboxAttribute.setType(DynamicAttributeType.String);
        mailboxAttribute.setValue(mailbox.getId());
        mailboxAttribute.setRefName("mailboxId");
        dynamicSearchRequest.getSearchFields().getAttributes().put("mailboxId", mailboxAttribute);

        if (getEndpoint().getSubject() != null) {
            DynamicAttribute subjectAttribute = new DynamicAttribute();
            subjectAttribute.setType(DynamicAttributeType.String);
            subjectAttribute.setValue(getEndpoint().getSubject());
            subjectAttribute.setRefName("subject");
            dynamicSearchRequest.getSearchFields().getAttributes().put("subject", subjectAttribute);
        }
        if (getEndpoint().getTo() != null) {
            DynamicAttribute toAttribute = new DynamicAttribute();
            toAttribute.setType(DynamicAttributeType.String);
            toAttribute.setValue(getEndpoint().getTo());
            toAttribute.setRefName("toUserId");
            dynamicSearchRequest.getSearchFields().getAttributes().put("toUserId", toAttribute);
        }
        if (getEndpoint().getFrom() != null) {
            DynamicAttribute fromAttribute = new DynamicAttribute();
            fromAttribute.setType(DynamicAttributeType.String);
            fromAttribute.setValue(getEndpoint().getFrom());
            fromAttribute.setRefName("fromUserId");
            dynamicSearchRequest.getSearchFields().getAttributes().put("fromUserId", fromAttribute);
        }
        dynamicSearchRequest.setSearchConditionType("and");

        if (LOG.isDebugEnabled()) {
            LOG.debug("maxMessagesPerPoll is " + maxMessagesPerPoll);
        }

        boolean queryMailboxAgain = true;
        int offsetMultiple = 0;
        LinkedList<Exchange> exchanges = new LinkedList<Exchange>();

        String sortMail = getEndpoint().getSortMail();

        while (exchanges.size() < maxMessagesPerPoll && queryMailboxAgain) {
            List<MailboxEntry> mailboxEntries = null;
            if("true".equals(sortMail)) {
                mailboxEntries = getEndpoint().getMailboxEntryDAO().getMailOrderedBySequenceNumber(
                        dynamicSearchRequest,
                        maxMessagesPerPoll * offsetMultiple, maxMessagesPerPoll,
                        null, dataDomains);
            } else {
                mailboxEntries = getEndpoint().getMailboxEntryDAO().getList(dynamicSearchRequest,
                        maxMessagesPerPoll * offsetMultiple, maxMessagesPerPoll,
                        null, dataDomains);
            }

            offsetMultiple++;

            if (mailboxEntries == null || mailboxEntries.size() == 0) {
                queryMailboxAgain = false;
            } else {
                for (final MailboxEntry mailboxEntry : mailboxEntries) {
                    if (exchanges.size() < maxMessagesPerPoll) {
                        //make sure we havent created an exchange in this loop or in an earlier poll
                        if (!getEndpoint().getInProgressRepository().contains(mailboxEntry.getId())) {

                            getEndpoint().getInProgressRepository().add(mailboxEntry.getId());
                            String idAsBody = getEndpoint().getIdAsBody() != null ? getEndpoint().getIdAsBody()
                                    : "false";

                            if("true".equals(idAsBody)) {
                                Exchange exchange = getEndpoint().createExchange();
                                exchange.getIn().setBody(mailboxEntry.getId(), String.class);
                                if (mailboxEntry.getAttachments() != null && !mailboxEntry.getAttachments().isEmpty()) {
                                    Attachment attachment = mailboxEntry.getAttachments().get(0);
                                    exchange.getIn().setHeader(Exchange.FILE_NAME, attachment.getFileName());
                                    try {
                                        String docType = DocumentTypeHelper.getDocumentType(exchange);
                                        if (docType != null) {
                                            exchange.getIn().setHeader(B2bmbCamelConstants.DOCUMENT_TYPE, docType);
                                        }
                                    } catch(MailboxRouterNotFoundException e) {

                                       if(LOG.isWarnEnabled()) {
                                           LOG.warn("An unrecognized document has enter the system and mailbox router" +
                                                   "has not been found.", e.getMessage());
                                       }

                                        addCommonExchangeAttributes(exchange, mailboxEntry);
                                        exchanges.add(exchange);
                                        continue;
                                    }
                                }
                                addCommonExchangeAttributes(exchange, mailboxEntry);
                                exchanges.add(exchange);
                            } else if (mailboxEntry.getAttachments() != null &&
                                    !mailboxEntry.getAttachments().isEmpty()) {
                                //default behavior is to set attachment contents as body.
                                //TODO: implement flag to provide behavior consistent w/ email?
                                for (Attachment attachment : mailboxEntry.getAttachments()) {
                                    //supporting multiple, but best practice is to send one at a time
                                    Exchange exchange = getEndpoint().createExchange();

                                    //everytime I see this logic I hate it more - needs to be encapsulated
                                    if (attachment.isInlinePayload()) {
                                        exchange.getIn().setBody(attachment.getPayload(), String.class);
                                    } else {
                                        Blob attachmentContents = getEndpoint().getBlobDAO()
                                                .getById(attachment.getPayloadId());
                                        exchange.getIn().setBody(attachmentContents);
                                    }
                                    exchange.getIn().setHeader(Exchange.FILE_NAME, attachment.getFileName());
                                    exchange.getIn().setHeader(Exchange.FILE_LENGTH, attachment.getFileSize());
                                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, attachment.getContentType());
                                    exchange.getIn().setMessageId(mailboxEntry.getId());

                                     try {
                                        String docType = DocumentTypeHelper.getDocumentType(exchange);
                                        if (docType != null) {
                                             exchange.getIn().setHeader(B2bmbCamelConstants.DOCUMENT_TYPE, docType);
                                        }
                                    } catch(MailboxRouterNotFoundException e) {

                                        if(LOG.isWarnEnabled()) {
                                            LOG.warn("An unrecognized document has enter the " +
                                                    "system and mailbox router" +
                                                    "has not been found.", e.getMessage());
                                        }

                                        addCommonExchangeAttributes(exchange, mailboxEntry);
                                         exchanges.add(exchange);
                                        continue;
                                    }

                                    addCommonExchangeAttributes(exchange, mailboxEntry);
                                    exchanges.add(exchange);

                                }
                            } else {
                                Exchange exchange = getEndpoint().createExchange();
                                //set body of mbe as body
                                exchange.getIn().setBody(mailboxEntry.getMessage(), String.class);
                                addCommonExchangeAttributes(exchange, mailboxEntry);
                                exchanges.add(exchange);
                            }
                        }
                    }
                }
            }
        }
        if (exchanges.size() > 0){
            Deque<Exchange> q = exchanges;
            return processBatch(CastUtils.cast(q));
        } else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("No Mailbox Entry found  so setting  nothing in exchange ");
            }
            return 0;
        }

    }

    private void addCommonExchangeAttributes(Exchange exchange, final MailboxEntry mailboxEntry) {
        if(mailboxEntry.getMetaData().get(B2bmbCamelConstants.DOCUMENT_TYPE) != null) {
            exchange.getIn().setHeader(B2bmbCamelConstants.DOCUMENT_TYPE,
                    mailboxEntry.getMetaData().get(B2bmbCamelConstants.DOCUMENT_TYPE));
        }

        if(mailboxEntry.getMetaData().get(B2bmbCamelConstants.FILE_TYPE) != null) {
            exchange.getIn().setHeader(B2bmbCamelConstants.FILE_TYPE,
                    mailboxEntry.getMetaData().get(B2bmbCamelConstants.FILE_TYPE));
        }
        exchange.setProperty(B2bmbCamelConstants.TRANSMISSION_ID,  mailboxEntry.getTransmissionId());
        exchange.getIn().setHeader(B2bmbCamelConstants.TRANSMISSION_ID,
                mailboxEntry.getTransmissionId());
        exchange.setProperty("MailboxEntryId", mailboxEntry.getId());
        //set headers
        if(mailboxEntry.getSequenceNumber() != null) {
            exchange.getIn().setHeader(B2bmbCamelConstants.MAIL_SEQUENCE_NUMBER,
                    mailboxEntry.getSequenceNumber());
        }

        if(mailboxEntry.getMetaData() != null) {
            exchange.getIn().setHeader(B2bmbCamelConstants.REFERENCE_DATA,
                    mailboxEntry.getMetaData());
        }

        exchange.getIn().setHeader(B2bmbCamelConstants.TO, mailboxEntry.getToUserId());
        exchange.getIn().setHeader(B2bmbCamelConstants.FROM, mailboxEntry.getFromUserId());
        exchange.getIn().setHeader(B2bmbCamelConstants.SUBJECT, mailboxEntry.getSubject());
        exchange.getIn().setHeader(B2bmbCamelConstants.MAILBOX_ENTRY_ID, mailboxEntry.getId());
        exchange.addOnCompletion(new SynchronizationAdapter() {
            @Override
            public void onComplete(Exchange exchange) {
                processRouteCompletion(mailboxEntry, getEndpoint(), exchange, true);
            }

            @Override
            public void onFailure(Exchange exchange) {
                processRouteCompletion(mailboxEntry, getEndpoint(), exchange, false);
            }

            @Override
            public boolean allowHandover() {
                return false;
            }

            @Override
            public String toString() {
                return "B2bmbMailboxConsumerOnCompletion";
            }
        });
    }

    private void processRouteCompletion(MailboxEntry mailboxEntry, B2bmbMailboxEndpoint endpoint,
                                        Exchange exchange, boolean isSuccessful) {
        try {
            Mailbox mailbox = null;

            if(exchange.getIn().getHeader(B2bmbCamelConstants.MAIL_SEQUENCE_NUMBER) != null){
                mailboxEntry.setSequenceNumber(
                        exchange.getIn().getHeader(B2bmbCamelConstants.MAIL_SEQUENCE_NUMBER,
                                BigInteger.class));
            }

            if(exchange.getIn().getHeader(B2bmbCamelConstants.DOCUMENT_TYPE, String.class) != null) {
                mailboxEntry.getMetaData().put(B2bmbCamelConstants.DOCUMENT_TYPE,
                        exchange.getIn().getHeader(B2bmbCamelConstants.DOCUMENT_TYPE, String.class));
            }

            if(exchange.getIn().getHeader(B2bmbCamelConstants.FILE_TYPE, String.class) != null) {
                mailboxEntry.getMetaData().put(B2bmbCamelConstants.FILE_TYPE,
                        exchange.getIn().getHeader(B2bmbCamelConstants.FILE_TYPE, String.class));
            }

            if(exchange.getIn().getHeader(B2bmbCamelConstants.REFERENCE_DATA, Map.class) != null) {
                Map<String, Object> referenceData =    (Map<String, Object>) exchange.getIn().getHeader(
                        B2bmbCamelConstants.REFERENCE_DATA, Map.class);
                mailboxEntry.getMetaData().putAll(referenceData);
            }

            if (isSuccessful) {
                mailbox = endpoint.getMailboxDAO().getByRefName(endpoint.getProcessedMailboxName(),
                        endpoint.getDomain());
                if (mailbox == null){
                    Mailbox processedMailbox = new Mailbox();
                    processedMailbox.setDataDomain(endpoint.getDomain());
                    processedMailbox.setRefName(endpoint.getProcessedMailboxName());
                    processedMailbox.setMailboxRole(MailboxRole.INTERMEDIATE);
                    processedMailbox.setSystemMailbox(true);
                    mailbox = endpoint.getMailboxDAO().save(processedMailbox);
                }
            } else {
                mailbox = endpoint.getMailboxDAO().getByRefName(endpoint.getErrorMailboxName(), endpoint.getDomain());
                if (mailbox == null){
                    Mailbox errorMailbox = new Mailbox();
                    errorMailbox.setDataDomain(endpoint.getDomain());
                    errorMailbox.setRefName(endpoint.getErrorMailboxName());
                    errorMailbox.setMailboxRole(MailboxRole.ERROR);
                    errorMailbox.setSystemMailbox(true);
                    mailbox = endpoint.getMailboxDAO().save(errorMailbox);
                }

            }
            mailboxEntry.setMailboxId(mailbox.getId());
            endpoint.getMailboxEntryDAO().save(mailboxEntry);

        } catch (B2BTransactionFailed b2BTransactionFailed) {
            if (LOG.isErrorEnabled()) {
                LOG.error("processRouteCompletion failed for mailboxEntry " + mailboxEntry.getId() +
                        "route was " + (isSuccessful?"successful":"unsuccessful"), b2BTransactionFailed);
            }
        } catch (B2BNotFoundException b2BNotFoundException) {
            if (LOG.isErrorEnabled()) {
                LOG.error("processRouteCompletion failed for mailboxEntry " + mailboxEntry.getId() +
                        "route was " + (isSuccessful?"successful":"unsuccessful"), b2BNotFoundException);
            }
        } catch (ValidationException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("processRouteCompletion failed for mailboxEntry " + mailboxEntry.getId() +
                        "route was " + (isSuccessful?"successful":"unsuccessful"), e);
            }
        } finally {
            getEndpoint().getInProgressRepository().remove(mailboxEntry.getId());
        }
    }




    @Override
    public int processBatch(Queue<Object> exchanges) {

        final int total = exchanges.size();

        for (int index = 0; index < total && isBatchAllowed(); index++) {
            // only loop if we are started (allowed to run)
            Exchange exchange = (Exchange) exchanges.poll();
            // add current index and total as properties
            exchange.setProperty(Exchange.BATCH_INDEX, index);
            exchange.setProperty(Exchange.BATCH_SIZE, total);
            exchange.setProperty(Exchange.BATCH_COMPLETE, index == total - 1);

            // update pending number of exchanges
            pendingExchanges = total - index - 1;
            String mailboxEntryId = exchange.getProperty("MailboxEntryId").toString();

            if (LOG.isInfoEnabled()) {
                LOG.info("Sending MBE " + index + " of " + total);
            }

            // process the current exchange
            try {
                getProcessor().process(exchange);
//CHECKSTYLE:OFF
            } catch (Exception e) {
                if (LOG.isErrorEnabled()) {
                    LOG.error("Exception occurred on processing exchange", e);
                }
                getEndpoint().getInProgressRepository().remove(mailboxEntryId);
            }
//CHECKSTYLE:ON
        }
        return total;
    }

    @Override
    public B2bmbMailboxEndpoint getEndpoint() {
        return (B2bmbMailboxEndpoint) super.getEndpoint();
    }

    @Override
    public String toString() {
        return "B2bmbMailboxConsumer";
    }
}

