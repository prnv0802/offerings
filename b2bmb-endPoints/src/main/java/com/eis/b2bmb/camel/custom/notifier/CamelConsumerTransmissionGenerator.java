package com.eis.b2bmb.camel.custom.notifier;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.core.api.v1.dao.TransmissionDAO;
import com.eis.core.api.v1.dao.TransmissionEventDAO;
import com.eis.core.api.v1.model.DynamicAttribute;
import com.eis.core.api.v1.model.DynamicAttributeSet;
import com.eis.core.api.v1.model.DynamicAttributeType;
import com.eis.core.api.v1.model.Transmission;
import com.eis.core.api.v1.model.TransmissionDirection;
import com.eis.core.api.v1.model.TransmissionEvent;
import com.eis.core.api.v1.model.TransmissionStatus;
import org.apache.camel.Exchange;
import org.apache.camel.management.event.ExchangeCreatedEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Calendar;
import java.util.EventObject;
import java.util.Map;

/**
 * Make transmissions for exchange created events - only fires on from endpoints
 * Augments CamelTransmissionInterceptor which creates transmission events on processors.
 * @see com.eis.b2bmb.camel.custom.interceptor.CamelTransmissionInterceptor
 * @author aaldredge
 */
public class CamelConsumerTransmissionGenerator extends EventNotifierSupport {

    @Autowired
    private TransmissionDAO transmissionDAO;

    @Autowired
    private TransmissionEventDAO transmissionEventDAO;

    private static final Logger LOG = LoggerFactory.getLogger(CamelConsumerTransmissionGenerator.class);

    @Override
    public void notify(EventObject eventObject) throws Exception {
        ExchangeCreatedEvent event = (ExchangeCreatedEvent) eventObject;
        Exchange exchange = event.getExchange();

        /**
         * Bypassing saving transmission for spring-ws calls as the exchanges are created on the fly
         * when these webservices are called
         */
        if(exchange != null && exchange.getFromEndpoint() != null
                && exchange.getFromEndpoint().getEndpointUri() != null
                && exchange.getFromEndpoint().getEndpointUri().startsWith("spring-ws")) {
            return;
        }

        try {
            String transmissionId = exchange.getProperty(B2bmbCamelConstants.TRANSMISSION_ID, String.class);
            Transmission transmission = null;
            if (transmissionId != null) {
                transmission = transmissionDAO.getById(transmissionId);
            } else if(exchange.getIn().getHeader(B2bmbCamelConstants.TRANSMISSION_ID) != null) {
                transmissionId = exchange.getIn().getHeader(B2bmbCamelConstants.TRANSMISSION_ID, String.class);
                transmission = transmissionDAO.getById(transmissionId);
            }
            if (transmission == null) {
                transmission = new Transmission();
                //this would be very weird but maybe you preset the id in the route so supporting
                if (transmissionId != null) {
                    transmission.setId(transmissionId);
                }
                transmission.setDataDomain(CamelDataDomainHelper.getDataDomainFromExchange(exchange));
                transmission.setDirection(TransmissionDirection.INBOUND);
                transmission.setStartDateTime(Calendar.getInstance().getTime());
                if (exchange.getIn().getHeader(Exchange.FILE_NAME) != null) {
                    transmission.setFileNames(exchange.getIn().getHeader(Exchange.FILE_NAME, String.class));
                }
                transmission.setTransmissionObjectId(exchange.getExchangeId());
                transmission.setTransmissionObjectIdType("Route Contents");
                transmission.setStatus(TransmissionStatus.ROUTED);
                transmission.setFromUser("Route:" + exchange.getFromRouteId());
                transmission = transmissionDAO.save(transmission);
                transmissionId = transmission.getId();
                exchange.setProperty(B2bmbCamelConstants.TRANSMISSION_ID, transmissionId);
                exchange.getIn().setHeader(B2bmbCamelConstants.TRANSMISSION_ID, transmissionId);
            }

            if(transmission.getDataDomain() != null &&
                    !transmission.getDataDomain().equals(
                    CamelDataDomainHelper.getDataDomainFromExchange(exchange))) {
                transmission.getDataDomains().add(CamelDataDomainHelper.getDataDomainFromExchange(exchange));
            } else if (transmission.getDataDomain() == null) {
                transmission.getDataDomains().add(CamelDataDomainHelper.getDataDomainFromExchange(exchange));
            }
            TransmissionEvent transmissionEvent = new TransmissionEvent();
            transmissionEvent.setStartDateTime(Calendar.getInstance().getTime());
            transmissionEvent.setTimestamp(System.nanoTime());

            transmissionEvent.setDataDomain(transmission.getDataDomain());
            if(transmissionEvent.getDataDomain() != null && !transmissionEvent.getDataDomain().equals(
                    CamelDataDomainHelper.getDataDomainFromExchange(exchange))) {
                transmissionEvent.getDataDomains().add(
                        CamelDataDomainHelper.getDataDomainFromExchange(exchange));
            } else if(transmissionEvent.getDataDomain() == null) {
                transmissionEvent.getDataDomains().add(
                        CamelDataDomainHelper.getDataDomainFromExchange(exchange));
            }

            transmissionEvent.setTransmissionId(transmissionId);
            DynamicAttributeSet das = new DynamicAttributeSet();

            if(exchange.getIn().getHeader(B2bmbCamelConstants.REFERENCE_DATA, Map.class) != null) {
                Map<String, Object> referenceData =    (Map<String, Object>) exchange.getIn().getHeader(
                        B2bmbCamelConstants.REFERENCE_DATA, Map.class);
                transmissionEvent.getReferenceData().putAll(referenceData);
                if(transmission.getReferenceData() != null && transmission.getReferenceData().size() > 0) {
                    transmission.getReferenceData().putAll(referenceData);
                    transmissionDAO.save(transmission);
                }
            }

            for (String headerKey : exchange.getIn().getHeaders().keySet()) {
                if(!headerKey.equals(B2bmbCamelConstants.REFERENCE_DATA)) {
                    DynamicAttribute headerAttribute = new DynamicAttribute();
                    headerAttribute.setType(DynamicAttributeType.Text);
                    headerAttribute.setLabel(headerKey + ":");
                    headerAttribute.setRequired(false);
                    headerAttribute.setRefName(headerKey);
                    headerAttribute.setValue(String.valueOf(exchange.getIn().getHeader(headerKey)));
                    das.getAttributes().put(headerKey, headerAttribute);
                }
            }
            //probably won't ever be null but just in case
            if (exchange.getFromRouteId() != null){
                DynamicAttribute route = new DynamicAttribute();
                route.setType(DynamicAttributeType.Text);
                route.setLabel("Route:");
                route.setRequired(false);
                route.setRefName("route");
                route.setValue(exchange.getFromRouteId());
                das.getAttributes().put("route", route);
            }

            transmissionEvent.getAttributes().getAttributes().putAll(das.getAttributes());
            transmissionEvent.setEventType("Route from: " + exchange.getFromEndpoint().getEndpointUri());
            transmissionEventDAO.save(transmissionEvent);
//CHECKSTYLE:OFF
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to log transmission event on exchange :" + exchange.getExchangeId() + " route " + exchange.getFromRouteId(), e);
            }
        }
//CHECKSTYLE:ON

    }

    @Override
    public boolean isEnabled(EventObject eventObject) {
        return eventObject instanceof ExchangeCreatedEvent;
    }
}
