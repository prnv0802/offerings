package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.api.v1.dao.EDIProfileDAO;
import com.eis.b2bmb.api.v1.dao.MSIProfileDAO;
import com.eis.b2bmb.api.v1.exception.EDIProfileNotFoundException;
import com.eis.b2bmb.api.v1.model.B2BProfile;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.b2bmb.camel.custom.util.DocumentTypeHelper;
import com.eis.b2bmb.util.TransmissionRecorder;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Looks at a B2B Profile to determine what directory the file in the Exchange should be placed to that it will
 * be picked up by a Communication Configuration and sent out.  This class just determines the directory and adds
 * it to the Camel Headers in the Header B2BDirectoryName.  It is the responsibility of the next route to use
 * this header to determine what to do. In order to use this processor you must specify the following headers:
 *   - TradingPartnerDirection - FROM or TO - this option is used to determine which directory from the B2B Profile
 *     will be set in the B2BDirectoryName header.
 *   - B2BProfileType - MSI or EDI - this option is used to determine which type of profile will be looked up.
 *
 * These headers are required and an exception will be thrown if they are not set or if one the values is not
 * recognized.
 */
public class ExternalCommunicationSender implements Processor  {

    private static final Logger LOG = LoggerFactory.getLogger(ExternalCommunicationSender.class);

    /**
     * edi profile dao to get edi profile
     */
    @Autowired
    protected EDIProfileDAO ediProfileDAO;

    /**
     * msi profile dao to get msi profile
     */
    @Autowired
    protected MSIProfileDAO msiProfileDAO;


    /**
     * transmission recorder
     */
    @Autowired
    protected TransmissionRecorder transmissionRecorder;

    @Override
    public void process(Exchange exchange) throws Exception {
        String toAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class);
        String fromAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class);
        String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
        String tradingPartnerDirection = ExchangeHelper.getMandatoryHeader(exchange,
                B2bmbCamelConstants.TRADING_PARTNER_DIRECTION, String.class);
        String b2bProfileType = ExchangeHelper.getMandatoryHeader(exchange,
                B2bmbCamelConstants.B2B_PROFILE_TYPE, String.class);
        B2BProfile b2bProfile = null;

        String docType = DocumentTypeHelper.getDocumentType(exchange);
        String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);
        try {
            if(B2bmbCamelConstants.B2B_PROFILE_TYPE_EDI.equals(b2bProfileType)) {
                b2bProfile = ediProfileDAO.getEDIProfile(fromAddress, toAddress, docType);
            } else if(B2bmbCamelConstants.B2B_PROFILE_TYPE_MSI.equals(b2bProfileType))  {
                b2bProfile = msiProfileDAO.getMSIProfileFromInternal(fromAddress, toAddress, docType);
            } else {
                if(LOG.isErrorEnabled()) {
                    LOG.error("An unrecognized header value:"+b2bProfileType +" for the camel header: B2BProfileType" +
                            "was passed in.");
                }

                throw new EDIProfileNotFoundException("An unrecognized header value:"+b2bProfileType +" " +
                        "for the camel header: B2BProfileType" +
                        "was passed in.");
            }

        } catch (B2BNotFoundException nfe) {
            throw new EDIProfileNotFoundException(String.format("There is no EDI Profile setup for %s %s %s",
                    fromAddress, toAddress, docType), nfe);
        }  catch (B2BTransactionFailed nfe) {
            throw new EDIProfileNotFoundException(String.format("There is no EDI setup for %s %s %s",
                    fromAddress, toAddress, docType), nfe);
        }
        String outDirectoryName = null;
        if(B2bmbCamelConstants.TRADING_PARTNER_DIRECTION_TO.equals(tradingPartnerDirection))  {
            outDirectoryName = b2bProfile.getToVendorOutDir();
        } else if (B2bmbCamelConstants.TRADING_PARTNER_DIRECTION_FROM.equals(tradingPartnerDirection)) {
            outDirectoryName = b2bProfile.getFromVendorOutDir();
        } else {
            if(LOG.isErrorEnabled()) {
                LOG.error("An unrecognized header value:"+tradingPartnerDirection +" for the camel" +
                        " header: TradingPartnerDirection was passed in.");
            }

            throw new IllegalStateException("An unrecognized header value:"+b2bProfileType +" " +
                    "for the camel header: B2BProfileType" +
                    "was passed in.");
        }
        exchange.getIn().setHeader(B2bmbCamelConstants.B2B_DIRECTORY_NAME, outDirectoryName);
    }

}




