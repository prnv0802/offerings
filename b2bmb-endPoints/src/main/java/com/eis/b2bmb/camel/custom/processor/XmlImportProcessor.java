package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.b2bmb.objectbuilders.JAXBImporter;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.spring.util.SpringApplicationContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;

import java.io.InputStream;

/**
 * @author aaldredge
 */
public class XmlImportProcessor implements Processor{

    @Override
    public void process(Exchange exchange) throws Exception {
        String fileName = exchange.getIn().getHeader(Exchange.FILE_NAME, String.class);
        String schemaName = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.SCHEMA_NAME, String.class);
        if (fileName == null) {
            fileName = schemaName;
        }
        InputStream message = exchange.getIn().getBody(InputStream.class);

        try {

            String dataDomain = CamelDataDomainHelper.getDataDomainFromExchange(exchange);

            JAXBImporter jaxbImporter = (JAXBImporter) SpringApplicationContext.getBean(
                    schemaName + "Importer");
            if (jaxbImporter == null) {
                throw new B2BNotFoundException(String.format("There is no object builder for setup for the" +
                        schemaName + "Importer"));
            }
            String results = jaxbImporter.buildModelObjectFromInputStream(message, dataDomain);
            exchange.getIn().setBody(results);

            exchange.getIn().setHeader(Exchange.FILE_NAME, fileName + ".results.xml");

        } finally {
            if(message != null) {
                message.close();
            }
        }
    }
}
