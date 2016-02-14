package com.eis.b2bmb.camel.custom.util;

import com.eis.b2bmb.api.v1.dao.EDIProfileDAO;
import com.eis.b2bmb.api.v1.model.EDIProfile;
import com.eis.b2bmb.api.v1.model.MailboxRouter;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.BlobStoreException;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.core.router.MailboxRouterHelper;
import inedi.InEDIException;
import org.apache.camel.Exchange;
import org.apache.camel.NoSuchHeaderException;
import org.apache.camel.util.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;

/**
 * Utility class used to create mailbox entries in camel.
 */
public class EDIFileProcessorHelper {

    private static final Logger LOG = LoggerFactory.getLogger(EDIFileProcessorHelper.class);

    /**
     * Processes the EDI File and returns a mailbox router.
     *
     * @param exchange - Camel Exchange object
     * @param mailboxRouterHelper - Mailbox Router Helper
     * @param ediProfileDao - edi profile DAO
     * @param message - InputStream
     * @return MailboxRouter - a mailbox router
     * @throws org.apache.camel.NoSuchHeaderException - if a required camel header is not passed
     * @throws com.eis.core.api.v1.exception.BlobStoreException - if there is a problem with the blob store
     * @throws com.eis.core.api.v1.exception.B2BTransactionFailed - if the blob or mailbox entry can not be saved
     * @throws com.eis.core.api.v1.exception.B2BNotFoundException - in there is problem finding the object
     * @throws com.eis.core.api.v1.exception.ValidationException - if the mailbox entry is not valid
     * @throws inedi.InEDIException - if edi file can not be parsed
     * @throws java.util.TooManyListenersException - if the edi can not be parsed
     */
    public static MailboxRouter processEDIFile(Exchange exchange, MailboxRouterHelper mailboxRouterHelper,
                                      EDIProfileDAO ediProfileDao, InputStream message)
            throws NoSuchHeaderException, BlobStoreException, B2BTransactionFailed, B2BNotFoundException,
            ValidationException, InEDIException, TooManyListenersException{

        String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
        String fileType = exchange.getIn().getHeader(B2bmbCamelConstants.FILE_TYPE, String.class);

        if (fileName == null) {
            throw new IllegalStateException("fileName can not be null.");
        }

        if (fileType == null) {
            throw new IllegalStateException("fileType can not be null.");
        }

        MailboxRouter mailboxRouter = mailboxRouterHelper.getMailboxRouterFromFile(message,
                fileName, fileType);

        if (mailboxRouter == null) {
            throw new B2BNotFoundException("No mailbox router has been set up for sender, receiver " +
                    "and document type that was passed in.  Check the original document that was passed in " +
                    "and make sure the mailbox router is setup");
        }
        EDIProfile ediProfile = ediProfileDao.getEDIProfile(mailboxRouter.getFromInternalEmailAddress(),
                mailboxRouter.getToInternalEmailAddress(),
                mailboxRouterHelper.getDocumentNumber());
        if (ediProfile == null) {
            throw new B2BNotFoundException(String.format("There is no EDI setup for %s %s %s",
                    mailboxRouter.getFromInternalEmailAddress(), mailboxRouter.getFromInternalEmailAddress(),
                    mailboxRouterHelper.getDocumentNumber()));
        }

        Map<String, Object> referenceData = new HashMap<String, Object>();
        referenceData.put("fromVendor", ediProfile.getFromVendor());
        referenceData.put("toVendor", ediProfile.getToVendor());


        if (ediProfile.getLastInterchangeControlNumber() == null) {
            ediProfile.setLastInterchangeControlNumber(
                    new BigInteger(mailboxRouterHelper.getInterchangeControlNumber()));
            ediProfileDao.save(ediProfile);
        }

        if (mailboxRouterHelper.getInterchangeControlNumber() != null) {

            exchange.getIn().setHeader(B2bmbCamelConstants.MAIL_SEQUENCE_NUMBER,
                    new BigInteger(mailboxRouterHelper.getInterchangeControlNumber()));
        }

        if (mailboxRouterHelper.getDocumentNumber() != null) {
            exchange.getIn().setHeader(B2bmbCamelConstants.DOCUMENT_TYPE,
                    mailboxRouterHelper.getDocumentNumber());
        }

        if (fileType != null) {
            exchange.getIn().setHeader(B2bmbCamelConstants.FILE_TYPE,
                    fileType);
        }

        if (mailboxRouterHelper.getReferenceData() != null &&
                mailboxRouterHelper.getReferenceData().size() > 0) {
            referenceData.putAll(mailboxRouterHelper.getReferenceData());
        }

        exchange.getIn().setHeader(B2bmbCamelConstants.REFERENCE_DATA,
                referenceData);

        return mailboxRouter;

    }
}
