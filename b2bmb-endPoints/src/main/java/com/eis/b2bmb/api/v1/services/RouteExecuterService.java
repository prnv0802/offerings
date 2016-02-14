package com.eis.b2bmb.api.v1.services;

import com.eis.b2bmb.api.v1.model.RouteDef;
import com.eis.b2bmb.api.v1.model.RouteStatus;
import com.eis.core.api.v1.exception.B2BNotAuthenticatedException;
import com.eis.core.api.v1.exception.B2BNotAuthorizedException;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * User: mingardia
 * Date: 2/6/14
 * Time: 3:15 PM
 */
public interface RouteExecuterService {
    /**
     * finds the route in the given refName and dataDomain and pauses the route
     *
     * @param refName    the refName of the route
     * @param domainName the dataDomain of the router
     * @return the routeDef
     * @throws B2BNotAuthenticatedException - the caller is not authenticated
     * @throws B2BTransactionFailed         - the transaction failed due to an exception when executing the action
     * @throws B2BNotAuthorizedException    - the caller does not have the privilege to make this call
     * @throws B2BNotFoundException         - the passed refName / Dataomain combination could not be resolved to an
     * object
     * @throws ValidationException - validation exception
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = B2BTransactionFailed.class)
    RouteDef pause(String refName, String domainName) throws B2BNotAuthenticatedException, B2BTransactionFailed,
            B2BNotAuthorizedException, B2BNotFoundException, ValidationException;

    /**
     * finds the route in the given refName and dataDomain and pauses the route
     *
     * @param refName    the refName of the route
     * @param domainName the dataDomain of the router
     * @return the routeDef
     * @throws B2BNotAuthenticatedException - the caller is not authenticated
     * @throws B2BTransactionFailed         - the transaction failed due to an exception when executing the action
     * @throws B2BNotAuthorizedException    - the caller does not have the privilege to make this call
     * @throws B2BNotFoundException         - the passed refName / Dataomain combination could not be resolved to an
     * object
     * @throws ValidationException - validation exception
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = B2BTransactionFailed.class)
    RouteDef resume(String refName, String domainName) throws B2BNotAuthenticatedException, B2BTransactionFailed,
            B2BNotAuthorizedException, B2BNotFoundException, ValidationException;

    /**
     * finds the route in the given refName and dataDomain and pauses the route
     *
     * @param refName    the refName of the route
     * @param domainName the dataDomain of the router
     * @return the routeDef
     * @throws B2BNotAuthenticatedException - the caller is not authenticated
     * @throws B2BTransactionFailed         - the transaction failed due to an exception when executing the action
     * @throws B2BNotAuthorizedException    - the caller does not have the privilege to make this call
     * @throws B2BNotFoundException         - the passed refName / Dataomain combination could not be resolved to an
     * object
     * @throws ValidationException - validation exception
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = B2BTransactionFailed.class)
    RouteDef stop(String refName, String domainName) throws B2BNotAuthenticatedException, B2BTransactionFailed,
            B2BNotAuthorizedException, B2BNotFoundException, ValidationException;

    /**
     * finds the route in the given refName and dataDomain and pauses the route
     *
     * @param refName    the refName of the route
     * @param domainName the dataDomain of the router
     * @return the routeDef
     * @throws B2BNotAuthenticatedException - the caller is not authenticated
     * @throws B2BTransactionFailed         - the transaction failed due to an exception when executing the action
     * @throws B2BNotAuthorizedException    - the caller does not have the privilege to make this call
     * @throws B2BNotFoundException         - the passed refName / Dataomain combination could not be resolved to an
     * object
     * @throws ValidationException - validation exception
     */
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = B2BTransactionFailed.class)
    RouteDef start(String refName, String domainName) throws B2BNotAuthenticatedException, B2BTransactionFailed,
            B2BNotAuthorizedException, B2BNotFoundException, ValidationException;


    /**
     * Get the status of a given route
     * @param dataDomain the dataDomain of the routes we want status on
     * @param refName the refname of the routes we want status on
     * @return RouteStatus the status of the route
     * @throws com.eis.core.api.v1.exception.B2BTransactionFailed if the transaction fails
     */
    public RouteStatus getRouteStatus(String dataDomain, String refName) throws B2BTransactionFailed;

}
