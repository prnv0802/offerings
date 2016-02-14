package com.eis.b2bmb.endpts.as2;

import com.eis.b2bmb.api.v1.model.AS2ClientConnectionConfig;
import inedi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;

/**
 * User: mingardia
 * Date: 11/16/14
 * Time: 11:03 AM
 */

public class AS2LogListener implements As2ClientListener, As2receiverEventListener {
    private static final Logger LOG = LoggerFactory
            .getLogger(As2ClientListener.class);

    // CHECKSTYLE:OFF


    /**
     * the configuration associated with this log listener *
     */
    protected AS2ClientConnectionConfig config;

    /**
     * the receiver implementation *
     */
    protected As2receiver receiver;

    /**
     * the constructor *
     */
    public AS2LogListener(AS2ClientConnectionConfig config) {
        this.config = config;
    }

    /**
     * the receiver *
     */
    public AS2LogListener(As2receiver receiver) {
        this.receiver = receiver;
    }

    @Override
    public void connected(As2senderConnectedEvent as2senderConnectedEvent) {
        if (LOG.isInfoEnabled()) {
            LOG.info("AS2 connected:" + config.getServerURL() + " Status:" +
                    as2senderConnectedEvent.statusCode + " description:" +
                    as2senderConnectedEvent.description);
        }


    }

    @Override
    public void disconnected(As2senderDisconnectedEvent as2senderDisconnectedEvent) {
        if (LOG.isInfoEnabled()) {
            LOG.info("AS2 disconnected:" + config.getServerURL() + " Status:" +
                    as2senderDisconnectedEvent.statusCode + " description:" +
                    as2senderDisconnectedEvent.description);
        }

    }

    @Override
    public void endTransfer(As2senderEndTransferEvent as2senderEndTransferEvent) {
        if (LOG.isInfoEnabled()) {
            LOG.info("Transfer Complete");
        }
    }

    @Override
    public void error(As2senderErrorEvent as2senderErrorEvent) {
        if (LOG.isErrorEnabled()) {
            LOG.error("Transmission Error:" + as2senderErrorEvent.description +
                    " errorCode:" + as2senderErrorEvent.errorCode);
        }
    }


