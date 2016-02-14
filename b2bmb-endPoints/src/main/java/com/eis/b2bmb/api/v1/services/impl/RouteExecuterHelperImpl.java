package com.eis.b2bmb.api.v1.services.impl;

import com.eis.b2bmb.api.v1.model.RouteDef;
import com.eis.b2bmb.api.v1.model.RouteStatus;
import com.eis.b2bmb.api.v1.services.RouteExecuterHelper;
import com.eis.b2bmb.camel.custom.util.CamelDataDomainHelper;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import org.apache.camel.Route;
import org.apache.camel.ServiceStatus;
import org.apache.camel.model.Constants;
import org.apache.camel.model.ModelCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.camel.model.RoutesDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * @author sudhakars
 *         Date: 31 Jan 2014
 *         This class is responsible for executing the route definition.
 */
public class RouteExecuterHelperImpl implements RouteExecuterHelper {

    private static final Logger LOG = LoggerFactory.getLogger(RouteExecuterHelperImpl.class);

    /**
     * camelContext - camelContext object from SpringContext
     */
    @Autowired
    protected ModelCamelContext camelContext;


    private RoutesDefinition parseRouteDefinition(RouteDef routeDef) throws ValidationException {
        RoutesDefinition routesDefinition = null;
        String xml = routeDef.getRouteDefinition();

        try {
            JAXBContext jaxb = JAXBContext.newInstance(Constants.JAXB_CONTEXT_PACKAGES);
            Unmarshaller unmarshaller = jaxb.createUnmarshaller();
            InputStream is = new ByteArrayInputStream(xml.getBytes());
            routesDefinition = (RoutesDefinition) unmarshaller.unmarshal(is);
        } catch (JAXBException e) {
            throw new ValidationException("Illegal route definition found with refName: " + routeDef.getRefName() + "" +
                    " data domain:" + routeDef.getDataDomain() + " Caused by msg:" + e.getMessage() + " route " +
                    "definition:" + xml, e);
        }
        return routesDefinition;
    }

    @Override
    public RouteDef pause(RouteDef routeDef) throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        try {
            //currently not supporting multiple route per routes on save so should be just one
            RoutesDefinition routesDefinition = parseRouteDefinition(routeDef);
            for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
                camelContext.suspendRoute(routeDefinition.getId());
            }
        }
//CHECKSTYLE:OFF
        catch (Exception e) {
//CHECKSTYLE:ON
            if (LOG.isDebugEnabled()) {
                LOG.debug("Route pausing failed refName :: " + routeDef.getRefName());
            }
            throw new B2BTransactionFailed("Exception while pausing the route" + routeDef.getRefName(), e);
        }

        routeDef.setRouteStatus(RouteStatus.PAUSED);
        return routeDef;
    }

    @Override
    public RouteDef resume(RouteDef routeDef) throws B2BTransactionFailed, B2BNotFoundException, ValidationException {


        try {
            //currently not supporting multiple route per routes on save so should be just one
            RoutesDefinition routesDefinition = parseRouteDefinition(routeDef);
            for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
                camelContext.resumeRoute(routeDefinition.getId());
            }
        }
//CHECKSTYLE:OFF
        catch (Exception e) {
//CHECKSTYLE:ON
            if (LOG.isDebugEnabled()) {
                LOG.debug("Route resuming failed refName :: " + routeDef.getRefName());
            }
            throw new B2BTransactionFailed("Can not able to resume the route" + routeDef.getRefName(), e);
        }

        routeDef.setRouteStatus(RouteStatus.STARTED);
        return routeDef;
    }

    @Override
    public RouteDef stop(RouteDef routeDef) throws B2BTransactionFailed, B2BNotFoundException, ValidationException {

        try {

            //currently not supporting multiple route per routes on save so should be just one
            RoutesDefinition routesDefinition = parseRouteDefinition(routeDef);
            for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
                if (camelContext.getRoute(routeDefinition.getId()) != null
                        && camelContext.getRouteStatus(routeDefinition.getId()) != null) {
                    camelContext.stopRoute(routeDefinition.getId());

                    int count = 0;
                    while (!camelContext.getRouteStatus(routeDefinition.getId()).equals(ServiceStatus.Stopped)
                            && count < 5) {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Waiting for route " + routeDefinition.getId() + " to stop");
                        }

                        Thread.currentThread().sleep(2000);
                        count++;

                    }

                    if (!camelContext.getRouteStatus(routeDefinition.getId()).equals(ServiceStatus.Stopped)) {
                        if (LOG.isErrorEnabled()) {
                            LOG.error("Timed out waiting for route to stop");
                        }
                    } else {
                        if (LOG.isInfoEnabled()) {
                            LOG.info("Route " + routeDefinition.getId() + " stopped");
                        }
                    }
                }
            }
        }
