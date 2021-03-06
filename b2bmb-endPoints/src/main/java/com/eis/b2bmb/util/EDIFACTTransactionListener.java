package com.eis.b2bmb.util;

import com.eis.b2bmb.api.v1.dao.AvailableDocumentDAO;
import com.eis.b2bmb.api.v1.model.AvailableDocument;
import com.eis.b2bmb.api.v1.model.SegmentData;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.BlobStoreException;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.api.v1.model.*;
import com.eis.security.multitenancy.model.SecureSession;
import inedi.*;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Collection;

/**
 * Listener used to read transactions from an EDI file and create mailbox entries out of
 * them.
 */
public class EDIFACTTransactionListener implements EdireaderEventListener{

    private static final Logger LOG = LoggerFactory.getLogger(EDIFACTTransactionListener.class);

    private List<String> transactions = new ArrayList<String>();

    private boolean recordTransaction = false;

    private StringBuilder builder = null;

    private String startInterchange = null;

    private String endInterchange = null;

    private String startFunctionalGroup = null;

    private String endFunctionalGroup = null;

    private String interchangeContolNumber = null;

    private String functionalGroupControlNumber = null;


    private BlobStore blobStore = null;

    private MailboxEntryDAO mailboxEntryDAO = null;

    private Mailbox mailbox = null;

    private String transmissionId = null;

    private String toAddress = null;

    private String fromAddress = null;

    private String documentType;
    private String documentId;

    private Map<String,Object> referenceData = new HashMap<String, Object>();

    private boolean containsFunctionalGroups = false;

    private String version;
    private Edireader edireader;

    private String senderQualifier;
    private String senderAddress;
    private String receiverQualifier;
    private String receiverAddress;

    private String componentDelimiter;
    private String elementDelimiter;
    private String lineTerminator;
    private String releaseCharacter;

    private AvailableDocumentDAO availableDocumentDAO;
    private AvailableDocument availableDocument =   null;

    /**
     * Creates an EDITransactionListener.
     *
     * @param edireader - Edireader object
     * @param blobStore - blobStore object
     * @param mailboxEntryDAO - mailboxEntryDAO object
     * @param availableDocumentDAO - availableDocumentDAO object
     * @param mailbox - the mailbox where to the save the transactions
     * @param transmissionId - the transmissionId
     * @param toAddress - to address
     * @param fromAddress - from address
     */
    public EDIFACTTransactionListener(Edireader edireader, BlobStore blobStore, MailboxEntryDAO mailboxEntryDAO,
                                      AvailableDocumentDAO availableDocumentDAO,
                                      Mailbox mailbox, String transmissionId, String toAddress,
                                      String fromAddress) {
         this.blobStore = blobStore;
         this.mailbox = mailbox;
         this.transmissionId = transmissionId;
         this.mailboxEntryDAO = mailboxEntryDAO;
         this.toAddress = toAddress;
         this.fromAddress = fromAddress;
         this.edireader = edireader;
         this.availableDocumentDAO = availableDocumentDAO;
    }

