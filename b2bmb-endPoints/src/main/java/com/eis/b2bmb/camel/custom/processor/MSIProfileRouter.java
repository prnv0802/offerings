package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.api.v1.dao.MSIProfileDAO;
import com.eis.b2bmb.api.v1.model.MSIProfile;
import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.core.api.v1.dao.MailboxEntryDAO;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.router.MSIRouter;
import com.eis.spring.util.SpringApplicationContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.util.ExchangeHelper;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;

/**
 * Created by Envista Tech on 6/16/2014.
 */
public class MSIProfileRouter implements Processor  {


        @Autowired
        MSIProfileDAO msiProfileDao;

        @Autowired
        MailboxEntryDAO mailboxEntryDao;

        @Override
        public void process(Exchange exchange) throws Exception {
            String fileName = ExchangeHelper.getMandatoryHeader(exchange, Exchange.FILE_NAME, String.class);
            InputStream message = exchange.getIn().getBody(InputStream.class);
            MSIRouter msiRouter = (MSIRouter)
                    SpringApplicationContext.getBean("msiRouter");

            try {
                MSIProfile msiProfile = msiRouter.getMSIProfileFromFile(message,
                        fileName);

                if(msiProfile == null) {
                    throw new B2BNotFoundException("No MSI Profile has been set up for sender, receiver " +
                            "and document type that was passed in.  Check the original document that was passed in "+
                            "and make sure the MSI Profile is setup");
                }


                if (msiRouter.getDocumentNumber() != null) {
                    exchange.getIn().setHeader(B2bmbCamelConstants.DOCUMENT_TYPE,
                            msiRouter.getDocumentNumber());
                }

                exchange.getIn().setHeader("MSI_PROFILE", msiProfile);
                exchange.getIn().setHeader(B2bmbCamelConstants.SUBJECT, buildSubjectFromFile(msiRouter));
                exchange.getIn().setHeader(Exchange.FILE_NAME, msiRouter.getDocumentNumber()
                        + "_" + fileName);
            } finally {
               if(message != null) {
                   message.close();
               }
            }

    }

    private String buildSubjectFromFile(MSIRouter msiRouter) {
        StringBuilder builder = new StringBuilder();

        if(msiRouter.getDocumentNumber() != null) {
            builder.append(msiRouter.getDocumentNumber() + "|");
        }

        if(msiRouter.getInterchangeControlNumber() != null) {
            builder.append(msiRouter.getInterchangeControlNumber()+"|");
        }

        if(msiRouter.getFunctionalGroupControlNumbers() != null) {
            builder.append(msiRouter.getFunctionalGroupControlNumbers());
        }

        return builder.toString();
    }

}
