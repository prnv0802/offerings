package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.api.v1.dao.EDIProfileDAO;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.core.common.NotifyAndCreateTaskHelper;
import com.eis.core.router.Edi997Checker;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.Date;

/**
 * Created by Envista Tech on 6/16/2014.
 */
public class Edi997DocumentChecker implements Processor  {

        private static final Logger LOG = LoggerFactory.getLogger(Edi997DocumentChecker.class);

        @Autowired
        EDIProfileDAO ediProfileDAO;

        @Autowired
        NotifyAndCreateTaskHelper taskHelper;

        @Override
        public void process(Exchange exchange) throws Exception {
            String toAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class);
            String fromAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class);
            InputStream message = exchange.getIn().getBody(InputStream.class);
            try {

                Edi997Checker checker = new Edi997Checker();
                boolean isRejected = checker.wasDocumentRejected(message);
                String interchangeControlNumber = checker.getInterchangeControlNumber();
                String transmissionId = exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID, String.class);

                if (isRejected) {

                    StringBuilder builder = new StringBuilder();
                    builder.append("A 997 has been processed that indicates one or more documents has been rejected " +
                            "from:" + fromAddress + ", to:" + toAddress + "\n");
                    builder.append("Please check the '997-Rejections' mailbox to see the 997 and follow the " +
                            "process per the trading partner to resolve the issue.  Some of the " +
                            "documents may have been processed.");
                    builder.append("\n\nTransmission Id:" + transmissionId);
                    builder.append("\n\nInterchange Control Number:" + interchangeControlNumber);
                    message = exchange.getIn().getBody(InputStream.class);
                    if (message != null) {
                        builder.append("\n997 Contents:\n" + IOUtils.toString(message));
                    }
                    String subject = "Document was rejected.";
                    String body = builder.toString();

                    taskHelper.notifyAndCreateTask("997-"+interchangeControlNumber+new Date(), subject,
                            body,subject, CamelDataDomainHelper.getDataDomainFromExchange(exchange),
                            "997 Rejection", "fa-rejections", "Lindsay Karlowsky");

                    exchange.getIn().setHeader(B2bmbCamelConstants.FUNCT_ACK_REJECTED_INDICATOR, "Y");
                } else {
                    exchange.getIn().setHeader(B2bmbCamelConstants.FUNCT_ACK_REJECTED_INDICATOR, "N");
                }
            } finally {
               if(message != null) {
                   message.close();
               }
            }
    }

}