    /**
     * Method that captures the start of the interchange segment.
     *
     * @param e - EdireaderStartInterchangeEvent
     */
    public void startInterchange(EdireaderStartInterchangeEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StartInterchange: " + " Tag:" + e.tag + " Control Number:" + e.controlNumber + " FullSegment:"
                    + e.fullSegment);
        }

        referenceData.put("interchangeControlNumber", e.controlNumber);
        startInterchange = e.fullSegment;
        interchangeContolNumber = e.controlNumber;
        try {
            componentDelimiter = edireader.config("ComponentDelimiter");
            elementDelimiter = edireader.config("ElementDelimiter");
            lineTerminator = edireader.config("SegmentDelimiter");
            releaseCharacter = edireader.config("ReleaseChar");
        } catch (InEDIException exception) {
            throw new IllegalStateException("Unable to determine delimiters.");
        }

        String[] interchangeResult = startInterchange.split("["+elementDelimiter+"]");
        String sender = interchangeResult[2];
        String receiver = interchangeResult[3];
        String[] senderParts = sender.split("["+componentDelimiter+"]");
        String[] receiverParts = receiver.split("["+componentDelimiter+"]");
        senderQualifier = senderParts[1].trim();
        senderAddress = senderParts[0].trim();
        receiverQualifier = receiverParts[1].trim();
        receiverAddress = receiverParts[0].trim();
        String interchangeInfo =  interchangeResult[7];
        String [] interchangeParts = interchangeInfo.split("["+componentDelimiter+"]");
        documentType = interchangeParts[0];

    }

    /**
     * Method that captures the end of the interchange segment.
     *
     * @param e - EdireaderEndInterchangeEvent
     */
    public void endInterchange(EdireaderEndInterchangeEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("EndInterchange: " + " Tag:" + e.tag + " Control Number:" + e.controlNumber + " FullSegment:"
                    + e.fullSegment);
        }

        endInterchange = e.fullSegment;
    }

    /**
     * Method that captures the start of the functional group segment.
     *
     * @param e - EdireaderStartFunctionalGroupEvent
     */
    public void startFunctionalGroup(EdireaderStartFunctionalGroupEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StartFunctionalGroup: " + " Tag:" + e.tag + " Control Number:" + e.controlNumber
                    + " FullSegment:" + e.fullSegment);
        }

        startFunctionalGroup = e.fullSegment;
        functionalGroupControlNumber = e.controlNumber;
        containsFunctionalGroups = true;

    }

    /**
     * Method that captures the end of the functional group segment.
     *
     * @param e - EdireaderEndFunctionalGroupEvent
     */
    public void endFunctionalGroup(EdireaderEndFunctionalGroupEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("EndFunctionalGroup: " + "Tag:" + e.tag + " Control Number:" + e.controlNumber + " Count:"
                    + e.count + " FullSegment:" + e.fullSegment);
        }

        if(containsFunctionalGroups) {
            endFunctionalGroup = e.fullSegment;
            endFunctionalGroup = endFunctionalGroup.replaceFirst(String.valueOf(e.count), "1");
        }
    }

    /**
     * Method that captures the start of the transaction segment.
     *
     * @param e - EdireaderStartTransactionEvent
     */
    public void startTransaction(EdireaderStartTransactionEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StartTransaction: " + "Tag:" + e.tag + " Control Number:" + e.controlNumber + " FullSegment:"
                    + e.fullSegment);
        }
        recordTransaction = true;
        builder = new StringBuilder();
        builder.append(startInterchange);
        if(containsFunctionalGroups) {
            builder.append(startFunctionalGroup);
        }
        builder.append(e.fullSegment);
        documentType = e.code;

        List<String> refData =  null;
        if (referenceData.get("transactionSetControlNumber") == null) {
            refData = new ArrayList<String>();
            referenceData.put("transactionSetControlNumber", refData);
        }

        refData = (List) referenceData.get("transactionSetControlNumber");
        refData.add(e.controlNumber);

        String[] transactionResult = e.fullSegment.split("["+elementDelimiter+"]");

        String transactionStart = transactionResult[2];
        String [] transactionStartResult = transactionStart.split("["+componentDelimiter+"]");
        documentId = transactionStartResult[0];
        version = transactionStartResult[1]+transactionStartResult[2];

    }

    /**
     * Method that captures the end of the transaction segment.
     *
     * @param e - EdireaderEndTransactionEvent
     */
    public void endTransaction(EdireaderEndTransactionEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("EndTransaction: " + "Tag:" + e.tag + " Control Number:" + e.controlNumber + " Count:"
                    + e.count + " FullSegment:" + e.fullSegment);
        }
        recordTransaction = false;
        builder.append(e.fullSegment);
        if(containsFunctionalGroups) {
            builder.append("GE" + elementDelimiter + "1" + elementDelimiter +
                    functionalGroupControlNumber + lineTerminator);
        }
        builder.append("UNZ"+elementDelimiter+"1"+elementDelimiter+interchangeContolNumber+lineTerminator);
        String fileName =  documentId + "-" + interchangeContolNumber +"-"+functionalGroupControlNumber+
                "-"+e.controlNumber;
        String path = mailbox.getDataDomain().replace(".", "/") + "/" + mailbox.getRefName() + "/" +
                fileName;

        String refName = fileName.replaceAll("\\s", "") + "-" +UUID.randomUUID();
        String contentType = "application/octet-stream";
        BlobMetaData metaData = blobStore.createMetaDataObject();
        metaData.setDataDomain(mailbox.getDataDomain());

        metaData.setPathString(path);
        metaData.setTxId(SecureSession.getTxId());
        Blob blob = null;

        try {
            blob = blobStore
                    .createBlobFromStream(refName, IOUtils.toInputStream(builder.toString()),
                            contentType, metaData);
        } catch (BlobStoreException e1) {
            throw new IllegalStateException("Could not create blob for transaction:"+fileName);
        }

        Attachment att = new Attachment();
        att.setInlinePayload(false);
        String fileId = blob.getIdAsString();

        att.setPayloadId(fileId);
        att.setFileSize(blob.getSize());

        if (LOG.isInfoEnabled()) {
            LOG.info("Done, file id: " + fileId);
        }

        att.setContentType(contentType);
        att.setFileName(fileName);
        att.setRefName(path);
        att.setDataDomain(mailbox.getDataDomain());
        att.setId(String.valueOf(UUID.randomUUID()));

        MailboxEntry mailboxEntry = new MailboxEntry();
        mailboxEntry.getAttachments().add(att);
        mailboxEntry.setMailboxId(mailbox.getId());
        mailboxEntry.setDataDomain(mailbox.getDataDomain());
        mailboxEntry.setTransmissionId(transmissionId);
        mailboxEntry.setSubject(fileName);
        mailboxEntry.setToUserId(toAddress);
        mailboxEntry.setFromUserId(fromAddress);
        mailboxEntry.setRefName(refName);
        mailboxEntry.setId(String.valueOf(UUID.randomUUID()));


        mailboxEntry.getMetaData().put(B2bmbCamelConstants.DOCUMENT_TYPE,
                documentId);

        mailboxEntry.getMetaData().put(B2bmbCamelConstants.REFERENCE_DATA,
                referenceData);

        try {
            mailboxEntryDAO.save(mailboxEntry);
        } catch (B2BTransactionFailed b2BTransactionFailed) {
            throw new IllegalStateException("Could not save mailbox entry for transaction:"+fileName);
        } catch (B2BNotFoundException e1) {
            throw new IllegalStateException("Could not save mailbox entry for transaction:"+fileName);
        } catch (ValidationException e1) {
            throw new IllegalStateException("Could not save mailbox entry for transaction:"+fileName);
        }

        transactions.add(mailboxEntry.getId());

    }

    /**
     * Method that captures the start of the loop segment.
     *
     * @param e - EdireaderStartLoopEvent
     */
    public void startLoop(EdireaderStartLoopEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("StartLoop: " + e.name);
        }
    }

    /**
     * Method that captures the end of the loop segment.
     *
     * @param e - EdireaderEndLoopEvent
     */
    public void endLoop(EdireaderEndLoopEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("EndLoop");
        }
        //functionalGroupBuilder.append("END").append("\n");
    }

    /**
     * Method that captures when an error occurs.
     *
     * @param e - EdireaderErrorEvent
     */
    public void error(EdireaderErrorEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ERROR: " + e.errorCode + ":" + e.description);
        }
    }

    /**
     * Method that captures when the schema is resolved.
     *
     * @param e - EdireaderResolveSchemaEvent
     */
    public void resolveSchema(EdireaderResolveSchemaEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("ResolveSchema: " + e.transactionCode);

        }
    }

    /**
     * Method that captures when a segment is read
     *
     * @param e - EdireaderSegmentEvent
     */
    public void segment(EdireaderSegmentEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Segment: " + e.name + "|L:" + e.loop + "|t:" + e.tag + "|F:" + e.fullSegment);
        }

        if(recordTransaction) {
            String fullSeqment = e.fullSegment;
            builder.append(fullSeqment);
            getReferenceData(e.tag, e.fullSegment);
        }


    }

    /**
     * Method that captures when a warning occurs.
     *
     * @param e - EdireaderWarningEvent
     */
    public void warning(EdireaderWarningEvent e) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("WARNING: " + e.warnCode + ": " + e.message);
        }
    }

    /**
     * Returns the list of Strings containing the transaction.
     *
     * @return List<String>
     */
    public List<String> getTransactions() {
        return transactions;
    }

    private void getReferenceData(String tag, String segment) {

        if (availableDocumentDAO != null) {

            try {
                if (availableDocument == null) {
                    availableDocument = availableDocumentDAO.getAvailableDocument(documentId, version);
                }
                if (availableDocument != null) {
                    LinkedHashMap<String, SegmentData> segmentDatas = availableDocument.getSegmentDatas();
                    if (segmentDatas.size() > 0) {
                        Collection<SegmentData> datas = segmentDatas.values();
                        for (SegmentData segmentData : datas) {
                            if (segmentData.getSegmentTag().equals(tag)) {
                                List<String> refData = null;
                                segment = segment.replace("\r", "").replace("\n", "");
                                String[] elementComponents = segment.split("[" + elementDelimiter + "]");
                                String elementComponent = elementComponents[segmentData.getElementPosition()];
                                String[] dataComponents = elementComponent.split("[" + componentDelimiter + "]");
                                String value = dataComponents[segmentData.getComponentPosition()];
                                if(value != null) {
                                    value = value.replace(lineTerminator, "");
                                    if (referenceData.get(segmentData.getDataFieldName()) == null) {
                                        refData = new ArrayList<String>();
                                        referenceData.put(segmentData.getDataFieldName(), refData);
                                    }
                                    refData = (List) referenceData.get(segmentData.getDataFieldName());
                                    refData.add(value);
                                }
                            }
                        }
                    }
                } else {
                    if (LOG.isWarnEnabled()) {
                        LOG.warn("No Available Document Found For Document:" + documentId + ", version:" + version);
                    }
                }
            } catch (B2BNotFoundException e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("No Available Document Found For Document:" + documentId + ", version:" + version, e);
                }
            } catch (B2BTransactionFailed e) {
                if (LOG.isWarnEnabled()) {
                    LOG.warn("No Available Document Found For Document:" + documentId + ", version:" + version, e);
                }

            }
        }
    }

}
