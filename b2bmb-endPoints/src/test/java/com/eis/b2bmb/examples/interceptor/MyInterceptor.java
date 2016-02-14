package com.eis.b2bmb.examples.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: mingardia
 * Date: 3/5/14
 * Time: 1:51 PM
 */
public class MyInterceptor
        extends BaseInterceptorImpl
        implements Interceptor {

    private static final Logger LOG = LoggerFactory.getLogger(MyInterceptor.class);


    @Override
    public InterceptorContext onBeforeAction(InterceptorContext context, String action) throws InterceptorException {

        LOG.info("<< MyOnBeforeAction:" + action);

        return context;
    }

    @Override
    public InterceptorContext onAfterAction(InterceptorContext context, String action) throws InterceptorException {
         LOG.info(">> MyOnAfterAction" + action);

        return context;
    }
}
