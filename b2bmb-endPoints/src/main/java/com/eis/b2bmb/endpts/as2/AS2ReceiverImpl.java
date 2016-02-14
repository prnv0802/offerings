package com.eis.b2bmb.endpts.as2;

import com.eis.b2bmb.api.v1.dao.AS2ServerConnectionConfigDAO;
import com.eis.b2bmb.api.v1.model.AS2ServerConnectionConfig;
import com.eis.b2bmb.api.v1.model.CommunicationProtocol;
import com.eis.b2bmb.endpoints.nsoftware.as2.serverHelper.CertificateHelper;
import com.eis.b2bmb.util.TransmissionRecorder;
import com.eis.common.Constants;
import com.eis.core.api.v1.dao.BlobDAO;
import com.eis.core.api.v1.dao.MailboxDAO;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.api.v1.exception.*;
import com.eis.core.api.v1.model.*;
import com.eis.license.LicenseConstants;
import com.eis.security.multitenancy.model.SecureSession;
import inedi.As2receiver;
import inedi.Certificate;
import inedi.Header;
import inedi.InEDIException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;

/**
 * User: mingardia
 * Date: 11/16/14
 * Time: 11:48 AM
 */
// CHECKSTYLE:OFF
public class AS2ReceiverImpl implements AS2Receiver{

    private static final Logger LOG = LoggerFactory
            .getLogger(AS2ReceiverImpl.class);


    @Autowired
    CertificateHelper certificateHelper;

    @Autowired
    AS2ServerConnectionConfigDAO serverConnectionConfigDAO;

    @Autowired
    BlobDAO blobDAO;

    @Autowired
    MailboxEntryDAO mbEntryDAO;

    @Autowired
    MailboxDAO mbDAO;

    @Autowired
    TransmissionRecorder transmissionRecorder;

    /**
     * Constructor
     */
    public AS2ReceiverImpl()
    {

    }

