package com.eis.b2bmb.camel.custom.notifier;

import com.eis.b2bmb.api.v1.dao.RouteDefDAO;
import com.eis.b2bmb.api.v1.model.RouteDef;
import com.eis.b2bmb.api.v1.services.RouteExecuterHelper;
import com.eis.core.api.v1.exception.B2BNotFoundException;
import com.eis.core.api.v1.exception.B2BTransactionFailed;
import com.eis.core.api.v1.exception.ValidationException;
import org.apache.camel.management.event.CamelContextStartedEvent;
import org.apache.camel.support.EventNotifierSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.EventObject;
import java.util.List;

/**
 * Runs at context start up, loads routes
 * @author aaldredge
 */
public final class CamelStartedRouteLoader extends EventNotifierSupport{

    private static final Logger LOG = LoggerFactory.getLogger(CamelStartedRouteLoader.class);

    @Autowired
    private RouteDefDAO routeDefDAO;

    @Autowired
    private RouteExecuterHelper routeExecuterHelper;

    /**
     * Starts routes w/ auto start true
     * @param eventObject CamelContextStartedEvent
     * @throws Exception exception when fails
     */
    @Override
    public void notify(EventObject eventObject) throws Exception {

        List<String> dataDomains = new ArrayList<String>();
        dataDomains.add("*");
        List<RouteDef> routes = routeDefDAO.getList(0, -1, null, dataDomains);

        if (routes != null) {
            for (RouteDef route : routes) {
                try {
                    if (routeExecuterHelper.isAutoStart(route)) {
                        routeExecuterHelper.start(route);
                    }
                } catch (B2BTransactionFailed b2BTransactionFailed) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Unable to load route " + route.getDataDomain() + route.getRefName(),
                                b2BTransactionFailed);
                    }
                } catch (B2BNotFoundException b2BNotFoundException) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Unable to load route " + route.getDataDomain() + route.getRefName(),
                                b2BNotFoundException);
                    }
                } catch (ValidationException validationException) {
                    if (LOG.isErrorEnabled()) {
                        LOG.error("Unable to load route " + route.getDataDomain() + route.getRefName(),
                                validationException);
                    }
                }

            }
        }
    }

    @Override
    public boolean isEnabled(EventObject eventObject) {
        return eventObject instanceof CamelContextStartedEvent;
    }
}
