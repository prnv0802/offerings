package com.eis.b2bmb.examples.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * User: mingardia
 * Date: 3/5/14
 * Time: 1:56 PM
 */
public class InterceptorChain extends BaseInterceptorImpl implements Interceptor {

    private List<Interceptor> interceptorsList = new ArrayList<>();

    private static final Logger LOG = LoggerFactory.getLogger(InterceptorChain.class);

    public void setInterceptors(List<Interceptor> ints)
    {
        for (Interceptor i : ints)
        {
            LOG.info("Adding interceptor:" + i.getClass().getName() + " to chain");
            interceptorsList.add(i);
        }
    }

    public Interceptor replaceOrAddInterceptor(Interceptor i)
    {
        int index =  interceptorsList.indexOf(i);
        Interceptor rc = null;

        if (index != -1)
        {
            rc = interceptorsList.remove(index);
            interceptorsList.add(index,i);
        }
        else
        {
            interceptorsList.add(i);
        }

        return rc;

    }

    @Override
    public InterceptorContext onBeforeAction(InterceptorContext context, String action) throws InterceptorException {
        for (Interceptor i : interceptorsList)
        {
               i.onBeforeAction(context, action);
        }

        return context;
    }

    @Override
    public InterceptorContext onAfterAction(InterceptorContext context, String action) throws InterceptorException {
        for (Interceptor i : interceptorsList)
        {
            i.onAfterAction(context, action);
        }

        return context;
    }
}