//CHECKSTYLE:OFF
        catch (Exception e) {
//CHECKSTYLE:ON

            if (LOG.isDebugEnabled()) {
                LOG.debug("Route with refName :: " + routeDef.getRefName() + " failed to stop");
            }

            throw new B2BTransactionFailed("Failed to Stop Route:" + routeDef.getRefName(), e);
        }
        routeDef.setRouteStatus(RouteStatus.STOPPED);
        return routeDef;
    }

    @Override
    public RouteDef start(RouteDef routeDef) throws ValidationException, B2BTransactionFailed, B2BNotFoundException {
        RoutesDefinition routesDefinition = parseRouteDefinition(routeDef);
        try {

            for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
                RouteDefinition existingRouteDefinition = camelContext.getRouteDefinition(routeDefinition.getId());
                if (existingRouteDefinition != null) {
                    ServiceStatus status = camelContext.getRouteStatus(existingRouteDefinition.getId());

                    if (status != null && status.isStarted()) {
                        if (LOG.isWarnEnabled()) {
                            LOG.warn("Route has already been started.  Stopping, reloading, and restarting route.");
                        }
                        camelContext.stopRoute(existingRouteDefinition.getId());
                    }
                    camelContext.removeRouteDefinition(existingRouteDefinition);
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Adding route definition");
                    }
                }
                camelContext.addRouteDefinition(routeDefinition);
                CamelDataDomainHelper.setDataDomainInRoute(camelContext.getRoute(routeDefinition.getId()),
                        routeDef.getDataDomain());
                camelContext.startRoute(routeDefinition.getId());
            }
            if (LOG.isDebugEnabled()) {
                List<RouteDefinition> routes = camelContext.getRouteDefinitions();
                for (RouteDefinition r : routes) {
                    LOG.debug("Route:" + r.getId() + " Status:" +
                            camelContext.getRouteStatus(r.getId()).toString());
                }
            }
        }
//CHECKSTYLE:OFF
        catch (Exception e) {
//CHECKSTYLE:ON

            if (LOG.isDebugEnabled()) {
                LOG.debug("Route starting failed refName :: " + routeDef.getRefName() );
            }
            for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
                RouteDefinition existingRouteDefinition = camelContext.getRouteDefinition(routeDefinition.getId());
                if (existingRouteDefinition != null) {
                    ServiceStatus status = camelContext.getRouteStatus(existingRouteDefinition.getId());
                    try {
                        if (status != null && status.isStarted()) {
                            if (LOG.isWarnEnabled()) {
                                LOG.warn("Cleaning out route " + existingRouteDefinition.getId());
                            }
                            camelContext.stopRoute(existingRouteDefinition.getId());
                        }
                        camelContext.removeRouteDefinition(existingRouteDefinition);
//CHECKSTYLE:OFF
                    } catch (Exception e1) {
//CHECKSTYLE:ON
                        e1.printStackTrace();
                    }
                }
            }

            throw new B2BTransactionFailed("Can not start the route" + routeDef.getRefName() + " Reason: "
                    + e.getMessage(), e);
        }

        routeDef.setRouteStatus(RouteStatus.STARTED);
        return routeDef;
    }

    @Override
    public boolean isAutoStart(RouteDef routeDef) throws ValidationException{

        RoutesDefinition routesDefinition = parseRouteDefinition(routeDef);
        for (RouteDefinition routeDefinition : routesDefinition.getRoutes()) {
            if ("true".equalsIgnoreCase(routeDefinition.getAutoStartup())) {
                return true;
            }
        }
        return false;
    }


    @Override
    public List<String> getRunningRoutes(String prefix) throws B2BTransactionFailed {
        List<Route> routes = camelContext.getRoutes();
        List<String> runningRoutes = new ArrayList<String>();
        for (Route route : routes) {
            if (route.getId().startsWith(prefix)) {
                if (ServiceStatus.Started.equals(camelContext.getRouteStatus(route.getId()))) {
                    runningRoutes.add(route.getId());
                }
            }
        }
        return runningRoutes;
    }

    @Override
    public RouteStatus getRouteStatus(String dataDomain, String refName) throws B2BTransactionFailed {

        RouteStatus rc = null;
        String id = dataDomain + "." + refName;
        List<Route> routes = camelContext.getRoutes();
        for (Route route : routes) {
            if (route.getId().equals(id) || route.getId().startsWith(id + ".")) {
                ServiceStatus status = camelContext.getRouteStatus(route.getId());
                if (status != null) {
                    switch (status) {
                        case Started:
                            rc = RouteStatus.STARTED;
                            break;
                        case Starting:
                            rc = RouteStatus.STARTING;
                            break;
                        case Stopped:
                            rc = RouteStatus.STOPPED;
                            break;
                        case Stopping:
                            rc = RouteStatus.STOPPING;
                            break;
                        case Suspended:
                            rc = RouteStatus.PAUSED;
                            break;
                        case Suspending:
                            rc = RouteStatus.PAUSING;
                            break;
                        default:
                            throw new IllegalStateException("Camel status value not recognized??");

                    }
                }
            }
            //found one running, favor that
            if (rc != null && !RouteStatus.STOPPED.equals(rc)) {
                return rc;
            }
        }
        return rc;

    }

}
