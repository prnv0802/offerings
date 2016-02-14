package com.eis.b2bmb.camel.custom.processor;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import java.io.InputStream;

/**
 * Created by Envista Tech on 6/16/2014.
 */
public class ConvertPDFtoTextProcessor implements Processor  {



        @Override
        public void process(Exchange exchange) throws Exception {
            String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
            InputStream message = exchange.getIn().getBody(InputStream.class);

            try {
                PDFTextStripper pdfTextStripper = new PDFTextStripper();
                PDDocument pdDocument = PDDocument.load(message);
                String text = pdfTextStripper.getText(pdDocument);
                exchange.getIn().setBody(IOUtils.toInputStream(text));
            } finally {
               if(message != null) {
                   message.close();
               }
            }

    }

}
