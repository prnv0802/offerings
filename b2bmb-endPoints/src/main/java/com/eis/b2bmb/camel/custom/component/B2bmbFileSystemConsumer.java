package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.Blob;
import com.eis.core.api.v1.model.DynamicAttribute;
import com.eis.core.api.v1.model.DynamicAttributeType;
import com.eis.core.api.v1.model.DynamicSearchRequest;
import com.eis.core.api.v1.model.FileSystemEntry;
import com.eis.core.api.v1.model.FileSystemEntryType;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledBatchPollingConsumer;
import org.apache.camel.support.SynchronizationAdapter;
import org.apache.camel.util.CastUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * @author sudhakars
 */
public class B2bmbFileSystemConsumer extends ScheduledBatchPollingConsumer {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbFileSystemConsumer.class);

    //package visibility, default delay if none set
    static final long DEFAULT_CONSUMER_DELAY = 60 * 1000L;

    //package visibility, default messages per poll if none set
    static final int DEFAULT_MAX_MESSAGES_PER_POLL = 10;


    /**
     * Parameterized  constuctor
     *
     * @param endpoint   endpoint instance
     * @param processor  processor instance
     */
    public B2bmbFileSystemConsumer(B2bmbFileSystemEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    @Override
    protected int poll() throws Exception {

        String regexFilter = getEndpoint().getRegexFilter();
        String refName = getEndpoint().getRefName();
        String contentType = getEndpoint().getContentType();

        if (LOG.isDebugEnabled()) {
            LOG.debug("Full Path :: " + getEndpoint().getFilePath());
        }

        if (getEndpoint().getDomain() == null) {
            throw new IllegalArgumentException(
                    "The domainName can not be null");
        }

        final FileSystemEntry parentFileSystemEntry = getEndpoint().getFileSystemEntryDAO().
                getByRefName(getEndpoint().getFilePath(), getEndpoint().getDomain());
        if (parentFileSystemEntry == null) {
            throw new B2BNotFoundException("Directory does not exist. Domain: " + getEndpoint().getDomain() +
                    " Path: " + getEndpoint().getFilePath());
        }

        DynamicSearchRequest dynamicSearchRequest = new DynamicSearchRequest();
        DynamicAttribute parentAttribute = new DynamicAttribute();
        parentAttribute.setType(DynamicAttributeType.String);
        parentAttribute.setValue(parentFileSystemEntry.getId());
        parentAttribute.setRefName("parentFileEntryId");
        dynamicSearchRequest.getSearchFields().getAttributes().put("parentFileEntryId", parentAttribute);

        DynamicAttribute fileOnlyAttribute = new DynamicAttribute();
        fileOnlyAttribute.setType(DynamicAttributeType.String);
        fileOnlyAttribute.setValue(FileSystemEntryType.File.value());
        fileOnlyAttribute.setRefName("type");
        dynamicSearchRequest.getSearchFields().getAttributes().put("type", fileOnlyAttribute);

        if (regexFilter != null) {
            DynamicAttribute regexAttribute = new DynamicAttribute();
            regexAttribute.setType(DynamicAttributeType.Regex);
            regexAttribute.setValue(regexFilter);
            regexAttribute.setRefName("refName");
            dynamicSearchRequest.getSearchFields().getAttributes().put("refName", regexAttribute);
        }
        if (contentType != null) {
            DynamicAttribute contentTypeAttribute = new DynamicAttribute();
            contentTypeAttribute.setType(DynamicAttributeType.String);
            contentTypeAttribute.setValue(contentType);
            contentTypeAttribute.setRefName("contentType");
            dynamicSearchRequest.getSearchFields().getAttributes().put("contentType", contentTypeAttribute);
        }
        if (refName != null) {
            //actually all refnames might have paths in them so this could be really useless
            DynamicAttribute refNameAttribute = new DynamicAttribute();
            refNameAttribute.setType(DynamicAttributeType.String);
            refNameAttribute.setValue(refName);
            refNameAttribute.setRefName("refName");
            dynamicSearchRequest.getSearchFields().getAttributes().put("refName", refNameAttribute);
        }
        dynamicSearchRequest.setSearchConditionType("and");
        List<String> dataDomains = new ArrayList<String>();
        dataDomains.add(parentFileSystemEntry.getDataDomain());

        if (LOG.isDebugEnabled()) {
            LOG.debug("maxMessagesPerPoll is " + maxMessagesPerPoll);
        }
        boolean queryFileSystemAgain = true;
        int offsetMultiple = 0;
        LinkedList<Exchange> exchanges = new LinkedList<Exchange>();
        while (exchanges.size() < maxMessagesPerPoll && queryFileSystemAgain) {
            List<FileSystemEntry> fileSystemEntries = getEndpoint().getFileSystemEntryDAO().getList(
                    dynamicSearchRequest, maxMessagesPerPoll * offsetMultiple, maxMessagesPerPoll,
                    null, dataDomains);
            offsetMultiple++;

            if (fileSystemEntries == null || fileSystemEntries.size() == 0) {
                queryFileSystemAgain = false;
            } else {
                for (final FileSystemEntry fileSystemEntry : fileSystemEntries) {
                    if (exchanges.size() < maxMessagesPerPoll) {
                        //make sure we havent created an exchange in this loop or in an earlier poll
                        if (!getEndpoint().getInProgressRepository().contains(fileSystemEntry.getId())) {

                            getEndpoint().getInProgressRepository().add(fileSystemEntry.getId());
                            //get the blob
                            Blob fileContents = getEndpoint().getBlobStore().getBlobByStringId(
                                    fileSystemEntry.getBlobId());
                            Exchange exchange = getEndpoint().createExchange();
                            exchange.getIn().setBody(fileContents);
                            exchange.getIn().setHeader(Exchange.FILE_NAME, fileSystemEntry.getName());
                            exchange.getIn().setHeader(Exchange.FILE_LENGTH, fileSystemEntry.getSize());
                            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, fileSystemEntry.getContentType());
                            exchange.setProperty(B2bmbCamelConstants.TRANSMISSION_ID,
                                    fileSystemEntry.getTransmissionId());
                            exchange.getIn().setHeader(B2bmbCamelConstants.TRANSMISSION_ID,
                                    fileSystemEntry.getTransmissionId());
                            exchange.setProperty("FileSystemEntryId", fileSystemEntry.getId());
                            exchange.addOnCompletion(new SynchronizationAdapter() {
                                @Override
                                public void onComplete(Exchange exchange) {
                                    processRouteCompletion(fileSystemEntry, parentFileSystemEntry,
                                            getEndpoint(), exchange, true);
                                }

                                @Override
                                public void onFailure(Exchange exchange) {
                                    processRouteCompletion(fileSystemEntry, parentFileSystemEntry,
                                            getEndpoint(), exchange, false);
                                }

                                @Override
                                public boolean allowHandover() {
                                    return false;
                                }

                                @Override
                                public String toString() {
                                    return "B2bmbFileSystemConsumerOnCompletion";
                                }
                            });
                            exchanges.add(exchange);
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
                LOG.debug("No File System Entry found  so setting  nothing in exchange ");
            }
            return 0;
        }
    }

    /**
     * Process successful completion of route
     * @param fileSystemEntry the fileSystemEntry that was sent
     * @param parentFileSystemEntry the base fileSystemEntry directory
     * @param endpoint file system endpoint
     * @param exchange the exchange at success
     * @param isSuccessful was the completion successful, false if it errored
     */
    private void processRouteCompletion(FileSystemEntry fileSystemEntry, FileSystemEntry parentFileSystemEntry,
                                        B2bmbFileSystemEndpoint endpoint, Exchange exchange, boolean isSuccessful) {
        try {
            //moved to success directory
            List<FileSystemEntry> fileSystemEntries = null;

            DynamicSearchRequest dynamicSearchRequest = new DynamicSearchRequest();
            DynamicAttribute parentAttribute = new DynamicAttribute();
            parentAttribute.setType(DynamicAttributeType.String);
            parentAttribute.setValue(parentFileSystemEntry.getId());
            parentAttribute.setRefName("parentFileEntryId");
            dynamicSearchRequest.getSearchFields().getAttributes().put("parentFileEntryId", parentAttribute);

            DynamicAttribute directoryOnlyAttribute = new DynamicAttribute();
            directoryOnlyAttribute.setType(DynamicAttributeType.String);
            directoryOnlyAttribute.setValue(FileSystemEntryType.Directory.value());
            directoryOnlyAttribute.setRefName("type");
            dynamicSearchRequest.getSearchFields().getAttributes().put("type", directoryOnlyAttribute);

            DynamicAttribute directoryNameAttribute = new DynamicAttribute();
            directoryNameAttribute.setType(DynamicAttributeType.String);
            if (isSuccessful) {
                directoryNameAttribute.setValue(endpoint.getSuccessDirectory());
            } else {
                directoryNameAttribute.setValue(endpoint.getErrorDirectory());
            }
            directoryNameAttribute.setRefName("name");
            dynamicSearchRequest.getSearchFields().getAttributes().put("name", directoryNameAttribute);

            List<String> dataDomains = new ArrayList<String>();
            dataDomains.add(parentFileSystemEntry.getDataDomain());

            fileSystemEntries = endpoint.getFileSystemEntryDAO().getList(dynamicSearchRequest, 0, 1, null, dataDomains);

            FileSystemEntry moveToDirectory = null;
            if (fileSystemEntries.isEmpty()) {
                //create directory since it doesn't exist
                FileSystemEntry newDirectory = new FileSystemEntry();
                newDirectory.setParentFileEntryId(parentFileSystemEntry.getId());
                newDirectory.setOwnerUserProfileRefName(parentFileSystemEntry.getOwnerUserProfileRefName());
                if (isSuccessful) {
                    newDirectory.setName(endpoint.getSuccessDirectory());
                } else {
                    newDirectory.setName(endpoint.getErrorDirectory());
                }
                newDirectory.setType(FileSystemEntryType.Directory);
                newDirectory.setDataDomain(parentFileSystemEntry.getDataDomain());
                moveToDirectory = endpoint.getFileSystemEntryDAO().save(newDirectory);
            } else {
                moveToDirectory = fileSystemEntries.get(0);
            }
            fileSystemEntry.setParentFileEntryId(moveToDirectory.getId());
            //update file name w/ timestamp to avoid duplicates
            String fileName = fileSystemEntry.getName();
            if (fileSystemEntry.getCreateDate() != null) {
                fileName = fileSystemEntry.getCreateDate().getTime() + "_" + fileName;
            } else {
                if (LOG.isDebugEnabled()) {
                    LOG.debug(String.format("Somehow fileSystemEntry with id %s domain %s refname %s " +
                                    "did not have a create date set", fileSystemEntry.getId(),
                            fileSystemEntry.getDataDomain(), fileSystemEntry.getRefName()));
                }
                fileName = Calendar.getInstance().getTime().getTime() + "_" + fileName;
            }
            fileSystemEntry.setName(fileName);
            endpoint.getFileSystemEntryDAO().save(fileSystemEntry);

        } catch (B2BTransactionFailed |B2BNotFoundException | ValidationException e) {

            if (LOG.isErrorEnabled()) {
                LOG.error("processRouteCompletion failed for fileSystemEntry " + fileSystemEntry.getId() +
                        "route was " + (isSuccessful?"successful":"unsuccessful"), e);
            }
        } finally {
            getEndpoint().getInProgressRepository().remove(fileSystemEntry.getId());
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
            String mailboxEntryId = exchange.getProperty("FileSystemEntryId").toString();

            if (LOG.isInfoEnabled()) {
                LOG.info("Sending FSE " + index + " of " + total);
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
    public B2bmbFileSystemEndpoint getEndpoint() {
        return (B2bmbFileSystemEndpoint) super.getEndpoint();
    }

    @Override
    public String toString() {
        return "B2bmbFileSystemConsumer";
    }
}
