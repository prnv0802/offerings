package com.eis.b2bmb.camel.custom.component;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import com.eis.core.api.v1.dao.NotificationDAO;
import com.eis.core.api.v1.model.Notification;
import com.eis.core.api.v1.model.NotificationObjectType;
import org.apache.camel.Exchange;
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.UUID;

/**
 * Camel producer for Notifications
 *
 * @author aaldredge
 */
public class B2bmbNotificationProducer extends DefaultProducer {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbNotificationProducer.class);

    /**
     * Constructor
     * @param endpoint   endPoint instance
     */
    public B2bmbNotificationProducer(B2bmbNotificationEndpoint endpoint) {
        super(endpoint);
    }

    /**
     * @param exchange created exchange
     * @throws Exception thrown by camel
     */
    public void process(Exchange exchange) throws Exception {
        NotificationDAO notificationDAO = getEndpoint().getNotificationDAO();
        if (notificationDAO == null) {
            throw new IllegalArgumentException("notificationDAO is not being initialized  ");
        }

        Notification notification = new Notification();
        notification.setDataDomain(getEndpoint().getDomain());
        notification.setNotificationDate(Calendar.getInstance().getTime());
        notification.setNotificationType(getEndpoint().getNotificationType());
        notification.setNotificationObjectType(NotificationObjectType.Route);
        notification.setId(String.valueOf(UUID.randomUUID()));
        notification.setRefName(notification.getId());
        if (getEndpoint().getSource() != null) {
            notification.setSource(getEndpoint().getSource());
        } else {
            //default to route name
            notification.setSource(exchange.getFromRouteId());
        }
        if (getEndpoint().getSubject() != null) {
            notification.setHeader(getEndpoint().getSubject());
        } else {
            notification.setHeader(exchange.getIn().getHeader(B2bmbCamelConstants.SUBJECT, String.class));
        }
        if (getEndpoint().getBody() != null) {
            notification.setBody(getEndpoint().getBody());
        } else {
            notification.setBody(exchange.getIn().getHeader(B2bmbCamelConstants.BODY, String.class));
        }
        notificationDAO.save(notification);
    }

    @Override
    public B2bmbNotificationEndpoint getEndpoint() {
        return (B2bmbNotificationEndpoint) super.getEndpoint();
    }
}
