package com.eis.b2bmb.camel.custom.notifier;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import org.apache.camel.management.event.ExchangeSendingEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EventObject;

/**
 * Log exchange sending events - only fires on endpoints
 * Less verbose than the OOB LoggingEventNotifier which prints out everything including the body of the exchange
 * @author aaldredge
 */
public class CamelExchangeSendingLogger extends EventNotifierSupport {

    private static final Logger LOG = LoggerFactory.getLogger(CamelExchangeSendingLogger.class);

    @Override
    public void notify(EventObject eventObject) throws Exception {
        ExchangeSendingEvent event = (ExchangeSendingEvent) eventObject;

        if (LOG.isInfoEnabled()) {
            LOG.info("Sending to endpoint " + event.getEndpoint().getEndpointUri() +
                    " via route " + event.getExchange().getFromRouteId());
            LOG.info("Headers: " + event.getExchange().getIn().getHeaders().toString());
            LOG.info("TransmissionId " +
                    String.valueOf(event.getExchange().getProperty(B2bmbCamelConstants.TRANSMISSION_ID)));
        }
    }

    @Override
    public boolean isEnabled(EventObject eventObject) {
        return eventObject instanceof ExchangeSendingEvent;
    }
}
