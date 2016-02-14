package com.eis.b2bmb.endpts.as2;

import com.eis.b2bmb.api.v1.model.AS2ClientConnectionConfig;
import com.eis.b2bmb.api.v1.model.CommunicationProtocol;
import com.eis.b2bmb.endpoints.nsoftware.as2.serverHelper.CertificateHelper;
import com.eis.b2bmb.util.TransmissionRecorder;
import com.eis.common.Constants;
import com.eis.core.api.v1.dao.FileSystemEntryDAO;
import com.eis.core.api.v1.events.LocalEventPublisher;
import com.eis.core.api.v1.events.TransmissionLifecycleEvent;
import com.eis.core.api.v1.exception.*;
import com.eis.core.api.v1.model.FileSystemEntry;
import com.eis.core.api.v1.model.Transmission;
import com.eis.core.api.v1.model.TransmissionDirection;
import com.eis.core.api.v1.model.TransmissionStatus;
import com.eis.license.LicenseConstants;
import inedi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.TooManyListenersException;
import java.util.UUID;

/**
 * User: mingardia
 * Date: 11/16/14
 * Time: 9:25 AM
 */
public class AS2ClientImpl implements AS2Client {


    private static final Logger LOG = LoggerFactory.getLogger(AS2ClientImpl.class);

    @Autowired
    CertificateHelper certificateHelper;

    @Autowired
    FileSystemEntryDAO fileSystemEntryDAO;

    @Autowired
    LocalEventPublisher localEventPublisher;

    @Autowired
    TransmissionRecorder transmissionRecorder;


