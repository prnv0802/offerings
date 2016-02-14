package com.eis.b2bmb.camel.custom.component;

import com.eis.core.api.v1.dao.NotificationDAO;
import com.eis.core.api.v1.model.NotificationType;
import org.apache.camel.Endpoint;
import org.apache.camel.ResolveEndpointFailedException;
import org.apache.camel.impl.DefaultComponent;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * Camel component for Notification
 *
 * @author aaldredge
 */
public class B2bmbNotificationComponent extends DefaultComponent {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbNotificationComponent.class);

    /**
     * Notification DAO - injected by camel
     */
    @Autowired
    protected NotificationDAO notificationDAO;

    @Override
    protected void validateURI(String uri, String path, Map parameters)
            throws ResolveEndpointFailedException {
        super.validateURI(uri, path, parameters);

        if (!path.contains("/")) {
            throw new ResolveEndpointFailedException("Data domain and notificationType must be in the uri. " +
                    "Valid notification types are " + StringUtils.join(NotificationType.values(), " , "));
        }
        boolean validNotificationType = false;
        for (NotificationType type : NotificationType.values()) {
            if (path.toLowerCase().endsWith("/" + type.name().toLowerCase())) {
                validNotificationType = true;
            }
        }
        if (!validNotificationType) {
            throw new ResolveEndpointFailedException("Data domain and notificationType must be in the uri. " +
                    "Valid notification types are " + StringUtils.join(NotificationType.values(), " , "));
        }
    }

    @Override
    protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Creating NotificationEndpoint with uri :: " + uri);
        }
        B2bmbNotificationEndpoint endpoint = new B2bmbNotificationEndpoint(uri, this);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Setting parameters in the properties Parameter Map :: " + parameters);
        }
        setProperties(endpoint, parameters);
        endpoint.setNotificationDAO(notificationDAO);
        endpoint.setDomain(remaining.substring(0, remaining.indexOf('/')));
        endpoint.setNotificationType(NotificationType.fromValue(remaining.substring(remaining.indexOf('/')+1)));
        return endpoint;
    }
}
