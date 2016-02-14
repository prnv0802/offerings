package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by Envista Tech on 6/16/2014.
 */
public class DetermineFileTypeProcessor implements Processor  {



        @Override
        public void process(Exchange exchange) throws Exception {
            String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
            InputStream message = exchange.getIn().getBody(InputStream.class);

            try {
                String fileType = null;
                InputStreamReader is = new InputStreamReader(message);
                StringBuilder sb=new StringBuilder();
                BufferedReader br = new BufferedReader(is);
                String read = br.readLine();
                if(read.startsWith("ISA")) {
                    fileType = "X12";
                } else if (read.startsWith("UNB")) {
                    fileType = "EDIFACT";
                }

                exchange.getIn().setHeader(B2bmbCamelConstants.FILE_TYPE, fileType);
            } finally {
               if(message != null) {
                   message.close();
               }
            }

    }

}
