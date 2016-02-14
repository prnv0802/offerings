package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.DocumentTypeHelper;
import com.eis.core.router.EnvelopingService;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;

/**
 * Processor that envelopes the XML with values from the EDI Profile so it can
 * be converted to edi.
 */
public class XmlEdiEnvelopeProcessor implements Processor{

    @Autowired
    EnvelopingService envelopingService;

    @Override
    public void process(Exchange exchange) throws Exception {
        String toAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class);
        String fromAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class);
        String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
        String documentType = DocumentTypeHelper.getDocumentType(exchange);
        InputStream newBodyStream = envelopingService.envelopeMessage(toAddress, fromAddress,
                fileName, documentType, exchange.getIn().getBody(InputStream.class));
        exchange.getIn().setBody(newBodyStream, InputStream.class);
    }
}
