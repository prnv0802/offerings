package com.eis.b2bmb.examples.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: mingardia
 * Date: 3/5/14
 * Time: 1:23 PM
 */
public class DefaultInterceptor extends BaseInterceptorImpl
        implements Interceptor {

    private static final Logger LOG = LoggerFactory.getLogger(DefaultInterceptor.class);


    @Override
    public InterceptorContext onBeforeAction(InterceptorContext context, String action) throws InterceptorException {

        LOG.info(action + ":Before Object:" + context.get("beforeObject"));


        return context;
    }

    @Override
    public InterceptorContext onAfterAction(InterceptorContext context, String action) throws InterceptorException {
        LOG.info(action + ":After Object:" + context.get("afterObject"));
        return context;
    }
}
