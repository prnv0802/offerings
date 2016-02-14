package com.eis.b2bmb.api.v1.services;

import com.eis.b2bmb.api.v1.model.EmailMessage;
import com.eis.b2bmb.api.v1.model.RouteDef;
import com.eis.b2bmb.api.v1.model.RouteStatus;
import com.eis.core.api.v1.exception.*;
import com.eis.core.api.v1.model.UserProfile;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;

/**
 * @author Nagaraj T
 */
public class EmailMessageService {
    private static final Logger LOG = LoggerFactory.getLogger(EmailMessageService.class);

    @Autowired
    JmsTemplate emailSendingJmsTemplate;

    @Autowired
    RouteExecuterService routeExecuterService;

    @Autowired
    RouteDefService routeDefService;

    /**
     *
     * @param emailMessage  - email message bean
     * @return boolean
     */
    protected boolean validateEmailMessageData(EmailMessage emailMessage) {
        boolean validData = true;

        String serverName = emailMessage.getServerName();
        String serverPort = emailMessage.getPort();
        String userName = emailMessage.getUserName();
        String password = emailMessage.getPassword();

        String toAddress = emailMessage.getToAddress();
        String fromAddress = emailMessage.getFromAddress();
        String routeId = emailMessage.getRouteId();

        if(serverName == null || serverName.isEmpty()) {
            validData = false;
            throw new IllegalArgumentException("Server Name is required");
        }

        if(serverPort == null || serverPort.isEmpty()) {
            validData = false;
            throw new IllegalArgumentException("Server Port is required");
        }

        if(userName == null || userName.isEmpty()) {
            validData = false;
            throw new IllegalArgumentException("User Name  is required");
        }

        if(password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Password is required");
        }

        if(toAddress == null || toAddress.isEmpty()) {
            validData = false;
            throw new IllegalArgumentException("To Address is required");
        }

        if(fromAddress == null || fromAddress.isEmpty()) {
            validData = false;
            throw new IllegalArgumentException("From Address is required");
        }

        if(routeId == null || routeId.isEmpty()) {
            validData = false;
            throw new IllegalArgumentException("Route Id is required");
        }
       return validData;
    }

    /**
     *
     * @param emailMessage -     email message bean
     * @throws B2BNotFoundException  - B2BNotFoundException
     * @throws B2BTransactionFailed   - B2BTransactionFailed
     * @throws B2BNotAuthenticatedException  - B2BNotAuthenticatedException
     * @throws B2BNotAuthorizedException   - B2BNotAuthorizedException
     * @throws ValidationException  - ValidationException
     * @throws InterruptedException  - InterruptedException
     */
    protected void sendEmail(EmailMessage emailMessage)
            throws B2BNotAuthorizedException, B2BNotFoundException, B2BTransactionFailed,
            B2BNotAuthenticatedException, ValidationException, InterruptedException {

        // validate email message input data
        boolean validData = validateEmailMessageData(emailMessage);

        if (validData) {
            Subject subject = SecurityUtils.getSubject();
            String dataDomain = ((UserProfile) subject.getSession().getAttribute("userProfile")).getDataDomain();
            emailMessage.setDataDomain(dataDomain);

            String routeId = emailMessage.getRouteId();
            RouteDef route =  routeDefService.getByRefName(routeId);

            if (LOG.isInfoEnabled()) {
                LOG.info("routeId "+routeId);
                LOG.info("route "+route);
            }
            if(route != null) {
                // check for status of route
                RouteStatus routeStatus = routeExecuterService.getRouteStatus(dataDomain, route.getRefName());
                if(routeStatus != RouteStatus.STARTED) {
                    routeExecuterService.start(route.getRefName(), dataDomain);
                }
                Thread.sleep(1000);
                emailSendingJmsTemplate.convertAndSend("send.email.queue", emailMessage);
            }
        }
    }
}