    /**
     * Validates the incoming configuration
     *
     * @param transmission - Transmission object
     * @param config       the configuration
     * @param as2sender    - AS2 Sender
     * @param fileName     - filename of file being sent
     */
    protected void validate(Transmission transmission, AS2ClientConnectionConfig config,
                            As2sender as2sender, String fileName) {
        StringBuilder errors = new StringBuilder();
        boolean isValid = true;

        if (config == null) {
            errors.append("Config has to be non null, ");
            isValid = false;
        }

        if (config.getAS2To() == null) {
            errors.append("As2Config must have a As2To, ");
            isValid = false;
        }

        if (config.getAS2From() == null) {
            errors.append("As2Config must have a As2From, ");
            isValid = false;
        }

        if (config.getServerURL() == null) {
            errors.append("As2Config must have a URL defined, ");
            isValid = false;
        }

        if (config.getFromOrgObjectRef() == null) {
            errors.append("The From OrgReference needs to be defined, ");
            isValid = false;
        }

        if (config.getMdmOptions() != null) {
            if (config.getMdmOptions().getMdmTo() == null) {
                errors.append("The MDN Options are set however the mdn to id is not set");
                isValid = false;
            }
        }


        if (!isValid) {
            Map<String, Object> data = new HashMap<String, Object>();
            data.put("Error", errors.toString());
            transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                    transmission.getDataDomain(), TransmissionStatus.FAILED,
                    "AS2 Server Configuration Validation Error.", as2sender.getAS2From(),
                    as2sender.getAS2To(), fileName,
                    "AS2 Client", TransmissionDirection.OUTBOUND);

            throw new IllegalArgumentException(errors.toString());
        }

    }


    @Override
    public void sendFile(AS2ClientConnectionConfig config, String dataPayloadName, InputStream dataStreamToSend,
                         String transmissionId) throws
            B2BTransactionFailed {
        Transmission transmission = null;
        Map<String, Object> transmissionData = new HashMap<String, Object>();

        if (config == null) {
            throw new IllegalArgumentException("config is required to be non null");
        }

        if (dataPayloadName == null || dataPayloadName.isEmpty()) {
            throw new IllegalArgumentException("dataPayloadName is required to be non null or not empty");
        }

        if (dataStreamToSend == null) {
            throw new IllegalArgumentException("input stream is required to be non null");
        }


        // Not sure if we can inject as2 senders for now just allocate  a new one for each send
        // FIXME later should be injected or pooled.
        As2sender as2sender = new As2sender();
        as2sender.setRuntimeLicense(LicenseConstants.getEdiLicenseString());

        AS2LogListener listener = new AS2LogListener(config);


        try {
            as2sender.addAs2senderEventListener(listener);

            as2sender.setAS2From(config.getAS2From());
            as2sender.setAS2To(config.getAS2To());


            transmission = transmissionRecorder.createTransmission(TransmissionDirection.OUTBOUND,
                    Constants.CANTATA_APP_DATADOMAIN, CommunicationProtocol.AS2,
                    as2sender.getAS2From(),
                    as2sender.getAS2To(), dataPayloadName,
                    "AS2 Client");

            validate(transmission, config, as2sender, dataPayloadName);

            transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, transmissionData,
                    transmission.getDataDomain(), TransmissionStatus.INPROCESS,
                    "AS2 Client Validation Successful",
                    as2sender.getAS2From(),
                    as2sender.getAS2To(), as2sender.getEDIData().getFilename(),
                    "AS2 Client", TransmissionDirection.OUTBOUND);

            if (config.getSigningCertRef() != null) {
                Certificate signingCert = certificateHelper.getCertificateWithPrivateKey(config.getSigningCertRef(),
                        config.getFromOrgObjectRef());

                as2sender.setSigningCert(signingCert);
                transmissionData.put("Signing Cert: Subject", signingCert.getSubject());
                transmissionData.put("Signing Cert: EffectiveDate", signingCert.getEffectiveDate());
                transmissionData.put("Signing Cert: SerialNumber", signingCert.getSerialNumber());
                transmissionData.put("Signing Cert: Issuer", signingCert.getIssuer());
            }



            if (config.getEncryptionCertObjectReference() == null)
            {
                throw new IllegalStateException("EncryptionCertObjectReference was not configured for config:" +
                config.getRefName());
            }

            Certificate recipientCert = certificateHelper.getpublicCertificate(config
                    .getEncryptionCertObjectReference());

            if (recipientCert == null)
            {
                throw new IllegalStateException("Could not find certificate with RefName:" +
                config.getEncryptionCertObjectReference().getRefName());
            }

            as2sender.getRecipientCerts().add(recipientCert);

            transmissionData.put("Recipient Cert: Subject", recipientCert.getSubject());
            transmissionData.put("Recipient Cert: EffectiveDate", recipientCert.getEffectiveDate());
            transmissionData.put("Recipient Cert: SerialNumber", recipientCert.getSerialNumber());
            transmissionData.put("Recipient Cert: Issuer", recipientCert.getIssuer());

            if (config.getMdmOptions() != null) {
                as2sender.setMDNTo(config.getMdmOptions().getMdmTo());

                if (!config.getMdmOptions().isSync()) {
                    throw new UnsupportedOperationException("Async MDN processing is not currently supported");
                }
            }


            if (config.getCompressData()) {
                // compress with Zlib compression
                as2sender.setCompressionFormat(1);
            }

            //as2sender.setLogDirectory("/inedi-logs");
            as2sender.setURL(config.getServerURL());

            transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, transmissionData,
                    transmission.getDataDomain(), TransmissionStatus.INPROCESS,
                    "AS2 Client Sending File:" + dataPayloadName + " to Server:" + config.getServerURL(),
                    as2sender.getAS2From(),
                    as2sender.getAS2To(), as2sender.getEDIData().getFilename(),
                    "AS2 Client", TransmissionDirection.OUTBOUND);


            EDIData data = new EDIData();
            data.setInputStream(dataStreamToSend);
            data.setFilename(dataPayloadName);

            as2sender.setEDIData(data);

            if (LOG.isInfoEnabled()) {
                LOG.info("Posting message with id:" + as2sender.getMessageId());
            }


            as2sender.post();

            if (as2sender.getMDNTo() != null) {
                MDNReceipt receipt = as2sender.getMDNReceipt();
                if (receipt != null) {
                    if (LOG.isInfoEnabled()) {
                        LOG.info("Receipt Received:");
                        LOG.info(" Signing Protocol:" + receipt.getSigningProtocol());
                        LOG.info(" Message:" + receipt.getMessage());
                        LOG.info(" MDN:" + receipt.getMDN());
                        LOG.info(" MIC:" + receipt.getMICValue());
                        LOG.info(" Headers:" + receipt.getHeaders());
                    }

                    transmissionData.put("Signing Protocol", receipt.getSigningProtocol());
                    transmissionData.put("Message", receipt.getMessage());
                    transmissionData.put("MDN", receipt.getMDN());
                    transmissionData.put("MIC", receipt.getMICValue());
                    transmissionData.put("Headers", receipt.getHeaders());
                    transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, transmissionData,
                            transmission.getDataDomain(), TransmissionStatus.DELIVERED,
                            "MDN Receipt Received", as2sender.getAS2From(),
                            as2sender.getAS2To(), as2sender.getEDIData().getFilename(),
                            "AS2 Client", TransmissionDirection.OUTBOUND);

                    // save the mdn to the directory specified in the configuration
                    if (config.getMdmOptions().getMdnDirectory() != null) {
                        StringBuffer buffer = new StringBuffer();
                        buffer.append("Receipt Received:\n");
                        buffer.append(" Signing Protocol:" + receipt.getSigningProtocol() + "\n");
                        buffer.append(" Message:" + receipt.getMessage() + "\n");
                        buffer.append(" MDN:" + receipt.getMDN() + "\n");
                        buffer.append(" MIC:" + receipt.getMICValue() + "\n");
                        buffer.append(" Headers:" + receipt.getHeaders() + "\n");

                        InputStream inputStream = new ByteArrayInputStream(buffer.toString()
                                .getBytes(StandardCharsets.UTF_8));

                        String id = String.valueOf(UUID.randomUUID());

                        FileSystemEntry e = fileSystemEntryDAO.createFileFromStreamInDirectoryName(id,
                                config.getAS2From() + "|" + config.getAS2To() + "|"
                                        + dataPayloadName + "-" + transmissionId,
                                config.getMdmOptions().getMdnDirectory(), inputStream,
                                "text/plain", config.getDataDomain(), "as2Server", transmissionId);

                        e.setTransmissionId(transmissionId);

                        transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2,
                                transmissionData,
                                transmission.getDataDomain(), TransmissionStatus.DELIVERED,
                                "MDR Receipt Saved in Directory:" + config.getMdmOptions().getMdnDirectory(),
                                as2sender.getAS2From(),
                                as2sender.getAS2To(), as2sender.getEDIData().getFilename(),
                                "AS2 Client", TransmissionDirection.OUTBOUND);

                        localEventPublisher.publish(new TransmissionLifecycleEvent(e, "MDN Created"));

                    }
                }
            }


        } catch (InEDIException e) {
            recordTransmisionEvent(transmission, transmissionData,
                    e, as2sender);
            throw new B2BTransactionFailed(e);
        } catch (CertificateException e) {
            recordTransmisionEvent(transmission, transmissionData,
                    e, as2sender);
            throw new B2BTransactionFailed(e);
        } catch (B2BNotAuthenticatedException e) {
            recordTransmisionEvent(transmission, transmissionData,
                    e, as2sender);
            throw new B2BTransactionFailed(e);
        } catch (IOException e) {
            recordTransmisionEvent(transmission, transmissionData,
                    e, as2sender);
            throw new B2BTransactionFailed(e);
        } catch (B2BNotFoundException e) {
            recordTransmisionEvent(transmission, transmissionData,
                    e, as2sender);
            throw new B2BTransactionFailed(e);
        } catch (B2BNotAuthorizedException e) {
            recordTransmisionEvent(transmission, transmissionData,
                    e, as2sender);
            throw new B2BTransactionFailed(e);
        } catch (TooManyListenersException e) {
            recordTransmisionEvent(transmission, transmissionData,
                    e, as2sender);
            throw new B2BTransactionFailed(e);
        }
    }

    private void recordTransmisionEvent(Transmission transmission, Map<String, Object> data,
                                        Exception e, As2sender as2sender) {
        transmissionRecorder.addTransmissionEvent(transmission, CommunicationProtocol.AS2, data,
                transmission.getDataDomain(), TransmissionStatus.FAILED,
                e.getMessage(), as2sender.getAS2From(),
                as2sender.getAS2To(), as2sender.getEDIData().getFilename(),
                "AS2 Client", TransmissionDirection.OUTBOUND);
    }
}
