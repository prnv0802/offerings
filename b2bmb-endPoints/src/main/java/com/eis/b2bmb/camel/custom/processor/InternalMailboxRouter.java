package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.api.v1.dao.EDIProfileDAO;
import com.eis.b2bmb.api.v1.model.MailboxRouter;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.DocumentTypeHelper;
import com.eis.b2bmb.camel.custom.util.EDIFileProcessorHelper;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.router.MailboxRouterHelper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.UUID;

/**
 * Created by Envista Tech on 6/16/2014.
 */
public class InternalMailboxRouter implements Processor {

    @Autowired
    MailboxRouterHelper mailboxRouterHelper;

    @Autowired
    EDIProfileDAO ediProfileDao;

    @Override
    public void process(Exchange exchange) throws Exception {

        String toAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.TO, String.class);
        String fromAddress = ExchangeHelper.getMandatoryHeader(exchange, B2bmbCamelConstants.FROM, String.class);
        String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
        InputStream message = exchange.getIn().getBody(InputStream.class);
        String docType = DocumentTypeHelper.getDocumentType(exchange);

        if (docType == null) {
            throw new IllegalStateException("document can not be null. Unable to get MailboxRouter for to:"+
                    toAddress+", from:"+fromAddress);
        }

        MailboxRouter mailboxRouter = mailboxRouterHelper.getMailboxRouterFromInternalAddresses(fromAddress,
                toAddress, docType);

        if (mailboxRouter == null) {
            throw new B2BNotFoundException(String.format("There is no MailboxRouter for from:%s to:%s docType:%s",
                    fromAddress, toAddress, docType));
        }

        // assumes extension is upper ?
        if (fileName != null && (fileName.toUpperCase().endsWith("EDI") || fileName.toUpperCase().endsWith("X12"))) {
            EDIFileProcessorHelper.processEDIFile(exchange, mailboxRouterHelper,
                    ediProfileDao, message);
        }

        if (mailboxRouter.getDocumentType() != null) {
            exchange.getIn().setHeader(B2bmbCamelConstants.DOCUMENT_TYPE,
                    mailboxRouter.getDocumentType());
        }


        StringBuilder dynamicRoute = new StringBuilder();
        dynamicRoute.append("b2bmbMailBox://");
        dynamicRoute.append(mailboxRouter.getRouterDataDomain());
        dynamicRoute.append("/");
        dynamicRoute.append(mailboxRouter.getEntryMailboxRefName());
        dynamicRoute.append("?to=").append(mailboxRouter.getToInternalEmailAddress());
        dynamicRoute.append("&from=").append(mailboxRouter.getFromInternalEmailAddress());
        dynamicRoute.append("&recordExchangedDocument=true");
        exchange.getIn().setHeader(B2bmbCamelConstants.INTERNAL_MAILBOX_ROUTE, dynamicRoute);
        exchange.getIn().setHeader(Exchange.FILE_NAME, UUID.randomUUID()
                + "_" + fileName);
    }
}
