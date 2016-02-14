package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.api.v1.dao.EDIProfileDAO;
import com.eis.b2bmb.api.v1.model.MailboxRouter;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.EDIFileProcessorHelper;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.router.MailboxRouterHelper;
import com.eis.spring.util.SpringApplicationContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;

/**
 * Created by Envista Tech on 6/16/2014.
 */
public class ExternalMailRouter implements Processor  {


        @Autowired
        EDIProfileDAO ediProfileDao;

        @Autowired
        MailboxEntryDAO mailboxEntryDao;

        @Override
        public void process(Exchange exchange) throws Exception {
            String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
            InputStream message = exchange.getIn().getBody(InputStream.class);
            MailboxRouterHelper mailboxRouterHelper = (MailboxRouterHelper)
                    SpringApplicationContext.getBean("mailboxRoutingHelper");


            try {
                MailboxRouter mailboxRouter = EDIFileProcessorHelper.processEDIFile(exchange, mailboxRouterHelper,
                        ediProfileDao, message);

                StringBuilder dynamicRoute = new StringBuilder();
                dynamicRoute.append("b2bmbMailBox://");
                dynamicRoute.append(mailboxRouter.getRouterDataDomain());
                dynamicRoute.append("/");
                dynamicRoute.append(mailboxRouter.getEntryMailboxRefName());

                dynamicRoute.append("?to=").append(mailboxRouter.getToInternalEmailAddress());
                dynamicRoute.append("&from=").append(mailboxRouter.getFromInternalEmailAddress());
                dynamicRoute.append("&recordExchangedDocument=true");

                exchange.getIn().setHeader(B2bmbCamelConstants.SUBJECT, buildSubjectFromFile(mailboxRouterHelper));
                exchange.getIn().setHeader(B2bmbCamelConstants.INTERNAL_MAILBOX_ROUTE, dynamicRoute);
                exchange.getIn().setHeader(Exchange.FILE_NAME, mailboxRouter.getDocumentType()
                        + "_" + fileName);
            } finally {
               if(message != null) {
                   message.close();
               }
            }

    }

    private String buildSubjectFromFile(MailboxRouterHelper mailboxRouterHelper) {
        StringBuilder builder = new StringBuilder();

        if(mailboxRouterHelper.getDocumentNumber() != null) {
            builder.append(mailboxRouterHelper.getDocumentNumber() + "|");
        }

        if(mailboxRouterHelper.getInterchangeControlNumber() != null) {
            builder.append(mailboxRouterHelper.getInterchangeControlNumber()+"|");
        }

        if(mailboxRouterHelper.getFunctionalGroupControlNumbers() != null) {
            builder.append(mailboxRouterHelper.getFunctionalGroupControlNumbers());
        }

        return builder.toString();
    }

}
