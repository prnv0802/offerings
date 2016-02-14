package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.api.v1.dao.EDIProfileDAO;
import com.eis.b2bmb.api.v1.model.EDIProfile;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.b2bmb.camel.custom.util.DocumentTypeHelper;
import com.eis.b2bmb.objectbuilders.JAXBObjectBuilder;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.spring.util.SpringApplicationContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;

/**
 * @author aaldredge
 */
public class InternalObjectBuilderProcessor implements Processor{

    /**
     * EDIProfileDAO
     */
    @Autowired
    protected EDIProfileDAO ediProfileDAO;

    @Override
    public void process(Exchange exchange) throws Exception {
        String toAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class);
        String fromAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class);
        String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
        InputStream message = exchange.getIn().getBody(InputStream.class);

        try {

            String docType = DocumentTypeHelper.getDocumentType(exchange);
            String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);
            EDIProfile ediProfile = ediProfileDAO.getEDIProfile(fromAddress, toAddress, docType);
            if (ediProfile == null) {
                throw new B2BNotFoundException(String.format("There is no EDI setup for %s %s %s",
                        fromAddress, toAddress, docType));
            }
            JAXBObjectBuilder jaxbObjectBuilder = (JAXBObjectBuilder) SpringApplicationContext.getBean(
                    "build" + ediProfile.getRelease() + "-" + ediProfile.getDocumentType() + "ModelObject");
            if (jaxbObjectBuilder == null) {
                throw new B2BNotFoundException(String.format("There is no object builder for setup for the %s-%s",
                        ediProfile.getRelease(), ediProfile.getDocumentType()));
            }
            jaxbObjectBuilder.buildModelObjectFromInputStream(message, dataDomain);
        } finally {
            if(message != null) {
                message.close();
            }
        }
    }
}