    /**
     * validates the configuration
     * @param config the config to validate
     */
    protected void validateConfig(Transmission transmission, AS2ServerConnectionConfig config,
                                  As2receiver as2receiver)
    {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;
        if (config.getAS2From() == null)
        {
            errors.append("AS2From required, ");
            isValid = false;
        }

        if (config.getAS2To() == null)
        {
            errors.append("AS2To required, ");
            isValid = false;
        }

        if (config.getMailboxFrom() == null)
        {
            errors.append("Mailbox from required, ");
            isValid = false;
        }

        if (config.getMailboxTo() == null)
        {
            errors.append("Mailbox to required, ");
            isValid = false;
        }

        if (config.getMailboxRefName() == null)
        {
            errors.append("mailbox refName is required, ");
            isValid = false;
        }

        if (config.getFromOrgObjectRef() == null)
        {
            errors.append("From Org Ref is required, ");
            isValid = false;
        }




        if(!isValid) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("Error", errors.toString());
            transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                    transmission.getDataDomain(), TransmissionStatus.FAILED,
                    "AS2 Server Configuration Validation Error.", as2receiver.getAS2From(),
                    as2receiver.getAS2To(), as2receiver.getEDIData().getFilename(),
                    "AS2 Server", TransmissionDirection.INBOUND);
            throw new IllegalArgumentException(errors.toString());
        }


    }




    @Override
    public void executeRequest(HttpServletRequest request, HttpServletResponse response)  {


        if (request.getMethod().equalsIgnoreCase("POST"))
        {   Transmission transmission = null;
            Map<String, Object> data = new HashMap<String, Object>();
            As2receiver as2receiver = new As2receiver();
            as2receiver.setRuntimeLicense(LicenseConstants.getEdiLicenseString());

            boolean failed = false;
            try {
                as2receiver.setLogDirectory("LogReceiver");
                //as2receiver.config("LogOptions=Status, Request, Response, Payload, Errors");
                as2receiver.config("LogOptions=All");
                as2receiver.config("LogDebug=True");

                as2receiver.readRequest(request);

                if (LOG.isInfoEnabled())
                {
                    LOG.info("Receiving AS2 Request:");
                    LOG.info("  AS2To:" + as2receiver.getAS2To());
                    LOG.info("  AS2From:" + as2receiver.getAS2From());
                    LOG.info("  MessageId:" + as2receiver.getMessageId());
                    LOG.info("  MDNTo:" + as2receiver.getMDNTo());
                    LOG.info("  Request Headers:" );

                    for (Header header :as2receiver.getRequestHeaders())
                    {
                        LOG.info("    Header:" + header.getField() + " Value:" + header.getValue());
                    }

                    LOG.info(" Requested MIC Algorithms:" + as2receiver.config("RequestedMICAlgorithms"));
                    LOG.info(" Requested Signature Algorithm:" + as2receiver.config("RequestedSignatureProtocol"));
                }

                transmission =  transmissionRecorder.createTransmission(TransmissionDirection.INBOUND,
                        Constants.CANTATA_APP_DATADOMAIN, CommunicationProtocol.AS2,
                        as2receiver.getAS2From(), as2receiver.getAS2To(), as2receiver.getEDIData().getFilename(),
                        "AS2 Server");

                for (Header header :as2receiver.getRequestHeaders())
                {
                    data.put("Header:"+header.getField(), header.getValue());
                }

                data.put(" MessageId:", as2receiver.getMessageId());
                data.put(" MDNTo:", as2receiver.getMDNTo());
                data.put(" About:", as2receiver.getAbout());
                data.put(" Requested MIC Algorithms:", as2receiver.config("RequestedMICAlgorithms"));
                data.put(" Requested SignatureProtocol:", as2receiver.config("RequestedSignatureProtocol"));
                data.put(" SenderSignatureAlgorithm:", as2receiver.config("SenderSignatureAlgorithm"));
                data.put(" SignatureType:", as2receiver.config("SignatureType"));
                data.put(" Number of Attachments:", Integer.toString(as2receiver.getAttachments().size()));


                transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                        transmission.getDataDomain(), TransmissionStatus.INPROCESS,
                        "Headers",  as2receiver.getAS2From(),
                        as2receiver.getAS2To(), as2receiver.getEDIData().getFilename(),
                        "AS2 Server", TransmissionDirection.INBOUND);

                data.clear();


                // Retrieve the server configuration based upon the to and from ids
                AS2ServerConnectionConfig config = serverConnectionConfigDAO.getServerConfig(
                        as2receiver.getAS2To(), as2receiver
                        .getAS2From());

                if (config == null)
                {

                    if (LOG.isWarnEnabled())
                    {
                        LOG.warn("No server side configuration found for AS2To:" + as2receiver.getAS2To() +
                                " AS2From:" + as2receiver.getAS2From());
                    }


                    data.put("Error", "No server side configuration found for AS2To:" + as2receiver.getAS2To() +
                            " AS2From:" + as2receiver.getAS2From());

                    transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                             transmission.getDataDomain(), TransmissionStatus.FAILED,
                             "AS2 Server Configuration Error.",  as2receiver.getAS2From(),
                            as2receiver.getAS2To(), as2receiver.getEDIData().getFilename(),
                             "AS2 Server", TransmissionDirection.INBOUND);

                    // no configuration for the ID pair given.
                    // return a bad status code.
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, " the AS2To:" + as2receiver.getAS2To() + "" +
                            " and the AS2From:" + as2receiver.getAS2From() + " is not configured for this server");

                    return;

                }

                try {
                    validateConfig(transmission, config, as2receiver);
                }
                catch (IllegalArgumentException e)
                {
                    throw new ValidationException(e);
                }



                Certificate ourEncryptionCert = certificateHelper.getCertificateWithPrivateKey(config
                                .getEncryptionCertObjectReference(),
                        config.getFromOrgObjectRef());


                if (LOG.isDebugEnabled())
                {
                    LOG.debug("Using encryption certificate:" + config.getEncryptionCertObjectReference().getRefName());
                    LOG.debug("> Subject:" + ourEncryptionCert.getSubject());
                    LOG.debug(" > SerialNumber:" + ourEncryptionCert.getSerialNumber());
                }

                data.put("Encryption / Server Signing certificate", config.getEncryptionCertObjectReference().getRefName());
                data.put("Subject",ourEncryptionCert.getSubject());
                data.put("Serial Number", ourEncryptionCert.getSerialNumber());
                data.put("Version:", ourEncryptionCert.getVersion());
                data.put("Signature Algorithm", ourEncryptionCert.getSignatureAlgorithm());
                data.put("Effective from date:", ourEncryptionCert.getEffectiveDate());
                data.put("Effective to date:", ourEncryptionCert.getExpirationDate());
                data.put("Issuer:",ourEncryptionCert.getIssuer());
                data.put("Finger Print:",ourEncryptionCert.getFingerprint());
                data.put("Has Private Key:", Boolean.toString(ourEncryptionCert.getPrivateKeyAvailable()));
                data.put("Public Key:", ourEncryptionCert.getPublicKey());
                data.put("Public Key Length:", Integer.toString(ourEncryptionCert.getPublicKeyLength()));
                data.put("Public Key Algorithm:", ourEncryptionCert.getPublicKeyAlgorithm());
                as2receiver.setCertificate(ourEncryptionCert);

                if (config.getSigningCertRef() != null)
                {

                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("Signing Cert is configured.:" + config.getSigningCertRef().getRefName());
                    }

                    data.put("TP Signing Cert RefName",config.getSigningCertRef().getRefName());

                    Certificate theirSigningCert = certificateHelper.getpublicCertificate(config.getSigningCertRef());

                    if (LOG.isDebugEnabled())
                    {

                        LOG.debug("TP Signing Cert: Subject:" + theirSigningCert.getSubject());
                        LOG.debug("TP Signing Cert: Effective From Date:" + theirSigningCert.getEffectiveDate());
                        LOG.debug("TP Signing Cert: Effective To Date:" + theirSigningCert.getExpirationDate());
                        LOG.debug("TP Signing Cert: SerialNumber" + theirSigningCert.getSerialNumber());
                        LOG.debug("TP Signing Cert: Issuer:" + theirSigningCert.getIssuer());
                        LOG.debug("TP Signing Cert: Signature Algorithm" + theirSigningCert.getSignatureAlgorithm());
                        LOG.debug("TP Signing Cert: Version:" + theirSigningCert.getVersion());
                    }

                    data.put("TP Signing Cert: Subject",theirSigningCert.getSubject());
                    data.put("TP Signing Cert: Effective From Date",theirSigningCert.getEffectiveDate());
                    data.put("TP Signing Cert: Effective To Date:",theirSigningCert.getExpirationDate());
                    data.put("TP Signing Cert: SerialNumber",theirSigningCert.getSerialNumber());
                    data.put("TP Signing Cert: Signature Algorithm" , theirSigningCert.getSignatureAlgorithm());
                    data.put("TP Signing Cert: Issuer",theirSigningCert.getIssuer());
                    data.put("TP Signing Cert: Version:", theirSigningCert.getVersion());

                    as2receiver.setSignerCert(theirSigningCert);


                }
                else {
                    if (LOG.isDebugEnabled())
                    {
                        LOG.debug("No Signing certificate configured.");
                    }

                    data.put("Signing Cert Configured","N");


                    if (LOG.isWarnEnabled())
                    {
                        LOG.warn("No signing certificate configured for AS2 TO:" + as2receiver.getAS2To() +
                                " AS2 From:" + as2receiver.getAS2From() + " will not validate the sender");
                    }


                }


                BlobMetaData blobMetaData = blobDAO.createMetaData();
                blobMetaData.setTxId(SecureSession.getTxId());
                blobMetaData.setDataDomain(config.getDataDomain());
                blobMetaData.setPathString("as2Data/" + config.getAS2To() + "/" + config.getAS2From());

                BlobHandle handle = blobDAO.createBlobHandle(as2receiver.getMessageId(),blobMetaData);
                OutputStream out = blobDAO.createStreamForBlob(handle);

                //ByteArrayOutputStream out = new ByteArrayOutputStream();
                transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                         transmission.getDataDomain(), TransmissionStatus.INPROCESS,
                         "Receiving File From:"+as2receiver.getAS2From()+" To:"+as2receiver.getAS2To(),
                         as2receiver.getAS2From(),
                         as2receiver.getAS2To(), as2receiver.getEDIData().getFilename(),
                         "AS2 Server", TransmissionDirection.INBOUND);

                as2receiver.getEDIData().setOutputStream(out);

                transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                        transmission.getDataDomain(), TransmissionStatus.RECEIVED,
                        "File Received From:" + as2receiver.getAS2From() +
                                " To:" + as2receiver.getAS2To(), as2receiver.getAS2From(),
                        as2receiver.getAS2To(), as2receiver.getEDIData().getFilename(),
                        "AS2 Server", TransmissionDirection.INBOUND);

                try {
                    as2receiver.processRequest();
                }
                finally
                {
                    out.close();
                }

                MailboxEntry entry = new MailboxEntry();
                Attachment attachment = new Attachment();
                attachment.setInlinePayload(false);
                attachment.setPayloadId(handle.getBlobIdAsString());

                if (as2receiver.getEDIData().getName() != null && !as2receiver.getEDIData().getName().isEmpty())
                {
                    attachment.setRefName(as2receiver.getEDIData().getName());
                    attachment.setFileName(as2receiver.getEDIData().getName());
                    attachment.setId(java.util.UUID.randomUUID().toString());
                    data.put("File Name", as2receiver.getEDIData().getName());
                    transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                            transmission.getDataDomain(), TransmissionStatus.RECEIVED,
                            "File:" + as2receiver.getEDIData().getName() + " Processed",
                            as2receiver.getAS2From(),
                            as2receiver.getAS2To(), as2receiver.getEDIData().getFilename(),
                            "AS2 Server", TransmissionDirection.INBOUND);
                }
                else
                {
                    attachment.setRefName("payload");
                    attachment.setFileName("payload");
                    attachment.setId(java.util.UUID.randomUUID().toString());
                    transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                             transmission.getDataDomain(), TransmissionStatus.RECEIVED,
                             "Unknown File Processed.", as2receiver.getAS2From(),
                            as2receiver.getAS2To(), as2receiver.getEDIData().getFilename(),
                            "AS2 Server", TransmissionDirection.INBOUND);
                }

                entry.setDateReceived(new Date());
                entry.setToUserId(config.getMailboxTo());
                entry.setFromUserId(config.getMailboxFrom());
                entry.setSubject("File received via as2 from as2Id:" + as2receiver.getAS2From() + " to as2Id:" +
                        as2receiver.getAS2To());
                entry.setRefName(String.valueOf(UUID.randomUUID()) + as2receiver.getAS2From() + "-" +
                        as2receiver.getAS2To());
                entry.setDataDomain(config.getDataDomain());

                if (as2receiver.getMDNTo() != null)
                {
                    Attachment mdnAttachment = new Attachment();
                    mdnAttachment.setInlinePayload(true);
                    mdnAttachment.setPayload(as2receiver.getMDNReceipt().getMDN());
                    mdnAttachment.setRefName("mdn");
                    mdnAttachment.setFileName("mdn");
                    mdnAttachment.setId(java.util.UUID.randomUUID().toString());
                    entry.getAttachments().add(mdnAttachment);

                    data.put("mdn", as2receiver.getMDNReceipt().getMDN());


                    transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                            transmission.getDataDomain(), TransmissionStatus.RECEIVED,
                            "MDN Processed.", as2receiver.getAS2From(),
                            as2receiver.getAS2To(), "mdn",
                            "AS2 Server", TransmissionDirection.INBOUND);


                }

                entry.getAttachments().add(attachment);

                Mailbox mb = mbDAO.getByRefName(config.getMailboxRefName(), config.getDataDomain());

                if (mb != null)
                {
                    entry.setMailboxId(mb.getId());
                }
                else
                {
                    mb = mbDAO.getByRefName("AS2-ErrorBox", config.getDataDomain());

                    if (mb == null)
                    {
                        Mailbox errorBox = new Mailbox();
                        errorBox.setMailboxRole(MailboxRole.ERROR);
                        errorBox.setRefName("AS2-ErrorBox");
                        errorBox.setDataDomain(config.getDataDomain());
                        mb = mbDAO.save(errorBox);
                    }

                    entry.setMailboxId(mb.getId());
                }

                mbEntryDAO.save(entry);

                transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                        transmission.getDataDomain(), TransmissionStatus.RECEIVED,
                        "File Sent To Mailbox:" + mb.getRefName(), as2receiver.getAS2From(),
                        as2receiver.getAS2To(), as2receiver.getEDIData().getFilename(),
                        "AS2 Server", TransmissionDirection.INBOUND);

            } catch (InEDIException e) {
                e.printStackTrace();
                failed = true;
            } catch (IOException e) {
                e.printStackTrace();
                failed = true;
            } catch (CertificateException e) {
                e.printStackTrace();
                failed = true;
            } catch (B2BNotAuthenticatedException e) {
                e.printStackTrace();
                failed = true;
            } catch (B2BTransactionFailed b2BTransactionFailed) {
                b2BTransactionFailed.printStackTrace();
                failed = true;
            } catch (B2BNotFoundException e) {
                e.printStackTrace();
                failed = true;
            } catch (B2BNotAuthorizedException e) {
                e.printStackTrace();
                failed = true;
            } catch (ValidationException e) {
                e.printStackTrace();
                failed = true;
            }

            if (as2receiver.getScanResult() != 0  || failed)
            {
                ArrayList<String> errorTextList = new ArrayList<String>();

                if ((as2receiver.getScanResult() & 1) == 1)
                {
                    errorTextList.add("Unable to decrypt data");
                }

                if ( (as2receiver.getScanResult() & 2) == 2)
                {
                    errorTextList.add("Unable to decompress data");
                }

                if ( (as2receiver.getScanResult() & 4) == 4)
                {
                    errorTextList.add("Unable to validate integrity of data");
                }

                if ( (as2receiver.getScanResult() & 8) == 8)
                {
                    errorTextList.add("Unable to validate Signature");
                }

                if ( (as2receiver.getScanResult() & 10) == 10)
                {
                    errorTextList.add("Client requested unsupported signature type");
                }

                if ( (as2receiver.getScanResult() & 20) == 20)
                {
                    errorTextList.add("Client requested unsupported MIC algorithm");
                }

                if ( (as2receiver.getScanResult() & 40) == 40 )
                {
                    errorTextList.add("Insufficient message security, as determined by the server config");
                }

                if ( (as2receiver.getScanResult() & 80) == 80  || failed)
                {
                    errorTextList.add("Unexpected processing error.  Check server log for more details");
                }

                data.put("Error", "Exception occurred on server:"+ StringUtils.join(
                        errorTextList, ", "));

                transmissionRecorder.addTransmissionEvent(transmission,CommunicationProtocol.AS2, data,
                        transmission.getDataDomain(), TransmissionStatus.FAILED,
                        "AS2 Server Exception", as2receiver.getAS2From(),
                        as2receiver.getAS2To(), as2receiver.getEDIData().getFilename(),
                        "AS2 Server", TransmissionDirection.INBOUND);

                if (LOG.isErrorEnabled())
                {
                    LOG.error(" --- Error Messages ---- ");
                    for (String msg : errorTextList)
                    {
                        LOG.error(msg);
                    }
                }

            }

            try {
                if (as2receiver.getScanResult() == 0 && failed)
                {
                    as2receiver.config("ProcessingError=True");

                    as2receiver.createMDNReceipt("","","An unexpected error occurred processing the file.  The file " +
                            "is NOT received, please resend");

                    data.put("Error", "An unexpected error occurred processing the file.  The file " +
                            "is NOT received, please resend");

                    transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                            transmission.getDataDomain(), TransmissionStatus.FAILED,
                            "AS2 Server Processing Error", as2receiver.getAS2From(),
                            as2receiver.getAS2To(), as2receiver.getEDIData().getFilename(),
                            "AS2 Server", TransmissionDirection.INBOUND);

                    as2receiver.sendResponse(response);
                } else if (failed) {

                    transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                            transmission.getDataDomain(), TransmissionStatus.FAILED,
                            "AS2 File Successfully Received But Had Errors", as2receiver.getAS2From(),
                            as2receiver.getAS2To(), as2receiver.getEDIData().getFilename(),
                            "AS2 Server", TransmissionDirection.INBOUND);

                    as2receiver.sendResponse(response);
                } else {

                    transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                            transmission.getDataDomain(), TransmissionStatus.RECEIVED,
                            "AS2 Transmission Successful", as2receiver.getAS2From(),
                            as2receiver.getAS2To(), as2receiver.getEDIData().getFilename(),
                            "AS2 Server", TransmissionDirection.INBOUND);

                    as2receiver.sendResponse(response);
                }

            } catch (InEDIException e) {
                e.printStackTrace();
            }

        }
        else {
            // no configuration for the ID pair given.
            // return a bad status code.
            try {

                response.sendError(HttpServletResponse.SC_BAD_REQUEST, " you can only post to this port");

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
// CHECKSTYLE:ON