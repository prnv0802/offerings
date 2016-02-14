package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.api.v1.dao.EDIProfileDAO;
import com.eis.b2bmb.api.v1.exception.EDIProfileNotFoundException;
import com.eis.b2bmb.api.v1.model.EDIProfile;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.b2bmb.camel.custom.util.DocumentTypeHelper;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author aaldredge
 */
public class EdiProfileValidator implements Processor{


    /**
     * edi profile dao to get edi profile
     */
    @Autowired
    protected EDIProfileDAO ediProfileDAO;

    @Override
    public void process(Exchange exchange) throws Exception {
        String toAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class);
        String fromAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class);
        EDIProfile ediProfile = null;

        String docType = DocumentTypeHelper.getDocumentType(exchange);
        String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);
        try {
            ediProfile = ediProfileDAO.getEDIProfile(fromAddress, toAddress, docType);
        } catch (B2BNotFoundException nfe) {
           throw new EDIProfileNotFoundException(String.format("There is no EDI setup for %s %s %s",
                    fromAddress, toAddress, docType), nfe);
        }  catch (B2BTransactionFailed nfe) {
            throw new EDIProfileNotFoundException(String.format("There is no EDI setup for %s %s %s",
                fromAddress, toAddress, docType), nfe);
        }

        exchange.getIn().setHeader(B2bmbCamelConstants.EDI_PROFILE, ediProfile);
        exchange.getIn().setHeader(B2bmbCamelConstants.DOCUMENT_TYPE, docType);
        StringBuilder dynamicRoute = new StringBuilder();
        dynamicRoute.append("b2bmbValidator://"+dataDomain+"/schemas/");
        dynamicRoute.append(ediProfile.getAxSchemaFileName());
        exchange.getIn().setHeader(B2bmbCamelConstants.DYNAMIC_SCHEMA_PATH, dynamicRoute);
    }

}
