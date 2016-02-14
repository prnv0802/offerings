package com.eis.b2bmb.camel.custom.processor;

import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.core.common.NotifyAndCreateTaskHelper;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.UUID;

/**
 * Error handler which captures errors that occur in the camel routes.
 */
public class B2BMBErrorHandler  implements Processor {

    @Autowired
    NotifyAndCreateTaskHelper taskHelper;

    @Override
    public void process(Exchange exchange) throws Exception {
        // the caused by exception is stored in a property on the exchange
        Throwable caused = exchange.getProperty(Exchange.EXCEPTION_CAUGHT, Throwable.class);
        String subject = "B2B Exception:"+caused.getMessage();
        StringBuilder builder = new StringBuilder();
        builder.append("\nException Trace\n:");
        for (StackTraceElement ste : exchange.getException().getStackTrace()) {
            builder.append(ste.toString() + "\n");
        }
        String body = builder.toString();
        taskHelper.notifyAndCreateTask("Error:"+String.valueOf(UUID.randomUUID()),  subject,
                body, subject, CamelDataDomainHelper.getDataDomainFromExchange(exchange),
                "System Error", "exceptions", "Tony Costanzo");
    }

}
