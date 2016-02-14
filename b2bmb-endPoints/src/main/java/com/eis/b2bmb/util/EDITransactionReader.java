package com.eis.b2bmb.util;

import com.eis.b2bmb.api.v1.dao.AvailableDocumentDAO;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.model.BlobStore;
import com.eis.core.api.v1.model.Mailbox;
import com.eis.license.LicenseConstants;
import inedi.Edireader;
import inedi.InEDIException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.Iterator;
import java.util.TooManyListenersException;

/**
 * Reads an EDI Transaction and splits it out into transactions, stores them in a mailbox
 * and returns a list of the mailbox entry ids.
 */
public class EDITransactionReader {

    private static final Logger LOG = LoggerFactory.getLogger(EDITransactionReader.class);

    /**
     * Method gets transactions out of document and saves them in MailboxEntries.
     *
     * @param inputStream            - inputStream object
     * @param blobStore - blobStore object
     * @param mailboxEntryDAO - mailboxEntryDAO object
     * @param availableDocumentDAO - AvailableDocumentDAO object
     * @param mailbox - the mailbox where to the save the transactions
     * @param transmissionId - the transmissionId
     * @param toUserId - to address
     * @param fromUserId - from address
     * @return boolean which indicates if document was rejected based on 997
     * @throws inedi.InEDIException               - if there is a problem reading the edi file
     * @throws java.util.TooManyListenersException    - if there are too many listeners on the edi reader
     * @throws com.eis.core.api.v1.exception.B2BNotFoundException         -  if something is not found
     * @throws com.eis.core.api.v1.exception.B2BTransactionFailed         - if the database had an issue
     */
    public Iterator getTransactions(InputStream inputStream, BlobStore blobStore,
                                    MailboxEntryDAO mailboxEntryDAO,
                                    AvailableDocumentDAO availableDocumentDAO,
                                    Mailbox mailbox, String transmissionId,
                                    String toUserId, String fromUserId)
            throws InEDIException, B2BNotFoundException, B2BTransactionFailed,
            TooManyListenersException {

        Edireader edireader1 = new Edireader();
        edireader1.setRuntimeLicense(LicenseConstants.getEdiLicenseString());
        EDITransactionListener eventListener = new  EDITransactionListener(edireader1,
                blobStore, mailboxEntryDAO,availableDocumentDAO,
                mailbox, transmissionId, toUserId, fromUserId);
        edireader1.addEdireaderEventListener(eventListener);

        try {
            edireader1.config("Encoding=iso-8859-1");
            edireader1.config("CrossFieldValidationEnabled=True");
            edireader1.setEDIStandard(edireader1.esX12);
            edireader1.setSchemaFormat(Edireader.schemaAltova);
            edireader1.setBuildDOM(Edireader.bdNone);
            edireader1.parseStream(inputStream);

            return eventListener.getTransactions().iterator();
//CHECKSTYLE:OFF
        } catch (Exception e) {
//CHECKSTYLE:ON

            throw new B2BTransactionFailed("An exception occurred trying to process the tranaction.", e);
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Done processing the transaction.");
            }
        }
    }
}
