package com.eis.b2bmb.camel.custom.component;

import com.eis.core.api.v1.dao.NotificationDAO;
import com.eis.core.api.v1.model.NotificationType;
import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.DefaultEndpoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Camel endpoint for Notification
 * @author aaldredge
 */
public class B2bmbNotificationEndpoint extends DefaultEndpoint {

    private static final Logger LOG = LoggerFactory.getLogger(B2bmbNotificationEndpoint.class);
    private NotificationType notificationType;
    private String source;
    private String subject;
    private String body;
    private String domain;
    private NotificationDAO notificationDAO;

    /**
     * Constructor
     * @param endpointUri endpoint uri
     * @param component component
     */
    public B2bmbNotificationEndpoint(String endpointUri, B2bmbNotificationComponent component) {
        super(endpointUri, component);
    }

    @Override
    public Consumer createConsumer(Processor processor) throws Exception {
        throw new UnsupportedOperationException("You cannot retrieve messages from this endpoint");
    }

    @Override
    public Producer createProducer() throws Exception {
        return new B2bmbNotificationProducer(this);
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * Return the notification type
     * @return notification type
     */
    public NotificationType getNotificationType() {
        return notificationType;
    }

    /**
     * Set the notification type
     * @param notificationType notification type
     */
    public void setNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
    }

    /**
     * Get the data domain
     * @return domain data domain
     */
    public String getDomain() {
        return domain;
    }

    /**
     * Set the data domain
     * @param domain data domain
     */
    public void setDomain(String domain) {
        this.domain = domain;
    }

    /**
     * Get the source
     * @return source
     */
    public String getSource() {
        return source;
    }

    /**
     * Set the source
     * @param source source
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Get the subject
     * @return subject subject in uri
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Set the subject
     * @param subject subject in uri
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Set the body
     * @return body
     */
    public String getBody() {
        return body;
    }

    /**
     * Get the body
     * @param body body in uri
     */
    public void setBody(String body) {
        this.body = body;
    }

    /**
     * Get the notificationDAO
     * @return notificationDAO
     */
    public NotificationDAO getNotificationDAO() {
        return notificationDAO;
    }

    /**
     * Set the notificationDAO
     * @param notificationDAO notificationDAO to set
     */
    public void setNotificationDAO(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    @Override
    public String toString() {
        return "B2bmbNotificationEndpoint{" +
                "notificationType=" + notificationType +
                ", source='" + source + '\'' +
                ", subject='" + subject + '\'' +
                ", body='" + body + '\'' +
                ", domain='" + domain + '\'' +
                '}';
    }
}