    @Override
    public void header(As2senderHeaderEvent as2senderHeaderEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Header:" + as2senderHeaderEvent.field + " : " +
                    as2senderHeaderEvent.value);
        }
    }

    @Override
    public void setCookie(As2senderSetCookieEvent as2senderSetCookieEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Cookie:" + as2senderSetCookieEvent.name);
            LOG.debug("    >Path:" + as2senderSetCookieEvent.path);
            LOG.debug("    >Domain:" + as2senderSetCookieEvent.domain);
            LOG.debug("    >secure:" + as2senderSetCookieEvent.secure);
            LOG.debug("    >expires:" + as2senderSetCookieEvent.expires);
            LOG.debug("    >value:" + as2senderSetCookieEvent.value);
        }
    }

    @Override
    public void SSLServerAuthentication(As2senderSSLServerAuthenticationEvent as2senderSSLServerAuthenticationEvent) {
        if (LOG.isInfoEnabled()) {
            LOG.info("SSL Connection requested");
            LOG.info("   CertIssuer:" + as2senderSSLServerAuthenticationEvent.certIssuer);
            LOG.info("   CertSubject:" + as2senderSSLServerAuthenticationEvent.certSubject);
            LOG.info("   CertEncoded:" + as2senderSSLServerAuthenticationEvent.certEncoded);
        }

        if (as2senderSSLServerAuthenticationEvent.accept == false) {
            if (LOG.isErrorEnabled()) {
                LOG.error(" The certificate for a As2 Connection with url:" + config.getServerURL() +
                        " could not validate the ssl certificate from Issuer:" +
                        as2senderSSLServerAuthenticationEvent.certIssuer + " Reason:" +
                        as2senderSSLServerAuthenticationEvent.status);
            }
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info(" ... Accepted");
            }
        }

    }

    @Override
    public void SSLStatus(As2senderSSLStatusEvent as2senderSSLStatusEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(" SSL Status:" + as2senderSSLStatusEvent.message);
        }
    }

    @Override
    public void startTransfer(As2senderStartTransferEvent as2senderStartTransferEvent) {
        if (LOG.isInfoEnabled()) {
            LOG.debug("Staring Transmission...");
        }
    }

    @Override
    public void transfer(As2senderTransferEvent as2senderTransferEvent) {
        if (LOG.isInfoEnabled()) {
            if (as2senderTransferEvent.direction == 0) {
                LOG.info(">> Transferred:" + as2senderTransferEvent.bytesTransferred + " bytes");


            } else {
                LOG.info("<< Transferred:" + as2senderTransferEvent.bytesTransferred + " bytes");
            }

            /*if (LOG.isDebugEnabled()) {
                String text = new String(as2senderTransferEvent.text);

                LOG.debug("   >> Data:" + text);
            }  */

            if (LOG.isInfoEnabled()) {
                LOG.info(" Percent Complete:" + as2senderTransferEvent.percentDone);
            }

        }
    }


    // --- Receiver methods ---


    @Override
    public void CEMRequest(As2receiverCEMRequestEvent as2receiverCEMRequestEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Certificate Exchange Message Received:");
            LOG.debug("   > As2From:" + as2receiverCEMRequestEvent.as2From);

            CEMDetailList cemList = receiver.getCEMDetails();
            Iterator<CEMDetail> it = cemList.iterator();

            while (it.hasNext()) {
                LOG.debug("-----");
                CEMDetail detail = it.next();

                LOG.debug(" > StoreType: " + detail.getCertStoreType());
                LOG.debug(" > Subject:" + detail.getCertSubject());
                LOG.debug(" > Issuer:" + detail.getCertIssuer());
                LOG.debug(" > SerialNumber:" + detail.getCertSerialNumber());
                LOG.debug(" > ResponseURL:" + detail.getResponseURL());
                LOG.debug(" > ResponseByDate:" + detail.getRespondByDate());
                LOG.debug(" > CertId:" + detail.getCertId());
            }

        }

    }

    @Override
    public void CEMResponse(As2receiverCEMResponseEvent as2receiverCEMResponseEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Certificate Exchange Message Received:");
            LOG.debug("   > As2From:" + as2receiverCEMResponseEvent.as2From);

            CEMDetailList cemList = receiver.getCEMDetails();
            Iterator<CEMDetail> it = cemList.iterator();

            while (it.hasNext()) {
                LOG.debug("-----");
                CEMDetail detail = it.next();


                LOG.debug(" > Issuer:" + detail.getCertIssuer());
                LOG.debug(" > SerialNumber:" + detail.getCertSerialNumber());
                LOG.debug(" > Accepted:" + detail.getAccepted());
                if (!detail.getAccepted()) ;
                LOG.debug(" > Rejection Reason:" + detail.getRejectionReason());

            }

        }

    }


    @Override
    public void error(As2receiverErrorEvent error) {
        if (LOG.isErrorEnabled()) {
            LOG.error("Error Occurred during AS2 Receive:" + error.description);
            LOG.error("Error Code:" + error.errorCode);
        }
    }


    @Override
    public void EDIDataInfo(As2receiverEDIDataInfoEvent as2receiverEDIDataInfoEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Name:" + as2receiverEDIDataInfoEvent.name);
            LOG.debug("dataType:" + as2receiverEDIDataInfoEvent.dataType);
        }
    }


    @Override
    public void recipientInfo(As2receiverRecipientInfoEvent var1) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Recipient Info:");
            LOG.debug("issuer:" + var1.issuer);
            LOG.debug("serialNumber:" + var1.serialNumber);
            LOG.debug("encryptionAlgorithm:" + var1.encryptionAlgorithm);

        }
    }

    @Override
    public void signerCertInfo(As2receiverSignerCertInfoEvent var1) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("SigninerCertInfo:");
            LOG.debug("issuer:" + var1.issuer);
            LOG.debug("serialNumber:" + var1.serialNumber);
        }

    }

    @Override
    public void SSLServerAuthentication(As2receiverSSLServerAuthenticationEvent as2receiverSSLServerAuthenticationEvent) {
        if (LOG.isInfoEnabled()) {
            LOG.info("SSL Connection requested");
            LOG.info("   CertIssuer:" + as2receiverSSLServerAuthenticationEvent.certIssuer);
            LOG.info("   CertSubject:" + as2receiverSSLServerAuthenticationEvent.certSubject);
            LOG.info("   CertEncoded:" + as2receiverSSLServerAuthenticationEvent.certEncoded);
        }

        if (as2receiverSSLServerAuthenticationEvent.accept == false) {
            if (LOG.isErrorEnabled()) {
                LOG.error(" The certificate for a As2 Connection with url:" + config.getServerURL() +
                        " could not validate the ssl certificate from Issuer:" +
                        as2receiverSSLServerAuthenticationEvent.certIssuer + " Reason:" +
                        as2receiverSSLServerAuthenticationEvent.status);
            }
        } else {
            if (LOG.isInfoEnabled()) {
                LOG.info(" ... Accepted");
            }
        }
    }

    @Override
    public void SSLStatus(As2receiverSSLStatusEvent as2receiverSSLStatusEvent) {
        if (LOG.isDebugEnabled()) {
            LOG.debug(" SSL Status:" + as2receiverSSLStatusEvent.message);
        }
    }

    // CHECKSTYLE:ON
}