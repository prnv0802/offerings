package com.eis.b2bmb.examples.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * User: mingardia
 * Date: 3/5/14
 * Time: 4:09 PM
 */
public class MyGuard extends BaseInterceptorImpl implements Interceptor {
    private static final Logger LOG = LoggerFactory.getLogger(MyGuard.class);

    protected String sampleExtensionProperty;


    public String getSampleExtensionProperty() {
        return sampleExtensionProperty;
    }

    public void setSampleExtensionProperty(String sampleExtensionProperty) {
        this.sampleExtensionProperty = sampleExtensionProperty;
    }

    @Override
    public InterceptorContext onBeforeAction(InterceptorContext context, String action) throws InterceptorException {

        LOG.info("My Guard Called");

        if (context.get("stopChain") != null )
        {
            throw new InterceptorException("Stopping Chain");
        }

        return context;
    }

    @Override
    public InterceptorContext onAfterAction(InterceptorContext context, String action) throws InterceptorException {
        return context;
    }
}
