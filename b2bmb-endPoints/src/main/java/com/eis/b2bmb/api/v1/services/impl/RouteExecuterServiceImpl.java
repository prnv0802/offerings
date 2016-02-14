package com.eis.b2bmb.api.v1.services.impl;

import com.eis.b2bmb.api.v1.dao.RouteDefDAO;
import com.eis.b2bmb.api.v1.model.RouteDef;
import com.eis.b2bmb.api.v1.model.RouteStatus;
import com.eis.b2bmb.api.v1.services.RouteExecuterHelper;
import com.eis.b2bmb.api.v1.services.RouteExecuterService;
import com.eis.core.api.v1.exception.B2BNotAuthenticatedException;
import com.eis.core.api.v1.exception.B2BNotAuthorizedException;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BSecurityException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import com.eis.security.multitenancy.model.SecureSession;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author sudhakars
 */
public class RouteExecuterServiceImpl implements RouteExecuterService {

    /**
     * routeExecuterHelper
     */
    @Autowired
    protected RouteExecuterHelper routeExecuterHelper;

    /**
     * routeDefDAO
     */
    @Autowired
    protected RouteDefDAO routeDefDAO;


    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = B2BTransactionFailed.class)
    public RouteDef pause(String refName, String domainName) throws B2BNotAuthenticatedException,
            B2BTransactionFailed, B2BNotAuthorizedException, B2BNotFoundException, ValidationException {
        // first check that the routeDef making the call is authenticated.
        Subject subject = SecurityUtils.getSubject();

        if (routeExecuterHelper == null) {
            throw new IllegalArgumentException("routeExecuterHelper is not injected properly into the service");
        }
        if (refName == null) {
            throw new IllegalArgumentException("refName can not be null");
        }
        if (domainName == null) {
            domainName = SecureSession.getUser().getDataDomain();
        }
        if (!subject.isAuthenticated()) {
            throw new B2BNotAuthenticatedException("Subject is not authenticated, " +
                    "while this call requires an authenticated subject, please authenticate before making this call",
                    B2BSecurityException.REASON_CODE.NOT_AUTHENTICATED);
        }

        RouteDef routeDef = routeDefDAO.getByRefName(refName, domainName);

        if (routeDef != null) {
            if (!subject.isPermitted("RouteDef:Execute:" + routeDef.getDataDomain())) {
                throw new B2BNotAuthorizedException("The Subject:" + subject.getPrincipal() + " does not have the " +
                        "privilege: RouteDef:Execute:" + domainName + " refusing request",
                        B2BSecurityException.REASON_CODE.NOT_AUTHORIZED);
            }
        } else {
            throw new B2BNotFoundException("The route with the refName:" + refName + " in data domain: " + domainName
                    + " was not found");
        }

        //pause only works on services that implement suspendable service
        routeDef = routeExecuterHelper.pause(routeDef);


        return routeDef;

    }


    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = B2BTransactionFailed.class)
    public RouteDef resume(String refName, String domainName) throws B2BNotAuthenticatedException,
            B2BTransactionFailed, B2BNotAuthorizedException, B2BNotFoundException, ValidationException {
        // first check that the routeDef making the call is authenticated.
        Subject subject = SecurityUtils.getSubject();

        if (routeExecuterHelper == null) {
            throw new IllegalArgumentException("routeExecuterHelper is not injected properly into the service");
        }
        if (refName == null) {
            throw new IllegalArgumentException("refName can not be null");
        }
        if (domainName == null) {
            domainName = SecureSession.getUser().getDataDomain();
        }
        if (!subject.isAuthenticated()) {
            throw new B2BNotAuthenticatedException("Subject is not authenticated, " +
                    "while this call requires an authenticated subject, please authenticate before making this call",
                    B2BSecurityException.REASON_CODE.NOT_AUTHENTICATED);
        }

        RouteDef routeDef = routeDefDAO.getByRefName(refName, domainName);

        if (routeDef != null) {
            if (!subject.isPermitted("RouteDef:Execute:" + domainName)) {
                throw new B2BNotAuthorizedException("The Subject:" + subject.getPrincipal() + " does not have the " +
                        "privilege: RouteDef:Execute:" + domainName + " refusing request",
                        B2BSecurityException.REASON_CODE.NOT_AUTHORIZED);
            }
        } else {
            throw new B2BNotFoundException("The route with the refName:" + refName + " in data domain: " + domainName
                    + " was not found");
        }

        routeDef = routeExecuterHelper.resume(routeDef);


        return routeDef;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = B2BTransactionFailed.class)
    public RouteDef stop(String refName, String domainName) throws B2BNotAuthenticatedException,
            B2BTransactionFailed, B2BNotAuthorizedException, B2BNotFoundException, ValidationException {
        // first check that the routeDef making the call is authenticated.
        Subject subject = SecurityUtils.getSubject();

        if (routeExecuterHelper == null) {
            throw new IllegalArgumentException("routeExecuterHelper is not injected properly into the service");
        }
        if (refName == null) {
            throw new IllegalArgumentException("refName can not be null");
        }
        if (domainName == null) {
            domainName = SecureSession.getUser().getDataDomain();
        }

        if (!subject.isAuthenticated()) {
            throw new B2BNotAuthenticatedException("Subject is not authenticated, " +
                    "while this call requires an authenticated subject, please authenticate before making this call",
                    B2BSecurityException.REASON_CODE.NOT_AUTHENTICATED);
        }

        RouteDef routeDef = routeDefDAO.getByRefName(refName, domainName);

        if (routeDef != null) {
            if (!subject.isPermitted("RouteDef:Execute:" + domainName)) {
                throw new B2BNotAuthorizedException("The Subject:" + subject.getPrincipal() + " does not have the " +
                        "privilege: RouteDef:Execute:" + domainName + " refusing request",
                        B2BSecurityException.REASON_CODE.NOT_AUTHORIZED);
            }
        } else {
            throw new B2BNotFoundException("The route with the refName:" + refName + " in data domain: " + domainName
                    + " was not found");
        }

        routeDef = routeExecuterHelper.stop(routeDef);


        return routeDef;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = B2BTransactionFailed.class)
    public RouteDef start(String refName, String domainName) throws B2BNotAuthenticatedException,
            B2BTransactionFailed, B2BNotAuthorizedException, B2BNotFoundException, ValidationException {
        // first check that the routeDef making the call is authenticated.
        Subject subject = SecurityUtils.getSubject();

        if (routeExecuterHelper == null) {
            throw new IllegalArgumentException("routeExecuterHelper is not injected properly into the service");
        }

        if (refName == null) {
            throw new IllegalArgumentException("refName can not be null");
        }

        if (domainName == null) {
            domainName = SecureSession.getUser().getDataDomain();
        }

        if (!subject.isAuthenticated()) {
            throw new B2BNotAuthenticatedException("Subject is not authenticated, " +
                    "while this call requires an authenticated subject, please authenticate before making this call",
                    B2BSecurityException.REASON_CODE.NOT_AUTHENTICATED);
        }

        RouteDef routeDef = routeDefDAO.getByRefName(refName, domainName);

        if (routeDef != null) {
            if (!subject.isPermitted("RouteDef:Execute:" + domainName)) {
                throw new B2BNotAuthorizedException("The Subject:" + subject.getPrincipal() + " does not have the " +
                        "privilege: RouteDef:Execute:" + domainName + " refusing request",
                        B2BSecurityException.REASON_CODE.NOT_AUTHORIZED);
            }
        } else {
            throw new B2BNotFoundException("The route with the refName:" + refName + " in data domain: " + domainName
                    + " was not found");
        }

        try {
            routeDef = routeExecuterHelper.start(routeDef);
        } catch (ValidationException e) {
            throw new B2BTransactionFailed("Unable to Start route due to a validation exception", e);
        }

        return routeDef;
    }


    @Override
    public RouteStatus getRouteStatus(String dataDomain, String refName) throws B2BTransactionFailed {

      return routeExecuterHelper.getRouteStatus(dataDomain, refName);

    }

}
