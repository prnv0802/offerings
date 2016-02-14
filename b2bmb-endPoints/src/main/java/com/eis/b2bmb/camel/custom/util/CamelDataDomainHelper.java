package com.eis.b2bmb.camel.custom.util;

import com.eis.b2bmb.camel.custom.B2bmbCamelConstants;
import org.apache.camel.Exchange;
import org.apache.camel.Route;

import java.util.Map;

/**
 * Helper for managing the relationship of data domains in exchanges
 * @author aaldredge
 */
public class CamelDataDomainHelper {

    private CamelDataDomainHelper() {
    }

    /**
     * Get the data domain from the exchange
     * @param exchange exchange to attempt to retrieve data domain from
     * @return dataDomain
     */
    public static String getDataDomainFromExchange(Exchange exchange) {
        String dataDomain = null;
        if (exchange.getContext().getRoute(exchange.getFromRouteId()) != null &&
                exchange.getContext().getRoute(exchange.getFromRouteId()).getProperties() != null &&
                exchange.getContext().getRoute(exchange.getFromRouteId()).getProperties().get("dataDomain") != null) {
            dataDomain = exchange.getContext().getRoute(
                    exchange.getFromRouteId()).getProperties().get("dataDomain").toString();
        }

        if(dataDomain == null) {
            dataDomain = exchange.getIn().getHeader(B2bmbCamelConstants.DATA_DOMAIN, String.class);
        }
        return dataDomain;
    }

    /**
     * Get the data domain from the route
     * @param route route to attempt to retrieve data domain from
     * @return dataDomain
     */
    public static String getDataDomainFromRoute(Route route) {
        String dataDomain = null;
        if (route.getProperties().get("dataDomain") != null) {
            dataDomain = route.getProperties().get("dataDomain").toString();
        }
        return dataDomain;
    }

    /**
     * Set the dataDomain in the route so that it can be retrieved when needed during route runtime
     * @param route route to set the domain in
     * @param dataDomain dateDomain to put in route
     */
    public static void setDataDomainInRoute(Route route, String dataDomain){
        Map routeProperties = route.getProperties();
        routeProperties.put("dataDomain", dataDomain);
    }

}
