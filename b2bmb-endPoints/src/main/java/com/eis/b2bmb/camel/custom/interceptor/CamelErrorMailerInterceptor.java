package com.eis.b2bmb.camel.custom.interceptor;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.core.common.NotifyAndCreateTaskHelper;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.spi.InterceptStrategy;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.UUID;

/**
 * @author aaldredge
 */
public class CamelErrorMailerInterceptor implements InterceptStrategy{

    private static final Logger LOG = LoggerFactory.getLogger(CamelErrorMailerInterceptor.class);

    @Autowired
    NotifyAndCreateTaskHelper taskHelper;

    @Override
    public Processor wrapProcessorInInterceptors(CamelContext camelContext,
                                                 final ProcessorDefinition<?> processorDefinition,
                                                 final Processor target,
                                                 final Processor nextTarget) throws Exception {

        return new DelegateAsyncProcessor(new Processor() {
            @Override
            public void process(Exchange exchange) throws Exception {

                target.process(exchange);

                String transmissionId = exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID, String.class);

                if (exchange.getException() != null) {

                    StringBuilder builder = new StringBuilder();
                    builder.append("Route:" + processorDefinition.toString());
                    builder.append("\nTransmission Id:" + transmissionId);
                    builder.append("\nException Message:" + exchange.getException().getMessage());
                    if (exchange.getException().getCause() != null) {
                        builder.append("\nException Cause:" + exchange.getException().getCause().getMessage());
                    }
                    builder.append("\nException Trace\n:");
                    for (StackTraceElement ste : exchange.getException().getStackTrace()) {
                        builder.append(ste.toString() + "\n");
                    }
                    if (exchange.getIn().getHeader(Exchange.FILE_LENGTH) != null) {
                        long fileLength = exchange.getIn().getHeader(Exchange.FILE_LENGTH, Long.class);
                        if (fileLength < 10000) {
                            InputStream fileContents = exchange.getIn().getBody(InputStream.class);
                            if (fileContents != null) {
                                builder.append("\nFile Contents:\n" + IOUtils.toString(fileContents));
                            }
                        }
                        else {
                            builder.append("\nFile Contents to long to include:" + fileLength +"\n");
                        }
                    }

                    String subject = "Route Exception in Route:" + processorDefinition.toString();
                    String body = builder.toString();

                    taskHelper.notifyAndCreateTask("Error:"+String.valueOf(UUID.randomUUID()), subject,
                            body,subject, CamelDataDomainHelper.getDataDomainFromExchange(exchange),
                            "System Error", "exceptions", "Tony Costanzo");

                }

            }

        });
    }
}